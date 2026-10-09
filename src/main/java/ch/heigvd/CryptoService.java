package ch.heigvd;

import java.io.File;
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

    public static void encrypt(
            InputStream input,
            OutputStream output,
            String key,
            CipherMode mode
    ) throws IOException, GeneralSecurityException {

        // Convert the key String into bytes
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        // AES accepts 128, 192 or 256-bit keys
        if (keyBytes.length != 16 &&
                keyBytes.length != 24 &&
                keyBytes.length != 32) {

            throw new InvalidKeyException("Invalid AES key length");
        }

        // Create an AES key object
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

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
            File inputFile,
            File outputFile,
            String key,
            CipherMode mode
    ) {
        // TODO
    }
}