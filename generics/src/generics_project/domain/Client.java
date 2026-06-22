package generics_project.domain;

import java.util.Objects;

public class Client implements Persistence{

    private  String name;
    private Long cpf;
    private Long phoneNumber;
    private String address;
    private Integer number;
    private String city;
    private String state;

    public Client(String name) {
        this.name = name;
    }
    public Client(String name, String cpf) {
        this.name = name;
        this.cpf = Long.valueOf(cpf.trim());
    }

    public Client(String name, String cpf, String phoneNumber) {
        this.name = name;
        this.cpf = Long.valueOf(cpf.trim());
        this.phoneNumber = Long.valueOf(phoneNumber.trim());
    }

    public Client(String name, String cpf, String phoneNumber, String address) {
        this.name = name;
        this.cpf = Long.valueOf(cpf.trim());
        this.phoneNumber = Long.valueOf(phoneNumber.trim());
        this.address = address;
    }

    public Client(String name, String cpf, String phoneNumber, String address, String number) {
        this.name = name;
        this.cpf = Long.valueOf(cpf.trim());
        this.phoneNumber = Long.valueOf(phoneNumber.trim());
        this.address = address;
        this.number = Integer.valueOf(number.trim());
    }

    public Client(String name, String cpf, String phoneNumber, String address, String number, String city) {
        this.name = name;
        this.cpf = Long.valueOf(cpf.trim());
        this.phoneNumber = Long.valueOf(phoneNumber.trim());
        this.address = address;
        this.number = Integer.valueOf(number.trim());
        this.city = city;
    }

    public Client(String name, String cpf, String phoneNumber, String address, String number, String city, String state) {
        this.name = name;
        this.cpf = Long.valueOf(cpf.trim());
        this.phoneNumber = Long.valueOf(phoneNumber.trim());
        this.address = address;
        this.number = Integer.valueOf(number.trim());
        this.city = city;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCpf() {
        return cpf;
    }

    public void setCpf(Long cpf) {
        this.cpf = cpf;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(name, client.name) && Objects.equals(cpf, client.cpf) && Objects.equals(phoneNumber, client.phoneNumber) && Objects.equals(address, client.address) && Objects.equals(number, client.number) && Objects.equals(city, client.city) && Objects.equals(state, client.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, cpf, phoneNumber, address, number, city, state);
    }

    @Override
    public String toString() {
        return "Client{" +
                "name='" + name + '\'' +
                ", cpf=" + cpf +
                ", phoneNumber=" + phoneNumber +
                ", address='" + address + '\'' +
                ", number=" + number +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                '}';
    }

    @Override
    public Long getCode() {
        return this.cpf;
    }
}
