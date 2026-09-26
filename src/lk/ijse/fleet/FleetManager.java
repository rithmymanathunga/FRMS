package lk.ijse.fleet;

import java.util.ArrayList;
import java.util.List;

public class FleetManager {

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<RentalAgreement> agreements = new ArrayList<>();

    private RentalEventListener rentalEventListener;

    public void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    public void addCustomer(Customer c) {
        customers.add(c);
    }

    public Vehicle findVehicleById(String id) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getVehicleId().equals(id)) {
                return vehicle;
            }
        }

        return null;
    }

    public Customer findCustomerById(String id) {
        for (Customer customer : customers) {
            if (customer.getCustomerId().equals(id)) {
                return customer;
            }
        }

        return null;
    }

    public void setRentalEventListener(RentalEventListener listener) {
        this.rentalEventListener = listener;
    }

    public List<Vehicle> getAvailableVehicles() {
        List<Vehicle> availableVehicles = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            if (vehicle.getStatus() == RentalStatus.AVAILABLE) {
                availableVehicles.add(vehicle);
            }
        }

        return availableVehicles;
    }

    public RentalAgreement createRentalAgreement(
            String vehicleId,
            String customerId,
            int rentalDays)
            throws VehicleNotAvailableException, InsufficientCreditException {

        Vehicle vehicle = findVehicleById(vehicleId);

        if (vehicle == null) {
            System.out.println("Vehicle not found");
            return null;
        }

        if (vehicle.getStatus() != RentalStatus.AVAILABLE) {
            throw new VehicleNotAvailableException(
                    "Vehicle is not available for rental"
            );
        }

        Customer customer = findCustomerById(customerId);

        if (customer == null) {
            System.out.println("Customer not found");
            return null;
        }

        double subtotal = vehicle.calculateRentalCost(rentalDays);
        double cost = customer.applyDiscount(subtotal);

        if (customer instanceof CorporateCustomer corporateCustomer) {

            if (!corporateCustomer.hasSufficientCredit(cost)) {
                throw new InsufficientCreditException(
                        "Insufficient credit for this rental"
                );
            }
        }

        RentalAgreement agreement =
                new RentalAgreement(vehicle, customer, rentalDays);

        vehicle.setStatus(RentalStatus.RENTED);

        agreements.add(agreement);

        if (rentalEventListener != null) {
            rentalEventListener.onRentalCreated(agreement);
        }

        return agreement;
    }

    public void returnVehicle(String agreementId) {

        for (RentalAgreement agreement : agreements) {

            if (agreement.getAgreementId().equals(agreementId)) {
                agreement.getVehicle().setStatus(RentalStatus.AVAILABLE);

                System.out.println(
                        "Vehicle returned successfully"
                );

                return;
            }
        }

        System.out.println("Rental agreement not found");
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    public List<RentalAgreement> getAllAgreements() {
        return new ArrayList<>(agreements);
    }
}