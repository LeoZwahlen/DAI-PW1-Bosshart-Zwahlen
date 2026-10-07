package ch.heigvd;

import picocli.CommandLine;

@CommandLine.Command(
        name = "filecrypt",
        subcommands = {
                EncryptCommand.class
        }
)
public class Main {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}