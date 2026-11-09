import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * CLASSE PRINCIPAL — Sistema de Monitoramento (Requisito 4).
 *
 * É o ponto de entrada do programa (main) e dona do menu interativo. Ela
 * "orquestra" os outros objetos: cria o território, a estação com os sensores,
 * as estratégias de previsão e o sistema de alertas, e oferece opções para o
 * usuário verificar sensores, rodar previsões, gerenciar dados, simular leituras,
 * ver o status completo e rodar a simulação de 24 horas (bônus).
 *
 * Recorte escolhido: CLIMA LOCAL — chuva intensa e risco de alagamento.
 */
public class SistemaMonitoramento {

    private Scanner scanner;
    private DadosTerritorio dados;
    private EstacaoDeColeta estacao;
    private PrevisaoMediaMovel mediaMovel;
    private PrevisaoComparacao comparacao;
    private SistemaAlertas alertas;
    private LeitorDadosReais leitor;
    private boolean dadosReaisCarregados;

    public static void main(String[] args) {
        new SistemaMonitoramento().iniciar();
    }

    /** Prepara o território e entra no loop principal do menu. */
    public void iniciar() {
        scanner = new Scanner(System.in);

        // Dados do território (encapsulamento). O código de acesso padrão está no README.
        dados = new DadosTerritorio("Vila Esperança", 12000, "TERRITORIO2026");

        // Estação de coleta (componente concreto) com 3 sensores (interface Sensor).
        estacao = new EstacaoDeColeta("EST-001", "Estação Central", dados.getNomeRegiao());
        estacao.adicionarSensor(new SensorChuva("CHU-01", "Sensor de Chuva",
                dados.getNomeRegiao(), dados.getLimiarChuva()));
        estacao.adicionarSensor(new SensorNivelRio("RIO-01", "Sensor Nível do Rio",
                dados.getNomeRegiao(), dados.getLimiarNivelRio()));
        estacao.adicionarSensor(new SensorUmidade("UMI-01", "Sensor Umidade do Solo",
                dados.getNomeRegiao(), dados.getLimiarUmidade()));

        // Duas estratégias de previsão (herança).
        mediaMovel = new PrevisaoMediaMovel(5);
        comparacao = new PrevisaoComparacao(5);

        // Sistema de alertas.
        alertas = new SistemaAlertas();

        // Leitor de dados reais de fonte pública (bônus).
        leitor = new LeitorDadosReais();
        dadosReaisCarregados = false;

        imprimirCabecalho();

        boolean executando = true;
        while (executando) {
            imprimirMenu();
            int opcao = lerInteiro("Escolha uma opção: ");
            System.out.println();
            switch (opcao) {
                case 1: verificarSensores(); break;
                case 2: simularLeituras(); break;
                case 3: rodarPrevisoes(); break;
                case 4: gerenciarDados(); break;
                case 5: exibirStatusCompleto(); break;
                case 6: simular24Horas(); break;
                case 7: carregarDadosReais(); break;
                case 0:
                    executando = false;
                    System.out.println("Encerrando o monitoramento. Até logo!");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
            if (executando) pausar();
        }
        scanner.close();
    }

    // ---------------------------------------------------------------------
    // Opções do menu
    // ---------------------------------------------------------------------

    /** Opção 1: verifica o funcionamento e o status de cada sensor. */
    private void verificarSensores() {
        System.out.println("=== VERIFICAÇÃO DOS SENSORES ===");
        for (Sensor sensor : estacao.getSensores()) {
            boolean ok = sensor.verificarFuncionamento();
            System.out.printf("%-16s : %s%n", sensor.retornarTipo(),
                    ok ? "FUNCIONANDO" : "COM FALHA / DESATIVADO");
            if (sensor instanceof ComponenteTerritorial) {
                System.out.println("   " + ((ComponenteTerritorial) sensor).verificarStatus());
            }
        }
    }

    /** Opção 2: simula várias rodadas de leitura e guarda no histórico. */
    private void simularLeituras() {
        System.out.println("=== SIMULAR PRÓXIMAS LEITURAS ===");
        int rodadas = lerInteiro("Quantas rodadas de leitura deseja simular? (sugestão: 5): ");
        if (rodadas <= 0) {
            System.out.println("Número de rodadas deve ser positivo.");
            return;
        }
        if (rodadas > 100) rodadas = 100; // limite de sanidade
        for (int i = 1; i <= rodadas; i++) {
            System.out.println("--- Rodada " + i + " ---");
            estacao.coletarLeituras();
        }
        System.out.println("Histórico atualizado. Use a opção 3 para rodar as previsões.");
    }

    /** Opção 3: roda as duas estratégias sobre o histórico e emite alertas. */
    private void rodarPrevisoes() {
        System.out.println("=== PREVISÕES E ALERTAS ===");
        if (dadosReaisCarregados) {
            System.out.println("(base: dados reais - " + leitor.getFonte() + ")");
        }
        boolean algumaLeitura = false;
        for (Sensor sensor : estacao.getSensores()) {
            ArrayList<Double> historico = estacao.getHistorico(sensor);
            if (historico.isEmpty()) continue;
            algumaLeitura = true;
            double limiar = limiarDoSensor(sensor);
            System.out.println("\n# " + sensor.retornarTipo() + " (" + sensor.getUnidade() + ")");

            rodarEstrategia(mediaMovel, sensor, historico, limiar);
            rodarEstrategia(comparacao, sensor, historico, limiar);
        }
        if (!algumaLeitura) {
            System.out.println("Ainda não há leituras. Use a opção 2 (simular) ou 7 (dados reais).");
        }
    }

    /** Aplica uma estratégia a um sensor, mostra a previsão e dispara o alerta. */
    private void rodarEstrategia(EstrategiaPrevisao estrategia, Sensor sensor,
                                 ArrayList<Double> historico, double limiar) {
        double previsto = estrategia.prever(historico);
        System.out.printf("  [%s] previsto = %.2f %s%n",
                estrategia.getNome(), previsto, sensor.getUnidade());
        System.out.println("     " + estrategia.explicar(historico));
        if (!estrategia.confiavel(historico)) {
            System.out.println("     [!] Aviso: poucas leituras no histórico. "
                    + "Previsão pouco confiável — o sistema recomenda cautela.");
        }
        alertas.avaliarEAlertar(sensor.retornarTipo(), previsto, limiar, sensor.getUnidade());
    }

    /** Opção 4: submenu para gerenciar os dados encapsulados do território. */
    private void gerenciarDados() {
        boolean voltar = true;
        while (voltar) {
            System.out.println("\n=== GERENCIAR DADOS DO TERRITÓRIO ===");
            System.out.println("Região atual: " + dados.getNomeRegiao()
                    + " | População: " + dados.getPopulacao());
            System.out.printf("Limiares -> Chuva: %.2f mm | Rio: %.2f m | Umidade: %.2f %%%n",
                    dados.getLimiarChuva(), dados.getLimiarNivelRio(), dados.getLimiarUmidade());
            System.out.println(" (Limiares são sensíveis: exigem o código de acesso.)");
            System.out.println(" a) Alterar nome da região");
            System.out.println(" b) Alterar população");
            System.out.println(" c) Alterar limiar de chuva");
            System.out.println(" d) Alterar limiar de nível do rio");
            System.out.println(" e) Alterar limiar de umidade");
            System.out.println(" f) Alterar código de acesso");
            System.out.println(" v) Voltar");
            String op = lerTexto("Opção: ").toLowerCase();
            switch (op) {
                case "a":
                    dados.setNomeRegiao(lerTexto("Novo nome da região: "));
                    break;
                case "b":
                    dados.setPopulacao(lerInteiro("Nova população: "));
                    break;
                case "c": {
                    double valor = lerDouble("Novo limiar de chuva (mm, 0 a 200): ");
                    String codigo = lerTexto("Código de acesso: ");
                    dados.setLimiarChuva(valor, codigo);
                    break;
                }
                case "d": {
                    double valor = lerDouble("Novo limiar de nível do rio (m, 0 a 10): ");
                    String codigo = lerTexto("Código de acesso: ");
                    dados.setLimiarNivelRio(valor, codigo);
                    break;
                }
                case "e": {
                    double valor = lerDouble("Novo limiar de umidade (%, 0 a 100): ");
                    String codigo = lerTexto("Código de acesso: ");
                    dados.setLimiarUmidade(valor, codigo);
                    break;
                }
                case "f": {
                    String atual = lerTexto("Código de acesso atual: ");
                    String novo = lerTexto("Novo código (mín. 4 caracteres): ");
                    dados.alterarCodigoAcesso(atual, novo);
                    break;
                }
                case "v":
                    voltar = false;
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    /** Opção 5: mostra um panorama completo do sistema. */
    private void exibirStatusCompleto() {
        System.out.println("=== STATUS COMPLETO DO TERRITÓRIO ===");
        System.out.println("Região     : " + dados.getNomeRegiao());
        System.out.println("População  : " + dados.getPopulacao());
        System.out.printf("Limiar Chuva: %.2f mm | Limiar Rio: %.2f m | Limiar Umidade: %.2f %%%n",
                dados.getLimiarChuva(), dados.getLimiarNivelRio(), dados.getLimiarUmidade());
        System.out.println("Base de dados: " + (dadosReaisCarregados
                ? "REAIS (" + leitor.getFonte() + ")" : "simulada (Math.random)"));
        System.out.println();
        System.out.println(estacao.verificarStatus());
        System.out.println("\nÚltimas leituras por sensor:");
        for (Sensor sensor : estacao.getSensores()) {
            ArrayList<Double> h = estacao.getHistorico(sensor);
            if (h.isEmpty()) {
                System.out.printf("  %-16s : sem leituras%n", sensor.retornarTipo());
            } else {
                System.out.printf("  %-16s : %.2f %s (%d leituras)%n",
                        sensor.retornarTipo(), h.get(h.size() - 1), sensor.getUnidade(), h.size());
            }
        }
    }

    /**
     * Opção 6 (BÔNUS): simula 24 horas, coletando uma leitura por hora e mostrando
     * como os alertas evoluem ao longo do tempo. No final, resume quantos alertas
     * de cada nível ocorreram e qual foi o pior momento.
     */
    private void simular24Horas() {
        if (dadosReaisCarregados) {
            replayDadosReais();
            return;
        }
        System.out.println("=== SIMULAÇÃO DE 24 HORAS (BÔNUS) ===");
        System.out.println("Cada hora coleta uma leitura e avalia o risco (previsão por média móvel).\n");

        int normal = 0, atencao = 0, alerta = 0, critico = 0;
        int piorHora = 0;
        NivelAlerta piorNivel = NivelAlerta.NORMAL;

        for (int hora = 1; hora <= 24; hora++) {
            // Coleta silenciosa: uma leitura por sensor, guardada no histórico.
            for (Sensor sensor : estacao.getSensores()) {
                if (sensor.verificarFuncionamento()) {
                    estacao.getHistorico(sensor).add(sensor.lerValor());
                }
            }
            NivelAlerta maxHora = avaliarHora(String.format("[Hora %02d] ", hora));
            switch (maxHora) {
                case NORMAL: normal++; break;
                case ATENCAO: atencao++; break;
                case ALERTA: alerta++; break;
                case CRITICO: critico++; break;
            }
            if (maxHora.ordinal() > piorNivel.ordinal()) {
                piorNivel = maxHora;
                piorHora = hora;
            }
        }

        imprimirResumoTemporal("24 HORAS SIMULADAS", normal, atencao, alerta, critico,
                piorNivel, piorNivel != NivelAlerta.NORMAL ? String.format("Hora %02d", piorHora) : null);
    }

    /**
     * Opção 7 (BÔNUS): carrega dados reais de fonte pública (CSV do Open-Meteo) e
     * preenche o histórico dos sensores. Depois roda as previsões sobre os dados reais.
     */
    /**
     * Localiza o CSV mesmo quando o programa é iniciado de outra pasta (ex.: IDE).
     * Procura no diretório atual e depois ao lado dos .class (code-source), subindo
     * até 3 níveis — cobre o caso do Eclipse, que copia o recurso para a pasta bin.
     */
    private String resolverCaminhoCsv(String nome) {
        File direto = new File(nome);
        if (direto.isFile()) return direto.getAbsolutePath();
        try {
            File codeSource = new File(SistemaMonitoramento.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            File dir = codeSource.isDirectory() ? codeSource : codeSource.getParentFile();
            for (int i = 0; i < 3 && dir != null; i++) {
                File cand = new File(dir, nome);
                if (cand.isFile()) return cand.getAbsolutePath();
                dir = dir.getParentFile();
            }
        } catch (Exception e) {
            // Sem code-source disponível: mantém o caminho informado.
        }
        return nome;
    }

    private void carregarDadosReais() {
        System.out.println("=== CARREGAR DADOS REAIS (FONTE PÚBLICA) ===");
        String caminho = lerTexto("Caminho do CSV (ENTER para 'dados_reais.csv'): ");
        if (caminho.isEmpty()) caminho = "dados_reais.csv";
        caminho = resolverCaminhoCsv(caminho);

        if (!leitor.carregar(caminho)) {
            System.out.println("Não foi possível carregar dados reais. Verifique o arquivo e o caminho.");
            return;
        }
        dadosReaisCarregados = true;
        System.out.println("Fonte   : " + leitor.getFonte());
        System.out.println("Período : " + leitor.getPeriodo());
        System.out.println("Leituras: " + leitor.quantidade() + " (horárias)");
        System.out.println("Obs.: chuva e umidade são reais; nível do rio é estimativa derivada da chuva real.");

        // Preenche o histórico de cada sensor com a coluna real correspondente.
        for (Sensor sensor : estacao.getSensores()) {
            ArrayList<Double> historico = estacao.getHistorico(sensor);
            historico.clear();
            ArrayList<Double> coluna;
            if (sensor instanceof SensorChuva) coluna = leitor.getChuvas();
            else if (sensor instanceof SensorNivelRio) coluna = leitor.getNiveis();
            else if (sensor instanceof SensorUmidade) coluna = leitor.getUmidades();
            else continue;
            historico.addAll(coluna);
            double ultima = coluna.get(coluna.size() - 1);
            if (sensor instanceof SensorChuva) ((SensorChuva) sensor).registrarLeitura(ultima);
            else if (sensor instanceof SensorNivelRio) ((SensorNivelRio) sensor).registrarLeitura(ultima);
            else if (sensor instanceof SensorUmidade) ((SensorUmidade) sensor).registrarLeitura(ultima);
        }
        System.out.println("\nHistóricos preenchidos com dados reais. Rodando previsões e alertas:");
        rodarPrevisoes();
    }

    /** Replay hora a hora do período real, reconstruindo o histórico progressivamente. */
    private void replayDadosReais() {
        System.out.println("=== REPLAY DOS DADOS REAIS (BÔNUS) ===");
        System.out.println("Fonte  : " + leitor.getFonte());
        System.out.println("Período: " + leitor.getPeriodo());
        System.out.println("Reavalia o risco hora a hora (média móvel) sobre os dados reais.\n");

        ArrayList<LeitorDadosReais.LeituraReal> reais = leitor.getLeituras();
        for (Sensor sensor : estacao.getSensores()) {
            estacao.getHistorico(sensor).clear();
        }

        int normal = 0, atencao = 0, alerta = 0, critico = 0;
        int piorIndice = 0;
        NivelAlerta piorNivel = NivelAlerta.NORMAL;

        for (int i = 0; i < reais.size(); i++) {
            LeitorDadosReais.LeituraReal lr = reais.get(i);
            for (Sensor sensor : estacao.getSensores()) {
                double v;
                if (sensor instanceof SensorChuva) v = lr.chuva;
                else if (sensor instanceof SensorNivelRio) v = lr.nivelRio;
                else if (sensor instanceof SensorUmidade) v = lr.umidade;
                else continue;
                estacao.getHistorico(sensor).add(v);
            }
            NivelAlerta maxHora = avaliarHora("[" + lr.timestamp + "] ");
            switch (maxHora) {
                case NORMAL: normal++; break;
                case ATENCAO: atencao++; break;
                case ALERTA: alerta++; break;
                case CRITICO: critico++; break;
            }
            if (maxHora.ordinal() > piorNivel.ordinal()) {
                piorNivel = maxHora;
                piorIndice = i;
            }
        }

        imprimirResumoTemporal("PERÍODO REAL (" + reais.size() + " HORAS)",
                normal, atencao, alerta, critico, piorNivel,
                piorNivel != NivelAlerta.NORMAL ? reais.get(piorIndice).timestamp : null);
    }

    /** Avalia todos os sensores no instante atual, imprime linha compacta e retorna o pior nível. */
    private NivelAlerta avaliarHora(String prefixo) {
        StringBuilder linha = new StringBuilder(prefixo);
        NivelAlerta maxHora = NivelAlerta.NORMAL;
        for (Sensor sensor : estacao.getSensores()) {
            ArrayList<Double> h = estacao.getHistorico(sensor);
            double previsto = mediaMovel.prever(h);
            NivelAlerta nivel = alertas.classificar(previsto, limiarDoSensor(sensor));
            linha.append(String.format("%s %.1f%s [%s]  ",
                    sensor.retornarTipo(), previsto, sensor.getUnidade(), nivel.getRotulo()));
            if (nivel.ordinal() > maxHora.ordinal()) maxHora = nivel;
        }
        System.out.println(linha.toString().trim());
        return maxHora;
    }

    /** Resumo comum das simulações temporais (aleatória ou real). */
    private void imprimirResumoTemporal(String titulo, int normal, int atencao, int alerta,
                                        int critico, NivelAlerta piorNivel, String piorMomento) {
        System.out.println("\n--- RESUMO: " + titulo + " ---");
        System.out.println("Horas NORMAL   : " + normal);
        System.out.println("Horas ATENÇÃO  : " + atencao);
        System.out.println("Horas ALERTA   : " + alerta);
        System.out.println("Horas CRÍTICO  : " + critico);
        if (piorNivel != NivelAlerta.NORMAL && piorMomento != null) {
            System.out.printf("Pior momento: %s com nível %s.%n", piorMomento, piorNivel.getRotulo());
        } else {
            System.out.println("Nenhum alerta acima do normal no período.");
        }
        System.out.println("Lembrete: o alerta apoia a decisão da comunidade, não decide por ela.");
    }

    // ---------------------------------------------------------------------
    // Apoio
    // ---------------------------------------------------------------------

    /** Mapeia cada sensor ao limiar protegido do território (encapsulamento). */
    private double limiarDoSensor(Sensor sensor) {
        if (sensor instanceof SensorChuva) return dados.getLimiarChuva();
        if (sensor instanceof SensorNivelRio) return dados.getLimiarNivelRio();
        if (sensor instanceof SensorUmidade) return dados.getLimiarUmidade();
        return sensor.getLimiteAlerta();
    }

    private void imprimirCabecalho() {
        System.out.println("==========================================================");
        System.out.println("   TERRITORY PREDICTIVE INSIGHTS  -  CitizenTech");
        System.out.println("   Monitoramento territorial: chuva e risco de alagamento");
        System.out.println("==========================================================");
        System.out.println("Território: " + dados.getNomeRegiao()
                + " | População atendida: " + dados.getPopulacao());
        System.out.println("Dica: opção 2 simula leituras; opção 7 carrega dados reais; opção 3 prevê.");
    }

    private void imprimirMenu() {
        System.out.println("\n------------------- MENU PRINCIPAL -------------------");
        System.out.println(" 1) Verificar sensores");
        System.out.println(" 2) Simular próximas leituras");
        System.out.println(" 3) Rodar previsões e alertas");
        System.out.println(" 4) Gerenciar dados do território");
        System.out.println(" 5) Exibir status completo");
        System.out.println(" 6) Simulação temporal (24h ou replay real)");
        System.out.println(" 7) Carregar dados reais (fonte pública)");
        System.out.println(" 0) Sair");
        System.out.println("------------------------------------------------------");
    }

    /** Lê uma linha de texto (usa nextLine para evitar o bug do newline do Scanner). */
    private String lerTexto(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /** Lê um inteiro, repetindo enquanto a entrada não for um número válido. */
    private int lerInteiro(String prompt) {
        while (true) {
            String entrada = lerTexto(prompt);
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    /** Lê um double, aceitando vírgula ou ponto como separador decimal. */
    private double lerDouble(String prompt) {
        while (true) {
            String entrada = lerTexto(prompt).replace(',', '.');
            try {
                return Double.parseDouble(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número (ex.: 45.5).");
            }
        }
    }

    private void pausar() {
        System.out.print("\nPressione ENTER para continuar...");
        scanner.nextLine();
    }
}