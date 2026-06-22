package br.com.murilo.dao.implement;

import br.com.murilo.dao.IContractDAO;
import br.com.murilo.domain.Contract;

public class ContractDAO implements IContractDAO {

    public static final String MESSAGE = "Need database configuration";

    @Override
    public String save() {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public Contract find(int id) {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public void remove(int id) {
        throw new UnsupportedOperationException(MESSAGE);

    }

    @Override
    public Contract update(Contract customer) {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
