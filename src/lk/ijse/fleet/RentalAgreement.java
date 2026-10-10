package lk.ijse.fleet;

public class RentalAgreement {

    private static int idCounter = 1000;

    private final String agreementId;
    private final Vehicle vehicle;
    private final Customer customer;
    private final int rentalDays;
    private final double totalCost;
    private int daysElapsed;

    public RentalAgreement(Vehicle vehicle, Customer customer, int rentalDays) {
        idCounter++;
        this.agreementId = String.format("AGR-%04d", idCounter);

        this.vehicle = vehicle;
        this.customer = customer;
        this.rentalDays = rentalDays;

        double subtotal = vehicle.calculateRentalCost(rentalDays);
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
        String line = "================================================================";
        // "IndividualCustomer" -> "Individual", "CorporateCustomer" -> "Corporate"
        String customerType = customer.getClass().getSimpleName().replace("Customer", "");

        return line + "\n"
                + "RENTAL AGREEMENT RECEIPT\n"
                + line + "\n"
                + "Agreement ID : " + agreementId + "\n"
                + "Vehicle      : " + vehicle.getVehicleId()
                + " - " + vehicle.getBrand() + " " + vehicle.getModel()
                + " (" + vehicle.getClass().getSimpleName() + ")\n"
                + "Customer     : " + customer.getCustomerId()
                + " - " + customer.getFullName()
                + " (" + customerType + ")\n"
                + "Rental Days  : " + rentalDays + "\n"
                + "Total Cost   : " + String.format("%,.2f", totalCost) + "\n"
                + line;
    }
}
