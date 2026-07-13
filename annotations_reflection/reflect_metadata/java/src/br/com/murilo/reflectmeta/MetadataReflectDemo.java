package br.com.murilo.reflectmeta;

import java.util.List;

/**
 * Demo espelhando o fluxo TypeScript: defineMetadata → getMetadata → DI manual.
 *
 * <pre>
 * cd annotations_reflection/reflect_metadata/java/src
 * javac br/com/murilo/reflectmeta/*.java
 * java br.com.murilo.reflectmeta.MetadataReflectDemo
 * </pre>
 */
public class MetadataReflectDemo {

    static final String INJECTABLE = "mini:injectable";
    static final String DESIGN_PARAMTYPES = "design:paramtypes";
    static final String CONFIG_KEY = "config:key";

    static class Logger {
        void log(String msg) {
            System.out.println("[Logger] " + msg);
        }
    }

    static class UserService {
        private final Logger logger;

        UserService(Logger logger) {
            this.logger = logger;
        }

        void greet(String name) {
            logger.log("hello, " + name);
        }
    }

    static class Base {
        void run() {}
    }

    static class Child extends Base {
        @Override
        void run() {}
    }

    public static void main(String[] args) {
        MetadataReflect.clearAll();
        System.out.println("=== MetadataReflect (espelho Java da API TS) ===\n");

        // equivalente a @Injectable(Logger)
        MetadataReflect.defineMetadata(INJECTABLE, true, UserService.class);
        MetadataReflect.defineMetadata(DESIGN_PARAMTYPES, List.of(Logger.class), UserService.class);

        System.out.println("--- define / get (classe) ---");
        System.out.println("injectable? " + MetadataReflect.getMetadata(INJECTABLE, UserService.class).orElse(false));
        System.out.println("paramtypes= " + MetadataReflect.getMetadata(DESIGN_PARAMTYPES, UserService.class).orElse(List.of()));

        System.out.println("\n--- DI manual via metadata ---");
        UserService service = resolve(UserService.class);
        service.greet("Murilo");

        System.out.println("\n--- metadata em \"propriedade\" (member key) ---");
        MetadataReflect.defineMetadata(CONFIG_KEY, "app.name", ConfigHolder.class, "appName");
        System.out.println("config:key = " + MetadataReflect.getMetadata(CONFIG_KEY, ConfigHolder.class, "appName").orElse(null));

        System.out.println("\n--- herança (superclass chain) ---");
        MetadataReflect.defineMetadata("role", "base", Base.class, "run");
        MetadataReflect.defineMetadata("role", "child", Child.class, "run");
        System.out.println("Child own role= " + MetadataReflect.getOwnMetadata("role", Child.class, "run").orElse(null));
        System.out.println("Child getMetadata role= " + MetadataReflect.getMetadata("role", Child.class, "run").orElse(null));

        System.out.println("\n--- deleteMetadata ---");
        MetadataReflect.deleteMetadata("role", Child.class, "run");
        System.out.println("após delete (sobe para Base)= "
                + MetadataReflect.getMetadata("role", Child.class, "run").orElse(null));
    }

    @SuppressWarnings("unchecked")
    static <T> T resolve(Class<T> type) {
        if (!Boolean.TRUE.equals(MetadataReflect.getMetadata(INJECTABLE, type).orElse(false))) {
            throw new IllegalStateException(type.getSimpleName() + " não é injectable");
        }
        List<Class<?>> params = (List<Class<?>>) MetadataReflect
                .getMetadata(DESIGN_PARAMTYPES, type)
                .orElse(List.of());
        try {
            Object[] deps = new Object[params.size()];
            for (int i = 0; i < params.size(); i++) {
                deps[i] = params.get(i).getDeclaredConstructor().newInstance();
            }
            return type.getDeclaredConstructor(params.toArray(Class[]::new)).newInstance(deps);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static class ConfigHolder {
        String appName = "java-estudos";
    }
}
