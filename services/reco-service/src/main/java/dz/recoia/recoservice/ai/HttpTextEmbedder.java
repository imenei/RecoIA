package dz.recoia.recoservice.ai;

import dz.recoia.recoservice.engine.TextEmbedder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class HttpTextEmbedder implements TextEmbedder {

    public record Req(List<String> texts) { }
    public record Resp(List<List<Double>> vectors) { }

    private final RestClient http;

    public HttpTextEmbedder(@Value("${reco.ai.url:http://localhost:8090}") String url) {
        this.http = RestClient.builder().baseUrl(url).build();
    }

    @Override
    public List<double[]> embed(List<String> texts) {
        Resp r = http.post().uri("/embed").contentType(MediaType.APPLICATION_JSON)
                .body(new Req(texts)).retrieve().body(Resp.class);
        return r.vectors().stream().map(l -> l.stream().mapToDouble(Double::doubleValue).toArray()).toList();
    }
}