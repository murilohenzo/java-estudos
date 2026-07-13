import { Reflect } from "./reflect-metadata";

const DESIGN_PARAMTYPES = "design:paramtypes";
const DESIGN_TYPE = "design:type";
const INJECTABLE = "mini:injectable";

/**
 * Decorator de classe — marca o tipo como injetável (análogo a @Injectable do Nest).
 * Também registra "paramtypes" manualmente (sem emitDecoratorMetadata).
 */
function Injectable(...paramTypes: Function[]) {
  return (target: Function) => {
    Reflect.defineMetadata(INJECTABLE, true, target);
    if (paramTypes.length > 0) {
      Reflect.defineMetadata(DESIGN_PARAMTYPES, paramTypes, target);
    }
  };
}

class Logger {
  log(msg: string): void {
    console.log(`[Logger] ${msg}`);
  }
}

@Injectable(Logger)
class UserService {
  constructor(private readonly logger: Logger) {}

  greet(name: string): void {
    this.logger.log(`hello, ${name}`);
  }
}

@Reflect.metadata(DESIGN_TYPE, String)
class ConfigHolder {
  @Reflect.metadata("config:key", "app.name")
  appName = "java-estudos";
}

function resolve<T>(ctor: new (...args: never[]) => T): T {
  if (!Reflect.getMetadata(INJECTABLE, ctor)) {
    throw new Error(`${ctor.name} não é @Injectable`);
  }
  const paramTypes = (Reflect.getMetadata(DESIGN_PARAMTYPES, ctor) as Function[] | undefined) ?? [];
  const deps = paramTypes.map((Dep) => new (Dep as new () => unknown)());
  return new (ctor as new (...args: unknown[]) => T)(...deps);
}

console.log("=== Mini Reflect Metadata — TypeScript 5.3.3 ===\n");

console.log("--- define / get (classe) ---");
console.log("injectable?", Reflect.getMetadata(INJECTABLE, UserService));
console.log("paramtypes=", Reflect.getMetadata(DESIGN_PARAMTYPES, UserService));

console.log("\n--- DI manual via metadata (estilo Nest + reflect-metadata) ---");
const service = resolve(UserService);
service.greet("Murilo");

console.log("\n--- metadata em propriedade ---");
console.log(
  "config:key =",
  Reflect.getMetadata("config:key", ConfigHolder.prototype, "appName")
);
console.log(
  "design:type (classe) =",
  Reflect.getOwnMetadata(DESIGN_TYPE, ConfigHolder)
);

console.log("\n--- herança de metadata (prototype chain) ---");
class Base {
  @Reflect.metadata("role", "base")
  run(): void {}
}
class Child extends Base {
  @Reflect.metadata("role", "child")
  override run(): void {}
}
console.log("Child own role=", Reflect.getOwnMetadata("role", Child.prototype, "run"));
console.log("Child get (own wins)=", Reflect.getMetadata("role", Child.prototype, "run"));
console.log("keys=", Reflect.getMetadataKeys(Child.prototype, "run"));

console.log("\n--- deleteMetadata ---");
Reflect.deleteMetadata("role", Child.prototype, "run");
console.log("após delete, getMetadata role=", Reflect.getMetadata("role", Child.prototype, "run"));
