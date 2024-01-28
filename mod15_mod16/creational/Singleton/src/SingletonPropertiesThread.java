/*
*   A classe Singleton declara o método estático getInstance
*   que retorna a mesma instância de sua própria classe.
*   O construtor da singleton deve ser escondido do código cliente. Chamando o método getInstance deve ser o único
*   modo de obter o objeto singleton.
* */

public class SingletonPropertiesThread {

    public static void main(String[] args) {
        Thread t1 = new Thread(new ThreadFoo());
        Thread t2 = new Thread(new ThreadBar());

        t1.start();
        t2.start();
    }

    static class ThreadFoo implements Runnable {

        @Override
        public void run() {
            SingletonProperties singletonProperties = SingletonProperties.getInstance("teste");
            System.out.println(singletonProperties.getValue());
        }
    }

    static class ThreadBar implements Runnable {

        @Override
        public void run() {
            SingletonProperties singletonProperties1 = SingletonProperties.getInstance("teste1");
            System.out.println(singletonProperties1.getValue());
        }
    }
}
