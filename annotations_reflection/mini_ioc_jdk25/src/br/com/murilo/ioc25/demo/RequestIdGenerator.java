package br.com.murilo.ioc25.demo;

import br.com.murilo.ioc25.annotation.BeanScope;
import br.com.murilo.ioc25.annotation.Component;
import br.com.murilo.ioc25.annotation.Inject;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bean prototype — nova instância a cada getBean (ex.: request-scoped simplificado).
 */
@Component(scope = BeanScope.PROTOTYPE)
public class RequestIdGenerator {

    private static final AtomicInteger INSTANCES = new AtomicInteger();

    private final String id;
    private final int instanceNumber;

    @Inject
    public RequestIdGenerator() {
        this.instanceNumber = INSTANCES.incrementAndGet();
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }

    public String id() {
        return id;
    }

    public int instanceNumber() {
        return instanceNumber;
    }

    public static void resetCounter() {
        INSTANCES.set(0);
    }
}
