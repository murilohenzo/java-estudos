package useCases;

import entities.User;
import repositories.UsersRepository;

public class DeleteUserUseCase {
    private UsersRepository usersRepository;
    public DeleteUserUseCase(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public void execute(User user) {this.usersRepository.remove(user);
    }
}
