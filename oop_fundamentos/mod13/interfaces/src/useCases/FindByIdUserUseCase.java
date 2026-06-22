package useCases;

import entities.User;
import repositories.UsersRepository;

public class FindByIdUserUseCase {
    private UsersRepository usersRepository;
    public FindByIdUserUseCase(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public User execute(Integer id) {
        return this.usersRepository.findById(id).orElse(null);
    }
}
