import java.util.Arrays;
public class Denoiser {
    public Denoiser() {
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
        int frames = (int)(Math.floor((len - d) / ww / d));
        int start = 0;
        int stop = 0;
        double[] window = hamming(ww);

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
     * Calculates N samples of Hamming window
     * @param N Number of samples
     * @return samples Array of samples
     */
    private static double[] hamming(int N) {
        double[] samples = new double[N];

        for (int k = 0; k < N; k++) {
            samples[k] = 0.54 - 0.46 * Math.cos(2 * Math.PI * k / (N - 1));
        }
        return samples;

    }

    public static void main(String[] args) {
        double[] test = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

        double[][] stack = segmentSignal(test, 4, 0.5);

    }
}