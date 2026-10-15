package br.com.murilo.javaee.adapters.outbound.notification;
import br.com.murilo.javaee.application.port.outbound.Simulado;

import br.com.murilo.javaee.application.port.outbound.Notificador;
import jakarta.enterprise.context.ApplicationScoped;

@Simulado
@ApplicationScoped
public class NotificadorSilencioso implements Notificador {
    @Override
    public void informar(String mensagem) {
        // Implementação simulada para demonstrar CDI @Qualifier.
    }
}
