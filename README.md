# filecrypt

A command-line tool to encrypt and decrypt files with AES, in GCM or CBC mode. It is written in Java with [picocli](https://picocli.info/) as the first practical work (PW1) of the DAI course at HEIG-VD.

## Authors

- Thomas Bosshart
- Léo Zwahlen

## Build

Requirement: a Java 25 JDK. Maven does not need to be installed: the Maven wrapper downloads it.

```bash
git clone https://github.com/LeoZwahlen/DAI-PW1-Bosshart-Zwahlen.git
cd DAI-PW1-Bosshart-Zwahlen
./mvnw clean package
```

The build runs the tests and creates an executable JAR in `target/`, with picocli included.

## Usage

Run the commands from the project folder after building. To keep them short:

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
  encrypt  Encrypts file with GCM or CBC.
  decrypt  Decrypts file that was encrypted with the encrypt command.
```

Each command has its own help:

```bash
java -jar $JAR encrypt --help
```

```text
Usage: filecrypt encrypt [-hV] --input=<inputFile> --key=<key> --mode=<mode>
                         --output=<outputFile>
Encrypts file with GCM or CBC.
  -h, --help                Show this help message and exit.
      --input=<inputFile>   File to encrypt.
      --key=<key>           Key to encrypt with (16,24,32 characters).
      --mode=<mode>         AES mode either GCM or CBC.
      --output=<outputFile> File to write encrypted stuff to.
  -V, --version             Print version information and exit.
```

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

Like `cp` or `mv`, the command prints nothing when it succeeds. The result is a new binary file:

```bash
ls -l examples/hello.txt hello.enc
```

```text
-rw-r--r-- 1 Thomas Thomas 34 Oct 11 16:08 examples/hello.txt
-rw-r--r-- 1 Thomas Thomas 62 Oct 12 12:50 hello.enc
```

The encrypted file is 28 bytes larger than the original: a random 12-byte IV at the start and a 16-byte authentication tag at the end (see [File format](#file-format)).

### Decrypt

Use the same key and the same mode as for encryption. The mode is case-insensitive (`gcm` works too).

```bash
java -jar $JAR decrypt --input hello.enc --output hello-decrypted.txt --mode GCM --key 0123456789abcdef
cat hello-decrypted.txt
```

```text
Bonjour, comment ça va ? élève.
```

The decrypted file is byte-for-byte identical to the original (`cmp examples/hello.txt hello-decrypted.txt` prints nothing).

### Errors

Errors are printed on the error output (stderr), and the tool exits with code `1`. Invalid command lines are reported by picocli with the usage message and exit code `2`.

| Situation | Message | Exit code |
|---|---|---|
| Wrong key or wrong mode | `Decryption failed: wrong key, wrong mode, or the file is corrupted.` | 1 |
| Input file does not exist | `File error: missing.enc (No such file or directory)` | 1 |
| Key of the wrong length | `Error: The key must be 16, 24 or 32 bytes long (got 3)` | 1 |
| Input is not an encrypted file | `File error: The input file is too short to be an encrypted file` | 1 |
| Key of the wrong length (encrypt) | `Encryption error: The key must be 16, 24 or 32 bytes long (got 3)` | 1 |
| Missing option | `Missing required option: '--key=<key>'` (followed by the usage) | 2 |
| Invalid mode | `Invalid value for option '--mode': expected one of [GCM, CBC] (case-insensitive) but was 'XYZ'` | 2 |

Example with a wrong key:

```bash
java -jar $JAR decrypt --input examples/hello-gcm.enc --output out.txt --mode GCM --key fedcba9876543210
```

```text
Decryption failed: wrong key, wrong mode, or the file is corrupted.
```

## Example files

The `examples/` folder contains files to try the tool right away. Files ending in `.enc` are encrypted files. The demo key is `0123456789abcdef`.

| File | Content |
|---|---|
| `hello.txt` | A short French sentence with accents (34 bytes in UTF-8) |
| `hello-gcm.enc` | `hello.txt` encrypted in GCM mode with the demo key |
| `hello-cbc.enc` | `hello.txt` encrypted in CBC mode with the demo key |
| `too-short.enc` | 3 bytes only: not a valid encrypted file, to see an error |

```bash
java -jar $JAR decrypt --input examples/hello-gcm.enc --output gcm.txt --mode GCM --key 0123456789abcdef
java -jar $JAR decrypt --input examples/hello-cbc.enc --output cbc.txt --mode CBC --key 0123456789abcdef
java -jar $JAR decrypt --input examples/too-short.enc --output x.txt --mode GCM --key 0123456789abcdef
```

## How it works

### Key

The key is given as text after `--key`. Its UTF-8 bytes are used directly as the AES key, so it must be 16, 24 or 32 bytes long (AES-128, AES-192 or AES-256). A character like `é` takes 2 bytes in UTF-8, which is why the length is checked in bytes, not in characters.

### File format

AES needs a random initialization vector (IV) for every encryption, and decryption needs the same IV. The IV is not secret, so `encrypt` writes it at the start of the output file and `decrypt` reads it back:

| Part | GCM | CBC |
|---|---|---|
| IV | 12 bytes | 16 bytes |
| Encrypted data | same size as the input | input padded to a multiple of 16 bytes (PKCS#5) |
| Authentication tag | 16 bytes, at the end | none |

The mode is not stored in the file: the same `--mode` must be given to `decrypt`. In the hex dump of `hello.enc` above (GCM, 62 bytes), the first 12 bytes are the IV:

```text
0000000 5a 2a 62 d4 e6 c9 8a 97 f4 f0 0d 4f 35 2b aa af
0000016 a2 92 34 04 74 b5 a5 83 c9 be 51 25 29 6d 81 57
0000032 9e 03 78 e0 21 8a 6b 3c 06 d1 d4 01 16 e7 96 78
0000048 8e 53 84 dc ca e4 f8 9b 80 16 7a de bf b4
```

### GCM or CBC?

- **GCM** encrypts the data and computes an authentication tag. During decryption the tag is checked, so a wrong key or a modified file is always detected.
- **CBC** chains 16-byte blocks and pads the last one. It has no integrity check: a wrong key is usually detected because the padding is invalid, but not always.

### I/O choices

- **Binary files.** Encrypted data is not text, so we read and write the files as raw bytes (`FileInputStream` / `FileOutputStream`).
- **Small blocks.** The file is read 1024 bytes at a time instead of all at once, so big files work too.
- **Files always closed.** The files are opened in a `try (...)` block, so Java closes them automatically, even when there is an error.
- **Processing separated from the commands.** `CryptoService` does the encryption on streams, and the commands only open the files and call it. This is also what lets us test it without real files.

## Tests

```bash
./mvnw test
```

```text
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

The tests check that encrypting then decrypting gives back the original bytes, in GCM and in CBC mode, and for an empty input (edge case). The data is built in memory with `ByteArrayInputStream` and `ByteArrayOutputStream`, so the tests do not depend on any file.

## Dependencies

- [picocli](https://picocli.info/) 4.7.7: command-line parsing (commands, options, help messages).
- [JUnit](https://junit.org/) 6.1.3 (test scope only): unit tests.
- maven-shade-plugin 3.6.2 (build only): builds an executable JAR that includes picocli.

The encryption itself uses `javax.crypto`, which is part of the JDK: no external crypto library.

## Sources and AI use

- DAI course material, chapters 02 to 04: Git workflow, Maven wrapper, shade plugin and JUnit setup, Java I/O patterns.
- [Baeldung: Java AES Encryption and Decryption](https://www.baeldung.com/java-aes-encryption-decryption): AES modes in Java.
- [Java SE 25 documentation of `javax.crypto.Cipher`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/javax/crypto/Cipher.html).
- [JUnit User Guide](https://junit.org/junit5/docs/current/user-guide/).
- AI: Claude (Anthropic) was used to explain concepts, to review our code, to help write this README.