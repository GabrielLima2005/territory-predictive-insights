# Territory Predictive Insights — CitizenTech

**Global Solution 2º/2026 — Programação Orientada a Objetos (POO)**

Plataforma de monitoramento territorial que lê medições de sensores, gera previsões
simples e emite alertas automáticos fundamentados nas leituras. A pergunta que o
sistema ajuda a responder é: *"qual problema vai atingir o nosso território nos
próximos dias, e o que podemos fazer hoje para evitar ou reduzir o impacto?"*

**Recorte escolhido:** Clima local — chuva intensa e risco de alagamento em pontos vulneráveis.

---

## 1. Público beneficiado

Moradores de áreas vulneráveis a alagamento (encostas, várzeas, margens de rio) e a
organização comunitária / defesa civil local que precisa decidir, por exemplo:

- vale a pena acionar a rota de evacuação preventiva?
- em quais pontos concentrar sacos de areia, bombas ou equipes?
- quando comunicar idosos e pessoas com mobilidade reduzida para se anteciparem?

O sistema **não decide** por essas pessoas: ele organiza leituras e previsões para
**apoiar** a decisão de quem conhece o território. O território é descrito por
**região e equipamento** (ex.: "Vila Esperança", "Estação Central"), nunca por
indivíduos — não há nomes, CPF ou endereços de pessoas reais.

---

## 2. Como compilar e rodar (IMPORTANTE)

Pré-requisito: **JDK 17 ou superior** (testado com OpenJDK 25). Todos os arquivos
estão no pacote padrão, na mesma pasta.

### Windows (Prompt de Comando ou PowerShell)

```bat
cd territory-predictive-insights
chcp 65001
javac -encoding UTF-8 *.java
java -Dstdout.encoding=UTF-8 -Dfile.encoding=UTF-8 SistemaMonitoramento
```

### Linux / macOS

```bash
cd territory-predictive-insights
javac -encoding UTF-8 *.java
java -Dfile.encoding=UTF-8 SistemaMonitoramento
```

> O `-encoding UTF-8` na compilação e o `-Dstdout.encoding=UTF-8` na execução
> garantem que os acentos do português apareçam corretamente no console. Sem isso,
> o programa ainda funciona, mas alguns caracteres podem sair trocados no Windows.

### Roteiro rápido de teste

1. Opção **7** → carregar **dados reais** (ENTER usa `dados_reais.csv`) e já rodar as previsões.
2. Opção **6** → simulação temporal: com dados reais carregados, faz o **replay do período real**;
   sem dados reais, simula 24 horas aleatórias. Mostra resumo de alertas.
3. Opção **2** → simular **5** rodadas de leitura (modo aleatório, alimenta o histórico).
4. Opção **3** → rodar previsões e alertas (média móvel + comparação).
5. Opção **4** → gerenciar dados: tente população negativa (é rejeitada) e alterar
   um limiar com código errado (rejeitado) e com código certo (aceito).
6. Opção **5** → status completo. Opção **0** → sair.

**Código de acesso padrão dos limiares:** `TERRITORIO2026`
( Pode ser trocado na opção 4 → f. )

---

## 3. Estrutura de arquivos

| Arquivo | Papel | Conceito de POO |
|---|---|---|
| `ComponenteTerritorial.java` | Molde de tudo que é instalado no território | **Classe abstrata** |
| `Sensor.java` | Contrato que todo sensor deve cumprir | **Interface** |
| `DadosTerritorio.java` | Dados sensíveis protegidos e validados | **Encapsulamento** |
| `EstrategiaPrevisao.java` | Molde das formas de prever | **Herança (base abstrata)** |
| `PrevisaoMediaMovel.java` | Média das últimas N leituras | Herança (`extends` + `super`) |
| `PrevisaoComparacao.java` | Período atual vs. anterior | Herança (`extends` + `super`) |
| `SensorChuva.java` | Sensor de chuva (mm) | `extends` + `implements Sensor` |
| `SensorNivelRio.java` | Sensor de nível do rio (m) | `extends` + `implements Sensor` |
| `SensorUmidade.java` | Sensor de umidade do solo (%) | `extends` + `implements Sensor` |
| `EstacaoDeColeta.java` | Componente que agrupa sensores e guarda histórico | Classe concreta que herda da abstrata |
| `NivelAlerta.java` | Níveis NORMAL / ATENÇÃO / ALERTA / CRÍTICO | Enum (boas práticas) |
| `SistemaAlertas.java` | Compara previsão com limiar e emite alertas | Sistema de alertas |
| `SistemaMonitoramento.java` | Classe principal com menu interativo | Orquestração + `main` |
| `LeitorDadosReais.java` | Carrega o CSV de dados reais (fonte pública) | Leitura de arquivo / bônus |
| `dados_reais.csv` | Dataset real (Open-Meteo, São Paulo, 16–19/01/2026) | Dados de fonte pública |

