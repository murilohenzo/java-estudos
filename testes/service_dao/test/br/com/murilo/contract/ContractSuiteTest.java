package br.com.murilo.contract;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ br.com.murilo.customer.contract.ContractTest.class, br.com.murilo.contract.ContractServiceTest.class })
public class ContractSuiteTest {
}
