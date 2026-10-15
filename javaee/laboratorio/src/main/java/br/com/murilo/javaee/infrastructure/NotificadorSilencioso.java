package br.com.murilo.javaee.infrastructure;

import br.com.murilo.javaee.application.Notificador;
import jakarta.enterprise.context.ApplicationScoped;

@Simulado
@ApplicationScoped
public class NotificadorSilencioso implements Notificador {
    @Override
    public void informar(String mensagem) {
        // Implementação simulada para demonstrar CDI @Qualifier.
    }
}
