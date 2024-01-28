package director;

import builders.Builder;
import builders.BurgerBuilder;
import burgers.BurgerType;


/**
 * Director define a ordem das etapas de construção. Funciona com um objeto construtor
 * através da interface comum do Builder. Portanto, pode não saber qual é o produto
 * em construção.
 */
public class Director {
    BurgerBuilder burgerBuilder;

    public void constructorVeganBurger(Builder builder) {
        builder.setBurgerType(BurgerType.VEGAN_BURGER);
        builder.setBun("Vegan White Bread");
        builder.setMeat("Vegan Beef");
        builder.setSalad("Vegan Iceberg");
        builder.setCheese("Vegan American Cheese");
        builder.setSauce("Vegan Secret Sauce");
    }

    public void constructorCheeseBurger(Builder builder) {
        builder.setBurgerType(BurgerType.CHEESE_BURGER);
        builder.setBun("White Bread");
        builder.setMeat("Beef");
        builder.setSalad("Iceberg");
        builder.setCheese("American Cheese");
        builder.setSauce("Secret Sauce");
    }
}
