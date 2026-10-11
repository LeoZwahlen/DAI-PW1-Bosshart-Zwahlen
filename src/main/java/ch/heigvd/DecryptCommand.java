package ch.heigvd;

import picocli.CommandLine;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Callable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

@CommandLine.Command(
        name = "decrypt",
        description = "Decrypts file that was encrypted with the encrypt command.",
        mixinStandardHelpOptions = true
)

// Implements Callable<Integer> so Picocli can execute this command
public class DecryptCommand implements Callable<Integer> {

    // Tells Picocli that --key xxxx should be stored in the key variable
    @CommandLine.Option(
            names = "--key",
            description = "Key used for encryption (16,24,32 characters).",
            required = true // User must provide this argument
    )
    private String key;

    @CommandLine.Option(
            names = "--mode",
            description = "AES mode for encryption: GCM or CBC.",
            required = true
    )
    private CipherMode mode;

    @CommandLine.Option(
            names = "--input",
            description = "Encrypted file to read from.",
            required = true
    )
    private File inputFile;

    @CommandLine.Option(
            names = "--output",
            description = "File to write decrypted content to.",
            required = true
    )
    private File outputFile;

    // Required method from the Callable<Integer> interface
    @Override
    public Integer call() {

        try (
                FileInputStream input = new FileInputStream(inputFile);
                FileOutputStream output = new FileOutputStream(outputFile)
        ) {

            CryptoService.decrypt(
                    input,
                    output,
                    key,
                    mode
            );

        } catch (BadPaddingException | IllegalBlockSizeException e) {
            // wrong key, wrong mode or corrupted file
            System.err.println("Decryption failed: wrong key, wrong mode, or the file is corrupted.");
            return 1;

        } catch (IOException e) {
            // file not found or too short
            System.err.println("File error: " + e.getMessage());
            return 1;

        } catch (GeneralSecurityException e) {
            // key with wrong length for ex
            System.err.println("Error: " + e.getMessage());
            return 1;
        }

        return 0;
    }
}
