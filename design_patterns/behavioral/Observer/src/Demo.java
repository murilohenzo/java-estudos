public class Demo {
    public static void main(String[] args) {
        Journalist journalist = new Journalist();
        journalist.add(new TVObserver());
        journalist.add(new EmailObserver());
        journalist.add(new SMSObserver());
        journalist.notifyAll("Teste");
    }
}