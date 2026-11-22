package dz.recoia.catalogueservice.service;

import dz.recoia.catalogueservice.csv.CourseCsvMapper;
import dz.recoia.catalogueservice.csv.CsvParser;
import dz.recoia.catalogueservice.domain.Course;
import dz.recoia.catalogueservice.repo.CourseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CatalogueImportService implements ApplicationRunner {
    private final CourseRepository repo;
    private final boolean seed;

    public CatalogueImportService(CourseRepository repo, @Value("${catalogue.seed-on-startup:true}") boolean seed) {
        this.repo = repo;
        this.seed = seed;
    }

    /** Au demarrage : si la table est vide, charge data/courses.csv (96 formations). */
    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (!seed || repo.count() > 0) return;
        try (Reader r = new InputStreamReader(new ClassPathResource("data/courses.csv").getInputStream(), StandardCharsets.UTF_8)) {
            int n = importCsv(r);
            System.out.println("[catalogue] " + n + " formations importees");
        }
    }

    @Transactional
    public int importCsv(Reader reader) throws IOException {
        List<Course> courses = CourseCsvMapper.map(CsvParser.parse(reader));
        repo.saveAll(courses);
        return courses.size();
    }
}
