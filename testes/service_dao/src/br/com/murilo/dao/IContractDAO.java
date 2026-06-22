package br.com.murilo.dao;

import br.com.murilo.domain.Contract;

public interface IContractDAO {
    public String save();
    public Contract find(int id);
    public void remove(int id);
    public Contract update(Contract contract);
}