---

## 4. Como cada conceito de POO foi aplicado

### 4.1 Classe abstrata (`ComponenteTerritorial`)
- É `abstract` e **não pode ser instanciada**.
- Atributos comuns `protected`: `id`, `nome`, `regiao`, `ativo`.
- Métodos concretos herdados: `ativar()`, `desativar()`, `identificacao()`.
- Método **abstrato** `verificarStatus()` que toda subclasse é obrigada a implementar.
- Classes concretas que herdam: `SensorChuva`, `SensorNivelRio`, `SensorUmidade` e
  `EstacaoDeColeta` (todas usam `extends`).

### 4.2 Interface (`Sensor`)
- Contrato com `lerValor()`, `verificarFuncionamento()`, `retornarTipo()`,
  `getUnidade()` e `getLimiteAlerta()`.
- Implementada por **3 classes**: `SensorChuva`, `SensorNivelRio`, `SensorUmidade`
  (todas usam `implements Sensor`). O sistema trata os três de forma uniforme
  (polimorfismo) ao coletar leituras e rodar previsões.

### 4.3 Encapsulamento (`DadosTerritorio`)
- **Todos os atributos são `private`**.
- O `codigoAcesso` é sensível e **não tem getter** — não pode ser lido de fora.
- Setters com validação: população negativa/absurda é rejeitada; nome de região
  vazio é rejeitado; limiares fora da faixa são rejeitados.
- Alterar limiares **exige o código de acesso correto**. Alteração inválida gera
  mensagem clara e **mantém o valor anterior**.

### 4.4 Herança (`EstrategiaPrevisao`)
- Classe base `abstract` com atributos comuns (`nome`, `leiturasMinimas`) e métodos
  concretos reaproveitáveis (`calcularMedia`, `temLeiturasSuficientes`).
- Duas subclasses com **atributos próprios**:
  - `PrevisaoMediaMovel` → `tamanhoJanela` (média das últimas N leituras).
  - `PrevisaoComparacao` → `tamanhoPeriodo` (atual vs. anterior, projeta a tendência).
- Ambas **sobrescrevem** `prever()` e `explicar()` e usam **`super(...)`** no
  construtor e **`super.calcularMedia(...)`** / **`super.temLeiturasSuficientes(...)`**.

### 4.5 Sistema de alertas (`SistemaAlertas` + `NivelAlerta`)
- Compara automaticamente o valor **previsto** com o limiar protegido do território.
- Níveis proporcionais ao quanto o limite foi estourado:
  - até o limite → **NORMAL**
  - até 10% acima → **ATENÇÃO**
  - até 25% acima → **ALERTA**
  - mais de 25% acima → **CRÍTICO**
- Cada alerta explica o **porquê**: qual sensor, valor previsto, limite estourado e
  em quanto (valor e percentual).

---

## 5. Funcionalidades

- **Sensores:** leitura simulada com `Math.random()` em faixas horárias realistas
  (chuva 0–15 mm/h, rio 1,5–5,5 m, umidade 45–100%), verificação de funcionamento,
  limites de alerta e detecção de valor acima do limite. Os mesmos sensores também
  aceitam **dados reais** carregados do CSV (opção 7).
- **Previsão:** histórico guardado em `ArrayList`; as duas estratégias rodam sobre
  as leituras; resultado claro (valor previsto + significado).
- **Dados do território:** limiares protegidos por código; população e região com
  validação; alterações inválidas rejeitadas com mensagem.
