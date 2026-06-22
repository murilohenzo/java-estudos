import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<List<User>> users = new ArrayList<>();
        List<User> usersMale = new ArrayList<>();
        List<User> usersFemale = new ArrayList<>();

        System.out.print("Qual numero de usuarios a serem adicionados? ");
        int n = sc.nextInt();

        for (int i = 0; i < n; i++) {
            System.out.print("Name: ");
            String name = sc.next();
            System.out.print("Sex: ");
            String sex = sc.next();

            if (Objects.equals(sex, "F")) {
                usersFemale.add(new User(name, sex));
            } else if (Objects.equals(sex, "M")) {
                usersMale.add(new User(name, sex));
            } else {
                System.out.println("Categoria de sexo desconhecida");
            }
        }

        users.add(usersFemale);
        users.add(usersMale);
        System.out.println(users);
    }
}