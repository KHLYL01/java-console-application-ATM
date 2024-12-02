package util;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Base64;

public class RSAUtil {

    public static String encrypt(String message,PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptBytes = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptBytes);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String message,PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encryptBytes = cipher.doFinal(Base64.getDecoder().decode(message));
            return new String(encryptBytes);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toStringPublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static PublicKey toPublicKey(String publicKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new java.security.spec.X509EncodedKeySpec(keyBytes));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
