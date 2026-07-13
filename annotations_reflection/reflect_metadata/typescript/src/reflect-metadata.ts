/**
 * Mini Reflect Metadata — API no estilo microsoft/reflect-metadata.
 * TypeScript 5.3.3 puro (sem dependência do pacote oficial).
 *
 * Modelo de armazenamento (igual à proposta / lib):
 *   WeakMap<target, Map<propertyKey | undefined, Map<metadataKey, metadataValue>>>
 *
 * Observação: com `emitDecoratorMetadata: false` de propósito — aqui o metadado
 * é explícito (defineMetadata / @metadata), para deixar o mecanismo visível.
 */

export type MetadataKey = string | symbol | number;
export type PropertyKey = string | symbol;

type MetadataMap = Map<MetadataKey, unknown>;
type PropertyMetadataMap = Map<PropertyKey | undefined, MetadataMap>;

const metadataRegistry = new WeakMap<object, PropertyMetadataMap>();

function isObject(value: unknown): value is object {
  return (typeof value === "object" && value !== null) || typeof value === "function";
}

function ordinaryOwnMetadataKeys(
  target: object,
  propertyKey: PropertyKey | undefined
): MetadataKey[] {
  const targetMap = metadataRegistry.get(target);
  if (!targetMap) return [];
  const meta = targetMap.get(propertyKey);
  if (!meta) return [];
  return [...meta.keys()];
}

function ordinaryDefineOwnMetadata(
  metadataKey: MetadataKey,
  metadataValue: unknown,
  target: object,
  propertyKey: PropertyKey | undefined
): void {
  let targetMap = metadataRegistry.get(target);
  if (!targetMap) {
    targetMap = new Map();
    metadataRegistry.set(target, targetMap);
  }
  let meta = targetMap.get(propertyKey);
  if (!meta) {
    meta = new Map();
    targetMap.set(propertyKey, meta);
  }
  meta.set(metadataKey, metadataValue);
}

function ordinaryHasOwnMetadata(
  metadataKey: MetadataKey,
  target: object,
  propertyKey: PropertyKey | undefined
): boolean {
  const targetMap = metadataRegistry.get(target);
  if (!targetMap) return false;
  const meta = targetMap.get(propertyKey);
  return meta ? meta.has(metadataKey) : false;
}

function ordinaryGetOwnMetadata(
  metadataKey: MetadataKey,
  target: object,
  propertyKey: PropertyKey | undefined
): unknown {
  const targetMap = metadataRegistry.get(target);
  if (!targetMap) return undefined;
  const meta = targetMap.get(propertyKey);
  return meta ? meta.get(metadataKey) : undefined;
}

function ordinaryHasMetadata(
  metadataKey: MetadataKey,
  target: object,
  propertyKey: PropertyKey | undefined
): boolean {
  if (ordinaryHasOwnMetadata(metadataKey, target, propertyKey)) {
    return true;
  }
  const parent = Object.getPrototypeOf(target);
  if (parent === null) return false;
  return ordinaryHasMetadata(metadataKey, parent, propertyKey);
}

function ordinaryGetMetadata(
  metadataKey: MetadataKey,
  target: object,
  propertyKey: PropertyKey | undefined
): unknown {
  if (ordinaryHasOwnMetadata(metadataKey, target, propertyKey)) {
    return ordinaryGetOwnMetadata(metadataKey, target, propertyKey);
  }
  const parent = Object.getPrototypeOf(target);
  if (parent === null) return undefined;
  return ordinaryGetMetadata(metadataKey, parent, propertyKey);
}

function ordinaryMetadataKeys(
  target: object,
  propertyKey: PropertyKey | undefined
): MetadataKey[] {
  const own = ordinaryOwnMetadataKeys(target, propertyKey);
  const parent = Object.getPrototypeOf(target);
  if (parent === null) return own;
  const inherited = ordinaryMetadataKeys(parent, propertyKey);
  const set = new Set<MetadataKey>([...inherited, ...own]);
  return [...set];
}

