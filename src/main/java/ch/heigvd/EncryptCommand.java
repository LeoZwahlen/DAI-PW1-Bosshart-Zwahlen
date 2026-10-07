package ch.heigvd;

import picocli.CommandLine;
import java.io.File; // pour les files
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "encrypt"
)

// implements Callable<Integer> --> respect l'interface callable
public class EncryptCommand implements Callable<Integer> {

    //commande pour que picocli comprenne que --key xxxx -> key = xxxx
    @CommandLine.Option(
            names = "--key",
            required = true // utilisateur est obligé de renseigner cet argument
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

    //méthode obligatoire a implémenter
    @Override
    public Integer call() {

        System.out.println("Encrypt command launched");
        System.out.println("Key: " + key);
        System.out.println("Mode: " + mode);
        System.out.println("Input: " + inputFile);
        System.out.println("Output: " + outputFile);

        return 0;
    }

}
