/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

public class Bessel {

    /**
    * Evaluates a Chebyshev series.
    * @param x value at which to evaluate series
    * @param series the coefficients of the series
    */

    public static double chebyshev(double x, double series[]) {
        double twox, b0 = 0.0, b1 = 0.0, b2 = 0.0;
        twox = 2 * x;
        for (int i = series.length - 1; i > -1; i--) {
            b2 = b1;
            b1 = b0;
            b0 = twox * b1 - b2 + series[i];
        }
        return 0.5 * (b0 - b2);
    }

    /**
    * Modified Bessel function of first kind, order zero.
    * Based on the NETLIB Fortran function besi0 written by W. Fullerton.
    */

    public static double modBesselFirstZero(double x) {
        double y = Math.abs(x);
        if (y > 3.0)
            return Math.exp(y) * expModBesselFirstZero(x);
        else
            return 2.75 + chebyshev(y * y / 4.5 - 1.0, bi0cs);
    }

    /**
    * Exponential scaled modified Bessel function of first kind, order zero.
    * Based on the NETLIB Fortran function besi0e written by W. Fullerton.
    */

    private static double expModBesselFirstZero(double x) {
        final double y = Math.abs(x);
        if (y > 3.0) {
            if (y > 8.0)
                return (0.375 + chebyshev(16.0 / y - 1.0, ai02cs)) / Math.sqrt(y);
            else
                return (0.375 + chebyshev((48.0 / y - 11.0) / 5.0, ai0cs)) / Math.sqrt(y);
        } else
            return Math.exp(-y) * (2.75 + chebyshev(y * y / 4.5 - 1.0, bi0cs));
    }

    /**
    * Modified Bessel function of first kind, order one.
    * Based on the NETLIB Fortran function besi0 written by W. Fullerton.
    */

    public static double modBesselFirstOne(double x) {
        final double y = Math.abs(x);
        if (y > 3.0)
            return Math.exp(y) * expModBesselFirstOne(x);
        else if (y == 0.0)
            return 0.0;
        else
            return x * (0.875 + chebyshev(y * y / 4.5 - 1.0, bi1cs));
    }

    /**
    * Exponential scaled modified Bessel function of first kind, order one.
    * Based on the NETLIB Fortran function besi1e written by W. Fullerton.
    */
   
    private static double expModBesselFirstOne(double x) {
        final double y = Math.abs(x);
        if (y > 3.0) {
            if (y > 8.0)
                return x / y * (0.375 + chebyshev(16.0 / y - 1.0, ai12cs)) / Math.sqrt(y);
            else
                return x / y * (0.375 + chebyshev((48.0 / y - 11.0) / 5.0, ai1cs)) / Math.sqrt(y);
        } else if (y == 0.0)
            return 0.0;
        else
            return Math.exp(-y) * x * (0.875 + chebyshev(y * y / 4.5 - 1.0, bi1cs));
    }

    // CHEBYSHEV SERIES

    // series for ai0 on the interval  1.25000d-01 to  3.33333d-01
    //                                        with weighted error   7.87e-17
    //                                         log weighted error  16.10
    //                               significant figures required  14.69
    //                                    decimal places required  16.76

    private final static double ai0cs[] = {
        0.07575994494023796,
        0.00759138081082334,
        0.00041531313389237,
        0.00001070076463439,
        -0.00000790117997921,
        -0.00000078261435014,
        0.00000027838499429,
        0.00000000825247260,
        -0.00000001204463945,
        0.00000000155964859,
        0.00000000022925563,
        -0.00000000011916228,
        0.00000000001757854,
        0.00000000000112822,
        -0.00000000000114684,
        0.00000000000027155,
        -0.00000000000002415,
        -0.00000000000000608,
        0.00000000000000314,
        -0.00000000000000071,
        0.00000000000000007
    };

