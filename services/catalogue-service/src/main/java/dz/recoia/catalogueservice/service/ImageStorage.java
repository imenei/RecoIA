package dz.recoia.catalogueservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageStorage {
    private static final Map<String, String> TYPES = Map.of("image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");
    private final Path dir;

    public ImageStorage(@Value("${catalogue.upload-dir:uploads}") String uploadDir) throws IOException {
        this.dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
    }

    /** Enregistre l'image et renvoie son nom de fichier. */
    public String save(Long courseId, MultipartFile file) throws IOException {
        String ext = TYPES.get(file.getContentType());
        if (ext == null) throw new IllegalArgumentException("Format refuse : utilisez JPG, PNG ou WebP");
        String name = courseId + "-" + UUID.randomUUID() + "." + ext;
        Files.copy(file.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        return name;
    }

    public Resource load(String filename) {
        Path p = dir.resolve(filename).normalize();
        if (!p.startsWith(dir) || !Files.isRegularFile(p)) return null;   // anti path traversal
        return new PathResource(p);
    }

    public static String contentType(String filename) {
        String f = filename.toLowerCase();
        return f.endsWith(".png") ? "image/png" : f.endsWith(".webp") ? "image/webp" : "image/jpeg";
    }
}
