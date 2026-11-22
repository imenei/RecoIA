package dz.recoia.recoservice.engine;

import java.util.List;

public interface TextEmbedder {
    List<double[]> embed(List<String> texts);
}