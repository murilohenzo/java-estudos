package exemplo01.domain;

public class Customer {
    private final String request;
    private final boolean hasCompanyContract;

    public Customer(String request, boolean hasCompanyContract) {
        this.request = request;
        this.hasCompanyContract = hasCompanyContract;
    }

    public boolean hasCompanyContract() {
        return hasCompanyContract;
    }

    public String getRequest() {
        return request;
    }
}

