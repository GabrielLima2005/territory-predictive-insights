/**
 * CLASSE ABSTRATA (Conceito 1 de POO).
 *
 * ComponenteTerritorial é o "molde" de tudo que é instalado no território
 * (sensores, estações, painéis). Por ser abstrata, NÃO pode ser instanciada
 * diretamente (new ComponenteTerritorial() não compila). Ela existe para ser
 * herdada por classes concretas, que são obrigadas a implementar verificarStatus().
 *
 * Assim reaproveitamos os atributos e comportamentos comuns (id, nome, região,
 * ativar/desativar) e garantimos que todo componente saiba reporting seu status.
 */
public abstract class ComponenteTerritorial {

    // Atributos comuns a qualquer componente. "protected" permite que as
    // subclasses acessem diretamente, mas classes de fora não.
    protected String id;
    protected String nome;
    protected String regiao;
    protected boolean ativo;

    public ComponenteTerritorial(String id, String nome, String regiao) {
        this.id = id;
        this.nome = nome;
        this.regiao = regiao;
        this.ativo = true; // todo componente nasce ativado
    }

    // ----- Métodos concretos (já prontos, herdados por todas as subclasses) -----

    public void ativar() {
        this.ativo = true;
        System.out.println("[COMPONENTE] " + nome + " foi ATIVADO.");
    }

    public void desativar() {
        this.ativo = false;
        System.out.println("[COMPONENTE] " + nome + " foi DESATIVADO.");
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRegiao() {
        return regiao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    /** Método concreto auxiliar: mostra a identificação básica do componente. */
    public String identificacao() {
        return String.format("%s (%s) - Região: %s | %s",
                nome, id, regiao, ativo ? "ATIVO" : "INATIVO");
    }

    // ----- Método ABSTRATO -----
    // Não tem corpo aqui. Cada classe concreta é OBRIGADA a implementar.
    // É isso que torna a classe abstrata e define um contrato para as subclasses.
    public abstract String verificarStatus();
}