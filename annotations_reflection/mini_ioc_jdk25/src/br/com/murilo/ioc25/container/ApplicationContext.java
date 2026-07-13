package br.com.murilo.ioc25.container;

import br.com.murilo.ioc25.annotation.BeanScope;
import br.com.murilo.ioc25.annotation.Component;
import br.com.murilo.ioc25.annotation.Inject;
import br.com.murilo.ioc25.annotation.PostConstruct;
import br.com.murilo.ioc25.annotation.PreDestroy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mini ApplicationContext estilo Spring — <b>JDK 25</b>.
 *
 * <p>Diferença vs {@code mini_ioc} (JDK 21): detecção de dependência circular usa
 * {@link ScopedValue} (JEP 506) em vez de {@link ThreadLocal}.
 *
 * <p>O conjunto de beans em construção é <em>imutável</em> e é <em>rebound</em>
 * a cada nível de criação — padrão idiomático de Scoped Values (sem mutar estado
 * thread-local e com lifetime lexical claro). Herda automaticamente para
 * virtual threads filhas.
 */
public final class ApplicationContext implements AutoCloseable {

    /**
     * Contexto imutável: nomes dos beans cuja criação está em andamento nesta cadeia.
     * Sempre acessado via {@link ScopedValue#where(ScopedValue, Object)} + {@code call}.
     */
    private static final ScopedValue<Set<String>> BEANS_UNDER_CONSTRUCTION = ScopedValue.newInstance();

    private final BeanRegistry registry = new BeanRegistry();

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

    /**
     * Cria o bean dentro de um binding ScopedValue: o set anterior + {@code name}.
     * Se {@code name} já estiver no set ancestral → ciclo.
     */
    private Object createAndInitialize(String name) throws Exception {
        Set<String> parent = BEANS_UNDER_CONSTRUCTION.isBound()
                ? BEANS_UNDER_CONSTRUCTION.get()
                : Set.of();

        if (parent.contains(name)) {
            throw new BeanCreationException("dependência circular detectada em '" + name + "'");
        }

        Set<String> current = copyWith(parent, name);

        return ScopedValue.where(BEANS_UNDER_CONSTRUCTION, current).call(() -> {
            BeanDefinition def = registry.findByName(name)
                    .orElseThrow(() -> new BeanCreationException("definição ausente: " + name));

            Object instance = instantiate(def.type());
            injectFields(instance);
            invokeAnnotated(instance, PostConstruct.class);

            // Diagnóstico: mostra o escopo lexical atual (imutável)
            System.out.println("  [ScopedValue] criando '" + name + "' | underConstruction=" + BEANS_UNDER_CONSTRUCTION.get());
            return instance;
        });
    }

    private static Set<String> copyWith(Set<String> parent, String name) {
        Set<String> next = new HashSet<>(parent);
        next.add(name);
        return Set.copyOf(next);
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
        List<Object> instances = new ArrayList<>(registry.singletonInstances());
        for (int i = instances.size() - 1; i >= 0; i--) {
            try {
                invokeAnnotated(instances.get(i), PreDestroy.class);
            } catch (Exception e) {
                throw new BeanCreationException("falha em @PreDestroy", e);
            }
        }
        registry.close();
        // ScopedValue não precisa de remove() — binding morre com o call/run
    }

    public BeanRegistry registry() {
        return registry;
    }

    /** Expõe se o ScopedValue de criação está bound (útil em testes/demo). */
    public static boolean isCreationScopeBound() {
        return BEANS_UNDER_CONSTRUCTION.isBound();
    }

    private static String resolveName(String explicit, Class<?> type) {
        if (explicit != null && !explicit.isBlank()) {
            return explicit;
        }
        String simple = type.getSimpleName();
        return Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
    }
}
