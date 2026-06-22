package entities;

public class PessoaJuridica extends Pessoa{
    private String cnpj;

    public PessoaJuridica(String name) {
        super(name);
    }

    public PessoaJuridica(String name, String cnpj) {
        super(name);
        this.cnpj = cnpj;
    }

    public void cnpjIsValid() {
        if (cnpj.length() == 14) {
            System.out.println("CNPJ VALIDO");
        } else {
            System.out.println("CNPJ INVALIDO");
        }
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    @Override
    public String toString() {
        return "PessoaJuridica{" +
                "name='" + this.getName() + '\'' +
                "cnpj='" + cnpj + '\'' +
                '}';
    }
}
