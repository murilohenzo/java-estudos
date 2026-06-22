package factories;

import exemplo01.domain.Ship;
import exemplo01.domain.TransportType;
import exemplo01.domain.Truck;

public class TransportFactory {
    public TransportFactory() {
        // TODO document why this constructor is empty
    }

    public static void getTransport(TransportType type) {
        switch (type) {
            case SHIP:
                new Ship().deliver();
                break;
            case TRUCK:
                new Truck().deliver();
                break;
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }
}
