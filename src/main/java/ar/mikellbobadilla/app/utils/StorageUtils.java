package ar.mikellbobadilla.app.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class StorageUtils {

    public static void createDir(Path path) {
        try {
            if (Files.notExists(path)) Files.createDirectories(path);
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    public static void saveResource(Path posterPath, MultipartFile file) {
        try(InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, posterPath, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteResource(Path fullPosterPath) {
        try {
            Files.deleteIfExists(fullPosterPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void forceDeleteDir(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child: children) forceDeleteDir(child);
            }
            if (Objects.requireNonNull(file.listFiles()).length == 0) {
                if (!file.delete()) System.err.println("Failed to delete dir" + file.getAbsolutePath());
            }
        } else {
            if (!file.delete()) System.err.println("Failed to delete file " + file.getAbsolutePath());
        }
    }
}
