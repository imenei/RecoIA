package dz.recoia.authservice.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterestsTest {

    @Test
    void trimsDedupesAndDropsBlanks() {
        List<String> cleaned = Interests.clean(Arrays.asList(" Python ", "python", "", null, "Design"));
        assertEquals(List.of("Python", "Design"), cleaned);
    }

    @Test
    void limitsCountAndLength() {
        List<String> many = Arrays.asList("a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "a10");
        assertEquals(8, Interests.clean(many).size());
        assertEquals(60, Interests.clean(List.of("x".repeat(100))).get(0).length());
    }

    @Test
    void nullGivesEmptyList() {
        assertEquals(List.of(), Interests.clean(null));
    }
}
