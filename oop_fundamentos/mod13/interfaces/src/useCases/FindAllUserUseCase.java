package useCases;

import entities.User;
import repositories.UsersRepository;

import java.util.List;

public class FindAllUserUseCase {
    private final UsersRepository usersRepository;

    public FindAllUserUseCase(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public List<User> execute() {
        return this.usersRepository.findAll();
    }
}
