import java.util.ArrayList;

/**
 * HERANÇA — subclasse concreta de EstrategiaPrevisao.
 *
 * Previsão por MÉDIA MÓVEL: o valor previsto é a média das últimas N leituras.
 * A ideia é suavizar ruídos: em vez de reagir a um pico isolado, olhamos a
 * tendência recente das últimas N medições.
 *
 * Atributo específico desta estratégia: tamanhoJanela (o N da média móvel).
 * Usa super(...) no construtor e super.calcularMedia(...) / super.temLeiturasSuficientes(...)
 * para reaproveitar o código da classe mãe.
 */
public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    // Atributo específico: quantas leituras recentes entram na média.
    private int tamanhoJanela;

    public PrevisaoMediaMovel(int tamanhoJanela) {
        super("Média Móvel", 5); // chamando o construtor da classe mãe
        this.tamanhoJanela = Math.max(1, tamanhoJanela);
    }

    public int getTamanhoJanela() {
        return tamanhoJanela;
    }

    @Override
    public double prever(ArrayList<Double> leituras) {
        if (leituras == null || leituras.isEmpty()) {
            return 0.0;
        }
        // Pega apenas as últimas N leituras (a "janela" móvel).
        int inicio = Math.max(0, leituras.size() - tamanhoJanela);
        ArrayList<Double> janela = new ArrayList<>(leituras.subList(inicio, leituras.size()));
        // Reaproveita o cálculo de média da classe mãe.
        return super.calcularMedia(janela);
    }

    @Override
    public String explicar(ArrayList<Double> leituras) {
        double previsto = prever(leituras);
        String confianca = super.temLeiturasSuficientes(leituras)
                ? "confiável" : "POUCO CONFIÁVEL (poucas leituras)";
        return String.format(
                "Média das últimas %d leituras = %.2f. Tendência recente %s.",
                Math.min(tamanhoJanela, leituras == null ? 0 : leituras.size()),
                previsto, confianca);
    }
}