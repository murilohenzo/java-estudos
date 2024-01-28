package exemplo01.main.domain;

public class Apartment {
    private Long code;
    private String address;

    public Apartment(Long code, String address) {
        this.code = code;
        this.address = address;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "{Apartment " +
                "code=" + code +
                ", address='" + address + '\'' +
                '}';
    }
}
