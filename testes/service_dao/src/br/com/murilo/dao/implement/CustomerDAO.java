package br.com.murilo.dao.implement;

import br.com.murilo.dao.ICustomerDAO;

public class CustomerDAO implements ICustomerDAO {
    @Override
    public String save() {
        throw new UnsupportedOperationException("Need database configuration");
    }
}
