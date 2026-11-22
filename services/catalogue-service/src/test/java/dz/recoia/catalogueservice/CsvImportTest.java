package dz.recoia.catalogueservice;

import dz.recoia.catalogueservice.csv.CourseCsvMapper;
import dz.recoia.catalogueservice.csv.CsvParser;
import dz.recoia.catalogueservice.domain.Course;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvImportTest {

    @Test
    void parsesQuotesCommasAndNewlines() {
        var rows = CsvParser.parse("a,b\n\"x, y\",\"ligne1\nligne2\"\n\"dit \"\"oui\"\"\",z\n");
        assertEquals(3, rows.size());
        assertEquals("x, y", rows.get(1).get(0));
        assertEquals("ligne1\nligne2", rows.get(1).get(1));
        assertEquals("dit \"oui\"", rows.get(2).get(0));
    }

    @Test
    void detectsSemicolonAndBom() {
        var rows = CsvParser.parse("\uFEFFtitle;rating\nJava;4,5\n");
        assertEquals("title", rows.get(0).get(0));
        assertEquals("4,5", rows.get(1).get(1));
    }

    @Test
    void mapsCourseraColumns() {
        String csv = "Course Name,University,Difficulty Level,Course Rating,Course Description,Skills\n"
                + "Machine Learning,Stanford,Advanced,4.9,\"Cours, complet\",\"ml, python\"\n"
                + ",Vide,Beginner,3,x,y\n";
        List<Course> list = CourseCsvMapper.map(CsvParser.parse(csv));
        assertEquals(1, list.size());                         // la ligne sans titre est ignoree
        assertEquals("Doctorat", list.get(0).getLevel());
        assertEquals(4.9, list.get(0).getRating(), 1e-9);
        assertEquals("Cours, complet", list.get(0).getDescription());
    }

    @Test
    void missingTitleColumnFails() {
        assertThrows(IllegalArgumentException.class, () -> CourseCsvMapper.map(CsvParser.parse("foo,bar\n1,2\n")));
    }

    @Test
    void bundledCatalogueHas96Courses() throws Exception {
        try (var r = new InputStreamReader(getClass().getResourceAsStream("/data/courses.csv"), StandardCharsets.UTF_8)) {
            List<Course> list = CourseCsvMapper.map(CsvParser.parse(r));
            assertEquals(96, list.size());
            assertTrue(list.stream().allMatch(c -> c.getRating() >= 0 && c.getRating() <= 5));
        }
    }
}
