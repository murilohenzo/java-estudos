package builders;

import burgers.BurgerType;


/**
 * A interface do Builder define todas as formas possíveis de configurar um produto.
 */
public interface Builder {
    void setBurgerType(BurgerType burgerType);
     void setBun(String bun);
     void setMeat(String meat);
     void setSalad(String salad);
     void setCheese(String cheese);
     void setSauce(String sauce);
}
