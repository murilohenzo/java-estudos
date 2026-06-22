import java.util.PriorityQueue;
import java.util.Queue;

public class ExamplePriorityQueue {
    public static void main(String[] args) {
        Queue<User> userQueue = new PriorityQueue<>();
        User user1 = new User("Murilo", 'M', 23);
        User user2 = new User("Henzo", 'M', 21);
        User user3 = new User("Cris", 'F', 19);
        User user4 = new User("Maria", 'F', 27);

        userQueue.add(user1);
        userQueue.add(user2);
        userQueue.add(user3);
        userQueue.add(user4);

        System.out.println(userQueue);

        while (userQueue.size() != 0) {
            System.out.println(userQueue.element());
            System.out.println(userQueue.remove());
        }
        System.out.println(userQueue);
    }
}