    // series for ai02       on the interval  0.          to  1.25000d-01
    //                                        with weighted error   3.79e-17
    //                                         log weighted error  16.42
    //                               significant figures required  14.86
    //                                    decimal places required  17.09
    private final static double ai02cs[] = {
        0.05449041101410882,
        0.00336911647825569,
        0.00006889758346918,
        0.00000289137052082,
        0.00000020489185893,
        0.00000002266668991,
        0.00000000339623203,
        0.00000000049406022,
        0.00000000001188914,
        -0.00000000003149915,
        -0.00000000001321580,
        -0.00000000000179419,
        0.00000000000071801,
        0.00000000000038529,
        0.00000000000001539,
        -0.00000000000004151,
        -0.00000000000000954,
        0.00000000000000382,
        0.00000000000000176,
        -0.00000000000000034,
        -0.00000000000000027,
        0.00000000000000003
    };

    // series for ai1        on the interval  1.25000d-01 to  3.33333d-01
    //                                        with weighted error   6.98e-17
    //                                         log weighted error  16.16
    //                               significant figures required  14.53
    //                                    decimal places required  16.82

    private final static double ai1cs[] = {
        -0.02846744181881479,
        -0.01922953231443221,
        -0.00061151858579437,
        -0.00002069971253350,
        0.00000858561914581,
        0.00000104949824671,
        -0.00000029183389184,
        -0.00000001559378146,
        0.00000001318012367,
        -0.00000000144842341,
        -0.00000000029085122,
        0.00000000012663889,
        -0.00000000001664947,
        -0.00000000000166665,
        0.00000000000124260,
        -0.00000000000027315,
        0.00000000000002023,
        0.00000000000000730,
        -0.00000000000000333,
        0.00000000000000071,
        -0.00000000000000006
    };

    // series for ai12       on the interval  0.          to  1.25000d-01
    //                                        with weighted error   3.55e-17
    //                                         log weighted error  16.45
    //                               significant figures required  14.69
    //                                    decimal places required  17.12

    private final static double ai12cs[] = {
        0.02857623501828014,
        -0.00976109749136147,
        -0.00011058893876263,
        -0.00000388256480887,
        -0.00000025122362377,
        -0.00000002631468847,
        -0.00000000383538039,
        -0.00000000055897433,
        -0.00000000001897495,
        0.00000000003252602,
        0.00000000001412580,
        0.00000000000203564,
        -0.00000000000071985,
        -0.00000000000040836,
        -0.00000000000002101,
        0.00000000000004273,
        0.00000000000001041,
        -0.00000000000000382,
        -0.00000000000000186,
        0.00000000000000033,
        0.00000000000000028,
        -0.00000000000000003
    };


    // series for bi0        on the interval  0.          to  9.00000d+00
    //                                        with weighted error   2.46e-18
    //                                         log weighted error  17.61
    //                               significant figures required  17.90
    //                                    decimal places required  18.15

    private final static double bi0cs[] = {
        -0.07660547252839144951,
        1.927337953993808270,
        0.2282644586920301339,
        0.01304891466707290428,
        0.00043442709008164874,
        0.00000942265768600193,
        0.00000014340062895106,
        0.00000000161384906966,
        0.00000000001396650044,
        0.00000000000009579451,
        0.00000000000000053339,
        0.00000000000000000245
    };


    // series for bi1        on the interval  0.          to  9.00000d+00
    //                                        with weighted error   2.40e-17
    //                                         log weighted error  16.62
    //                               significant figures required  16.23
    //                                    decimal places required  17.14

    private final static double bi1cs[] = {
        -0.001971713261099859,
        0.40734887667546481,
        0.034838994299959456,
        0.001545394556300123,
        0.000041888521098377,
        0.000000764902676483,
        0.000000010042493924,
        0.000000000099322077,
        0.000000000000766380,
        0.000000000000004741,
        0.000000000000000024
    };

    public static void main(String[] args) {
        double y = modBesselFirstZero(1);
        double z = modBesselFirstOne(1);
        System.out.println(y);
        System.out.println(z);
    }
}