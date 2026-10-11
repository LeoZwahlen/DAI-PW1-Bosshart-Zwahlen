package ch.heigvd;

import picocli.CommandLine;
import java.io.File; // pour les files
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Callable;


@CommandLine.Command(
        name = "encrypt",
        description = "Encrypts file with GCM or CBC",
        mixinStandardHelpOptions = true
)

// Implements Callable<Integer> so Picocli can execute this command
public class EncryptCommand implements Callable<Integer> {

    // Tells Picocli that --key xxxx should be stored in the key variable
    @CommandLine.Option(
            names = "--key",
            required = true // User must provide this argument
    )
    private String key;

    @CommandLine.Option(
            names = "--mode",
            required = true
    )
    private CipherMode mode;

    @CommandLine.Option(
            names = "--input",
            required = true
    )
    private File inputFile;

    @CommandLine.Option(
            names = "--output",
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

            System.out.println("File error: " + e.getMessage());
            return 1;

        } catch (GeneralSecurityException e) {

            System.out.println("Encryption error: " + e.getMessage());
            return 1;
        }

        return 0;
    }
}
