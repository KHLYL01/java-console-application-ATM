package util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;
import java.util.Base64;

public class AESUtil {

    public static final String secretKeyFile = "Secret.key";
    public static SecretKey secretKey;

    //initial static secret key
    static {
        File file = new File(secretKeyFile);
        if (!file.exists()) {
            //generate secret key and stora in file
            try (
                    ObjectOutputStream writeFile = new ObjectOutputStream(new FileOutputStream(file));
            ) {

                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(256);
                secretKey = keyGenerator.generateKey();

                writeFile.writeObject(secretKey);

            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }
        } else {
            //loading secret key from file
            try (
                    ObjectInputStream readFile = new ObjectInputStream(new FileInputStream(secretKeyFile));
            ) {
                secretKey = (SecretKey) readFile.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static String encrypt(String message) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptBytes = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptBytes);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String decrypt(String message) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptBytes = cipher.doFinal(Base64.getDecoder().decode(message));
            return new String(encryptBytes);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return "";
    }

}
