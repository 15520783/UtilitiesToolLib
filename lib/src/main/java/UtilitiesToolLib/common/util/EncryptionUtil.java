package UtilitiesToolLib.common.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import UtilitiesToolLib.common.constant.CommonConstant;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * AESEncryptionUtil
 * 
 * @author thaint
 *
 */
@Slf4j
@UtilityClass
public class EncryptionUtil {

  private static final String SECRET_KEY_ALGORITHM = "AES";

  /**
   * Encrypt plain text by passPhase using AES algorithm
   * 
   * @param plainText String
   * @return String
   * @throws Exception
   */
  public static String encrypt(String plainText) throws Exception {
    return encrypt(CommonConstant.ENCRYPTION_PASSPHRASE, plainText);
  }

  /**
   * Encrypt plain text by passPhase using AES algorithm
   * 
   * @param passPhrase String
   * @param plainText String
   * @return String
   * @throws Exception
   */
  private static String encrypt(String passPhrase, String plainText) throws Exception {
    try {
      SecretKey secretKey = generateKey(passPhrase);
      byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, secretKey, plainText.getBytes(StandardCharsets.UTF_8));
      return toBase64(encrypted);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new Exception("Failed to encrypt plaintext", e);
    }
  }

  /**
   * Decrypt plain text by passPhase using AES algorithm
   * 
   * @param cipherText String
   * @return String
   * @throws Exception
   */
  public static String decrypt(String cipherText) throws Exception {
    return decrypt(CommonConstant.ENCRYPTION_PASSPHRASE, cipherText);
  }

  /**
   * Decrypt plain text by passPhase using AES algorithm
   * 
   * @param passPhrase String
   * @param cipherText String
   * @return String
   * @throws Exception
   */
  private static String decrypt(String passPhrase, String cipherText) throws Exception {
    try {
      SecretKey key = generateKey(passPhrase);
      byte[] encrypted = fromBase64(cipherText);
      byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, key, encrypted);
      return new String(Objects.requireNonNull(decrypted), StandardCharsets.UTF_8);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new Exception("Failed to decrypt plaintext", e);
    }
  }

  /**
   * Generate secret key using SHA-256 hash algorithm
   * 
   * @param privateKey String
   * @return SecretKey
   * @throws Exception
   */
  private static SecretKey generateKey(String passPhrase) throws Exception {
    try {
      byte[] key = passPhrase.getBytes(CommonConstant.UTF_8);
      MessageDigest sha256 = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_256);
      key = sha256.digest(key);
      return new SecretKeySpec(key, SECRET_KEY_ALGORITHM);
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      log.error(e.getMessage());
      throw new Exception("Failed to generate secret key encode", e);
    }
  }

  /**
   * Do final in encryption processing
   * 
   * @param mode int
   * @param secretKey SecretKey
   * @param bytes byte[]
   * @return byte[]
   * @throws Exception
   */
  private static byte[] doFinal(int mode, SecretKey secretKey, byte[] bytes) throws Exception {
    try {
      Cipher cipher = Cipher.getInstance(SECRET_KEY_ALGORITHM);
      cipher.init(mode, secretKey);
      return cipher.doFinal(bytes);
    } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException
        | NoSuchPaddingException e) {
      log.error(e.getMessage());
      throw new Exception("Failed to do final encrypt/decrypt data", e);
    }
  }

  /**
   * Convert base64 to byte array
   * 
   * @param str String
   * @return byte[]
   */
  private static byte[] fromBase64(String str) {
    return new Base64().decode(str);
  }

  /**
   * Convert byte array to base64
   * 
   * @param ba byte[]
   * @return String
   */
  private static String toBase64(byte[] ba) {
    return new Base64().encodeToString(ba);
  }

}
