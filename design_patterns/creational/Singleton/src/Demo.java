public class Demo {
    public static void main(String[] args) {
        Singleton singleton = Singleton.getInstance();
        System.out.println(singleton);
        Singleton singleton1 = Singleton.getInstance();
        System.out.println(singleton1);
    }
}