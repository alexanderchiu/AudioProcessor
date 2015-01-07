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
     * @return   X Radix-2 length N signal spectrum
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

    /**
     * Perfoms ifft using fft function
     * @param  X Radix-2 length N signal spectrum
     * @return   x Radix-2 length N signal array
     */

    public static ComplexNumber[] ifft(ComplexNumber[] X) {
        int N = X.length;
        ComplexNumber[] x = new ComplexNumber[N];

        for (int k = 0; k < N; k ++) {
            x[k]  = X[k].conjugate();
        }

        x = fft(x);

        for (int k = 0; k < N; k ++) {
            x[k] = x[k].conjugate();
            x[k] = x[k].times(1.0 / N);
        }

        return x;
    }

/**
 * [Lanczos approximation of gamma function
 * @param  x Input value
 * @return  a Value of gamma function at x
 */

    public static double gamma(double x) {
        int g = 7;
        double[] p = {0.99999999999980993, 676.5203681218851, -1259.1392167224028,
                      771.32342877765313, -176.61502916214059, 12.507343278686905,
                      -0.13857109526572012, 9.9843695780195716e-6, 1.5056327351493116e-7
                     };

        if (x < 0.5) {
            return Math.PI / (Math.sin(Math.PI * x) * gamma(1 - x));
        }

        x -= 1;
        double a = p[0];
        double t = x + g + 0.5;
        for (int i = 1; i < p.length; i++) {
            a += p[i] / (x + i);
        }

        return Math.sqrt(2 * Math.PI) * Math.pow(t, x + 0.5) * Math.exp(-t) * a;
    }mma

    public static void main(String[] args) {

        double x = gamma(1.5);
        System.out.println(x);
    }
}