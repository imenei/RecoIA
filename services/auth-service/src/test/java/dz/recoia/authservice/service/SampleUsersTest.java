package dz.recoia.authservice.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleUsersTest {

    private final List<SampleUsers.Spec> students = SampleUsers.students(30, 42);

    @Test
    void generatesThirtyDistinctStudents() {
        assertEquals(30, students.size());
        Set<String> emails = new HashSet<>();
        students.forEach(s -> emails.add(s.email()));
        assertEquals(30, emails.size());
    }

    @Test
    void everyStudentHasTwoToFiveInterests() {
        for (SampleUsers.Spec s : students) {
            assertTrue(s.interests().size() >= 2 && s.interests().size() <= 5, s.toString());
            assertEquals(s.interests().size(), new HashSet<>(s.interests()).size());
        }
    }

    @Test
    void allProfilesAreRepresented() {
        Set<String> clusters = new HashSet<>();
        students.forEach(s -> clusters.add(s.cluster()));
        assertEquals(SampleUsers.CLUSTERS.size(), clusters.size());
    }

    @Test
    void sameSeedGivesSameStudents() {
        assertEquals(students, SampleUsers.students(30, 42));
    }
}
