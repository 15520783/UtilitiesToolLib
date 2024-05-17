package UtilitiesToolLib.module.TemporaryStorage;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StorageUtil {

  /** UTF8 charset */
  public static final Charset UTF8 = Charset.forName("UTF-8");

  /**
   * Get byte count of specific string in UTF-8 charset
   * 
   * @param value String
   * @return int
   */
  public static int getBytesCount(String value) {
    return value.getBytes(UTF8).length;
  }

  /**
   * Convert byte array to string
   * 
   * @param array byte[]
   * @return String
   */
  public static String convertByteArrayToString(byte[] array) {
    return new String(array, UTF8);
  }

  /**
   * Convert integer value into byte array in Big Endian
   * 
   * @param value int
   * @return byte[]
   */
  public static byte[] convertIntToByteArray(int value) {
    return ByteBuffer.allocate(4).putInt(value).array();
  }

  /**
   * Convert object into byte array in Big Endian
   * 
   * @param value Object
   * @return byte[]
   * @throws IOException
   */
  public static byte[] convertObjectToByteArray(Object value) throws IOException {
    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(byteStream);) {
      objStream.writeObject(value);
      objStream.flush();
      return byteStream.toByteArray();
    } catch (IOException e) {
      throw e;
    }
  }

  /**
   * Convert byte array into integer
   * 
   * @param array byte[]
   * @return int
   */
  public static int convertByteArrayToInt(byte[] array) {
    return ByteBuffer.wrap(array).getInt();
  }

  /**
   * Closes a <code>Closeable</code> unconditionally.
   * <p>
   * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored. This is typically used in finally
   * blocks.
   * 
   * @param closeable the objects to close, may be null or already closed
   */
  public static void closeQuietly(final Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (final IOException ioe) {
      // ignore
    }
  }

}
