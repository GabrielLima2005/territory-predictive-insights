/**
 * INTERFACE (Conceito 2 de POO).
 *
 * Sensor é um "contrato": define os métodos que TODO sensor deve ter, mas não
 * implementa nenhum deles. Qualquer classe que use "implements Sensor" é
 * obrigada a fornecer o corpo desses métodos.
 *
 * Isso permite que o sistema trate SensorChuva, SensorNivelRio e SensorUmidade
 * de forma uniforme (polimorfismo): todos sabem ler um valor, verificar
 * funcionamento e informar seu tipo, mesmo que cada um meça uma coisa diferente.
 */
public interface Sensor {

    /** Realiza uma leitura e retorna o valor medido (simulado nesta GS). */
    double lerValor();

    /** Retorna true se o sensor está operando corretamente. */
    boolean verificarFuncionamento();

    /** Retorna o tipo do sensor (ex.: "Chuva", "Nível do Rio", "Umidade"). */
    String retornarTipo();

    /** Unidade de medida (ex.: "mm", "m", "%"), útil para exibir os valores. */
    String getUnidade();

    /** Limite a partir do qual o valor é considerado de risco para o território. */
    double getLimiteAlerta();
}