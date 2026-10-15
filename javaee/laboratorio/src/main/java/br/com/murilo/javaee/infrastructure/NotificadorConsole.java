package br.com.murilo.javaee.infrastructure;

import br.com.murilo.javaee.application.Notificador;
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
