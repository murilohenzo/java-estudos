package exemplo01.main.repositories;

import exemplo01.main.domain.Apartment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ApartmentRepositoryArrayList implements  IApartmentRepository{
    private final List<Apartment> apartments = new ArrayList<>();

    @Override
    public void create(Apartment apartment) {
        apartments.add(apartment);
    }

    @Override
    public Collection<Apartment> findAll() {
        return apartments;
    }

    @Override
    public Apartment findByCode(Long code) {
        return  apartments.stream().filter(apartment -> Objects.equals(apartment.getCode(), code)).findFirst().orElse(null);
    }
}
