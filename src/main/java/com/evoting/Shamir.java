package com.evoting;

import java.math.BigInteger;
import java.util.Random;

public final class Shamir {

    private BigInteger prime;

    private final int k;
    private final int n;
    private final Random random;

    private static final int CERTAINTY = 50;

    public final class SecretShare {
        public SecretShare(final int num, final BigInteger share) {
            this.num = num;
            this.share = share;
        }

        public int getNum() {
            return num;
        }

        public BigInteger getShare() {
            return share;
        }

        @Override
        public String toString() {
            return "SecretShare [num=" + num + ", share=" + share + "]";
        }

        private final int num;
        private final BigInteger share;
    }

    public Shamir(final int k, final int n) {
        this.k = k;
        this.n = n;

        random = new Random();
    }

    public BigInteger[] split(final BigInteger secret) {
        final int modLength = secret.bitLength() + 1;

        prime = new BigInteger(modLength, CERTAINTY, random);
        final BigInteger[] coeff = new BigInteger[k - 1];

        System.out.println("Prime Number: " + prime);

        for (int i = 0; i < k - 1; i++) {
            coeff[i] = randomZp(prime);
            System.out.println("a" + (i + 1) + ": " + coeff[i]);
        }

        final BigInteger shares[] = new BigInteger[n];
        for (int i = 1; i <= n; i++) {
            BigInteger accum = secret;

            for (int j = 1; j < k; j++) {
                final BigInteger t1 = BigInteger.valueOf(i).modPow(BigInteger.valueOf(j), prime);
                final BigInteger t2 = coeff[j - 1].multiply(t1).mod(prime);

                accum = accum.add(t2).mod(prime);
            }
            shares[i - 1] = accum;
            System.out.println("Share " + shares[i - 1]);
        }

        return shares;
    }

    public BigInteger getPrime() {
        return prime;
    }

    public BigInteger combine(final SecretShare[] shares, final BigInteger primeNum) {
        BigInteger accum = BigInteger.ZERO;
        for (int i = 0; i < k; i++) {
            BigInteger num = BigInteger.ONE;
            BigInteger den = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    num = num.multiply(BigInteger.valueOf(-j - 1)).mod(primeNum);
                    den = den.multiply(BigInteger.valueOf(i - j)).mod(primeNum);
                }
            }

            System.out.println("den: " + den + ", num: " + den + ", inv: " + den.modInverse(primeNum));
            final BigInteger value = shares[i].getShare();

            final BigInteger tmp = value.multiply(num).multiply(den.modInverse(primeNum)).mod(primeNum);
            accum = accum.add(primeNum).add(tmp).mod(primeNum);

            System.out.println("value: " + value + ", tmp: " + tmp + ", accum: " + accum);
        }

        System.out.println("The secret is: " + accum);

        return accum;
    }

    private BigInteger randomZp(final BigInteger p) {
        while (true) {
            final BigInteger r = new BigInteger(p.bitLength(), random);
            if (r.compareTo(BigInteger.ZERO) > 0 && r.compareTo(p) < 0) {
                return r;
            }
        }
    }



//    public static void main(final String[] args) {
//        final com.evoting.Shamir shamir = new com.evoting.Shamir(11, 20);
//
//        final BigInteger secret = new BigInteger("Hiii".getBytes());
//        final BigInteger[] shares = shamir.split(secret);
//        final BigInteger prime = shamir.getPrime();
//
//
////        final com.evoting.Shamir shamir2 = new com.evoting.Shamir(11, 20);
////        final BigInteger result = shamir2.combine(shares, prime);
//
//    }
}
