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

    public Denoiser(int fs, double noSpeechDuration, int noiseLength) {
        windowLength = 256;
        overlapRatio = 0.5;

        this.fs = fs;
        this.noSpeechDuration = noSpeechDuration;
        this.noSpeechSegments = (int)Math.floor((noSpeechDuration * fs - windowLength) / (overlapRatio * windowLength) + 1);
        this.speechFlag = false;
        this.noiseFlag = false;
        this.noiseCounter = 0;
        this.noiseLength = noiseLength;
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
                sampledSignalWindowedComplex[i][k] = new ComplexNumber(sampledSignalWindowed[k][i]); //convert samples to Complex form for fft and transpose matrix
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

        double[][] noise = new double[frames][noSpeechSegments];

        noise  = Arrays.copyOfRange(signalFFTMagnitude, 0, noSpeechSegments);


        double[] noiseMean = Utils.mean(noise, 0);

        for (int i = 0; i < noSpeechSegments; i++) {
            for (int k = 0; k < windowLength; k++) {
                noise[i][k] = Math.pow(noise[i][k], 2);
            }
        }

        double[] noiseVar = Utils.mean(noise, 0);

        double gamma1p5 = Utils.gamma(1.5);
        double[] G = new double[windowLength];
        double[] gamma = new double[windowLength];

        Arrays.fill(G, 1);
        Arrays.fill(gamma, 1);

        double[][] enhancedSpectrum = new double[frames][windowLength];








        System.out.println(Arrays.toString(G));



        double[] enhanced = {};

        return enhanced;

    }

    public void vad(double[] frame, double[] noise, int noiseCounter, int noiseThreshold, int frameReset) {

        double[] spectralDifference = new double[windowLength];

        for (int i = 0; i < windowLength; i++) {
            spectralDifference[i] = 20 * (Math.log10(frame[i]) - Math.log10(noise[i]));
            if (spectralDifference[i] < 0) {
                spectralDifference[i] = 0;
            }
        }

        double diff = Utils.mean(spectralDifference);

        if (diff < noiseThreshold) {
            noiseFlag = true;
            noiseCounter += 1;
        } else {
            noiseFlag = false;
            noiseCounter = 0;
        }

        if (noiseCounter > frameReset) {
            speechFlag = false;
        } else {
            speechFlag = true;
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
        int frames = (int)(Math.floor((len - ww) / ww / d));
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