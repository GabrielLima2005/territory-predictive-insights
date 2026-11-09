/**
 * ENCAPSULAMENTO (Conceito 3 de POO).
 *
 * Todos os atributos são PRIVATE: ninguém de fora altera os dados diretamente.
 * O acesso acontece só por getters e setters, e os setters VALIDAM a entrada.
 * Se o valor for inválido, a alteração é rejeitada com mensagem clara e o valor
 * anterior é mantido.
 *
 * Os limiares de alerta são dados sensíveis da comunidade: para alterá-los é
 * preciso informar o código de acesso correto. O código em si não tem getter,
 * então não pode ser lido de fora da classe.
 */
public class DadosTerritorio {

    // ----- Atributos privados (protegidos) -----
    private String nomeRegiao;
    private int populacao;
    private String codigoAcesso;          // sensível: sem getter público
    private double limiarChuva;           // mm
    private double limiarNivelRio;        // m
    private double limiarUmidade;         // %

    // Faixas válidas para cada limiar (usadas na validação).
    private static final double MAX_CHUVA = 200.0;
    private static final double MAX_NIVEL = 10.0;
    private static final double MAX_UMIDADE = 100.0;
    private static final int MAX_POPULACAO = 5_000_000;

    public DadosTerritorio(String nomeRegiao, int populacao, String codigoAcesso) {
        this.nomeRegiao = nomeRegiao;
        this.populacao = populacao;
        this.codigoAcesso = codigoAcesso;
        // Limiares padrão de risco de alagamento (escala horária, compatível com dados reais).
        this.limiarChuva = 8.0;        // mm/h - chuva forte
        this.limiarNivelRio = 4.0;     // m
        this.limiarUmidade = 85.0;     // %
    }

    // ----- Getters (leitura permitida dos dados NÃO sensíveis) -----

    public String getNomeRegiao() {
        return nomeRegiao;
    }

    public int getPopulacao() {
        return populacao;
    }

    public double getLimiarChuva() {
        return limiarChuva;
    }

    public double getLimiarNivelRio() {
        return limiarNivelRio;
    }

    public double getLimiarUmidade() {
        return limiarUmidade;
    }

    // NOTE que NÃO existe getCodigoAcesso(): o código é sensível e fica escondido.

    // ----- Setters com validação -----

    /** Altera o nome da região. Rejeita nome vazio. */
    public boolean setNomeRegiao(String nomeRegiao) {
        if (nomeRegiao == null || nomeRegiao.trim().isEmpty()) {
            System.out.println("[DADOS] Alteração REJEITADA: o nome da região não pode ser vazio.");
            return false;
        }
        this.nomeRegiao = nomeRegiao.trim();
        System.out.println("[DADOS] Nome da região atualizado para: " + this.nomeRegiao);
        return true;
    }

    /** Altera a população. Rejeita valor negativo ou absurdo. Mantém o anterior. */
    public boolean setPopulacao(int populacao) {
        if (populacao < 0) {
            System.out.println("[DADOS] Alteração REJEITADA: população não pode ser negativa ("
                    + populacao + "). Mantido: " + this.populacao);
            return false;
        }
        if (populacao > MAX_POPULACAO) {
            System.out.println("[DADOS] Alteração REJEITADA: população acima do máximo plausível ("
                    + MAX_POPULACAO + "). Mantido: " + this.populacao);
            return false;
        }
        this.populacao = populacao;
        System.out.println("[DADOS] População atualizada para: " + this.populacao);
        return true;
    }

    // ----- Setters dos limiares: exigem o código de acesso -----

    public boolean setLimiarChuva(double novoLimiar, String codigoInformado) {
        if (!autorizado(codigoInformado)) return false;
        if (!faixaValida(novoLimiar, 0, MAX_CHUVA, "Chuva")) return false;
        this.limiarChuva = novoLimiar;
        System.out.printf("[DADOS] Limiar de Chuva atualizado para %.2f mm.%n", this.limiarChuva);
        return true;
    }

    public boolean setLimiarNivelRio(double novoLimiar, String codigoInformado) {
        if (!autorizado(codigoInformado)) return false;
        if (!faixaValida(novoLimiar, 0, MAX_NIVEL, "Nível do Rio")) return false;
        this.limiarNivelRio = novoLimiar;
        System.out.printf("[DADOS] Limiar de Nível do Rio atualizado para %.2f m.%n", this.limiarNivelRio);
        return true;
    }

    public boolean setLimiarUmidade(double novoLimiar, String codigoInformado) {
        if (!autorizado(codigoInformado)) return false;
        if (!faixaValida(novoLimiar, 0, MAX_UMIDADE, "Umidade")) return false;
        this.limiarUmidade = novoLimiar;
        System.out.printf("[DADOS] Limiar de Umidade atualizado para %.2f %%%n", this.limiarUmidade);
        return true;
    }

    /** Troca o código de acesso exigindo o código atual (proteção extra). */
    public boolean alterarCodigoAcesso(String codigoAtual, String novoCodigo) {
        if (!autorizado(codigoAtual)) return false;
        if (novoCodigo == null || novoCodigo.trim().length() < 4) {
            System.out.println("[DADOS] Alteração REJEITADA: o novo código deve ter ao menos 4 caracteres.");
            return false;
        }
        this.codigoAcesso = novoCodigo.trim();
        System.out.println("[DADOS] Código de acesso alterado com sucesso.");
        return true;
    }

    // ----- Métodos privados de apoio (detalhes escondidos) -----

    /** Confere o código sem nunca expor o código armazenado. */
    private boolean autorizado(String codigoInformado) {
        if (codigoInformado == null || !codigoInformado.equals(this.codigoAcesso)) {
            System.out.println("[DADOS] Alteração REJEITADA: código de acesso incorreto. "
                    + "Limiares são dados sensíveis da comunidade.");
            return false;
        }
        return true;
    }

    private boolean faixaValida(double valor, double min, double max, String grandeza) {
        if (valor < min || valor > max) {
            System.out.printf("[DADOS] Alteração REJEITADA: limiar de %s deve estar entre %.1f e %.1f. "
                    + "Valor anterior mantido.%n", grandeza, min, max);
            return false;
        }
        return true;
    }
}