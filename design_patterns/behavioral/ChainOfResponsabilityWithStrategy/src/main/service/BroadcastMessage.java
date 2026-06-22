package main.service;

import main.chain.MessageHandler;
import main.strategies.Message;
import java.util.ArrayList;
import java.util.List;

/**
 * A classe {@code BroadcastMessage} representa um serviço de transmissão que utiliza
 * uma cadeia de manipuladores ({@code MessageHandler}) para processar e enviar mensagens.
 */
public class BroadcastMessage {
    private final List<Message> messages;
    private final MessageHandler messageHandler;

    /**
     * Construtor que aceita um manipulador de mensagens para ser utilizado durante a transmissão.
     *
     * @param messageHandler O manipulador de mensagens.
     */
    public BroadcastMessage(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        this.messages = new ArrayList<>();
    }

    /**
     * Adiciona uma mensagem à lista de mensagens a serem transmitidas.
     *
     * @param message A mensagem a ser adicionada.
     */
    public void addMessage(Message message) {
        this.messages.add(message);
    }

    /**
     * Envia todas as mensagens utilizando o manipulador de mensagens fornecido.
     * Para cada mensagem, imprime informações relacionadas ao envio.
     */
    public void sendAllMessages() {
        for (Message message : this.messages) {
            var handledMessage = this.messageHandler.sendMessage(message);
            System.out.println("Sent Message:");
            System.out.println(handledMessage);
            System.out.println("--------------\n");
        }
    }
}
