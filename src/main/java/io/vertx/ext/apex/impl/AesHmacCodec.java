package io.vertx.ext.apex.impl;

import io.vertx.ext.apex.Crypto;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Implementation of <a href="http://en.wikipedia.org/wiki/Authenticated_encryption">Encrypt-Then-Mac</a> based on the combination of AES and an HMAC.
 * To be accurate AES refers to "AES/CBC/PKCS5Padding". By default, AES 256 and HMac 256 is used, however AES 128, AES_192, AES 256 
 * and HMAC SHA 256, HMAC SHA 384, HMAC SHA 512 are available as well to provide either weaker or better protection
 *  
 * Also note that a new IV is generated for each encryption. 
 * 
 * @author <a href="mailto://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 *
 */
public class AesHmacCodec implements Crypto {

  public enum AesAlgorithm {
    AES_128("AES/CBC/PKCS5Padding", 128),
    AES_192("AES/CBC/PKCS5Padding", 192),
    AES_256("AES/CBC/PKCS5Padding", 256);

    private String algorithm;
    private int keySize;
    
    AesAlgorithm(String algorithm, int keySize) {
      Objects.requireNonNull(algorithm);
      
      this.algorithm = algorithm;
      this.keySize = keySize;
    }
  }

  public enum MacAlgorithm {
    HMAC_SHA_256("HmacSHA256", 256),
    HMAC_SHA_384("HmacSHA384", 384),
    HMAC_SHA_512("HmacSHA512", 512);

    private String algorithm;
    private int keySize;
    
    MacAlgorithm(String algorithm, int keySize) {
      Objects.requireNonNull(algorithm);
      
      this.algorithm = algorithm;
      this.keySize = keySize;
    }
  }

  private final static int DEFAULT_ITERATIONS = 1024;

  private final static Key getCipherKey(byte[] key) {
    return new SecretKeySpec(key, "AES");
  }

  private final static Key getMacKey(Mac mac, byte[] key) {
    String algorithm = mac.getAlgorithm();
    return new SecretKeySpec(key, algorithm);
  }

  /**
   * Builds a new Codec based on AES_256 + HMAC_SHA_256
   *  
   * @param aesPassphrase
   * @param macPassphrase
   * @param saltForKeyGenerator
   * @throws GeneralSecurityException
   */
  public final static Crypto build(String aesPassphrase, String macPassphrase, String saltForKeyGenerator) throws GeneralSecurityException {
    return build(AesAlgorithm.AES_256, macPassphrase, MacAlgorithm.HMAC_SHA_256, macPassphrase, saltForKeyGenerator);
  }

  /**
   * Builds a new Codec based on the specified AES algo, AES passPhrase, Mac algo, Mac passPhrase, and salt (to generate key from passphrases)
   * Note that the pass phrases are used to compute a key whose size is determined from the algo (either AesAlgorithm or MacAlgorithm).
   * 
   * @param aesAlgorithm
   * @param aesPassphrase
   * @param macAlgorithm
   * @param macPassphrase
   * @param saltForKeyGenerator 
   * @throws GeneralSecurityException
   */
  public final static Crypto build(AesAlgorithm aesAlgorithm, String aesPassphrase, MacAlgorithm macAlgorithm, String macPassphrase, String saltForKeyGenerator) throws GeneralSecurityException {
    return build(
            aesAlgorithm, 
            generateKey(aesPassphrase, saltForKeyGenerator, DEFAULT_ITERATIONS, aesAlgorithm.keySize), 
            macAlgorithm, 
            generateKey(macPassphrase, saltForKeyGenerator, DEFAULT_ITERATIONS, macAlgorithm.keySize)
    );
  }

  /**
   * Builds a new AesHmacCodec with the specified AES algo, AES key, Mac algo and Mac key
   * Note that the size of the both keys (aesKey and macKey) must match the size expected by the aesAlgorithm and macAlgorithm
   * 
   * @param aesAlgorithm
   * @param aesKey
   * @param macAlgorithm
   * @param macKey
   * @throws GeneralSecurityException
   */
  public final static Crypto build(AesAlgorithm aesAlgorithm, byte[] aesKey, MacAlgorithm macAlgorithm, byte[] macKey) throws GeneralSecurityException {
    return new AesHmacCodec(aesAlgorithm, aesKey, macAlgorithm, macKey);
  }

