package generics;

public class ExampleGenericEntry {

    public static void main(String args[]) {
        GenericEntry<String, Long> genericEntry = new GenericEntry<String, Long>("test", 10L);
        System.out.println(genericEntry);

        GenericEntry<String, Boolean> response = new GenericEntry<String, Boolean>("eventStatus", true);
        System.out.println(response);

    }
}
