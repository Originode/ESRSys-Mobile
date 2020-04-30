package ph.esrconstruction.esrsys.esrsysmobile.utils;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Etc {
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    public static byte[][] divideArray(byte[] source, int chunksize) {

        byte[][] ret = new byte[(int)Math.ceil(source.length / (double)chunksize)][chunksize];
        ByteBuffer cleanSource = ByteBuffer.allocate(ret.length*chunksize);
        cleanSource.put(source);
        source = cleanSource.array();
        int start = 0;

        for(int i = 0; i < ret.length; i++) {

            ret[i] = Arrays.copyOfRange(source,start, start + chunksize);
            start += chunksize ;
        }

        return ret;
    }
}
