package br.com.murilo.ioc25.container;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Registry thread-safe que encapsula definições e instâncias singleton.
 *
 * <h2>Garantias de concorrência</h2>
 * <ul>
 *   <li>{@link ConcurrentHashMap} para definições e cache de singletons</li>
 *   <li>Registro rejeitado após {@link #freeze()} (metadados imutáveis)</li>
 *   <li>Singletons com double-checked locking (não usa {@code computeIfAbsent}
 *       porque a factory cria dependências e publicaria no mesmo map)</li>
 *   <li>Shutdown idempotente com {@link AtomicBoolean}</li>
 * </ul>
 */
public final class BeanRegistry {

    private final ConcurrentHashMap<String, BeanDefinition> definitions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, String> typeIndex = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> singletons = new ConcurrentHashMap<>();

    /** Monitor dedicado à criação — evita publicar half-built beans sob corrida. */
    private final Object singletonCreationLock = new Object();

    private final AtomicBoolean frozen = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public void register(BeanDefinition definition) {
        ensureOpen();
        if (frozen.get()) {
            throw new IllegalStateException("registry congelado — não aceita novos beans");
        }
        Objects.requireNonNull(definition, "definition");

        BeanDefinition previous = definitions.putIfAbsent(definition.name(), definition);
        if (previous != null) {
            throw new IllegalStateException("bean já registrado: " + definition.name());
        }

        String existingName = typeIndex.putIfAbsent(definition.type(), definition.name());
        if (existingName != null) {
            definitions.remove(definition.name(), definition);
            throw new IllegalStateException(
                    "tipo já registrado como '" + existingName + "': " + definition.type().getName());
        }
    }

    public void freeze() {
        ensureOpen();
        frozen.set(true);
    }

    public boolean isFrozen() {
        return frozen.get();
    }

    public Optional<BeanDefinition> findByName(String name) {
        return Optional.ofNullable(definitions.get(name));
    }

    public Optional<BeanDefinition> findByType(Class<?> type) {
        String name = typeIndex.get(type);
        if (name != null) {
            return Optional.ofNullable(definitions.get(name));
        }
        return definitions.values().stream()
                .filter(def -> type.isAssignableFrom(def.type()))
                .findFirst();
    }

    public Collection<BeanDefinition> definitions() {
        return definitions.values();
    }

    @SuppressWarnings("unchecked")
    public <T> T getSingleton(String name) {
        return (T) singletons.get(name);
    }

    /**
     * Obtém ou cria singleton de forma thread-safe.
     * Double-checked locking permite que a factory registre dependências no mesmo map.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCreateSingleton(String name, BeanFactory factory) {
        ensureOpen();
        Object cached = singletons.get(name);
        if (cached != null) {
            return (T) cached;
        }
        synchronized (singletonCreationLock) {
            cached = singletons.get(name);
            if (cached != null) {
                return (T) cached;
            }
            try {
                Object created = factory.create(name);
                singletons.put(name, created);
                return (T) created;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new BeanCreationException("falha ao criar singleton '" + name + "'", e);
            }
        }
    }

    public boolean hasSingleton(String name) {
        return singletons.containsKey(name);
    }

    public Collection<Object> singletonInstances() {
        return singletons.values();
    }

    public void close() {
        if (closed.compareAndSet(false, true)) {
            singletons.clear();
        }
    }

    public boolean isClosed() {
        return closed.get();
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("registry fechado");
        }
    }

    @FunctionalInterface
    public interface BeanFactory {
        Object create(String beanName) throws Exception;
    }
}
