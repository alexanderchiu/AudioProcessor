import java.util.Arrays;

public class AGC implements AudioProcessor {
    private static int windowLength;
    private static double overlapRatio;

    private int fs;
    private double noSpeechDuration;
    private int noSpeechSegments;
    private boolean speechFlag;
    private boolean noiseFlag;
    private int noiseCounter;
    private int noiseLength;
    private int noiseThreshold;
    private int frameReset;

    private boolean initialNoiseEstimateFlag;
    private double[] noiseMean;
    private double[] noiseVar;

    public AGC(int fs) {
        windowLength = 256;
        overlapRatio = 0.5;
        this.fs = fs;
        this.noSpeechDuration = 0.4;
        this.noSpeechSegments = (int)Math.floor((noSpeechDuration * fs - windowLength) / (overlapRatio * windowLength) + 1);
        this.speechFlag = false;
        this.noiseFlag = false;
        this.initialNoiseEstimateFlag = true;
        this.noiseLength = 9;
        this.noiseThreshold = 3;
        this.frameReset = 8;
    }

    public AGC(int fs, double noSpeechDuration) {
        windowLength = 256;
        overlapRatio = 0.5;
        this.fs = fs;
        this.noSpeechDuration = noSpeechDuration;
        this.noSpeechSegments = (int)Math.floor((noSpeechDuration * fs - windowLength) / (overlapRatio * windowLength) + 1);
        this.speechFlag = false;
        this.noiseFlag = false;
        this.initialNoiseEstimateFlag = true;
        this.noiseLength = 9;
        this.noiseThreshold = 3;
        this.frameReset = 8;
    }

    public AGC(int fs, double noSpeechDuration, int noiseLength, int noiseThreshold, int frameReset) {
        windowLength = 256;
        overlapRatio = 0.5;
        this.fs = fs;
        this.noSpeechDuration = noSpeechDuration;
        this.noSpeechSegments = (int)Math.floor((noSpeechDuration * fs - windowLength) / (overlapRatio * windowLength) + 1);
        this.speechFlag = false;
        this.noiseFlag = false;
        this.initialNoiseEstimateFlag = true;
        this.noiseLength = noiseLength;
        this.noiseThreshold = noiseThreshold;
        this.frameReset = frameReset;
    }

    public double[][] process(double[][] input) {
        int channels = input.length;
        int signalLength = input[0].length;

        double[][] enhanced = new double[channels][signalLength];

        for (int i = 0; i < channels; i++) {
            enhanced[i] = process(input[i]);
        }
        return enhanced;
    }

    public double[] process(double[] input) {

        double[][] sampledSignalWindowed = segmentSignal(input, windowLength, overlapRatio);
        int frames = sampledSignalWindowed[0].length;
        ComplexNumber[][] sampledSignalWindowedComplex = new ComplexNumber[frames][windowLength];
        ComplexNumber[][] signalFFT = new ComplexNumber[frames][windowLength];
        double[][] signalFFTMagnitude = new double[frames][windowLength];
        double[][] signalFFTPhase = new double[frames][windowLength];

        for (int i = 0; i < frames; i++) {
            for (int k = 0; k < windowLength; k++) {
                sampledSignalWindowedComplex[i][k] = new ComplexNumber(sampledSignalWindowed[k][i]); //convert samples to Complex form for fft and perform transpose
            }
        }

        for (int i = 0; i < frames; i++) {
            signalFFT[i] = Utils.fft(sampledSignalWindowedComplex[i]);
        }

        for (int i = 0; i < frames; i++) {
            for (int k = 0; k < windowLength; k++) {
                signalFFTMagnitude[i][k] =  signalFFT[i][k].mod();
                signalFFTPhase[i][k] =  signalFFT[i][k].getArg();
            }
        }
        if (this.initialNoiseEstimateFlag == true) {
            double[][] noise = new double[this.noSpeechSegments][windowLength];
            double[][] noiseMag = new double[this.noSpeechSegments][windowLength];

            noise  = Arrays.copyOfRange(signalFFTMagnitude, 0, this.noSpeechSegments);

            for (int i = 0; i < this.noSpeechSegments; i++) {
                for (int k = 0; k < windowLength; k++) {
                    noiseMag[i][k] = Math.pow(noise[i][k], 2);
                }
            }

            this.noiseMean = Utils.mean(noise, 0);
            this.noiseVar = Utils.mean(noiseMag, 0);
        }

        double[] mean = Utils.mean(sampledSignalWindowed, 0);
        double[] energy = new double[frames];
        double[] gain = new double[frames];
        double alpha = 0.96;

        for (int i = 0; i < frames; i++) {
            if (i < this.noSpeechSegments) {
                this.speechFlag = false;
                this.noiseCounter = 100;
            } else {
                vad(signalFFTMagnitude[i], noiseMean);
            }

            if (!this.speechFlag) { // Noise estimate update during segements with no speech
                for (int k = 0; k < windowLength; k++) {
                    this.noiseMean[k] = (this.noiseLength * this.noiseMean[k] + signalFFTMagnitude[i][k]) / (this.noiseLength + 1);
                    this.noiseVar[k] = (this.noiseLength * this.noiseVar[k] + Math.pow(signalFFTMagnitude[i][k], 2)) / (this.noiseLength + 1);
                }
            }
            if (this.speechFlag) {
                double maxThresh = 0.025;
                double minThresh = 0.005;
                energy[i] = 0;
                for (int k = 0; k < windowLength; k++) {
                    energy[i] += sampledSignalWindowed[k][i];
                }

                for (int k = 0; k < windowLength; k++) {
                    if (sampledSignalWindowed[k][i] > mean[i]) {
                        if (energy[i] > minThresh && energy[i] < maxThresh) {
                            if (i >= 1) {
                                gain[i] = alpha * gain[i - 1] + (1 - alpha) * Math.sqrt(1.5/ energy[i]);
                            } else {
                                gain[i] = 1;
                            }

                            sampledSignalWindowed[k][i]  = sampledSignalWindowed[k][i] * gain[i];
                        }
                    }
                }
            }
        }

        double[] enhanced = overlapAndAdd(sampledSignalWindowed, overlapRatio);
        // System.out.println(Arrays.toString(energy));
        return enhanced;
    }

