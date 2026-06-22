package main.strategies;

import main.MessageType;
import main.chain.MessageHandler;

/**
 * A classe {@code MessageGIFHandler} é uma implementação concreta de {@code MessageHandler}
 * para lidar com mensagens de GIF (MessageType.GIF).
 */
public class MessageGIFHandler extends MessageHandler {

    /**
     * Construtor padrão.
     */
    public MessageGIFHandler() {
        super();
    }

    /**
     * Construtor que aceita um próximo manipulador na cadeia.
     *
     * @param nextHandler O próximo manipulador na cadeia.
     */
    public MessageGIFHandler(MessageHandler nextHandler) {
        super(nextHandler);
    }

    /**
     * Implementação do método abstrato para retornar o tipo de mensagem
     * associado ao manipulador.
     *
     * @return O tipo de mensagem associado ao manipulador (MessageType.GIF).
     */
    @Override
    protected MessageType getMessageType() {
        return MessageType.GIF;
    }
}
