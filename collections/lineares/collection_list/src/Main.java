import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<User> users = new ArrayList<>();
        User user1 = new User("Murilo", 'M', 23);
        User user2 = new User("Henzo", 'M', 25);
        User user3 = new User("Carla", 'M', 7);
        User user4 = new User("Mateus", 'M', 15);

        users.add(user1);
        users.add(user2);
        users.add(user3);
        users.add(user4);

        for (User user:
             users) {
            System.out.println(user);
        }
        System.out.println(users);
        Collections.sort(users);
        System.out.println(users);


        System.out.println("\n###########################\n");
        CompareUserAge compare = new CompareUserAge();
        Collections.sort(users, compare);
        System.out.println(users);
    }
}