package useCases;

import entities.User;
import repositories.UsersRepository;

import java.util.ArrayList;

public class CreateUserUseCase {
    private UsersRepository usersRepository;
    public CreateUserUseCase(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public void execute (User user) {
        this.usersRepository.addUser(user);
    }
}
