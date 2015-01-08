import java.util.Arrays;
public class Denoiser implements AudioProcessor {

    private static int windowLength = 256;
    private static double overlapRatio = 0.5;
    private int fs;
    private double noSpeechDuration;
    private double noSpeechSegments;
    private boolean speechFlag;
    private boolean noiseFlag;
    private int noiseCounter;
    private int noiseLength;

    public Denoiser() {
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
                sampledSignalWindowedComplex[i][k] = new ComplexNumber(sampledSignalWindowed[k][i]);
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
        System.out.println(Arrays.deepToString(signalFFTMagnitude));


        double[] enhanced = {};

        return enhanced;

    }


    /**
     * Windows sampled signal using overlapping Hamming windows
     * @param ss The sampled signal
     * @param ww The window width
     * @param or The overlap ratio
     * @return seg The overlapping windowed segments
     */

    private static double[][] segmentSignal(double[] ss, int ww, double or ) {
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

    private static double[] overlapAndAdd(double[][] segments, double or ) {
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