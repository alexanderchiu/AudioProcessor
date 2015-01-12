import java.util.Arrays;

public class Denoiser implements AudioProcessor {

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


    public Denoiser(int fs) {
        windowLength = 256;
        overlapRatio = 0.5;
        this.fs = fs;
        this.noSpeechDuration = 0.4;
        this.noSpeechSegments = (int)Math.floor((noSpeechDuration * fs - windowLength) / (overlapRatio * windowLength) + 1);
        this.speechFlag = false;
        this.noiseFlag = false;
        this.noiseLength = 9;
        this.noiseThreshold = 3;
        this.frameReset = 8;
    }

    public Denoiser(int fs, double noSpeechDuration) {
        windowLength = 256;
        overlapRatio = 0.5;
        this.fs = fs;
        this.noSpeechDuration = noSpeechDuration;
        this.noSpeechSegments = (int)Math.floor((noSpeechDuration * fs - windowLength) / (overlapRatio * windowLength) + 1);
        this.speechFlag = false;
        this.noiseFlag = false;
        this.noiseLength = 9;
        this.noiseThreshold = 3;
        this.frameReset = 8;
    }

    public Denoiser(int fs, double noSpeechDuration, int noiseLength, int noiseThreshold, int frameReset) {
        windowLength = 256;
        overlapRatio = 0.5;
        this.fs = fs;
        this.noSpeechDuration = noSpeechDuration;
        this.noSpeechSegments = (int)Math.floor((noSpeechDuration * fs - windowLength) / (overlapRatio * windowLength) + 1);
        this.speechFlag = false;
        this.noiseFlag = false;
        this.noiseLength = noiseLength;
        this.noiseThreshold = noiseThreshold;
        this.frameReset = frameReset;
    }

    /**
     * Process function for multi-channel inputs
     * @param  input Multi channel signal
     * @return   enhanced Multi channel enhanced signal
     */

    public double[][] process(double[][] input) {
        int channels = input.length;
        int signalLength = input[0].length;

        double[][] enhanced = new double[channels][signalLength];

        for (int i = 0; i < channels; i++) {
            enhanced[i] = process(input[i]);
        }
        return enhanced;
    }

    /**
     * Performs speech denoising on array of doubles based on  Speech Enhancement Using a Minimum Mean-Square
     * Error Short-Time Spectral Amplitude Estimator by Eprahiam and Malah
     * @param  input Double array of signal values
     * @return   enhanced Double array of enhanced signal array
     */

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

        double[][] noise = new double[this.noSpeechSegments][windowLength];
        double[][] noiseMag = new double[this.noSpeechSegments][windowLength];

        noise  = Arrays.copyOfRange(signalFFTMagnitude, 0, this.noSpeechSegments);

        for (int i = 0; i < this.noSpeechSegments; i++) {
            for (int k = 0; k < windowLength; k++) {
                noiseMag[i][k] = Math.pow(noise[i][k], 2);
            }
        }

        double[] noiseMean = Utils.mean(noise, 0);
        double[] noiseVar = Utils.mean(noiseMag, 0);

        double gamma1p5 = Utils.gamma(1.5);
        double[] gain = new double[windowLength];
        double[] gamma = new double[windowLength];
        double[] gammaUpdate = new double[windowLength];
        double[] xi = new double[windowLength];
        double[] nu = new double[windowLength];

        double alpha = 0.96; //Smoothing factor

        Arrays.fill(gain, 1);
        Arrays.fill(gamma, 1);

        double[][] enhancedSpectrum = new double[frames][windowLength];

        for (int i = 0; i < frames; i++) {
            if (i < this.noSpeechSegments) {
                this.speechFlag = false;
                this.noiseCounter = 100;
            } else {
                vad(signalFFTMagnitude[i], noiseMean);
            }

            if (this.speechFlag == false) { // Noise estimate update during segements with no speech
                for (int k = 0; k < windowLength; k++) {
                    noiseMean[k] = (this.noiseLength * noiseMean[k] + signalFFTMagnitude[i][k]) / (this.noiseLength + 1);
                    noiseVar[k] = (this.noiseLength * noiseVar[k] + Math.pow(signalFFTMagnitude[i][k], 2)) / (this.noiseLength + 1);
                }
            }

            for (int k = 0; k < windowLength; k++) {
                gammaUpdate[k] = Math.pow(signalFFTMagnitude[i][k], 2) / noiseVar[k];
                xi[k] = alpha * Math.pow(gain[k], 2) * gamma[k] + (1 - alpha) * Math.max(gammaUpdate[k] - 1, 0);
                gamma[k] = gammaUpdate[k];
                nu[k] = gamma[k] * xi[k] / (xi[k] + 1);
                gain[k] = (gamma1p5 * Math.sqrt(nu[k])) / gamma[k] * Math.exp(-1 * nu[k] / 2) * ((1 + nu[k]) * Bessel.modBesselFirstZero(nu[k] / 2) + nu[k] * Bessel.modBesselFirstOne(nu[k] / 2));
 
                if (Double.isNaN(gain[k]) || Double.isInfinite(gain[k])) {
                    gain[k] = xi[k] / (xi[k] + 1);
                }

                enhancedSpectrum[i][k] = gain[k] * signalFFTMagnitude[i][k];
            }
        }
        ComplexNumber[][] enhancedSpectrumComplex = new ComplexNumber[frames][windowLength];

        for (int i = 0; i < frames; i++) {
            for (int k = 0; k < windowLength; k++) {
                enhancedSpectrumComplex[i][k] = ComplexNumber.exp(new ComplexNumber(0, signalFFTPhase[i][k]));
                enhancedSpectrumComplex[i][k] = enhancedSpectrumComplex[i][k].times(enhancedSpectrum[i][k]);
            }
        }

        ComplexNumber[][] enhancedSegments = new ComplexNumber[frames][windowLength];
        double[][] enhancedSegmentsReal = new double[windowLength][frames];

        for (int i = 0; i < frames; i++) {
            enhancedSegments[i] = Utils.ifft(enhancedSpectrumComplex[i]);
        }

        for (int i = 0; i < frames; i++) {
            for (int k = 0; k < windowLength; k++) {
                enhancedSegmentsReal[k][i] =  enhancedSegments[i][k].getRe(); //convert samples to real from and perform tranpose
            }
        }

        double[] enhanced = overlapAndAdd(enhancedSegmentsReal, overlapRatio);
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