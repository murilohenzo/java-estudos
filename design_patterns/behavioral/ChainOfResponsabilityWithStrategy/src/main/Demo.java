package main;

import main.service.BroadcastMessage;
import main.strategies.Message;
import main.strategies.MessageGIFHandler;
import main.strategies.MessageTextHandler;

public class Demo {

    public static final String ORIGIN = "whatsapp";
    public static final String FROM = "jhon.doe1";
    public static final String MESSAGE = "hello world from whatsapp";

    public static void main(String[] args) {

        Message msg1 = new Message();
        msg1.setOrigin(ORIGIN);
        msg1.setFrom(FROM);
        msg1.setMsg(MESSAGE);
        msg1.setMessageType(MessageType.GIF);

        Message msg2 = new Message();
        msg2.setOrigin(ORIGIN);
        msg2.setFrom(FROM);
        msg2.setMsg(MESSAGE);
        msg2.setMessageType(MessageType.TEXT);

        Message msg3 = new Message();
        msg3.setOrigin(ORIGIN);
        msg3.setFrom(FROM);
        msg3.setMsg(MESSAGE);
        msg3.setMessageType(MessageType.MEME);

        MessageGIFHandler messageGIFHandler = new MessageGIFHandler();
        MessageTextHandler messageTextHandler = new MessageTextHandler(messageGIFHandler);

        BroadcastMessage broadcastMessage = new BroadcastMessage(messageTextHandler);
        broadcastMessage.addMessage(msg1);
        broadcastMessage.addMessage(msg2);
        broadcastMessage.addMessage(msg3);

        try {
            broadcastMessage.sendAllMessages();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
