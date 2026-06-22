package br.com.murilo.service;

import br.com.murilo.dao.IContractDAO;
import br.com.murilo.domain.Contract;

public class ContractService implements IContractsService{

    private final IContractDAO iContractDAO;

    public ContractService(IContractDAO iContractDAO) {
        this.iContractDAO = iContractDAO;
    }

    @Override
    public String save() {
        return this.iContractDAO.save();
    }

    @Override
    public Contract find(int id) {
        return this.iContractDAO.find(id);
    }

    @Override
    public void remove(int id) {
        this.iContractDAO.remove(id);
    }

    @Override
    public Contract update(Contract contract) {
        return this.iContractDAO.update(contract);
    }
}
