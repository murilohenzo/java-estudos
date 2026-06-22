/*
* O Singleton é um padrão de projeto criacional que permite
* a você garantir que uma classe tenha apenas uma instância,
* enquanto provê um ponto de acesso global para
* essa instância.
*
* */



/*
*   A classe Singleton declara o método estático getInstance
*   que retorna a mesma instância de sua própria classe.
*   O construtor da singleton deve ser escondido do código cliente. Chamando o método getInstance deve ser o único
*   modo de obter o objeto singleton.
* */

public class Singleton {

    private static Singleton singleton;

    private Singleton() {}

    public static Singleton getInstance() {
        if (singleton == null) {
            singleton = new Singleton();
        }
        return singleton;
    }
}
