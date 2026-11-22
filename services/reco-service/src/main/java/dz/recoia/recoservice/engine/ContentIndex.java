package dz.recoia.recoservice.engine;

import java.util.Map;
import java.util.Set;

public interface ContentIndex {
    Map<String, Double> courseVector(long courseId);
    Map<String, Double> textVector(String text);
    Set<String> courseTerms(long courseId);
}