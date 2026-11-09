/**
 * Níveis de alerta do sistema (enum).
 *
 * Usar um enum em vez de Strings soltas é uma boa prática de POO: os valores
 * possíveis ficam definidos num único lugar, com rótulo e descrição, evitando
 * erros de digitação e facilitando a comparação.
 *
 * A gravidade aumenta de NORMAL -> ATENÇÃO -> ALERTA -> CRÍTICO.
 */
public enum NivelAlerta {
    NORMAL("NORMAL", "Valor dentro do limite seguro."),
    ATENCAO("ATENÇÃO", "Valor passou pouco do limite. Acompanhe de perto."),
    ALERTA("ALERTA", "Valor bem acima do limite. Prepare uma resposta."),
    CRITICO("CRÍTICO", "Valor muito acima do limite. Risco imediato à comunidade.");

    private final String rotulo;
    private final String descricao;

    NivelAlerta(String rotulo, String descricao) {
        this.rotulo = rotulo;
        this.descricao = descricao;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getDescricao() {
        return descricao;
    }
}