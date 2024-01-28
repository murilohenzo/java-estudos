package workers;

public class Postman extends Worker{
    @Override
    protected void work() {
        System.out.println("Trabalhando como " + getClass().getSimpleName());
    }
    @Override
    protected void wakeUp() {
        System.out.println("Levanta as 5h30m");
    }
}
