package maoinvisivel.metrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Grava os snapshots em CSV (ponto como separador decimal, vírgula entre colunas). */
public class CsvExporter {
    private static final String CABECALHO = "tick,preco,demanda,oferta,gini";

    public void exportar(List<MetricsSnapshot> snapshots, Path destino) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add(CABECALHO);
        for (MetricsSnapshot s : snapshots) {
            linhas.add(String.format(Locale.ROOT, "%d,%.4f,%.4f,%.4f,%.4f",
                    s.tick(), s.preco(), s.demanda(), s.oferta(), s.gini()));
        }

        Path pasta = destino.toAbsolutePath().getParent();
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
        Files.write(destino, linhas, StandardCharsets.UTF_8);
    }
}
