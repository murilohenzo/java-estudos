package generics_project.repository.product.implementation;

import generics_project.domain.Product;
import generics_project.repository.generic.implementation.GenericRepository;
import generics_project.repository.product.IProductsRepository;
import generics_project.singleton.SingletonMap;

import java.util.HashMap;
import java.util.Map;

public class ProductMapRepositoryInMemory extends GenericRepository<Product> implements IProductsRepository {

    public ProductMapRepositoryInMemory() {
        super();
        Map<Long, Product> tMap = (Map<Long, Product>) SingletonMap.getInstance().getMap().get(getClassType());
        if (tMap == null) {
            tMap = new HashMap<>();
            SingletonMap.getInstance().getMap().put(getClassType(), tMap);
        }
    }

    @Override
    public Class<Product> getClassType() {
        return Product.class;
    }

    @Override
    public void updateEntity(Product existsProduct, Product product) {
        existsProduct.setName(product.getName());
        existsProduct.setCode(product.getCode());
    }
}
