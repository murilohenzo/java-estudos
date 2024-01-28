import builders.BurgerBuilder;
import burgers.Burger;
import director.Director;

public class Main {
    public static void main(String[] args) {
        Director director = new Director();

        // Director obtém o objeto construtor concreto do cliente
        // (código do aplicativo). Isso porque o aplicativo sabe melhor qual
        // construtor a ser usado para obter um produto específico.
        BurgerBuilder builder = new BurgerBuilder();
        director.constructorVeganBurger(builder);


        // O produto final geralmente é recuperado de um objeto construtor, pois
        // Director não tem conhecimento e não depende de construtores concreto e
        // produtos.
        Burger veganBurger = builder.getResult();

        System.out.println("Burger build:\n" + veganBurger);

        director.constructorCheeseBurger(builder);
        Burger cheeseBurger = builder.getResult();

        System.out.println("Burger build:\n" + cheeseBurger);
    }
}