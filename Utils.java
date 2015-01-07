import java.util.Arrays;
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

/**
 * Performs Cooley–Tukey FFT algorithm and returns array of complex numbers
 * @param  x Radix-2 length N signal array
 * @return   X N sampled spectrum
 */

    public static ComplexNumber[] fft(ComplexNumber[] x) {
        int N = x.length;

        if ( N == 1 ) {
            return new ComplexNumber[] {x[0]};
        }

        if (N % 2 != 0) {
            throw new RuntimeException("Sample points N not radix-2");
        }

        ComplexNumber[] xEven = new ComplexNumber[N / 2];
        ComplexNumber[] xOdd = new ComplexNumber[N / 2];

        for (int k = 0; k < N / 2; k++) {
            xEven[k] = x[2 * k];
            xOdd[k] = x[2 * k + 1];
        }

        ComplexNumber[] Ek = fft(xEven);
        ComplexNumber[] Ok = fft(xOdd);
        ComplexNumber[] X = new ComplexNumber[N];

        for (int k = 0; k < N / 2; k++) {
            ComplexNumber tf = ComplexNumber.exp(new ComplexNumber(0, -2 * Math.PI * k / N));
            X[k] = ComplexNumber.add(Ek[k], ComplexNumber.multiply(tf, Ok[k]));
            X[k + N / 2] = ComplexNumber.subtract(Ek[k], ComplexNumber.multiply(tf, Ok[k]));
        }
        return X;
    }


    public static void main(String[] args) {

        Double[] xdata = { -0.034804, 0.079101, 0.723332, 0.16598};

        ComplexNumber[] x = new ComplexNumber[xdata.length];

        for (int i = 0; i < xdata.length; i++) {
            x[i] = new ComplexNumber(xdata[i]);
            // System.out.println(x[i]);
        }

        ComplexNumber[] X = fft(x);

        System.out.println(Arrays.toString(X));

    }
}