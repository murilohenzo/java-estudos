/*
*   A classe Singleton declara o método estático getInstance
*   que retorna a mesma instância de sua própria classe.
*   O construtor da singleton deve ser escondido do código cliente. Chamando o método getInstance deve ser o único
*   modo de obter o objeto singleton.
* */

public class SingletonPropertiesSync {

    private static SingletonPropertiesSync singletonPropertiesSync;
    private String value;

    private SingletonPropertiesSync(String value) {
        this.value = value;
    }

    /*
    * SO ENTRA UMA EXEC POR VEZ
    * */
    public static synchronized SingletonPropertiesSync getInstance(String value) {
        if (singletonPropertiesSync == null) {
            singletonPropertiesSync = new SingletonPropertiesSync(value);
        }
        return singletonPropertiesSync;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
