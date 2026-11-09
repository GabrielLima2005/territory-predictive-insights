/**
 * Sensor de NÍVEL DO RIO — mede a altura do rio em metros (m).
 *
 * Assim como SensorChuva, herda de ComponenteTerritorial (extends) e implementa
 * o contrato Sensor (implements). Rio acima do nível de alerta indica risco de
 * transbordamento e alagamento das áreas próximas.
 */
public class SensorNivelRio extends ComponenteTerritorial implements Sensor {

    private double limiteAlerta;    // nível de risco (m)
    private double ultimaLeitura;

    public SensorNivelRio(String id, String nome, String regiao, double limiteAlerta) {
        super(id, nome, regiao);
        this.limiteAlerta = limiteAlerta;
        this.ultimaLeitura = 0.0;
    }

    @Override
    public double lerValor() {
        // Faixa realista: 1,5 m a 5,5 m.
        this.ultimaLeitura = 1.5 + Math.random() * 4.0;
        return this.ultimaLeitura;
    }

    /** Registra uma leitura vinda de fonte externa (ex.: estimativa derivada da chuva real). */
    public void registrarLeitura(double valor) {
        this.ultimaLeitura = valor;
    }

    @Override
    public boolean verificarFuncionamento() {
        return isAtivo();
    }

    @Override
    public String retornarTipo() {
        return "Nível do Rio";
    }

    @Override
    public String getUnidade() {
        return "m";
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
        return String.format("%s | última leitura: %.2f m (limite %.2f m) - %s",
                identificacao(), ultimaLeitura, limiteAlerta, situacao);
    }
}