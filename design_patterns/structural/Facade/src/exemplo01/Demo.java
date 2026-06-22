package exemplo01;

import exemplo01.main.domain.Apartment;
import exemplo01.main.repositories.ApartmentRepositoryArrayList;
import exemplo01.main.repositories.IApartmentRepository;
import exemplo01.main.services.ApartmentService;

public class Demo {
    public static void main(String[] args) {
        IApartmentRepository apartmentRepository = new ApartmentRepositoryArrayList();
        ApartmentService apartmentService = new ApartmentService(apartmentRepository);
        apartmentService.registerApartment(new Apartment(1L, "TEST FACADE 1"));
        apartmentService.registerApartment(new Apartment(1L, "TEST FACADE 1 REPEAT"));
        apartmentService.registerApartment(new Apartment(2L, "TEST FACADE 2"));
    }
}