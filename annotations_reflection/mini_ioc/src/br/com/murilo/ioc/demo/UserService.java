package br.com.murilo.ioc.demo;

import br.com.murilo.ioc.annotation.Component;
import br.com.murilo.ioc.annotation.Inject;
import br.com.murilo.ioc.annotation.PostConstruct;

/** Serviço com injeção por construtor (prática preferida). */
@Component
public class UserService {

    private final UserRepository repository;

    @Inject
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    void ready() {
        System.out.println("[UserService] @PostConstruct — pronto");
    }

    public String greet(String userId) {
        return "Olá, " + repository.findNameById(userId) + "!";
    }
}
