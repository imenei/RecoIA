package dz.recoia.catalogueservice.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/** Lecteur CSV minimal (RFC 4180) : guillemets, virgules et retours a la ligne dans les champs, BOM, separateur , ou ; */
public final class CsvParser {
    private CsvParser() {}

    public static List<List<String>> parse(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[8192];
        int n;
        while ((n = reader.read(buf)) > 0) sb.append(buf, 0, n);
        return parse(sb.toString());
    }

    public static List<List<String>> parse(String text) {
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') text = text.substring(1);
        char sep = detectSeparator(text);
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') { field.append('"'); i++; }
                    else quoted = false;
                } else field.append(c);
            } else if (c == '"') {
                quoted = true;
            } else if (c == sep) {
                row.add(field.toString()); field.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') i++;
                row.add(field.toString()); field.setLength(0);
                addRow(rows, row);
                row = new ArrayList<>();
            } else field.append(c);
        }
        if (field.length() > 0 || !row.isEmpty()) { row.add(field.toString()); addRow(rows, row); }
        return rows;
    }

    private static void addRow(List<List<String>> rows, List<String> row) {
        boolean empty = row.stream().allMatch(s -> s.isBlank());
        if (!empty) rows.add(row);
    }

    /** Compte , et ; sur la premiere ligne (hors guillemets). */
    private static char detectSeparator(String text) {
        int commas = 0, semis = 0;
        boolean q = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"') q = !q;
            else if (!q && (c == '\n' || c == '\r')) break;
            else if (!q && c == ',') commas++;
            else if (!q && c == ';') semis++;
        }
        return semis > commas ? ';' : ',';
    }
}
