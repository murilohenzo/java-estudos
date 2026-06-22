/*
* O Abstract Factory é um padrão de projeto criacional que
* permite que você produza famílias de objetos relacionados
* sem ter que especificar suas classes concretas.
* */

/*
* 1.Produtos Abstratos declaram interfaces para um conjunto de
* produtos distintos mas relacionados que fazem parte de uma
* família de produtos.
* 2. Produtos Concretos são várias implementações de produtos
* abstratos, agrupados por variantes. Cada produto abstrato (cadeira/sofá) deve ser implementado em todas as variantes
* dadas (Vitoriano/Moderno).
* 3. A interface Fábrica Abstrata declara um conjunto de métodos
*  para criação de cada um dos produtos abstratos.
* 4. Fábricas Concretas implementam métodos de criação fábrica
* abstratos. Cada fábrica concreta corresponde a uma variante
* específica de produtos e cria apenas aquelas variantes de produto.
* 5. Embora fábricas concretas instanciam produtos concretos, assinaturas dos seus métodos de criação devem retornar produtos abstratos correspondentes. Dessa forma o código cliente
* que usa uma fábrica não fica ligada a variante específica do
* produto que ele pegou de uma fábrica. O Cliente pode trabalhar com qualquer variante de produto/fábrica concreto, desde
* que ele se comunique com seus objetos via interfaces abstratas.
* */

import exemplo01.domain.Car;
import exemplo01.domain.Customer;
import factories.ContratosFactory;
import factories.IFactory;
import factories.SemContratosFactory;

public class Main {
    public static void main(String[] args) {
        Customer customer = new Customer("A", false);
        IFactory factory = getFactory(customer);
        Car car = factory.create(customer.getRequest());
        car.startEngine();
        car.clean();
        car.mechanicCheck();
    }

    private static IFactory getFactory(Customer customer) {
        if (customer.hasCompanyContract()) {
            return new ContratosFactory(customer.getRequest());
        }
        return new SemContratosFactory(customer.getRequest());
    }
}