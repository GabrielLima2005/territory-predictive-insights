import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Leitor de DADOS REAIS de fonte pública (bônus da GS).
 *
 * Carrega o arquivo dados_reais.csv, que contém medições históricas reais de
 * precipitação (mm/h) e umidade relativa (%) obtidas da API pública Open-Meteo
 * (https://open-meteo.com), para São Paulo/SP no período de 16 a 19/01/2026.
 * O nível do rio é uma ESTIMATIVA simplificada derivada da chuva real (modelo
 * chuva-vazão), claramente sinalizada como estimativa, não medição de régua.
 *
 * A leitura é feita de arquivo (offline): o programa não depende de internet para
 * rodar, então não há risco de quebrar a execução por falta de rede. Se o arquivo
 * não existir, o carregamento falha de forma controlada e o sistema continua
 * funcionando com a simulação (Math.random()).
 *
 * Formato do CSV (com cabeçalho):
 *   timestamp,chuva_mm,umidade_pct,nivel_rio_est_m
 *   2026-01-16T00:00,0.40,96.0,2.06
 */
public class LeitorDadosReais {

    /** Uma linha do dataset: o instante e os três valores medidos/estimados. */
    public static class LeituraReal {
        public final String timestamp;
        public final double chuva;
        public final double umidade;
        public final double nivelRio;

        public LeituraReal(String timestamp, double chuva, double umidade, double nivelRio) {
            this.timestamp = timestamp;
            this.chuva = chuva;
            this.umidade = umidade;
            this.nivelRio = nivelRio;
        }
    }

    private ArrayList<LeituraReal> leituras = new ArrayList<>();
    private final String fonte = "Open-Meteo (archive-api.open-meteo.com) - São Paulo/SP, lat -23.55 lon -46.63";

    /** Carrega o CSV. Retorna true se ao menos uma leitura válida foi carregada. */
    public boolean carregar(String caminho) {
        leituras.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String linha = br.readLine(); // cabeçalho
            if (linha == null) {
                System.out.println("[DADOS REAIS] Arquivo vazio: " + caminho);
                return false;
            }
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;
                String[] partes = linha.split(",");
                if (partes.length != 4) continue; // ignora linha malformada
                try {
                    String timestamp = partes[0].trim();
                    double chuva = Double.parseDouble(partes[1].trim());
                    double umidade = Double.parseDouble(partes[2].trim());
                    double nivel = Double.parseDouble(partes[3].trim());
                    leituras.add(new LeituraReal(timestamp, chuva, umidade, nivel));
                } catch (NumberFormatException e) {
                    // linha com número inválido é ignorada
                }
            }
        } catch (IOException e) {
            System.out.println("[DADOS REAIS] Não foi possível ler '" + caminho + "': " + e.getMessage());
            System.out.println("[DADOS REAIS] O sistema segue funcionando com a simulação aleatória.");
            return false;
        }
        return !leituras.isEmpty();
    }

    public ArrayList<LeituraReal> getLeituras() {
        return leituras;
    }

    public int quantidade() {
        return leituras.size();
    }

    public String getFonte() {
        return fonte;
    }

    public String getPeriodo() {
        if (leituras.isEmpty()) return "(sem dados)";
        return leituras.get(0).timestamp + " até " + leituras.get(leituras.size() - 1).timestamp;
    }

    // Colunas separadas, úteis para popular o histórico de cada sensor.
    public ArrayList<Double> getChuvas() {
        ArrayList<Double> lista = new ArrayList<>();
        for (LeituraReal l : leituras) lista.add(l.chuva);
        return lista;
    }

    public ArrayList<Double> getUmidades() {
        ArrayList<Double> lista = new ArrayList<>();
        for (LeituraReal l : leituras) lista.add(l.umidade);
        return lista;
    }

    public ArrayList<Double> getNiveis() {
        ArrayList<Double> lista = new ArrayList<>();
        for (LeituraReal l : leituras) lista.add(l.nivelRio);
        return lista;
    }
}