package dz.recoia.catalogueservice.web;

import dz.recoia.catalogueservice.domain.Course;
import dz.recoia.catalogueservice.repo.CourseRepository;
import dz.recoia.catalogueservice.service.CatalogueImportService;
import dz.recoia.catalogueservice.service.ImageStorage;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class CourseController {
    private final CourseRepository repo;
    private final CatalogueImportService importer;
    private final ImageStorage images;

    public CourseController(CourseRepository repo, CatalogueImportService importer, ImageStorage images) {
        this.repo = repo;
        this.importer = importer;
        this.images = images;
    }

    /** GET /api/courses?q=python&level=Master&category=Data&minRating=4.5&page=0&size=12&sort=rating,desc */
    @GetMapping("/courses")
    public PagedModel<Course> list(@RequestParam(defaultValue = "") String q,
                                   @RequestParam(defaultValue = "") String level,
                                   @RequestParam(defaultValue = "") String category,
                                   @RequestParam(defaultValue = "0") double minRating,
                                   Pageable pageable) {
        return new PagedModel<>(repo.search(q.trim(), level, category, minRating, pageable));
    }

    /** Liste par identifiants : utilisee par reco-service et activity-service. */
    @GetMapping("/courses/all")
    public List<Course> all() { return repo.findAll(); }

    @GetMapping("/courses/{id}")
    public ResponseEntity<Course> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public Course create(@Valid @RequestBody Course c) {
        c.setId(null);
        return repo.save(c);
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<Course> update(@PathVariable Long id, @Valid @RequestBody Course c) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        c.setId(id);
        return ResponseEntity.ok(repo.save(c));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** Import d'un fichier CSV (admin). Champ multipart : file */
    @PostMapping(value = "/courses/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Integer> importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        try (var r = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            return Map.of("imported", importer.importCsv(r));
        }
    }

    /** Upload d'image (admin). Champ multipart : file */
    @PostMapping(value = "/courses/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Course> upload(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        var course = repo.findById(id);
        if (course.isEmpty()) return ResponseEntity.notFound().build();
        String name = images.save(id, file);
        course.get().setImageUrl("/api/images/" + name);
        return ResponseEntity.ok(repo.save(course.get()));
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> image(@PathVariable String filename) {
        Resource r = images.load(filename);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(ImageStorage.contentType(filename))).body(r);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> bad(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