- **Monitoramento:** menu interativo em loop com leitura de entrada do usuário.
- **Alertas:** automáticos, com níveis e explicação.

### Bônus implementado (+0,5): Dados reais de fonte pública + simulação temporal

A GS conta **apenas um** bônus; o projeto implementa dois recursos que se encaixam:

**a) Dados reais de fonte pública (opção 7).** O arquivo `dados_reais.csv` contém
medições históricas **reais** de precipitação (mm/h) e umidade relativa (%) obtidas
da API pública **Open-Meteo** (https://open-meteo.com), para **São Paulo/SP**
(lat -23.55, lon -46.63), no período de **16 a 19/01/2026** (96 leituras horárias,
um episódio de chuva contínua). Ao carregar, o sistema preenche o histórico dos
sensores e roda as previsões/alertas sobre os dados reais. A leitura é **offline**
(de arquivo), então o programa não depende de internet para rodar.

**b) Simulação temporal (opção 6).** Com dados reais carregados, faz o **replay hora
a hora** do período real; sem dados reais, simula 24 horas aleatórias. Em ambos os
casos mostra os alertas ao longo do tempo e um resumo (horas em cada nível e pior
momento), tornando visível como o risco evolui.

#### Fonte, licença e transparência dos dados
- **Fonte:** Open-Meteo Historical Weather API (`archive-api.open-meteo.com`), dados
  abertos de reanálise meteorológica. Uso gratuito para fins não comerciais, com
  atribuição (licença CC BY 4.0 — https://open-meteo.com/en/licence).
- **Chuva e umidade:** valores reais de precipitação horária e umidade relativa a 2 m.
- **Nível do rio:** NÃO é medição real de régua limnimétrica. É uma **estimativa
  simplificada** derivada da chuva real, por um modelo chuva-vazão de precipitação
  antecedente: `API[i] = chuva[i] + 0.85 * API[i-1]` e `nivel = 2.0 + 0.16 * API`
  (metros, com piso de 2,0 m). Está sinalizada como estimativa no CSV
  (`nivel_rio_est_m`) e no README, para não ser confundida com dado observado.
- **Como reproduzir o dataset:** chamada à API (qualquer navegador/HTTP):
  `https://archive-api.open-meteo.com/v1/archive?latitude=-23.55&longitude=-46.63&start_date=2026-01-16&end_date=2026-01-19&hourly=precipitation,relative_humidity_2m&timezone=America/Sao_Paulo`
  e, sobre cada hora, aplicar o modelo de nível acima.

---

## 6. Reflexão crítica

**Quando o sistema deve se calar?** Com poucas leituras, a previsão é frágil. Por
isso cada estratégia declara um mínimo de leituras (`leiturasMinimas = 5`) e o
sistema rotula a previsão como *"POUCO CONFIÁVEL"* quando o histórico é curto. A
previsão por comparação, por exemplo, só é completa com dois períodos (10 leituras);
antes disso ela avisa que está fazendo uma comparação parcial. Um alerta baseado em
1 ou 2 leituras poderia gerar pânico à toa — então o sistema sinaliza a baixa
confiança em vez de fingir certeza.

**O que os dados não representam?** Um sensor (ou um ponto de grade da reanálise) por
região inteira diz pouco sobre um ponto específico: a chuva pode ser intensa numa rua
e não na outra; o nível do rio numa margem não descreve toda a bacia. Mesmo os
**dados reais** do Open-Meteo são uma média de grade (~ bairro), não a realidade de
cada esquina, e o **nível do rio é apenas uma estimativa** derivada da chuva, não uma
medição de régua. No modo simulado, os valores são aleatórios. Além disso, sensores
podem falhar, estar desativados ou mal posicionados — e dado nenhum captura tudo
(ex.: bueiros entupidos, que pioram alagamento mesmo com chuva moderada).

**Por que o alerta apoia a decisão, em vez de decidir?** Quem conhece o território
sabe coisas que o sensor não mede: histórico de enchentes, rotas seguras, quem precisa
de ajuda para sair de casa. O sistema organiza evidências e antecipa tendências, mas a
decisão de evacuar, mobilizar ou comunicar é humana e contextual. Automatizar a decisão
tiraria a responsabilidade e o conhecimento local do processo — e poderia causar tanto
falsos alarmes (desgaste da confiança) quanto falsa sensação de segurança. O alerta é
um **insumo** para a decisão comunitária, não um veredito.

---

## 7. Uso de IA

Este projeto contou com **assistência de IA (Qoder)** para gerar e estruturar o código
Java e este README. A IA foi usada como apoio de programação e organização dos
conceitos de POO.

**Responsabilidade da equipe:** cada integrante deve **ler, entender e ser capaz de
explicar** qualquer trecho do próprio código durante a conversa de correção. Recomenda-
se estudar cada classe na ordem da seção 8, rodar o programa, testar as validações e
modificar pequenos trechos para confirmar o entendimento. Entregar sem compreensão
arriscar a correção individual — o objetivo da atividade é **aprender POO na prática**.

> Sugestão de revisão: cada integrante escolhe 2 arquivos, explica em voz alta o que
> cada método faz e onde o conceito de POO aparece, e só então considera o projeto
> "seu".

---

## 8. Divisão de tarefas (preencher com os nomes reais)

Sugestão de divisão entre os 3 integrantes (adaptar à equipe):

| Integrante | Responsabilidade | Arquivos principais |
|---|---|---|
| Integrante 1 — *(nome)* | Sensores e estação | `Sensor.java`, `SensorChuva`, `SensorNivelRio`, `SensorUmidade`, `EstacaoDeColeta` |
| Integrante 2 — *(nome)* | Previsões | `EstrategiaPrevisao`, `PrevisaoMediaMovel`, `PrevisaoComparacao` |
| Integrante 3 — *(nome)* | Dados, alertas e menu | `DadosTerritorio`, `ComponenteTerritorial`, `NivelAlerta`, `SistemaAlertas`, `SistemaMonitoramento` |

**Todos** devem commitar no GitHub com mensagens claras e revisar o código conjunto.
Exemplos de mensagens de commit:
- `feat: cria classe abstrata ComponenteTerritorial`
- `feat: adiciona interface Sensor e sensor de chuva`
- `feat: implementa encapsulamento em DadosTerritorio`
- `feat: adiciona estrategias de previsao (media movel e comparacao)`
- `feat: sistema de alertas com niveis`
- `feat: menu principal e simulacao temporal`
- `feat: leitor de dados reais (Open-Meteo) e replay do periodo real`
- `docs: README com reflexao critica, fonte de dados e uso de IA`

---

## 9. Decisões da equipe

- **Recorte:** clima local / alagamento, por ter sensores claros e impacto social direto.
- **Previsões simples:** média móvel e comparação de períodos (não é uma GS de
  estatística; o foco é modelar com POO).
- **Sensores como componentes:** cada sensor herda de `ComponenteTerritorial` **e**
  implementa `Sensor`, demonstrando os dois conceitos na mesma classe concreta.
- **Limiares no território:** os limiares de alerta ficam em `DadosTerritorio`
  (protegidos por código), e o sistema de alertas os consulta — assim a comunidade
  controla os limites de forma segura.
- **Bônus:** dados reais de fonte pública (Open-Meteo) + simulação temporal, por
  ancorar o sistema em um episódio real de chuva e mostrar a evolução do risco.
- **Escala horária realista:** limiares e faixas simuladas usam escala de mm/h
  (chuva forte ≈ 8 mm/h) para serem coerentes com os dados reais.
- **Sem dados pessoais:** o território é descrito por região e equipamento.

---

## 10. O que falta / como continuar ( honesto )

Tudo o que é obrigatório está implementado e rodando, incluindo o bônus de dados
reais. Melhorias futuras possíveis:
- Integrar nível do rio REAL (ex.: API da ANA/HidroWeb) em vez da estimativa.
- Buscar dados ao vivo da API em tempo de execução (hoje o dataset é offline).
- Salvar/recarregar o histórico gerado em arquivo.
- Adicionar mais tipos de sensor ou uma interface gráfica.
- Testes automatizados (JUnit) para as validações e previsões.