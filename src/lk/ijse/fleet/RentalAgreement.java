package lk.ijse.fleet;

public class RentalAgreement {

    private static int idCounter = 1000;

    private final String agreementId;
    private final Vehicle vehicle;
    private final Customer customer;
    private final int rentalDays;
    private final double subtotal;
    private final double totalCost;
    private int daysElapsed;

    public RentalAgreement(Vehicle vehicle, Customer customer, int rentalDays) {

        idCounter++;
        this.agreementId = String.format("AGR-%04d", idCounter);

        this.vehicle = vehicle;
        this.customer = customer;
        this.rentalDays = rentalDays;

        this.subtotal = vehicle.calculateRentalCost(rentalDays);
        this.totalCost = customer.applyDiscount(subtotal);

        this.daysElapsed = 0;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public int getDaysElapsed() {
        return daysElapsed;
    }

    public void setDaysElapsed(int daysElapsed) {
        this.daysElapsed = daysElapsed;
    }

    @Override
    public String toString() {
        return "RentalAgreement{"
                + "agreementId='" + agreementId + '\''
                + ", vehicle=" + vehicle.getVehicleId()
                + ", customer=" + customer.getCustomerId()
                + ", rentalDays=" + rentalDays
                + ", subtotal=" + subtotal
                + ", totalCost=" + totalCost
                + ", daysElapsed=" + daysElapsed
                + '}';
    }
}