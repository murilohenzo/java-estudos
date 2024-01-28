package main.java.br.com.murilo.dao.generic.jdbc.dao;

import main.java.br.com.murilo.domain.Client;

import java.util.List;

public interface IClientDAO {

    public Integer create(Client client) throws Exception;

    public Integer update(Client client) throws Exception;

    public Client findByCode(String codigo) throws Exception;

    public List<Client> findAll() throws Exception;

    public Integer delete(Client client) throws Exception;
}