package repositories;

import entities.User;

import java.util.List;
import java.util.Optional;

public interface UsersRepository {

    List<User> findAll();

    Optional<User> findById(Integer id);

    Optional<User> findByName(String name);

    void addUser(User user);

    void remove(User user);
}
