package builders;

import burgers.Burger;
import burgers.BurgerType;

/**
 * Construtores concreto implementam etapas definidas na interface comum.
 */
public class BurgerBuilder implements Builder{

    private BurgerType burgerType;
    private String bun;
    private String meat;
    private String salad;
    private String cheese;
    private String sauce;

    @Override
    public void setBurgerType(BurgerType burgerType) {
        this.burgerType = burgerType;
    }

    @Override
    public void setBun(String bun) {
        this.bun = bun;
    }

    @Override
    public void setMeat(String meat) {
        this.meat = meat;
    }

    @Override
    public void setSalad(String salad) {
        this.salad = salad;
    }

    @Override
    public void setCheese(String cheese) {
        this.cheese = cheese;
    }

    @Override
    public void setSauce(String sauce) {
        this.sauce = sauce;
    }

    public Burger getResult() {
        return new Burger(burgerType, bun, meat, salad, cheese, sauce);
    }
}
