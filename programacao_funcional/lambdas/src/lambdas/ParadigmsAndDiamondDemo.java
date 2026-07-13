package lambdas;

import java.util.function.Function;

/**
 * Compara reuso por herança (risco do diamante em defaults de interface)
 * com composição funcional (sem ambiguidade).
 */
public class ParadigmsAndDiamondDemo {

    interface Fly {
        default String move() {
            return "fly";
        }
    }

    interface Swim {
        default String move() {
            return "swim";
        }
    }

    /** Resolve o diamante de default methods explicitamente. */
    static class Duck implements Fly, Swim {
        @Override
        public String move() {
            return Fly.super.move() + " & " + Swim.super.move();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== OO vs Funcional · Problema do diamante ===\n");

        System.out.println("--- Diamante de interface (defaults) ---");
        System.out.println("Duck.move() = " + new Duck().move());

        System.out.println("\n--- Composição funcional (sem diamante) ---");
        Function<String, String> fly = s -> s + "→fly";
        Function<String, String> swim = s -> s + "→swim";
        Function<String, String> duck = fly.andThen(swim);
        System.out.println(duck.apply("duck"));

        System.out.println("""

            Resumo:
            - Herança múltipla de classes: Java não permite (evita diamante clássico).
            - Defaults conflitantes: a classe deve sobrescrever.
            - Funções compostas: ordem explícita, sem ambiguidade de herança.
            """);
    }
}
