package jgm;

/**
 * Contains some utility functions 
 * @author Tim
 * @since 0.1
 */
public class Util {	
	/**
	 * Convert a raw (unsigned) byte to an int since
	 * Java doesn't actually have unsigned numbers.
	 * @param b The byte to convert
	 * @return The converted byte
	 */
	public static final int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

	/**
	 * Convert an array with 4 bytes to the 32-bit integer
	 * that it would represent.
	 * @param buf An array of 4 bytes to convert
	 * @return The converted int
	 * @throws IllegalArgumentException If buf is not length 4
	 */
	public static final int byteArrayToInt(byte[] buf) 
		throws IllegalArgumentException {
		if (buf.length != 4) 
			throw new IllegalArgumentException("Need array of length 4");
		
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

	/**
	 * Swap an integer between big and little endian.
	 * @param i The integer to swap
	 * @return The swapped integer
	 */
	public final static int swapInt(int i) {
		return (i >>> 24) | (i << 24) | 
				((i << 8) & 0x00FF0000) | ((i >> 8) & 0x0000FF00);
	}
}
