package jgm;

public class Util {
	// convert a raw (unsigned) byte to an int since
	// java doesn't actually have unsigned numbers
	public static final int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

	// convert an array with 4 bytes to the integer
	// that it would represent
	public static final int byteArrayToInt(byte[] buf) {
		int i   = 0;
		int pos = 0;
/*		System.out.printf("Bytes: 0x%X 0x%X 0x%X 0x%X\n",
			unsignedByteToInt(buf[pos++]),
			unsignedByteToInt(buf[pos++]),
			unsignedByteToInt(buf[pos++]),
			unsignedByteToInt(buf[pos++]));
		pos = 0;
*/		i += unsignedByteToInt(buf[pos++]) << 0;
		i += unsignedByteToInt(buf[pos++]) << 8;
		i += unsignedByteToInt(buf[pos++]) << 16;
		i += unsignedByteToInt(buf[pos++]) << 24;

		return i;
		//return (int) ((Long.MAX_VALUE & ~((long) Integer.MAX_VALUE)) ^ ((long) i));
	}

	// swap between big and little endian
	public final static int swabInt(int v) {
		return (v >>> 24) | (v << 24) | 
				((v << 8) & 0x00FF0000) | ((v >> 8) & 0x0000FF00);
	}
}
