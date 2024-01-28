package exemplo01.main.services;

import exemplo01.main.domain.Apartment;
import exemplo01.main.repositories.IApartmentRepository;
public class ApartmentService implements IApartmentService{
    private final IApartmentRepository apartmentRepository;

    public ApartmentService(IApartmentRepository apartmentRepository) {
        this.apartmentRepository = apartmentRepository;
    }

    @Override
    public Boolean registerApartment(Apartment apartment) {
        Boolean existApartment = existApartment(apartment);
        Boolean isValidFields = isValidFields(apartment);
        if (Boolean.TRUE.equals(existApartment) && Boolean.FALSE.equals(isValidFields)) {
            return false;
        } else if (Boolean.FALSE.equals(existApartment) && Boolean.TRUE.equals(isValidFields)) {
            this.apartmentRepository.create(apartment);
            print();
            return true;
        } else {
            return  false;
        }
    }

    private Boolean existApartment(Apartment apartment) {
        return this.apartmentRepository.findByCode(apartment.getCode()) != null;
    }

    private Boolean isValidFields(Apartment apartment) {
        return apartment.getCode() != null && apartment.getAddress() != null;
    }

    private void print() {
        System.out.println(this.apartmentRepository.findAll());
    }
}
