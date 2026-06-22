import entities.Pessoa;
import entities.PessoaFisica;
import entities.PessoaJuridica;

public class Main {
    public static void main(String[] args) {
        PessoaFisica pessoaFisica = new PessoaFisica("Murilo", "11122");
        PessoaJuridica pessoaJuridica = new PessoaJuridica("Henzo", "111220004");

        System.out.println("***** TESTANDO CPF/CNPJ *****");

        System.out.println(pessoaFisica);
        pessoaFisica.cpfIsValid();
        System.out.println(pessoaJuridica);
        pessoaJuridica.cnpjIsValid();
        System.out.println("***** SETANDO NOVOS VALORES PARA CPF/CNPJ *****");
        pessoaFisica.setCpf("11111111111");
        pessoaFisica.cpfIsValid();
        pessoaJuridica.setCnpj("11111111111111");
        pessoaJuridica.cnpjIsValid();
    }
}