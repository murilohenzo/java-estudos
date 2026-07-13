package br.com.murilo.reflectmeta;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Espelho estrutural da API {@code reflect-metadata} (TypeScript).
 *
 * <pre>
 * WeakMap&lt;target, Map&lt;propertyKey?, Map&lt;metadataKey, value&gt;&gt;&gt;
 * </pre>
 *
 * Em Java usamos identidade do {@link Class} / {@link AnnotatedMemberKey} no lugar de
 * objetos JS + WeakMap (GC via weak keys não é idêntico; aqui o mapa é forte e
 * educacional — escopo de estudo, não produção).
 */
public final class MetadataReflect {

    private MetadataReflect() {}

    /** Chave de membro: classe + nome de membro opcional (null = metadado na classe). */
    public record MemberKey(Class<?> target, String propertyKey) {
        public MemberKey {
            Objects.requireNonNull(target, "target");
        }

        public static MemberKey of(Class<?> target) {
            return new MemberKey(target, null);
        }

        public static MemberKey of(Class<?> target, String propertyKey) {
            return new MemberKey(target, propertyKey);
        }
    }

    private static final ConcurrentHashMap<MemberKey, ConcurrentHashMap<Object, Object>> STORE =
            new ConcurrentHashMap<>();

    public static void defineMetadata(Object metadataKey, Object metadataValue, Class<?> target) {
        defineMetadata(metadataKey, metadataValue, MemberKey.of(target));
    }

    public static void defineMetadata(
            Object metadataKey, Object metadataValue, Class<?> target, String propertyKey) {
        defineMetadata(metadataKey, metadataValue, MemberKey.of(target, propertyKey));
    }

    public static void defineMetadata(Object metadataKey, Object metadataValue, MemberKey member) {
        Objects.requireNonNull(metadataKey, "metadataKey");
        Objects.requireNonNull(member, "member");
        STORE.computeIfAbsent(member, key -> new ConcurrentHashMap<>())
                .put(metadataKey, metadataValue);
    }

    public static boolean hasOwnMetadata(Object metadataKey, Class<?> target) {
        return hasOwnMetadata(metadataKey, MemberKey.of(target));
    }

    public static boolean hasOwnMetadata(Object metadataKey, Class<?> target, String propertyKey) {
        return hasOwnMetadata(metadataKey, MemberKey.of(target, propertyKey));
    }

    public static boolean hasOwnMetadata(Object metadataKey, MemberKey member) {
        Map<Object, Object> map = STORE.get(member);
        return map != null && map.containsKey(metadataKey);
    }

    public static boolean hasMetadata(Object metadataKey, Class<?> target) {
        return getMetadata(metadataKey, target).isPresent();
    }

    public static boolean hasMetadata(Object metadataKey, Class<?> target, String propertyKey) {
        return getMetadata(metadataKey, target, propertyKey).isPresent();
    }

    public static Optional<Object> getOwnMetadata(Object metadataKey, Class<?> target) {
        return getOwnMetadata(metadataKey, MemberKey.of(target));
    }

    public static Optional<Object> getOwnMetadata(
            Object metadataKey, Class<?> target, String propertyKey) {
        return getOwnMetadata(metadataKey, MemberKey.of(target, propertyKey));
    }

    public static Optional<Object> getOwnMetadata(Object metadataKey, MemberKey member) {
        Map<Object, Object> map = STORE.get(member);
        if (map == null) return Optional.empty();
        return Optional.ofNullable(map.get(metadataKey));
    }

    /** Sobe a hierarquia de classes (análogo à prototype chain). */
    public static Optional<Object> getMetadata(Object metadataKey, Class<?> target) {
        Class<?> current = target;
        while (current != null) {
            Optional<Object> own = getOwnMetadata(metadataKey, current);
            if (own.isPresent()) return own;
            current = current.getSuperclass();
        }
        return Optional.empty();
    }

    public static Optional<Object> getMetadata(
            Object metadataKey, Class<?> target, String propertyKey) {
        Class<?> current = target;
        while (current != null) {
            Optional<Object> own = getOwnMetadata(metadataKey, current, propertyKey);
            if (own.isPresent()) return own;
            current = current.getSuperclass();
        }
        return Optional.empty();
    }

    public static Set<Object> getOwnMetadataKeys(Class<?> target) {
        return getOwnMetadataKeys(MemberKey.of(target));
    }

    public static Set<Object> getOwnMetadataKeys(Class<?> target, String propertyKey) {
        return getOwnMetadataKeys(MemberKey.of(target, propertyKey));
    }

    public static Set<Object> getOwnMetadataKeys(MemberKey member) {
        Map<Object, Object> map = STORE.get(member);
        if (map == null) return Set.of();
        return Set.copyOf(map.keySet());
    }

    public static Set<Object> getMetadataKeys(Class<?> target) {
        Set<Object> keys = new LinkedHashSet<>();
        Class<?> current = target;
        while (current != null) {
            keys.addAll(getOwnMetadataKeys(current));
            current = current.getSuperclass();
        }
        return Collections.unmodifiableSet(keys);
    }

    public static boolean deleteMetadata(Object metadataKey, Class<?> target) {
        return deleteMetadata(metadataKey, MemberKey.of(target));
    }

    public static boolean deleteMetadata(Object metadataKey, Class<?> target, String propertyKey) {
        return deleteMetadata(metadataKey, MemberKey.of(target, propertyKey));
    }

    public static boolean deleteMetadata(Object metadataKey, MemberKey member) {
        Map<Object, Object> map = STORE.get(member);
        if (map == null) return false;
        boolean removed = map.remove(metadataKey) != null;
        if (map.isEmpty()) STORE.remove(member, map);
        return removed;
    }

    /** Limpa o store (apenas para demos/testes). */
    public static void clearAll() {
        STORE.clear();
    }
}
