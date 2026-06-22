package br.com.murilo.exceptions;


import javax.swing.*;

public class APPClient {
    public static void main(String[] args) {
        String opcao = JOptionPane.showInputDialog(null,
                "Enter customer code",
                "", JOptionPane.INFORMATION_MESSAGE);

        ClientService clientService = new ClientService(new ClientRepository());

        try {
            clientService.findClientById(opcao);
        } catch (ClientNotFoundException exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Exit", JOptionPane.INFORMATION_MESSAGE);
        }
    }}
