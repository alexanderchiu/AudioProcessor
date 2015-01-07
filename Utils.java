public class Utils {
    public Utils() {
    }

    /**
     * Calculates N samples of Hamming window
     * @param N Number of samples
     * @return samples Array of samples
     */
    
    public static double[] hamming(int N) {
        double[] samples = new double[N];

        for (int k = 0; k < N; k++) {
            samples[k] = 0.54 - 0.46 * Math.cos(2 * Math.PI * k / (N - 1));
        }

        return samples;
    }

    public static void main(String[] args) {

    }
}