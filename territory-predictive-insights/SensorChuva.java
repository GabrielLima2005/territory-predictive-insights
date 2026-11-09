/**
 * Sensor de CHUVA — mede a intensidade de chuva em milímetros (mm).
 *
 * Esta classe concreta demonstra DOIS conceitos ao mesmo tempo:
 *   - "extends ComponenteTerritorial": herda id, nome, região, ativar/desativar
 *     e é obrigada a implementar o método abstrato verificarStatus();
 *   - "implements Sensor": assina o contrato e implementa lerValor,
 *     verificarFuncionamento, retornarTipo, etc.
 *
 * No recorte "Clima local", chuva intensa é o principal gatilho de alagamento:
 * quanto maior o valor, maior o risco.
 */
public class SensorChuva extends ComponenteTerritorial implements Sensor {

    private double limiteAlerta;    // limite local de risco (mm)
    private double ultimaLeitura;

    public SensorChuva(String id, String nome, String regiao, double limiteAlerta) {
        super(id, nome, regiao); // construtor da classe abstrata mãe
        this.limiteAlerta = limiteAlerta;
        this.ultimaLeitura = 0.0;
    }

    @Override
    public double lerValor() {
        // Leitura simulada em faixa horária realista: 0 a 15 mm/h.
        // (Mesma escala dos dados reais do Open-Meteo, para os limiares fazerem sentido.)
        this.ultimaLeitura = Math.random() * 15.0;
        return this.ultimaLeitura;
    }

    /** Registra uma leitura vinda de fonte externa (ex.: dado real carregado do CSV). */
    public void registrarLeitura(double valor) {
        this.ultimaLeitura = valor;
    }

    @Override
    public boolean verificarFuncionamento() {
        // O sensor funciona se está ativo.
        return isAtivo();
    }

    @Override
    public String retornarTipo() {
        return "Chuva";
    }

    @Override
    public String getUnidade() {
        return "mm";
    }

    @Override
    public double getLimiteAlerta() {
        return limiteAlerta;
    }

    public double getUltimaLeitura() {
        return ultimaLeitura;
    }

    // Implementação OBRIGATÓRIA do método abstrato da classe mãe.
    @Override
    public String verificarStatus() {
        if (!isAtivo()) {
            return identificacao() + " | SENSOR DESATIVADO";
        }
        String situacao = ultimaLeitura > limiteAlerta ? "ACIMA DO LIMITE" : "normal";
        return String.format("%s | última leitura: %.2f mm (limite %.2f mm) - %s",
                identificacao(), ultimaLeitura, limiteAlerta, situacao);
    }
}