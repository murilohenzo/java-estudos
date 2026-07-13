package br.com.murilo.ioc.container;

import br.com.murilo.ioc.annotation.BeanScope;
import br.com.murilo.ioc.annotation.Component;
import br.com.murilo.ioc.annotation.Inject;
import br.com.murilo.ioc.annotation.PostConstruct;
import br.com.murilo.ioc.annotation.PreDestroy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mini ApplicationContext inspirado no Spring IoC — Java 21 puro.
 *
 * <p>Fases: {@code register} → {@code refresh} (freeze + eager singletons) →
 * {@code getBean} → {@code close} ({@code @PreDestroy}).
 *
 * <p>Thread-safety: registry com {@link java.util.concurrent.ConcurrentHashMap},
 * criação de singleton via {@code computeIfAbsent}, detecção de dependência circular
 * com {@link ThreadLocal} de beans em construção.
 */
public final class ApplicationContext implements AutoCloseable {

    private final BeanRegistry registry = new BeanRegistry();
    private final ThreadLocal<Map<String, Object>> beansUnderConstruction =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

    public ApplicationContext register(Class<?>... componentClasses) {
        for (Class<?> type : componentClasses) {
            registerType(type);
        }
        return this;
    }

    private void registerType(Class<?> type) {
        if (!type.isAnnotationPresent(Component.class)) {
            throw new IllegalArgumentException(
                    type.getName() + " precisa de @" + Component.class.getSimpleName());
        }
        Component meta = type.getAnnotation(Component.class);
        String name = resolveName(meta.value(), type);
        registry.register(new BeanDefinition(name, type, meta.scope()));
    }

    /** Congela definições e instancia singletons eagerly. */
    public ApplicationContext refresh() {
        registry.freeze();
        for (BeanDefinition def : registry.definitions()) {
            if (def.scope() == BeanScope.SINGLETON) {
                getBean(def.name());
            }
        }
        return this;
    }

    public <T> T getBean(Class<T> type) {
        BeanDefinition def = registry.findByType(type)
                .orElseThrow(() -> new BeanCreationException("nenhum bean para tipo " + type.getName()));
        return type.cast(getBean(def.name()));
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        BeanDefinition def = registry.findByName(name)
                .orElseThrow(() -> new BeanCreationException("bean não encontrado: " + name));

        return switch (def.scope()) {
            case SINGLETON -> (T) registry.getOrCreateSingleton(name, this::createAndInitialize);
            case PROTOTYPE -> (T) createAndInitialize(name);
        };
    }

    private Object createAndInitialize(String name) throws Exception {
        BeanDefinition def = registry.findByName(name)
                .orElseThrow(() -> new BeanCreationException("definição ausente: " + name));

        Map<String, Object> creating = beansUnderConstruction.get();
        if (creating.containsKey(name)) {
            throw new BeanCreationException("dependência circular detectada em '" + name + "'");
        }

        creating.put(name, Boolean.TRUE);
        try {
            Object instance = instantiate(def.type());
            injectFields(instance);
            invokeAnnotated(instance, PostConstruct.class);
            return instance;
        } finally {
            creating.remove(name);
            if (creating.isEmpty()) {
                beansUnderConstruction.remove();
            }
        }
    }

    private Object instantiate(Class<?> type) throws Exception {
        Constructor<?>[] constructors = type.getDeclaredConstructors();

        List<Constructor<?>> injectCtors = Arrays.stream(constructors)
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .toList();

        Constructor<?> ctor;
        if (injectCtors.size() > 1) {
            throw new BeanCreationException(
                    type.getName() + " tem mais de um construtor @Inject");
        } else if (injectCtors.size() == 1) {
            ctor = injectCtors.getFirst();
        } else if (constructors.length == 1) {
            ctor = constructors[0];
        } else {
            ctor = Arrays.stream(constructors)
                    .filter(c -> c.getParameterCount() == 0)
                    .findFirst()
                    .orElseThrow(() -> new BeanCreationException(
                            "sem construtor injetável em " + type.getName()));
        }

        ctor.setAccessible(true);
        Object[] args = resolveParameters(ctor);
        return ctor.newInstance(args);
    }

    private Object[] resolveParameters(Constructor<?> ctor) {
        Class<?>[] paramTypes = ctor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = getBean(paramTypes[i]);
        }
        return args;
    }

    private void injectFields(Object instance) throws Exception {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                throw new BeanCreationException("@Inject não suportado em campo static: " + field);
            }
            field.setAccessible(true);
            field.set(instance, getBean(field.getType()));
        }
    }

    private void invokeAnnotated(Object instance, Class<? extends java.lang.annotation.Annotation> annotation)
            throws Exception {
        List<Method> methods = new ArrayList<>();
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                if (method.getParameterCount() != 0) {
                    throw new BeanCreationException(
                            annotation.getSimpleName() + " deve ser sem parâmetros: " + method);
                }
                methods.add(method);
            }
        }
        methods.sort(Comparator.comparing(Method::getName));
        for (Method method : methods) {
            method.setAccessible(true);
            method.invoke(instance);
        }
    }

    @Override
    public void close() {
        // PreDestroy na ordem inversa aproximada de criação (lista atual)
        List<Object> instances = new ArrayList<>(registry.singletonInstances());
        for (int i = instances.size() - 1; i >= 0; i--) {
            try {
                invokeAnnotated(instances.get(i), PreDestroy.class);
            } catch (Exception e) {
                throw new BeanCreationException("falha em @PreDestroy", e);
            }
        }
        registry.close();
        beansUnderConstruction.remove();
    }

    public BeanRegistry registry() {
        return registry;
    }

    private static String resolveName(String explicit, Class<?> type) {
        if (explicit != null && !explicit.isBlank()) {
            return explicit;
        }
        String simple = type.getSimpleName();
        return Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
    }
}
