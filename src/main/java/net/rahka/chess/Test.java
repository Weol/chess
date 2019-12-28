package net.rahka.chess;

public class Test {

    public static void main(String[] args) {
        for (int i = 0; i < 64; i++) {
            long kernel = (-0x8000000000000000L >>> (63 - i));

            System.out.printf("(%02d): ", i);
            System.out.print(PrintBinaryLong(kernel));
            System.out.println(" (" + kernel + ") ");
        }
    }

    public static String PrintBinaryLong(long x) {
        String binary = "";
        for (int i = 0; i < (64 - Long.toBinaryString(x).length()); i++) {
            binary += "0";
        }
        binary += Long.toBinaryString(x);

        String asd = "";
        for (int i = 0; i < binary.length(); i++) {
            if (i % 8 == 0) {
                asd += " ";
            }
            asd += binary.charAt(i);
        }

        return asd;
    }

    public static String[] PrintBinaryLong(long[] x) {
        String[] asd = new String[x.length];
        for (int i = 0; i < x.length; i++) {
            asd[i] = Test.PrintBinaryLong(x[i]);
        }
        return asd;
    }

}
