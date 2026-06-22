package application;

import domain.Client;
import lib.LoggerCtx;
import repository.ClientMapRepositoryInMemory;
import repository.IClientsRepository;
import useCases.CreateClientUseCase;
import useCases.DeleteClientUseCase;
import useCases.SearchClientUseCase;
import useCases.UpdateClientUseCase;

import javax.swing.*;
import java.util.logging.Level;

public class App {
    public static final String SUCCESSFUL_ACTION = "Successful Action";
    public static final String SUCCESS = "Success";
    public static final String ERROR = "Error";
    public static final String INPUT_CPF = "Enter the CPF";
    public static final String UPDATE_CLIENT = "Update client";
    public static final String CLIENT_NOT_FOUND = "Client not found";
    private static IClientsRepository I_CLIENTS_REPOSITORY;

    public static void main(String[] args) {
        I_CLIENTS_REPOSITORY = new ClientMapRepositoryInMemory();

        String option = JOptionPane.showInputDialog(null,
                "Enter 1 to register, 2 to consult, 3 to delete, 4 to change or 5 to exit",
                "Menu", JOptionPane.INFORMATION_MESSAGE);

        while (!isValidOption(option)) {
            if ("".equals(option)) {
                exit();
            }
            option = JOptionPane.showInputDialog(null,
                    "Invalid option Enter 1 to register, 2 to consult, 3 to delete, 4 to change or 5 to exit",
                    "Menu", JOptionPane.INFORMATION_MESSAGE);
        }

        while (isValidOption(option)) {
            if (isExitOption(option)) {
                exit();
            } else if (isCreateOption(option)) {
                String data = JOptionPane.showInputDialog(null,
                        "Enter customer data separated by commas, as example: Name, CPF, Telephone, Address, Number, City and State",
                        "Create Client", JOptionPane.INFORMATION_MESSAGE);
                create(data);
            } else if (isSearchOption(option)) {
                String data = JOptionPane.showInputDialog(null,
                        INPUT_CPF,
                        "Search Client", JOptionPane.INFORMATION_MESSAGE);
                search(data);
            } else if (isDeleteOption(option)) {
                String data = JOptionPane.showInputDialog(null,
                        INPUT_CPF,
                        "Delete Client", JOptionPane.INFORMATION_MESSAGE);
                delete(data);
            } else if (isUpdateOption(option)) {
                String data = JOptionPane.showInputDialog(null,
                        INPUT_CPF,
                        "Search Client", JOptionPane.INFORMATION_MESSAGE);
                update(data);
            }

            option = JOptionPane.showInputDialog(null,
                    "Enter 1 to register, 2 to consult, 3 to delete, 4 to change or 5 to exit",
                    "Menu", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /*
    * OPCOES DE CRUD
    * */
    private static boolean isValidOption(String option) {
        return "1".equals(option) || "2".equals(option) || "3".equals(option) || "4".equals(option) || "5".equals(option);
    }

    private static boolean isCreateOption(String option) {
        return "1".equals(option);
    }

    private static boolean isSearchOption(String option) {
        return "2".equals(option);
    }

    private static boolean isDeleteOption(String option) {
        return "3".equals(option);
    }

    private static boolean isUpdateOption(String option) {
        return "4".equals(option);
    }

    private static boolean isExitOption(String option) {
        return "5".equals(option);
    }

    /*
    * OPERACOES DE CRUD
    * */

    private static void create(String data) {
        String[] splitData = data.split(",");
        CreateClientUseCase createClientUseCase = new CreateClientUseCase(I_CLIENTS_REPOSITORY);

        if (Boolean.TRUE.equals(clientCreated(splitData, createClientUseCase))) {
            LoggerCtx.createLogger(Level.INFO, CreateClientUseCase.class.getName(), "Create client", SUCCESSFUL_ACTION);
            JOptionPane.showMessageDialog(null, "Client registered successfully", SUCCESS, JOptionPane.INFORMATION_MESSAGE);
        } else {
            LoggerCtx.createLogger(Level.INFO, CreateClientUseCase.class.getName(), "Create client", "Already exists client in database");
            JOptionPane.showMessageDialog(null, "Already exists client", ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void search(String data) {
        SearchClientUseCase searchClientUseCase = new SearchClientUseCase(I_CLIENTS_REPOSITORY);
        Client client = searchClientUseCase.execute(Long.parseLong(data));
        if (client != null) {
            LoggerCtx.createLogger(Level.INFO, SearchClientUseCase.class.getName(), "Search client", SUCCESSFUL_ACTION);
            JOptionPane.showMessageDialog(null, "Client found " + client, SUCCESS, JOptionPane.INFORMATION_MESSAGE);
        } else {
            LoggerCtx.createLogger(Level.INFO, SearchClientUseCase.class.getName(), "Search client", CLIENT_NOT_FOUND);
            JOptionPane.showMessageDialog(null, CLIENT_NOT_FOUND, ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void delete(String data) {
        DeleteClientUseCase deleteClientUseCase = new DeleteClientUseCase(I_CLIENTS_REPOSITORY);
        SearchClientUseCase searchClientUseCase = new SearchClientUseCase(I_CLIENTS_REPOSITORY);
        Client client = searchClientUseCase.execute(Long.parseLong(data));
        if (client != null) {
            deleteClientUseCase.execute(client.getCpf());
            LoggerCtx.createLogger(Level.INFO, DeleteClientUseCase.class.getName(), "Delete client", SUCCESSFUL_ACTION);
            JOptionPane.showMessageDialog(null, "Client deleted", SUCCESS, JOptionPane.INFORMATION_MESSAGE);
        } else {
            LoggerCtx.createLogger(Level.INFO, DeleteClientUseCase.class.getName(), "Delete client", CLIENT_NOT_FOUND);
            JOptionPane.showMessageDialog(null, CLIENT_NOT_FOUND, ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void update(String data) {
        SearchClientUseCase searchClientUseCase = new SearchClientUseCase(I_CLIENTS_REPOSITORY);
        Client existsClient = searchClientUseCase.execute(Long.parseLong(data));

        if (existsClient != null) {
            String clientBody = JOptionPane.showInputDialog(null,
                    "Enter customer data separated by commas, as example: Name, CPF, Telephone, Address, Number, City and State",
                    "Update Client", JOptionPane.INFORMATION_MESSAGE);

            UpdateClientUseCase updateClientUseCase = new UpdateClientUseCase(I_CLIENTS_REPOSITORY);
            Client client = clientUpdated(clientBody.split(","));

            if (client != null) {
                updateClientUseCase.execute(existsClient.getCpf(), client);
                LoggerCtx.createLogger(Level.INFO, UpdateClientUseCase.class.getName(), UPDATE_CLIENT, SUCCESSFUL_ACTION);
                JOptionPane.showMessageDialog(null, "Client successfully updated", SUCCESS, JOptionPane.INFORMATION_MESSAGE);
            } else {
                LoggerCtx.createLogger(Level.INFO, UpdateClientUseCase.class.getName(), UPDATE_CLIENT, "Error updating client");
                JOptionPane.showMessageDialog(null, "Error updating client", ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } else {
            LoggerCtx.createLogger(Level.INFO, UpdateClientUseCase.class.getName(), UPDATE_CLIENT, "Not exists client in database");
            JOptionPane.showMessageDialog(null, CLIENT_NOT_FOUND, ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void exit() {
        JOptionPane.showMessageDialog(null, "Bye", SUCCESS, JOptionPane.WARNING_MESSAGE);
        System.exit(0);
    }

    /*
    * VALIDACOES DE DADOS
    * */
    private static Boolean clientCreated(String[] splitData, CreateClientUseCase createClientUseCase) {
        switch (splitData.length) {
            case 7: {
                return createClientUseCase.execute(new Client(splitData[0], splitData[1], splitData[2], splitData[3], splitData[4], splitData[5], splitData[6]));
            }
            case 6: {
                return createClientUseCase.execute(new Client(splitData[0], splitData[1], splitData[2], splitData[3], splitData[4], splitData[5]));
            }
            case 5: {
                return createClientUseCase.execute(new Client(splitData[0], splitData[1], splitData[2], splitData[3], splitData[4]));
            }
            case 4: {
                return createClientUseCase.execute(new Client(splitData[0], splitData[1], splitData[2], splitData[3]));
            }
            case 3: {
                return createClientUseCase.execute(new Client(splitData[0], splitData[1], splitData[2]));
            }
            case 2: {
                return createClientUseCase.execute(new Client(splitData[0], splitData[1]));
            }
            default: {
                return false;
            }
        }
    }
    private static Client clientUpdated(String[] splitData) {
        switch (splitData.length) {
            case 7: {
                return new Client(splitData[0], splitData[1], splitData[2], splitData[3], splitData[4], splitData[5], splitData[6]);
            }
            case 6: {
                return new Client(splitData[0], splitData[1], splitData[2], splitData[3], splitData[4], splitData[5]);
            }
            case 5: {
                return new Client(splitData[0], splitData[1], splitData[2], splitData[3], splitData[4]);
            }
            case 4: {
                return new Client(splitData[0], splitData[1], splitData[2], splitData[3]);
            }
            case 3: {
                return new Client(splitData[0], splitData[1], splitData[2]);
            }
            case 2: {
                return new Client(splitData[0], splitData[1]);
            }
            case 1: {
                return new Client(splitData[0]);
            }
            default: {
                return null;
            }
        }
    }


}
