package ch.heigvd;

import picocli.CommandLine;

@CommandLine.Command(
        name = "filecrypt",
        description = "Encrypts and decrypts files with AES (GCM or CBC).",
        subcommands = {
                EncryptCommand.class,
                DecryptCommand.class
        }
)
public class Main {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}