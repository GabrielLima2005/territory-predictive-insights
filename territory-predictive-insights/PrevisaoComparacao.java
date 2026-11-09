import java.util.ArrayList;

/**
 * HERANÇA — subclasse concreta de EstrategiaPrevisao.
 *
 * Previsão por COMPARAÇÃO: compara o período atual (últimas N leituras) com o
 * período anterior (as N leituras antes dessas). A diferença entre as médias
 * revela a tendência (subindo ou descendo). Projetamos a próxima leitura como:
 *
 *     previsto = mediaAtual + (mediaAtual - mediaAnterior)
 *
 * Ou seja, se o nível está subindo, a previsão continua subindo na mesma ritmo.
 *
 * Atributo específico: tamanhoPeriodo (quantas leituras formam cada período).
 * Usa super(...) no construtor e super.calcularMedia(...) para reaproveitar a mãe.
 */
public class PrevisaoComparacao extends EstrategiaPrevisao {

    // Atributo específico: tamanho de cada período comparado.
    private int tamanhoPeriodo;

    public PrevisaoComparacao(int tamanhoPeriodo) {
        super("Comparação de Períodos", 5); // construtor da classe mãe
        this.tamanhoPeriodo = Math.max(1, tamanhoPeriodo);
    }

    public int getTamanhoPeriodo() {
        return tamanhoPeriodo;
    }

    @Override
    public double prever(ArrayList<Double> leituras) {
        if (leituras == null || leituras.isEmpty()) {
            return 0.0;
        }
        int total = leituras.size();

        // Período atual = últimas N leituras.
        int inicioAtual = Math.max(0, total - tamanhoPeriodo);
        ArrayList<Double> periodoAtual = new ArrayList<>(leituras.subList(inicioAtual, total));
        double mediaAtual = super.calcularMedia(periodoAtual);

        // Se não há leituras suficientes para um período anterior, usa só a média atual.
        if (total < 2 * tamanhoPeriodo) {
            return Math.max(0.0, mediaAtual);
        }

        // Período anterior = as N leituras imediatamente antes do período atual.
        int fimAnterior = inicioAtual;
        int inicioAnterior = Math.max(0, fimAnterior - tamanhoPeriodo);
        ArrayList<Double> periodoAnterior = new ArrayList<>(leituras.subList(inicioAnterior, fimAnterior));
        double mediaAnterior = super.calcularMedia(periodoAnterior);

        double tendencia = mediaAtual - mediaAnterior;
        double previsto = mediaAtual + tendencia;
        // Grandezas físicas (chuva, nível, umidade) não podem ser negativas.
        return Math.max(0.0, previsto);
    }

    @Override
    public String explicar(ArrayList<Double> leituras) {
        int total = leituras == null ? 0 : leituras.size();
        double previsto = prever(leituras);

        if (total < 2 * tamanhoPeriodo) {
            return String.format(
                    "Comparação parcial: ainda faltam leituras para dois períodos completos "
                    + "(tem %d, precisa de %d). Projeção atual = %.2f.",
                    total, 2 * tamanhoPeriodo, previsto);
        }

        int inicioAtual = total - tamanhoPeriodo;
        double mediaAtual = super.calcularMedia(new ArrayList<>(leituras.subList(inicioAtual, total)));
        double mediaAnterior = super.calcularMedia(
                new ArrayList<>(leituras.subList(inicioAtual - tamanhoPeriodo, inicioAtual)));
        double tendencia = mediaAtual - mediaAnterior;
        String direcao = tendencia > 0.01 ? "SUBINDO" : (tendencia < -0.01 ? "DESCENDO" : "ESTÁVEL");

        return String.format(
                "Período atual = %.2f | anterior = %.2f | tendência %s (%+.2f). Projeção = %.2f.",
                mediaAtual, mediaAnterior, direcao, tendencia, previsto);
    }
}