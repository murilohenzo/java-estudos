package br.com.murilo.ioc25.demo;

import br.com.murilo.ioc25.container.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Demo do mini IoC JDK 25 — mesma ideia do {@code mini_ioc}, com
 * {@link java.lang.ScopedValue} no lugar de {@link ThreadLocal}.
 *
 * <pre>
 * cd annotations_reflection/mini_ioc_jdk25/src
 * javac --release 25 br/com/murilo/ioc25/annotation/*.java \
 *       br/com/murilo/ioc25/container/*.java \
 *       br/com/murilo/ioc25/demo/*.java
 * java br.com.murilo.ioc25.demo.MiniIocDemo
 * </pre>
 */
public class MiniIocDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Mini IoC JDK 25 — Scoped Values (JEP 506) ===\n");
        System.out.println("creation scope bound (fora de create)? " + ApplicationContext.isCreationScopeBound());

        RequestIdGenerator.resetCounter();

        try (ApplicationContext ctx = new ApplicationContext()
                .register(UserRepository.class, UserService.class, RequestIdGenerator.class)
                .refresh()) {

            System.out.println("\ncreation scope bound (após refresh)? " + ApplicationContext.isCreationScopeBound());

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
        System.out.println("creation scope bound (após close)? " + ApplicationContext.isCreationScopeBound());
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
