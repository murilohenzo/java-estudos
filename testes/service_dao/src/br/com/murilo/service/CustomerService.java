package br.com.murilo.service;

import br.com.murilo.dao.ICustomerDAO;

public class CustomerService {
    private final ICustomerDAO iCustomerDAO;

    public CustomerService(ICustomerDAO iCustomerDAO) {
        this.iCustomerDAO = iCustomerDAO;
    }

    public String save() {
        this.iCustomerDAO.save();
        return "Successful";
    }
}
