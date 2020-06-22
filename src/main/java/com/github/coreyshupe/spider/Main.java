package com.github.coreyshupe.spider;

import com.github.coreyshupe.spider.reconstructor.ClassReconstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    public final static Logger log = LoggerFactory.getLogger("{Spider}");

    public static void main(String[] args) {
        if (args.length < 2) {
            log.error("Failed to read input. Not enough args given.");
            log.error("Please input a target file and an output directory location.");
            System.exit(1);
            return;
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            log.error("Failed to parse input file. Please enter a file which exists.");
            System.exit(1);
            return;
        }
        if (file.isDirectory()) {
            // todo we should probably implement this
            log.error("Please enter a class file or jar file. Cannot reconstruct directories.");
            System.exit(1);
            return;
        }
        String name = file.getName();
        boolean classFile;
        if (name.endsWith(".class")) {
            classFile = true;
        } else if (name.endsWith(".jar") || name.endsWith(".zip")) {
            classFile = false;
        } else {
            log.error("Unknown file type inputted. File \"" + name + "\" currently unsupported.");
            System.exit(1);
            return;
        }
        Optional<File> optionalTarget = loadTargetFile(args[1]);
        if (!optionalTarget.isPresent()) {
            return;
        }
        File targetLocation = optionalTarget.get();
        log.debug("Found file. Class file: {}, File: {}, Target Directory: {}", classFile, file.getAbsolutePath(), targetLocation.getAbsolutePath());
        if (classFile) {
            log.debug("Reconstructing: {}", file.getAbsolutePath());
            try {
                ClassReconstructor reconstructor = new ClassReconstructor(file);
                File parent = new File(targetLocation, reconstructor.getPackageData());
                if (!parent.exists() && !parent.mkdirs()) {
                    log.error("Failed to create parent directory: " + parent.getAbsolutePath());
                    System.exit(1);
                    return;
                }
                File newFile = new File(parent, name.substring(0, name.lastIndexOf('.')) + ".java");
                log.debug("Currently translating into: {}", newFile.getAbsolutePath());
                try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
                    reconstructor.dumpResult(outputStream);
                } catch (IOException ex) {
                    log.error("Ran into io exception when writing to stream.", ex);
                    System.exit(1);
                }
            } catch (IOException ex) {
                log.error("Ran into fatal exception.", ex);
                System.exit(1);
            }
        } else {
            throw new UnsupportedOperationException(); // todo
        }
    }

    private static @NotNull Optional<File> loadTargetFile(@NotNull String path) {
        File targetLocation = new File(path);
        boolean overwrite;
        if (targetLocation.exists()) {
            if (targetLocation.isFile()) {
                log.error("The target location given already exists as a file.");
                System.exit(1);
                return Optional.empty();
            }
            log.warn("The target location specified already exists. Would you like to overwrite the contents?");
            System.out.println("Enter Y to overwrite, N to add new files, or E to exit.");
            Scanner scanner = new Scanner(System.in);
            char n = scanner.next().toLowerCase(Locale.ENGLISH).charAt(0);
            switch (n) {
                case 'y':
                    overwrite = true;
                    break;
                case 'n':
                    overwrite = false;
                    break;
                case 'e':
                    System.exit(0);
                    return Optional.empty();
                default:
                    log.error("Failed to parse given input. Failed to continue.");
                    System.exit(1);
                    return Optional.empty();
            }
            if (overwrite) {
                try {
                    if (walkDelete(Paths.get(path))) {
                        log.error("Failed to delete certain files in target location.");
                        System.exit(1);
                        return Optional.empty();
                    }
                } catch (IOException ex) {
                    log.error("Failed to read file: " + targetLocation.getAbsolutePath(), ex);
                    System.exit(1);
                    return Optional.empty();
                }
            }
        }
        if (!targetLocation.exists() && !targetLocation.mkdirs()) {
            log.error("Failed to make a directory in the specified location. Make sure you have permission for this file.");
            System.exit(1);
            return Optional.empty();
        }
        return Optional.of(targetLocation);
    }

    private static boolean walkDelete(@NotNull Path path) throws IOException {
        return Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).map(File::delete).anyMatch(b -> !b);
    }
}
