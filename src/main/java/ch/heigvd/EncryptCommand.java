package ch.heigvd;

import picocli.CommandLine;
import java.io.File; // pour les files
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Callable;

// pour la partie AES
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import javax.crypto.spec.GCMParameterSpec;


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

        //convert the text of the key into octets
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        //check key length
        if(keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32){
            System.out.println("Invalid AES key length");
            return 1;
        }
        //don't encrypt anything just associate it AES (create an object that cryptographique class can use)
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // Generate a unique random nonce for this encryption
        SecureRandom secureRandom = new SecureRandom();

        byte[] nonce = new byte[12];

        //put random bytes into the array
        secureRandom.nextBytes(nonce);

        //Configure GCM with a 128-bit authentication tag and the generated nonceT
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonce);


        try {
            //Ask Java for an implentation AES without padding
            //Depends on the mod choose by the user
            if (mode == CipherMode.GCM) {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

                cipher.init(
                        Cipher.ENCRYPT_MODE,
                        secretKey,
                        gcmSpec
                );



                try (
                        FileInputStream input = new FileInputStream(inputFile);
                        FileOutputStream output = new FileOutputStream(outputFile);
                ) {
                    //Get the random nonce into the file for the Decryption
                    output.write(nonce);

                    byte[] buffer = new byte[1024];//créer un tableau de byte
                    int bytesRead;

                    while ((bytesRead = input.read(buffer)) != -1) {

                        // Encrypt the bytes currently stored in the buffer
                        byte[] encrypted = cipher.update(buffer, 0, bytesRead);

                        if (encrypted != null) {
                            output.write(encrypted);
                        }
                    }

                    // Finalize encryption and generate the GCM authentication tag
                    byte[] finalBytes = cipher.doFinal();
                    if (finalBytes != null) {
                        output.write(finalBytes);
                    }

                } catch (IOException e) {

                    System.out.println("Erreur : " + e.getMessage());
                    return 1;
                }




            }



            if (mode == CipherMode.CBC) {
                System.out.println("CBC not implemented yet");
                return 1;
            }



        } catch (GeneralSecurityException e) {
            System.out.println("Encryption error: " + e.getMessage());
            return 1;
        }




        return 0;
    }

}
