package main.strategies;

import main.MessageType;
import main.chain.MessageHandler;

/**
 * A classe {@code MessageTextHandler} é uma implementação concreta de {@code MessageHandler}
 * para lidar com mensagens de texto (MessageType.TEXT).
 */
public class MessageTextHandler extends MessageHandler {

    /**
     * Construtor padrão.
     */
    public MessageTextHandler() {
        super();
    }

    /**
     * Construtor que aceita um próximo manipulador na cadeia.
     *
     * @param nextHandler O próximo manipulador na cadeia.
     */
    public MessageTextHandler(MessageHandler nextHandler) {
        super(nextHandler);
    }

    /**
     * Implementação do método abstrato para retornar o tipo de mensagem
     * associado ao manipulador.
     *
     * @return O tipo de mensagem associado ao manipulador (MessageType.TEXT).
     */
    @Override
    protected MessageType getMessageType() {
        return MessageType.TEXT;
    }
}
