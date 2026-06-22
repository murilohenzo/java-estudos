package br.com.murilo.contract;

import br.com.murilo.dao.IContractDAO;
import br.com.murilo.dao.implement.ContractDAO;
import br.com.murilo.domain.Contract;
import br.com.murilo.service.ContractService;
import org.junit.Assert;
import org.junit.Test;

public class ContractServiceTest {
    public static final String MESSAGE = "Need database configuration";

    @Test()
    public void handleExceptionSaveContract() {
        IContractDAO contractDAO = new ContractDAO();
        ContractService contractService = new ContractService(contractDAO);
        Exception exception = Assert.assertThrows(UnsupportedOperationException.class, contractService::save);

        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(MESSAGE));
    }

    @Test
    public void handleExceptionFindContract() {
        IContractDAO contractDAO = new ContractDAO();
        ContractService contractService = new ContractService(contractDAO);
        Exception exception = Assert.assertThrows(UnsupportedOperationException.class, () -> contractService.find(1));

        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(MESSAGE));
    }

    @Test
    public void handleExceptionRemoveContract() {
        IContractDAO contractDAO = new ContractDAO();
        ContractService contractService = new ContractService(contractDAO);
        Exception exception = Assert.assertThrows(UnsupportedOperationException.class, () -> contractService.remove(1));

        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(MESSAGE));
    }

    @Test
    public void handleExceptionUpdateContract() {
        IContractDAO contractDAO = new ContractDAO();
        ContractService contractService = new ContractService(contractDAO);
        Exception exception = Assert.assertThrows(UnsupportedOperationException.class, () -> contractService.update(new Contract("Test", 1F)));

        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(MESSAGE));
    }
}