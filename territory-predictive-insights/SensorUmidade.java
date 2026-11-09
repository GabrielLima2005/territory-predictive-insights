/**
 * Sensor de UMIDADE DO SOLO — mede a umidade em porcentagem (%).
 *
 * Herda de ComponenteTerritorial e implementa Sensor. Solo muito úmido (perto de
 * 100%) está saturado: não consegue absorver mais chuva, então qualquer precipitação
 * extra vira escoamento e aumenta o risco de alagamento. Por isso, neste recorte,
 * umidade ALTA também é sinal de risco.
 */
public class SensorUmidade extends ComponenteTerritorial implements Sensor {

    private double limiteAlerta;    // umidade de risco (%)
    private double ultimaLeitura;

    public SensorUmidade(String id, String nome, String regiao, double limiteAlerta) {
        super(id, nome, regiao);
        this.limiteAlerta = limiteAlerta;
        this.ultimaLeitura = 0.0;
    }

    @Override
    public double lerValor() {
        // Faixa realista: 45% a 100%.
        this.ultimaLeitura = 45.0 + Math.random() * 55.0;
        return this.ultimaLeitura;
    }

    /** Registra uma leitura vinda de fonte externa (ex.: dado real carregado do CSV). */
    public void registrarLeitura(double valor) {
        this.ultimaLeitura = valor;
    }

    @Override
    public boolean verificarFuncionamento() {
        return isAtivo();
    }

    @Override
    public String retornarTipo() {
        return "Umidade do Solo";
    }

    @Override
    public String getUnidade() {
        return "%";
    }

    @Override
    public double getLimiteAlerta() {
        return limiteAlerta;
    }

    public double getUltimaLeitura() {
        return ultimaLeitura;
    }

    @Override
    public String verificarStatus() {
        if (!isAtivo()) {
            return identificacao() + " | SENSOR DESATIVADO";
        }
        String situacao = ultimaLeitura > limiteAlerta ? "ACIMA DO LIMITE" : "normal";
        return String.format("%s | última leitura: %.2f %% (limite %.2f %%) - %s",
                identificacao(), ultimaLeitura, limiteAlerta, situacao);
    }
}