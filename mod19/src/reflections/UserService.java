package reflections;

/**
 * Classe de exemplo com métodos anotados.
 */
public class UserService {

    /**
     * Método anotado para auditoria.
     */
    @Auditable(action = "user-login")
    public void login() {
        System.out.println("Usuário logado.");
    }

    /**
     * Método anotado para auditoria.
     */
    @Auditable(action = "user-logout")
    public void logout() {
        System.out.println("Usuário desconectado.");
    }

    /**
     * Método não anotado.
     */
    public void register() {
        System.out.println("Usuário registrado.");
    }
}
