/*
*   A classe Singleton declara o método estático getInstance
*   que retorna a mesma instância de sua própria classe.
*   O construtor da singleton deve ser escondido do código cliente. Chamando o método getInstance deve ser o único
*   modo de obter o objeto singleton.
* */

public class SingletonProperties {

    private static SingletonProperties singleton;
    private String value;

    private SingletonProperties(String value) {
        this.value = value;
    }

    public static SingletonProperties getInstance(String value) {
        if (singleton == null) {
            singleton = new SingletonProperties(value);
        }
        return singleton;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
