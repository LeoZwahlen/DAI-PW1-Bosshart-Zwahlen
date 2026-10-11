package ch.heigvd;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import java.security.InvalidKeyException;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;

import javax.crypto.spec.IvParameterSpec;

public class CryptoService {

    private static SecretKeySpec toAesKey(String key) throws InvalidKeyException{
        // Convert the key String into bytes
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        // AES accepts 128, 192 or 256-bit keys
        if (keyBytes.length != 16 &&
                keyBytes.length != 24 &&
                keyBytes.length != 32) {

            throw new InvalidKeyException("The key must be 16, 24 or 32 bytes long (got " + keyBytes.length + ")");
        }

        // Create an AES key object
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static void encrypt(
            InputStream input,
            OutputStream output,
            String key,
            CipherMode mode
    ) throws IOException, GeneralSecurityException {

        SecretKeySpec secretKey = toAesKey(key);

        if (mode == CipherMode.GCM) {

            // Generate a unique random nonce
            SecureRandom secureRandom = new SecureRandom();

            byte[] nonce = new byte[12];
            secureRandom.nextBytes(nonce);

            // GCM parameters: 128-bit authentication tag + nonce
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonce);

            // Create and initialize the AES-GCM cipher
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    gcmSpec
            );

            // Store nonce at the beginning of the encrypted file
            output.write(nonce);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {

                byte[] encrypted = cipher.update(
                        buffer,
                        0,
                        bytesRead
                );

                if (encrypted != null) {
                    output.write(encrypted);
                }
            }

            // Finish encryption + generate GCM authentication tag
            byte[] finalBytes = cipher.doFinal();

            if (finalBytes != null) {
                output.write(finalBytes);
            }
        }

        if (mode == CipherMode.CBC) {

            //  Generate a random IV for CBC
            SecureRandom secureRandom = new SecureRandom();

            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    ivSpec
            );

            // Store iv at the beginning of the encrypted file
            output.write(iv);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {

                byte[] encrypted = cipher.update(
                        buffer,
                        0,
                        bytesRead
                );

                if (encrypted != null) {
                    output.write(encrypted);
                }
            }

            // Finish CBC encryption and apply final padding
            byte[] finalBytes = cipher.doFinal();

            if (finalBytes != null) {
                output.write(finalBytes);
            }
        }
    }

    public static void decrypt(
            InputStream input,
            OutputStream output,
            String key,
            CipherMode mode
    ) throws IOException, GeneralSecurityException {

        int ivLength = (mode == CipherMode.GCM) ? 12 : 16;

        Cipher cipher = Cipher.getInstance(mode == CipherMode.GCM ? "AES/GCM/NoPadding" : "AES/CBC/PKCS5Padding");

        byte[] iv = input.readNBytes(ivLength);

        if(iv.length != ivLength){
            throw new IOException("The input file is too short to be an encrypted file");
        }

        SecretKeySpec secretKey = toAesKey(key);

        if(mode == CipherMode.GCM){
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(128, iv)
            );
        } else{
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    new IvParameterSpec(iv)
            );
        }

        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = input.read(buffer)) != -1) {

            byte[] decrypted = cipher.update(
                    buffer,
                    0,
                    bytesRead
            );

            if (decrypted != null) {
                output.write(decrypted);
            }
        }

        // Finish decryption : GCM checks the tag and CBC removes the padding
        byte[] finalBytes = cipher.doFinal();

        if (finalBytes != null) {
            output.write(finalBytes);
        }
    }
}