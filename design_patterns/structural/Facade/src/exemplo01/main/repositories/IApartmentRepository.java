package exemplo01.main.repositories;

import exemplo01.main.domain.Apartment;
import java.util.Collection;


public interface IApartmentRepository {
    public void create(Apartment apartment);
    public Collection<Apartment> findAll();
    public Apartment findByCode(Long code);
}
