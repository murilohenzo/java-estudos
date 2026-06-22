import entities.Circle;
import entities.Rectangle;
import entities.Shape;
import enums.Color;

public class Main {
    public static void main(String[] args) {
        Shape shape = new Rectangle(Color.RED, 4.0, 5.0);
        Shape shape1 = new Circle(Color.BLUE, 3.0);

        Rectangle rectangle = new Rectangle(Color.BLUE, 2.0, 2.0);
        Circle circle = new Circle(Color.GREEN, 1.0);

        System.out.println(shape.area());
        System.out.println(shape1.area());
        System.out.println(circle.area());
        System.out.println(rectangle.area());
    }
}