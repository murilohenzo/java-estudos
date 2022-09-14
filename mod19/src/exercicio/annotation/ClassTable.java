package exercicio.annotation;

public class ClassTable {

    @Table(value = "getMethod")
    public void method() {
        System.out.println("invoke method");
    }
}
