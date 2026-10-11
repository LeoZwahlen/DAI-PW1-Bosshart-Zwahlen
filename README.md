# filecrypt

A command-line tool to encrypt and decrypt files with AES (GCM or CBC mode), written in Java with picocli.

## Authors

- Thomas Bosshart
- Léo Zwahlen

## Build

```bash
git clone https://github.com/LeoZwahlen/DAI-PW1-Bosshart-Zwahlen.git
cd DAI-PW1-Bosshart-Zwahlen
./mvnw clean package
```

## Usage

Run the commands from the project folder after building. 

```bash
JAR=target/DAI-PW1-Bosshart-Zwahlen-1.0-SNAPSHOT.jar
```
### Help

```bash
java -jar $JAR --help
```

```text
Usage: filecrypt [-hV] [COMMAND]
Encrypts and decrypts files with AES (GCM or CBC).
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  encrypt  Encrypts a file with AES (GCM or CBC).
  decrypt  Decrypts a file that was encrypted with the encrypt command.
```

Each command also has its own help for example : 

```bash
java -jar $JAR decrypt --help
```

```text
Usage: filecrypt decrypt [-hV] --input=<inputFile> --key=<key> --mode=<mode>
--output=<outputFile>
Decrypts file that was encrypted with the encrypt command.
-h, --help                Show this help message and exit.
--input=<inputFile>   Encrypted file to read from.
--key=<key>           Key used for encryption (16,24,32 characters).
--mode=<mode>         AES mode for encryption: GCM or CBC.
--output=<outputFile> File to write decrypted content to.
-V, --version             Print version information and exit.
```

### Encrypt

```bash
java -jar $JAR encrypt --input examples/hello.txt --output hello.enc --mode GCM --key 0123456789abcdef
```

Does not print anything in console as there aren't any errors.

We can however list file details for info : 

```bash
ls -l examples/hello.txt hello.enc
```

and see :

```text
-rw-r--r-- 1 Thomas Thomas 34 Oct 11 16:08 examples/hello.txt
-rw-r--r-- 1 Thomas Thomas 62 Oct 11 16:08 hello.enc
```

### Decrypt

Must use same key and mode as encryption.

```bash
java -jar $JAR decrypt --input hello.enc --output helloDecrypted.txt --mode GCM --key 0123456789abcdef
cat helloDecrypted.txt
```

```text
Bonjour, comment ça va ? élève.
```

### Errors

Tool prints clear error messages on stderr.

Wrong key : java -jar $JAR decrypt --input hello.enc --output out.txt --mode GCM --key fedcba9876543210
Decryption failed: wrong key, wrong mode, or the file is corrupted.

Wrong mode : java -jar $JAR decrypt --input hello.enc --output out.txt --mode CBC --key 0123456789abcdef
Decryption failed: wrong key, wrong mode, or the file is corrupted.

Input file not found : java -jar $JAR decrypt --input missing.enc --output out.txt --mode GCM --key 0123456789abcdef
File error: missing.enc (No such file or directory)

Key wrong length : java -jar $JAR decrypt --input hello.enc --output out.txt --mode GCM --key abc
Error: The key must be 16, 24 or 32 bytes long (got 3)

(tiny.enc contains 'abc')
TooShort : java -jar $JAR decrypt --input tiny.enc --output out.txt --mode GCM --key 0123456789abcdef
File error: The input file is too short to be an encrypted file

Missing option : java -jar $JAR decrypt --input hello.enc --output out.txt --mode GCM;
Missing required option: '--key=<key>'
Usage: filecrypt decrypt [-hV] --input=<inputFile> --key=<key> --mode=<mode>
--output=<outputFile>
Decrypts file that was encrypted with the encrypt command.
-h, --help                Show this help message and exit.
--input=<inputFile>   Encrypted file to read from.
--key=<key>           Key used for encryption (16,24,32 characters).
--mode=<mode>         AES mode for encryption: GCM or CBC.
--output=<outputFile> File to write decrypted content to.
-V, --version             Print version information and exit.

## Example files



## How it works

- Key: 16, 24 or 32 characters (AES-128/192/256)
- File format: IV (12 bytes GCM / 16 bytes CBC), then the encrypted data
- GCM vs CBC (2-3 lines: GCM also detects a wrong key or a modified file)
- I/O choices: binary streams, processing in 1024-byte blocks, try-with-resources

## Dependencies

- picocli: command-line parsing
- JUnit: tests (`./mvnw test`)

## Sources and AI use

(Baeldung, JUnit User Guide, Java Cipher documentation, Claude used to explain concepts and review code)