function ordinaryDeleteMetadata(
  metadataKey: MetadataKey,
  target: object,
  propertyKey: PropertyKey | undefined
): boolean {
  const targetMap = metadataRegistry.get(target);
  if (!targetMap) return false;
  const meta = targetMap.get(propertyKey);
  if (!meta || !meta.has(metadataKey)) return false;
  meta.delete(metadataKey);
  if (meta.size === 0) targetMap.delete(propertyKey);
  if (targetMap.size === 0) metadataRegistry.delete(target);
  return true;
}

/** Namespace público — espelha a superfície usada pela lib oficial. */
export const ReflectMetadata = {
  defineMetadata(
    metadataKey: MetadataKey,
    metadataValue: unknown,
    target: object,
    propertyKey?: PropertyKey
  ): void {
    if (!isObject(target)) throw new TypeError("Reflect.defineMetadata called on non-object");
    ordinaryDefineOwnMetadata(metadataKey, metadataValue, target, propertyKey);
  },

  hasMetadata(
    metadataKey: MetadataKey,
    target: object,
    propertyKey?: PropertyKey
  ): boolean {
    if (!isObject(target)) throw new TypeError("Reflect.hasMetadata called on non-object");
    return ordinaryHasMetadata(metadataKey, target, propertyKey);
  },

  hasOwnMetadata(
    metadataKey: MetadataKey,
    target: object,
    propertyKey?: PropertyKey
  ): boolean {
    if (!isObject(target)) throw new TypeError("Reflect.hasOwnMetadata called on non-object");
    return ordinaryHasOwnMetadata(metadataKey, target, propertyKey);
  },

  getMetadata(
    metadataKey: MetadataKey,
    target: object,
    propertyKey?: PropertyKey
  ): unknown {
    if (!isObject(target)) throw new TypeError("Reflect.getMetadata called on non-object");
    return ordinaryGetMetadata(metadataKey, target, propertyKey);
  },

  getOwnMetadata(
    metadataKey: MetadataKey,
    target: object,
    propertyKey?: PropertyKey
  ): unknown {
    if (!isObject(target)) throw new TypeError("Reflect.getOwnMetadata called on non-object");
    return ordinaryGetOwnMetadata(metadataKey, target, propertyKey);
  },

  getMetadataKeys(target: object, propertyKey?: PropertyKey): MetadataKey[] {
    if (!isObject(target)) throw new TypeError("Reflect.getMetadataKeys called on non-object");
    return ordinaryMetadataKeys(target, propertyKey);
  },

  getOwnMetadataKeys(target: object, propertyKey?: PropertyKey): MetadataKey[] {
    if (!isObject(target)) throw new TypeError("Reflect.getOwnMetadataKeys called on non-object");
    return ordinaryOwnMetadataKeys(target, propertyKey);
  },

  deleteMetadata(
    metadataKey: MetadataKey,
    target: object,
    propertyKey?: PropertyKey
  ): boolean {
    if (!isObject(target)) throw new TypeError("Reflect.deleteMetadata called on non-object");
    return ordinaryDeleteMetadata(metadataKey, target, propertyKey);
  },

  /**
   * Decorator factory — equivalente a `@Reflect.metadata(key, value)`.
   * Funciona em class e em method/property (experimentalDecorators).
   */
  metadata(metadataKey: MetadataKey, metadataValue: unknown) {
    return (target: object, propertyKey?: PropertyKey): void => {
      if (propertyKey !== undefined) {
        ordinaryDefineOwnMetadata(metadataKey, metadataValue, target, propertyKey);
      } else {
        ordinaryDefineOwnMetadata(metadataKey, metadataValue, target, undefined);
      }
    };
  },
} as const;

/** Alias curto no estilo da lib (`Reflect.defineMetadata`). */
export const Reflect = ReflectMetadata;
