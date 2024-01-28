package burgers;

/**
 * Burger eh um produto class.
 */
public class Burger {
    private BurgerType burgerType;
    private String bun = "No Bun";
    private String meat = "No Meat";
    private String salad = "No Salad";
    private String cheese = "No Cheese";
    private String sauce = "No Sauce";

    public Burger(BurgerType burgerType, String bun, String meat, String salad, String cheese, String sauce) {
        this.burgerType = burgerType;
        this.bun = bun;
        this.meat = meat;
        this.salad = salad;
        this.cheese = cheese;
        this.sauce = sauce;
    }

    public void setBurgerType(BurgerType burgerType) {
        this.burgerType = burgerType;
    }

    public void setBun(String bun) {
        this.bun = bun;
    }
    public void setMeat(String meat) {
        this.meat = meat;
    }
    public void setSalad(String salad) {
        this.salad = salad;
    }
    public void setCheese(String cheese) {
        this.cheese = cheese;
    }
    public void setSauce(String sauce) {
        this.sauce = sauce;
    }


    @Override
    public String toString() {
        return
                "burgerType = " + burgerType +
                ", bun =' " + bun + '\'' +
                ", meat =' " + meat + '\'' +
                ", salad =' " + salad + '\'' +
                ", cheese =' " + cheese + '\'' +
                ", sauce =' " + sauce + '\'';
    }
}
