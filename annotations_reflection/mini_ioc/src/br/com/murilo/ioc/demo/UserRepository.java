package br.com.murilo.ioc.demo;

import br.com.murilo.ioc.annotation.Component;
import br.com.murilo.ioc.annotation.PostConstruct;
import br.com.murilo.ioc.annotation.PreDestroy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Repositório em memória — bean singleton. */
@Component("userRepository")
public class UserRepository {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        store.put("1", "Murilo");
        store.put("2", "Ana");
        System.out.println("[UserRepository] @PostConstruct — seed ok");
    }

    public String findNameById(String id) {
        return store.getOrDefault(id, "<unknown>");
    }

    @PreDestroy
    void shutdown() {
        store.clear();
        System.out.println("[UserRepository] @PreDestroy — store limpo");
    }
}
