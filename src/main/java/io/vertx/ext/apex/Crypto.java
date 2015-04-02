package io.vertx.ext.apex;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;

/**
 * A crypto provides method to encrypt and decrypt data. 
 * Its main use case right now it to encrypt Cookies but it could be used to anytime there is a need to protect sensitive information
 * The first concrete implementation of this interface is based on AES/CBC/Padding + Hmac.
 * 
 * @author <a href="mailto://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 *
 */
@VertxGen (concrete=false)
public interface Crypto {
  
  /**
   * Encrypts the specified buffer
   * 
   * @param unencryptedData
   * @return a new buffer containing encrypted data
   * 
   */
  String encrypt(String unencryptedData);

  /**
   * Decrypts the specified buffer
   * 
   * @param encryptedData a buffer which must have previously been encrypted by {link {@link Crypto#encrypt(Buffer)}
   * @return a new buffer containing decrypted data
   * 
   */
  String decrypt(String encryptedData);
  
}
