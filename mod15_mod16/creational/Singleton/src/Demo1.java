public class Demo1 {
    public static void main(String[] args) {
        SingletonProperties singletonProperties = SingletonProperties.getInstance("teste");
        SingletonProperties singletonProperties1 = SingletonProperties.getInstance("teste1");

        System.out.println(singletonProperties.getValue());
        System.out.println(singletonProperties1.getValue());
    }
}