  /**
   * Constructs a key of the specified size based on the specified passPhrase, salt and number of iterations
   * 
   * @param passPhrase
   * @param salt
   * @param iterations
   * @param keySize
   * @return
   * @throws GeneralSecurityException
   */
  public final static byte[] generateKey(String passPhrase, String salt, int iterations, int keySize) throws GeneralSecurityException {
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    KeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), salt.getBytes(), iterations, keySize); ;
    return factory.generateSecret(spec).getEncoded();
  }

  private Key aesKey;
  private Cipher aesCipherDecode;
  private Cipher aesCipherEncode;
  private int ivSize;
  private Mac mac;
  private int macSize;
  private SecureRandom ivGenerator;
  
  /**
   * Constructs a new AesHmacCodec with the specified AES algo, AES key, Mac algo and Mac key
   * Note that the size of the both keys (aesKey and macKey) must match the size expected by the aesAlgorithm and macAlgorithm
   * 
   * @param aesAlgorithm
   * @param aesKey
   * @param macAlgorithm
   * @param macKey
   * @throws GeneralSecurityException
   */
  private AesHmacCodec(AesAlgorithm aesAlgorithm, byte[] aesKey, MacAlgorithm macAlgorithm, byte[] macKey) throws GeneralSecurityException {
    Objects.requireNonNull(aesAlgorithm);
    Objects.requireNonNull(aesKey);
    Objects.requireNonNull(macAlgorithm);
    Objects.requireNonNull(macKey);
    
    // check the size of both encryption and signatureKey
    if (aesKey.length!=aesAlgorithm.keySize/8
        || macKey.length!=macAlgorithm.keySize/8) {
      throw new GeneralSecurityException("Invalid key size");
    }
    
    // setup 2 ciphers, one to encrypt and one to decrypt
    this.aesCipherEncode = Cipher.getInstance(aesAlgorithm.algorithm);
    this.aesCipherDecode = Cipher.getInstance(aesAlgorithm.algorithm);
    this.aesKey = getCipherKey(aesKey);
    this.ivSize = aesCipherEncode.getBlockSize();
    
    // setup the mac
    this.mac = Mac.getInstance(macAlgorithm.algorithm);
    this.mac.init(getMacKey(this.mac, macKey));
    this.macSize = this.mac.getMacLength(); // for AES it should always be 16

    // secure random to generate iv
    this.ivGenerator = new SecureRandom();
  }

  private byte[] concatenate(byte[]... buffers) {
    int bufferSize = 0;
    for (byte[] buffer : buffers) {
      bufferSize += buffer.length;
    }
    ByteBuffer result = ByteBuffer.allocate(bufferSize);
    // now concatenate each buffer
    for (byte[] buffer : buffers) {
      result.put(buffer);
    }
    return result.array();
  }
  
  @Override
  public String encrypt(String unencryptedData) {
    Objects.requireNonNull(unencryptedData);
    
    try {
      byte[] encryptedBytes = encrypt(unencryptedData.getBytes());
      return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String decrypt(String encryptedData) {
    Objects.requireNonNull(encryptedData);
    
    try {
      byte[] base64DecodedValue = Base64.getDecoder().decode(encryptedData);
      byte[] decryptedValue = decrypt(base64DecodedValue);
      return new String(decryptedValue);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * reverse operation of the encode method. works as follow:
   *    dataToEncode    := iv + encryptedData + mac
   *    newMac          := mac(iv + encryptedData)
   *    if (newMac!=mac) {
   *        throw an exception ("data has been tampered")
   *    }
   *    return decrypt(encryptedData)

   * @param unencryptedData
   * @return
   * @throws BadPaddingException 
   * @throws IllegalBlockSizeException 
   * @throws InvalidAlgorithmParameterException 
   * @throws InvalidKeyException 
   * @throws Throwable
   */
  private synchronized byte[] decrypt(byte[] unencryptedData) throws GeneralSecurityException {
    // compute a new mac
    byte[] newMac = mac(unencryptedData, 0, unencryptedData.length-this.macSize); 
    // make sure the new computed mac matches the one that's inside the buffer
    if (!equals(unencryptedData, unencryptedData.length-this.macSize, newMac, 0, this.macSize)) {
      throw new GeneralSecurityException("data has been tampered");
    }
    // extract the iv from the data
    byte[] iv = new byte[ivSize];
    System.arraycopy(unencryptedData, 0, iv, 0, ivSize);
    // finally return the decrypted value
    return decrypt(unencryptedData, ivSize, unencryptedData.length - ivSize - this.macSize, iv);
  }
  
  private synchronized byte[] decrypt(byte[] toDecrypt, int offset, int length, byte[] iv) throws GeneralSecurityException {
    // create the param spec for the iv
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    aesCipherDecode.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
    return aesCipherDecode.doFinal(toDecrypt, offset, length);
  }
  
  /**
   * encode the specified data as follow:
   *    iv              := get a new iv
   *    encryptedData   := encrypt(iv, dataToEncode)
   *    mac             := mac(iv + encryptedData)
   *    result          := iv + encryptedData + mac
   *    
   * @param dataToEncode
   * @return
   * @throws Throwable
   */
  private synchronized byte[] encrypt(byte[] dataToEncode) throws GeneralSecurityException {
    // generate a new iv
    byte[] iv = newIv(ivSize);
    // encrypt data with the 
    byte[] encryptedData = encrypt(dataToEncode, iv);
    // sign the iv + encrypted data
    byte[] mac = mac(iv, encryptedData);
    // build the result which is: iv + encryptedData + mac
    return concatenate(iv, encryptedData, mac);
  }
  
  private synchronized byte[] encrypt(byte[] input, byte[] iv) throws GeneralSecurityException {
    // create the param spec for the iv
    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
    // init the cipher to encrypt with the specified key and iv
    aesCipherEncode.init(Cipher.ENCRYPT_MODE, aesKey, ivParameterSpec);
    // encrypt 
    return aesCipherEncode.doFinal(input);
  }

  /**
   * returns whether two buffers are equals or not
   * 
   * @param buffer1
   * @param offset1
   * @param buffer2
   * @param offset2
   * @param length
   * @return
   */
  private boolean equals(byte[] buffer1, int offset1, byte[] buffer2, int offset2, int length) {
    if (length>0 && buffer1.length>=offset1 + length && buffer2.length>=offset2 + length) {
      for (int i=0; i<length; i++) {
        if (buffer1[offset1 + i]!=buffer2[offset2 + i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  
  private synchronized byte[] mac(byte[]... inputs) throws GeneralSecurityException {
    for (byte[] input: inputs) {
      mac.update(input);
    }
    return mac.doFinal();
  }

  private synchronized byte[] mac(byte[] input, int offset, int length) {
    mac.update(input, offset, length);
    return mac.doFinal();
  }

  private byte[] newIv(int size) {
    byte[] iv = new byte[size];
    ivGenerator.nextBytes(iv);
    return iv;
  }

}
