import entities.User;
import repositories.UsersRepositoryInMemory;
import useCases.*;

public class Main {
    public static void main(String[] args) {

        User user1 = new User("murilo", 23);
        User user2 = new User("henzo", 23);
        User user3 = new User("mateus", 26);
        User user4 = new User("luiz", 26);
        User user5 = new User("cris", 28);

        System.out.println("******* Use Cases *******");
        System.out.println(CreateUserUseCase.class.getName());
        System.out.println(FindAllUserUseCase.class.getName());
        System.out.println(FindByIdUserUseCase.class.getName());
        System.out.println(FindByNameUserUseCase.class.getName());
        System.out.println(DeleteUserUseCase.class.getName());

        System.out.println();
        System.out.println("******* Repository In Memory *******");
        System.out.println(UsersRepositoryInMemory.class.getName());
        System.out.println();
        UsersRepositoryInMemory usersRepositoryInMemory = new UsersRepositoryInMemory();

        CreateUserUseCase createUserUseCase = new CreateUserUseCase(usersRepositoryInMemory);
        FindAllUserUseCase findAllUserUseCase = new FindAllUserUseCase(usersRepositoryInMemory);
        FindByIdUserUseCase findByIdUserUseCase = new FindByIdUserUseCase(usersRepositoryInMemory);
        FindByNameUserUseCase findByNameUserUseCase = new FindByNameUserUseCase(usersRepositoryInMemory);
        DeleteUserUseCase deleteUserUseCase = new DeleteUserUseCase(usersRepositoryInMemory);

        createUserUseCase.execute(user1);
        createUserUseCase.execute(user2);
        createUserUseCase.execute(user3);
        createUserUseCase.execute(user4);
        createUserUseCase.execute(user5);


        System.out.println("******* FIND ALL USERS *******");
        System.out.println(findAllUserUseCase.execute());
        deleteUserUseCase.execute(user5);
        System.out.println("******* REMOVE USER *******");
        System.out.println(findAllUserUseCase.execute());
        System.out.println("******* FIND USER BY ID *******");
        System.out.println(findByIdUserUseCase.execute(1));
        System.out.println("******* FIND USER BY NAME ******* ");
        System.out.println(findByNameUserUseCase.execute("murilo"));
    }
}