package repositories;

import entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UsersRepositoryInMemory implements UsersRepository {

    private final List<User> users = new ArrayList<>();

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    @Override
    public Optional<User> findById(Integer id) {
        if (id == null || id < 0 || id >= users.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByName(String name) {
        return users.stream()
                .filter(u -> Objects.equals(u.getName(), name))
                .findFirst();
    }

    @Override
    public void addUser(User user) {
        users.add(user);
    }

    @Override
    public void remove(User user) {
        users.remove(user);
    }
}
