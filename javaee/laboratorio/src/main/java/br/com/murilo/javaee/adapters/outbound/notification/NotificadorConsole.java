package br.com.murilo.javaee.adapters.outbound.notification;
import br.com.murilo.javaee.application.port.outbound.Operacional;

import br.com.murilo.javaee.application.port.outbound.Notificador;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

@Operacional
@ApplicationScoped
public class NotificadorConsole implements Notificador {
    private static final Logger LOG = Logger.getLogger(NotificadorConsole.class.getName());

    @Override
    public void informar(String mensagem) {
        LOG.info(mensagem);
    }
}
