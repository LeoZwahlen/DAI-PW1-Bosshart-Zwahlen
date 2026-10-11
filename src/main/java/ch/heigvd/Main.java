package ch.heigvd;

import picocli.CommandLine;

@CommandLine.Command(
        name = "filecrypt",
        description = "Encrypts and decrypts files with AES (GCM or CBC).",
        mixinStandardHelpOptions = true,
        subcommands = {
                EncryptCommand.class,
                DecryptCommand.class
        }
)
public class Main {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
        System.exit(exitCode);
    }
}