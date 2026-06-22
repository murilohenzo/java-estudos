package main.chain;

import main.MessageType;
import main.strategies.Message;

/**
 * A classe {@code MessageHandler} representa um manipulador genérico na cadeia de responsabilidade
 * para processar mensagens de diferentes tipos. É uma classe abstrata que pode ser estendida
 * para criar manipuladores específicos de tipos de mensagem.
 */
public abstract class MessageHandler {
    private MessageHandler nextHandler;

    /**
     * Construtor padrão.
     */
    protected MessageHandler() {}

    /**
     * Construtor que aceita um próximo manipulador na cadeia.
     *
     * @param nextHandler O próximo manipulador na cadeia.
     */
    protected MessageHandler(MessageHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    /**
     * Método abstrato que deve ser implementado nas subclasses para retornar
     * o tipo de mensagem específico associado ao manipulador.
     *
     * @return O tipo de mensagem associado ao manipulador.
     */
    protected abstract MessageType getMessageType();

    /**
     * Método privado que valida se o tipo da mensagem corresponde ao tipo
     * que o manipulador pode lidar.
     *
     * @param message A mensagem a ser validada.
     * @return Verdadeiro se o tipo da mensagem for válido; falso caso contrário.
     */
    private boolean validateTypeMessage(Message message) {
        return message.getMessageType() == getMessageType();
    }

    /**
     * Método protegido que verifica se há um próximo manipulador na cadeia
     * e chama o método sendMessage no próximo manipulador.
     *
     * @param message A mensagem a ser processada.
     * @return A mensagem tratada.
     * @throws MessageHandlerException Se não houver próximo manipulador na cadeia.
     */
    protected Message checkNext(Message message) {
        if (nextHandler == null) {
            throw new MessageHandlerException("end of chain for messageType: " + message.getMessageType());
        }
        return this.nextHandler.sendMessage(message);
    }

    /**
     * Método protegido que decide se a mensagem atual deve ser tratada pelo manipulador atual
     * ou passada para o próximo na cadeia, com base na validação do tipo de mensagem.
     *
     * @param message A mensagem a ser processada.
     * @return A mensagem tratada.
     */
    public Message sendMessage(Message message) {
        if (validateTypeMessage(message)) {
            System.out.println("Handling message in class: " + this.getClass().getSimpleName());
            return message;
        } else {
            return checkNext(message);
        }
    }
}
