import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe CONCRETA que herda da classe abstrata ComponenteTerritorial.
 *
 * A EstacaoDeColeta é um componente territorial que AGRUPA vários sensores e
 * coordena a coleta. Ela mesma não é um Sensor: ela usa os sensores (composição).
 * Isso mostra que a classe abstrata serve de molde para componentes bem diferentes
 * (sensores individuais e uma estação que os agrega).
 *
 * Responsabilidades:
 *   - guardar a lista de sensores instalados;
 *   - manter o histórico de leituras de cada sensor em um ArrayList (requisito
 *     do sistema de previsão);
 *   - coletar novas leituras e reportar o status do conjunto.
 */
public class EstacaoDeColeta extends ComponenteTerritorial {

    private ArrayList<Sensor> sensores;
    private Map<Sensor, ArrayList<Double>> historicos;

    public EstacaoDeColeta(String id, String nome, String regiao) {
        super(id, nome, regiao);
        this.sensores = new ArrayList<>();
        this.historicos = new HashMap<>();
    }

    /** Instala um sensor na estação e cria seu histórico vazio. */
    public void adicionarSensor(Sensor sensor) {
        sensores.add(sensor);
        historicos.put(sensor, new ArrayList<>());
        System.out.println("[ESTAÇÃO] Sensor instalado: " + sensor.retornarTipo());
    }

    public ArrayList<Sensor> getSensores() {
        return sensores;
    }

    /** Histórico de leituras de um sensor (usado pelas estratégias de previsão). */
    public ArrayList<Double> getHistorico(Sensor sensor) {
        return historicos.getOrDefault(sensor, new ArrayList<>());
    }

    /**
     * Coleta uma leitura de cada sensor que está funcionando e guarda no histórico.
     * Sensores com defeito ou desativados são ignorados (e isso é reportado).
     */
    public void coletarLeituras() {
        if (!isAtivo()) {
            System.out.println("[ESTAÇÃO] " + nome + " está desativada. Coleta ignorada.");
            return;
        }
        for (Sensor sensor : sensores) {
            if (sensor.verificarFuncionamento()) {
                double valor = sensor.lerValor();
                historicos.get(sensor).add(valor);
                System.out.printf("[LEITURA] %-16s = %6.2f %s%n",
                        sensor.retornarTipo(), valor, sensor.getUnidade());
            } else {
                System.out.println("[LEITURA] " + sensor.retornarTipo()
                        + " não está funcionando. Leitura ignorada.");
            }
        }
    }

    // Implementação do método abstrato herdado de ComponenteTerritorial.
    @Override
    public String verificarStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append(identificacao()).append("\n");
        int funcionando = 0;
        for (Sensor sensor : sensores) {
            boolean ok = sensor.verificarFuncionamento();
            if (ok) funcionando++;
            sb.append(String.format("   - %-16s : %s | %d leituras no histórico%n",
                    sensor.retornarTipo(),
                    ok ? "OK" : "FALHA",
                    getHistorico(sensor).size()));
        }
        sb.append(String.format("   %d/%d sensores operando.", funcionando, sensores.size()));
        return sb.toString();
    }
}