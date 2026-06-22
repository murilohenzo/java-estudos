package entities;

public class PessoaFisica extends Pessoa{
    private String cpf;

    public PessoaFisica(String name) {
        super(name);
    }

    public PessoaFisica(String name, String cpf) {
        super(name);
        this.cpf = cpf;
    }

    public void cpfIsValid() {
        if (cpf.length() == 11) {
            System.out.println("CPF VALIDO");
        } else {
            System.out.println("CPF INVALIDO");
        }
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
            this.cpf = cpf;
    }

    @Override
    public String toString() {
        return "PessoaFisica{" +
                "name='" + this.getName() + '\'' +
                "cpf='" + cpf + '\'' +
                '}';
    }
}
