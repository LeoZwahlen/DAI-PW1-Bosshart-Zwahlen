package ch.heigvd;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;

import javax.crypto.Cipher;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoService {

    private static SecretKeySpec toAesKey(String key)
            throws InvalidKeyException {

        // Convert the key String into bytes
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        // AES accepts 128, 192 or 256-bit keys
        if (keyBytes.length != 16 &&
                keyBytes.length != 24 &&
                keyBytes.length != 32) {

            throw new InvalidKeyException(
                    "The key must be 16, 24 or 32 bytes long (got "
                            + keyBytes.length + ")"
            );
        }

        // Create an AES key object
        return new SecretKeySpec(keyBytes, "AES");
    }

    //read data from the input process it using the cipher(encryption or decryption) and write the result in the output stream
    private static void processStream(InputStream input, OutputStream output, Cipher cipher)
            throws IOException, GeneralSecurityException {

        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = input.read(buffer)) != -1) {

            byte[] processed = cipher.update(
                    buffer,
                    0,
                    bytesRead
            );

            if (processed != null) {
                output.write(processed);
            }
        }

        // Finish the cryptographic operation
        byte[] finalBytes = cipher.doFinal();

        if (finalBytes != null) {
            output.write(finalBytes);
        }
    }

    //Encrypts data using AES-GCM or AES-CBC
    public static void encrypt(InputStream input, OutputStream output, String key, CipherMode mode)
            throws IOException, GeneralSecurityException {

        SecretKeySpec secretKey = toAesKey(key);

        SecureRandom secureRandom = new SecureRandom();

        if (mode == CipherMode.GCM) {

            // Generate a unique random nonce for GCM
            byte[] nonce = new byte[12];
            secureRandom.nextBytes(nonce);


            // 128-bit authentication tag + 12 byte nonce
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonce);

            // Create the AES-GCM cipher
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            // Configure the cipher for encryption
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            // Store the nonce at the beginning of the encrypted file
            // so it can be recovered during decryption
            output.write(nonce);

            processStream(input, output, cipher);

        } else if (mode == CipherMode.CBC) {

            // Generate a random IV for CBC
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);

            IvParameterSpec ivSpec =
                    new IvParameterSpec(iv);

            // Create the AES-CBC cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Configure the cipher for encryption
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // Store the IV at the beginning of the encrypted file
            // so it can be recovered during decryption
            output.write(iv);

            processStream(input, output, cipher);
        }
    }


    public static void decrypt(InputStream input, OutputStream output, String key, CipherMode mode)
            throws IOException, GeneralSecurityException {

        // GCM uses a 12-byte nonce, CBC uses a 16-byte IV
        int initializationLength = (mode == CipherMode.GCM) ? 12 : 16;

        // Read the nonce or IV stored at the beginning of the file
        byte[] initializationData = input.readNBytes(initializationLength);

        if (initializationData.length != initializationLength) {
            throw new IOException("The input file is too short to be an encrypted file");
        }

        SecretKeySpec secretKey = toAesKey(key);

        Cipher cipher;

        if (mode == CipherMode.GCM) {

            cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, initializationData));

        } else {

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(initializationData));
        }

        processStream(input, output, cipher);
    }
}