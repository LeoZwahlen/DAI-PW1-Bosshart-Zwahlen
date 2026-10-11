package ch.heigvd;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

class CryptoServiceTest {

    private static final String KEY = "0123456789abcdef";

    private static byte[] encrypt(byte[] data, String key, CipherMode mode) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CryptoService.encrypt(new ByteArrayInputStream(data), out, key, mode);
        return out.toByteArray();
    }

    private static byte[] decrypt(byte[] data, String key, CipherMode mode) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CryptoService.decrypt(new ByteArrayInputStream(data), out, key, mode);
        return out.toByteArray();
    }

    @Test
    void gcmRoundTrip() throws Exception{
        byte[] original = "Hello, comment ça vas ? élève".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = encrypt(original, KEY, CipherMode.GCM);
        byte[] decrypted = decrypt(encrypted, KEY, CipherMode.GCM);
        assertArrayEquals(original,decrypted);
    }
    @Test
    void cbcRoundTrip() throws Exception{
        byte[] original = "Hello, comment ça vas ? élève".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = encrypt(original, KEY, CipherMode.CBC);
        byte[] decrypted = decrypt(encrypted, KEY, CipherMode.CBC);
        assertArrayEquals(original,decrypted);
    }
    @Test
    void emptyInputRoundTrip() throws Exception{
        byte[] original = "".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = encrypt(original, KEY, CipherMode.CBC);
        byte[] decrypted = decrypt(encrypted, KEY, CipherMode.CBC);
        assertArrayEquals(original,decrypted);
    }
}