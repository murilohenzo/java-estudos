package br.com.murilo.customer.contract;

import br.com.murilo.domain.Contract;
import org.junit.Assert;
import org.junit.Test;

public class ContractTest {

    @Test
    public void createContract() {
        Contract contract = new Contract("Test", 1F);
        Assert.assertEquals("Test", contract.getName());

        contract.setName("Test2");
        contract.setValue(2F);
        Assert.assertNotEquals("Test", contract.getName());
    }
}
