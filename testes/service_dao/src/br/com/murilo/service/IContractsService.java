package br.com.murilo.service;

import br.com.murilo.domain.Contract;

public interface IContractsService {
    String save();
    public Contract find(int id);
    public void remove(int id);
    public Contract update(Contract customer);
}
