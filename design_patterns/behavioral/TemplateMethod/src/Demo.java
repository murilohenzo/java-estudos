import workers.Firefighter;
import workers.Postman;
import workers.Worker;

public class Demo {
    public static void main(String[] args) {
        Worker firefighter = new Firefighter();
        Worker postman = new Postman();
        firefighter.routineExecute();
        System.out.println("***********************");
        postman.routineExecute();
    }
}