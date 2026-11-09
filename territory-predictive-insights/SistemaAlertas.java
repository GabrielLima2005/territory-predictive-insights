/**
 * SISTEMA DE ALERTAS (Requisito 5).
 *
 * Compara automaticamente o valor PREVISTO com o limiar de alerta do território
 * e emite alertas visuais no console, diferenciando níveis (ATENÇÃO, ALERTA,
 * CRÍTICO). Cada alerta explica o PORQUÊ: qual sensor, qual valor previsto,
 * qual limite foi estourado e em quanto.
 *
 * A gravidade é proporcional ao quanto o limite foi ultrapassado:
 *   - até o limite            -> NORMAL
 *   - até 10% acima do limite -> ATENÇÃO
 *   - até 25% acima do limite -> ALERTA
 *   - mais de 25% acima       -> CRÍTICO
 */
public class SistemaAlertas {

    /** Classifica a previsão em um nível de alerta conforme a distância do limiar. */
    public NivelAlerta classificar(double previsto, double limiar) {
        if (limiar <= 0) {
            // Limiar inválido/zero: por segurança, qualquer valor positivo é crítico.
            return previsto > 0 ? NivelAlerta.CRITICO : NivelAlerta.NORMAL;
        }
        double razao = previsto / limiar;
        if (razao <= 1.0) {
            return NivelAlerta.NORMAL;
        } else if (razao <= 1.10) {
            return NivelAlerta.ATENCAO;
        } else if (razao <= 1.25) {
            return NivelAlerta.ALERTA;
        } else {
            return NivelAlerta.CRITICO;
        }
    }

    /**
     * Avalia uma previsão e imprime o alerta correspondente, com explicação.
     * Retorna o nível emitido (útil para estatísticas da simulação).
     */
    public NivelAlerta avaliarEAlertar(String tipoSensor, double previsto, double limiar, String unidade) {
        NivelAlerta nivel = classificar(previsto, limiar);

        if (nivel == NivelAlerta.NORMAL) {
            System.out.printf("   [NORMAL] %-16s previsto %.2f %s (limite %.2f %s) - dentro do seguro.%n",
                    tipoSensor, previsto, unidade, limiar, unidade);
            return nivel;
        }

        double excessoPercentual = ((previsto - limiar) / limiar) * 100.0;
        System.out.println("   ----------------------------------------------------------");
        System.out.printf("   >>> [%s] %s%n", nivel.getRotulo(), tipoSensor);
        System.out.printf("       Valor previsto : %.2f %s%n", previsto, unidade);
        System.out.printf("       Limite do território: %.2f %s%n", limiar, unidade);
        System.out.printf("       Ultrapassou em : %.2f %s (%.1f%% acima)%n",
                previsto - limiar, unidade, excessoPercentual);
        System.out.printf("       Por quê        : %s%n", nivel.getDescricao());
        System.out.println("   ----------------------------------------------------------");
        return nivel;
    }
}