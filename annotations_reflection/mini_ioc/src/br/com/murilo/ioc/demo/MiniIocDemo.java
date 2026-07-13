package br.com.murilo.ioc.demo;

import br.com.murilo.ioc.annotation.Component;
import br.com.murilo.ioc.annotation.Inject;
import br.com.murilo.ioc.container.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Demo do mini IoC estilo Spring — JDK 21, reflection + registry thread-safe.
 *
 * <pre>
 * cd annotations_reflection/mini_ioc/src
 * javac br/com/murilo/ioc/annotation/*.java \
 *       br/com/murilo/ioc/container/*.java \
 *       br/com/murilo/ioc/demo/*.java
 * java br.com.murilo.ioc.demo.MiniIocDemo
 * </pre>
 */
public class MiniIocDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Mini IoC (estilo Spring Bean) — JDK 21 ===\n");

        RequestIdGenerator.resetCounter();

        try (ApplicationContext ctx = new ApplicationContext()
                .register(UserRepository.class, UserService.class, RequestIdGenerator.class)
                .refresh()) {

            System.out.println("\n--- Singleton: mesma instância ---");
            UserService s1 = ctx.getBean(UserService.class);
            UserService s2 = ctx.getBean("userService");
            System.out.println(s1.greet("1"));
            System.out.println("s1 == s2 ? " + (s1 == s2));

            System.out.println("\n--- Prototype: novas instâncias ---");
            RequestIdGenerator r1 = ctx.getBean(RequestIdGenerator.class);
            RequestIdGenerator r2 = ctx.getBean(RequestIdGenerator.class);
            System.out.println("r1=" + r1.id() + " #" + r1.instanceNumber());
            System.out.println("r2=" + r2.id() + " #" + r2.instanceNumber());
            System.out.println("r1 == r2 ? " + (r1 == r2));

            System.out.println("\n--- Thread-safety: getBean concorrente (Virtual Threads) ---");
            demonstrateConcurrentAccess(ctx);
        }

        System.out.println("\n(container fechado — @PreDestroy executados)");
    }

    private static void demonstrateConcurrentAccess(ApplicationContext ctx) throws Exception {
        int tasks = 32;
        List<Callable<UserService>> callables = new ArrayList<>();
        for (int i = 0; i < tasks; i++) {
            callables.add(() -> ctx.getBean(UserService.class));
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<UserService>> futures = executor.invokeAll(callables, 5, TimeUnit.SECONDS);
            UserService first = futures.getFirst().get();
            boolean allSame = futures.stream().allMatch(f -> {
                try {
                    return f.get() == first;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            System.out.println(tasks + " gets concorrentes → mesma instância singleton? " + allSame);
        }
    }
}
