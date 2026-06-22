package br.com.murilo.annotation;

import br.com.murilo.annotation.Anotacao;

@Anotacao(value = "Murilo", bairros = "Teste", numeroDaCasa = 10)
public class ClasseComAnotacao {

    @Anotacao(value = "Murilo", bairros = { "Teste", "Teste1" }, numeroDaCasa = 20, valores = 100d)
    private String name;

    @Anotacao(value = "Murilo", bairros = "Teste", numeroDaCasa = 10)
    public ClasseComAnotacao(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ClasseComAnotacao{" +
                "name='" + name + '\'' +
                '}';
    }
}
