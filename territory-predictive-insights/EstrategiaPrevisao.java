import java.util.ArrayList;

/**
 * HERANÇA (Conceito 4 de POO) — classe base abstrata.
 *
 * EstrategiaPrevisao é o molde de todas as formas de prever o futuro do
 * território. Ela é abstrata (não pode ser instanciada) e define:
 *   - atributos comuns (nome, leiturasMinimas);
 *   - métodos concretos reaproveitáveis (calcularMedia, temLeiturasSuficientes);
 *   - o método abstrato prever(), que CADA estratégia implementa do seu jeito.
 *
 * As subclasses (PrevisaoMediaMovel e PrevisaoComparacao) usam "extends" para
 * herdar esta classe e "super(...)" para chamar o construtor e os métodos da mãe.
 */
public abstract class EstrategiaPrevisao {

    protected String nome;
    protected int leiturasMinimas; // abaixo disso a previsão é pouco confiável

    public EstrategiaPrevisao(String nome, int leiturasMinimas) {
        this.nome = nome;
        this.leiturasMinimas = leiturasMinimas;
    }

    public String getNome() {
        return nome;
    }

    public int getLeiturasMinimas() {
        return leiturasMinimas;
    }

    // ----- Métodos concretos reutilizados pelas subclasses (via super) -----

    /** Diz se há leituras suficientes para uma previsão confiável. */
    protected boolean temLeiturasSuficientes(ArrayList<Double> leituras) {
        return leituras != null && leituras.size() >= leiturasMinimas;
    }

    /** Média aritmética simples de uma lista de leituras. */
    protected double calcularMedia(ArrayList<Double> leituras) {
        if (leituras == null || leituras.isEmpty()) {
            return 0.0;
        }
        double soma = 0.0;
        for (double valor : leituras) {
            soma += valor;
        }
        return soma / leituras.size();
    }

    /** Versão pública usada pelo sistema para rotular a confiança da previsão. */
    public boolean confiavel(ArrayList<Double> leituras) {
        return temLeiturasSuficientes(leituras);
    }

    // ----- Métodos ABSTRATOS: cada estratégia implementa do seu jeito -----

    /** Calcula o valor previsto para a próxima leitura. */
    public abstract double prever(ArrayList<Double> leituras);

    /** Explica, em texto, o que a previsão significa para o território. */
    public abstract String explicar(ArrayList<Double> leituras);
}