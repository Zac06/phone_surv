public class Arrays {
    /*public static byte[] copyOf(byte[] original, int newLength) {
        byte[] copy = new byte[newLength];
        for (int i = 0; i < original.length && i < newLength; i++) {
            copy[i] = original[i];
        }

        return copy;
    }*/

    public static byte[] copyOf(byte[] original, int newLength) {
        byte[] copy = new byte[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }


    public static String toString(int[] array) {
        if (array == null)
            return "null";
        int iMax = array.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuffer sb = new StringBuffer();
        sb.append('[');
        for (int i = 0; ; i++) {
            sb.append(array[i]);
            if (i == iMax)
                return sb.append(']').toString();
            sb.append(", ");
        }
    }

}
