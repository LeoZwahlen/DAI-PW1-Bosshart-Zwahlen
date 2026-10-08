package ch.heigvd;

import picocli.CommandLine;
import java.io.File; // pour les files
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

        try (
                FileInputStream input = new FileInputStream(inputFile);
                FileOutputStream output = new FileOutputStream(outputFile);
        ) {

            byte[] buffer = new byte[1024];//créer un tableau de byte

            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                System.out.println("Octets lus : " + bytesRead);

                output.write(buffer, 0, bytesRead);

            }


        } catch (IOException e) {

            System.out.println("Erreur : " + e.getMessage());
            return 1;
        }

        return 0;
    }

}