    /**
    * Voice activity detector that predicts wheter the current frame contains speech or not
    * @param frame  Current frame
    * @param noise   Current noise estimate
    * @param noiseCounter  Number of previous noise frames
    * @param noiseThreshold User set threshold
    * @param frameReset Number of frames after which speech flag is reset
    */
    private void vad(double[] frame, double[] noise) {
        double[] spectralDifference = new double[windowLength];

        for (int i = 0; i < windowLength; i++) {
            spectralDifference[i] = 20 * (Math.log10(frame[i]) - Math.log10(noise[i]));
            if (spectralDifference[i] < 0) {
                spectralDifference[i] = 0;
            }
        }

        double diff = Utils.mean(spectralDifference);

        if (diff < this.noiseThreshold) {
            this.noiseFlag = true;
            this.noiseCounter++;
        } else {
            this.noiseFlag = false;
            this.noiseCounter = 0;
        }

        if (this.noiseCounter > this.frameReset) {
            this.speechFlag = false;
        } else {
            this.speechFlag = true;
        }
    }

    /**
     * Windows sampled signal using overlapping Hamming windows
     * @param ss The sampled signal
     * @param ww The window width
     * @param or The overlap ratio
     * @return seg The overlapping windowed segments
     */

    private double[][] segmentSignal(double[] ss, int ww, double or ) {
        int len = ss.length;
        double d = 1 - or;
        int frames = (int)(Math.floor(len - ww) / ww / d);
        int start = 0;
        int stop = 0;

        double[] window = Utils.hamming(ww);
        double[][] seg = new double[ww][frames];

        for (int i = 0; i < frames; i++) {
            start = (int)(i * ww * or );
            stop =  start + ww;
            for (int k = 0; k < ww; k++) {
                seg[k][i] = ss[start + k] * window[k];
            }
        }
        return seg;
    }

    /**
     * Overlap and add segments to calculate reconstructed signal
     * @param  segments 2D array of overlapping signal segments
     * @param  or overlap ratio
     * @return   reconstructedSignal Speech signal post speech denoising
     */

    private double[] overlapAndAdd(double[][] segments, double or ) {
        int ww = segments.length;
        int frames = segments[0].length;
        int start = 0;
        int stop = 0;
        int signalLength = (int)(ww * (1 - or ) * (frames - 1) + ww);

        double[] reconstructedSignal = new double[signalLength];

        for (int i = 0; i < frames; i++) {
            start = (int)(i * ww * or );
            stop =  start + ww;
            for (int k = 0; k < ww; k++) {
                reconstructedSignal[start + k] = reconstructedSignal[start + k] + segments[k][i];
            }
        }
        return reconstructedSignal;
    }

    public static void main(String[] args) {

    }
}