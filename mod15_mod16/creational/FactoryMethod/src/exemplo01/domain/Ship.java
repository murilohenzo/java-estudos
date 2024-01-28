package exemplo01.domain;

public class Ship implements ITransport {
    @Override
    public void deliver() {
        System.out.println("Delivery by ship");
    }
}
