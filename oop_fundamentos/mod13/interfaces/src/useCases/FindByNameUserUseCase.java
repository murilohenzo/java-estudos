package useCases;

import entities.User;
import repositories.UsersRepository;

public class FindByNameUserUseCase {
    private UsersRepository usersRepository;
    public FindByNameUserUseCase(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public User execute(String name) {
        return this.usersRepository.findByName(name).orElse(null);
    }
}
