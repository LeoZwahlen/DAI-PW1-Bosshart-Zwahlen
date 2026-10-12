package ch.heigvd;

import picocli.CommandLine;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Callable;


@CommandLine.Command(
        name = "encrypt",
        description = "Encrypts file with GCM or CBC.",
        mixinStandardHelpOptions = true
)

// Implements Callable<Integer> so Picocli can execute this command
public class EncryptCommand implements Callable<Integer> {

    // Tells Picocli that --key xxxx should be stored in the key variable
    @CommandLine.Option(
            names = "--key",
            description = "Key to encrypt with (16,24,32 characters).",
            required = true // User must provide this argument
    )
    private String key;

    @CommandLine.Option(
            names = {"-m", "--mode"},
            description = "AES mode: GCM or CBC (default: ${DEFAULT-VALUE}).",
            defaultValue = "GCM"
    )
    private CipherMode mode;

    @CommandLine.Option(
            names = "--input",
            description = "File to encrypt.",
            required = true
    )
    private File inputFile;

    @CommandLine.Option(
            names = "--output",
            description = "File to write encrypted stuff to.",
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

            CryptoService.encrypt(
                    input,
                    output,
                    key,
                    mode
            );

        } catch (IOException e) {

            System.err.println("File error: " + e.getMessage());
            return 1;

        } catch (GeneralSecurityException e) {

            System.err.println("Encryption error: " + e.getMessage());
            return 1;
        }

        return 0;
    }
}
