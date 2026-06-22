import exemplo01.domain.TransportType;
import factories.TransportFactory;

public class Main {
    public static void main(String[] args) {
        TransportFactory.getTransport(TransportType.TRUCK);
        TransportFactory.getTransport(TransportType.SHIP);
    }
}