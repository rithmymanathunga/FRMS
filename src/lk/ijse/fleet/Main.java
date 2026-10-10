package lk.ijse.fleet;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String LINE = "========================================================";

    private FleetManager fleetManager;
    private UserManager userManager;
    private Scanner scanner;
    private SystemUser loggedInUser;

    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }

    public void run() {

        scanner = new Scanner(System.in);
        fleetManager = new FleetManager();
        userManager = new UserManager();
        fleetManager.initDisplay();

        // Mission 3 - Vehicle Status Monitor (daemon thread)
        Thread statusThread = new Thread(new VehicleStatusMonitor(fleetManager));
        statusThread.setDaemon(true);
        statusThread.start();

        // Mission 3 - Overdue Reminder Worker (daemon thread)
        Thread overdueThread = new Thread(new OverdueReminderWorker(fleetManager));
        overdueThread.setDaemon(true);
        overdueThread.start();

        // Mission 2 - RentalEventListener implemented with a lambda
        fleetManager.setRentalEventListener(agreement ->
                System.out.println("[EVENT] New rental created: " + agreement.getAgreementId())
        );

        showAuthMenu();
    }

    private void runForm(Runnable form) {
        fleetManager.beginForm();
        try {
            form.run();
        } finally {
            fleetManager.endForm();
        }
    }

    private String readInput(String prompt) {
        // showPrompt() lets the background threads re-draw this prompt
        // after they print, so typing is never left in a messy state.
        fleetManager.showPrompt(prompt);
        if (!scanner.hasNextLine()) {
            fleetManager.clearPrompt();
            System.out.println();
            System.out.println("Input closed. Exiting.");
            System.exit(0);
        }
        String line = scanner.nextLine();
        fleetManager.clearPrompt();
        return line;
    }


    private String readRequired(String prompt) {
        while (true) {
            String value = readInput(prompt).trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty.");
        }
    }


    private double readPositiveDouble(String prompt) {
        while (true) {
            try {
                double value = Double.parseDouble(readInput(prompt).trim());
                if (value > 0 && !Double.isInfinite(value)) {
                    return value;
                }
                System.out.println("Value must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }


    private int readPositiveInt(String prompt) {
        while (true) {
            try {
                int value = Integer.parseInt(readInput(prompt).trim());
                if (value > 0) {
                    return value;
                }
                System.out.println("Value must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }


    private int readNonNegativeInt(String prompt) {
        while (true) {
            try {
                int value = Integer.parseInt(readInput(prompt).trim());
                if (value >= 0) {
                    return value;
                }
                System.out.println("Value cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }


    private int readChoice(String prompt, int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(readInput(prompt).trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please choose a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // ------------------------------------------------------------------
    // Auth menu
    // ------------------------------------------------------------------

    public void showAuthMenu() {

        while (true) {

            System.out.println();
            System.out.println(LINE);
            System.out.println("FLEET RENTAL MANAGEMENT SYSTEM (FRMS)");
            System.out.println(LINE);
            System.out.println("[1] Login");
            System.out.println("[2] Register New User");
            System.out.println("[0] Exit");
            System.out.println(LINE);

            String input = readInput("Enter choice > ");

            try {
                int option = Integer.parseInt(input.trim());

                switch (option) {
                    case 1:
                        login();
                        break;

                    case 2:
                        runForm(() -> registerUser());
                        break;

                    case 0:
                        System.out.println("Goodbye!");
                        return;

                    default:
                        System.out.println("Invalid option.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void registerUser() {

        System.out.println();
        System.out.println("========== REGISTER NEW USER ==========");

        String username = readRequired("Enter username: ");
        String password = readRequired("Enter password: ");
        String fullName = readRequired("Enter full name: ");

        userManager.registerUser(username, password, fullName);
    }

    private void login() {

        System.out.println();
        System.out.println("============ LOGIN ============");

        String username;
        String password;

        fleetManager.beginForm();
        try {
            username = readInput("Username: ").trim();
            password = readInput("Password: ");
        } finally {
            fleetManager.endForm();
        }

        SystemUser user = userManager.login(username, password);

        if (user != null) {
            loggedInUser = user;

            System.out.println();
            System.out.println("Welcome, " + loggedInUser.getFullName()
                    + "! Loading fleet dashboard...");

            showMainMenu();

        } else {
            System.out.println("Invalid username or password");
        }
    }

    // ------------------------------------------------------------------
    // Main menu
    // ------------------------------------------------------------------

    public void showMainMenu() {

        while (loggedInUser != null) {

            System.out.println();
            System.out.println(LINE);
            System.out.println("FRMS MAIN MENU | Logged in as: " + loggedInUser.getFullName());
            System.out.println(LINE);
            System.out.println("[1] Add Vehicle");
            System.out.println("[2] Add Customer");
            System.out.println("[3] View Available Vehicles");
            System.out.println("[4] Create Rental Agreement");
            System.out.println("[5] Return Vehicle");
            System.out.println("[6] View Revenue Report");
            System.out.println("[7] View Vehicles by Fuel Type");
            System.out.println("[8] View Combined Fleet & Customer Report");
            System.out.println("[9] View All Customers");
            System.out.println("[10] View All Rental Agreements");
            System.out.println("[0] Logout");
            System.out.println(LINE);

            String input = readInput("Enter choice > ");

            try {
                int option = Integer.parseInt(input.trim());

                switch (option) {
                    case 1:
                        runForm(() -> addVehicle());
                        break;

                    case 2:
                        runForm(() -> addCustomer());
                        break;

                    case 3:
                        viewAvailableVehicles();
                        break;

                    case 4:
                        runForm(() -> createRentalAgreement());
                        break;

                    case 5:
                        runForm(() -> returnVehicle());
                        break;

                    case 6:
                        viewRevenueReport();
                        break;

                    case 7:
                        runForm(() -> viewVehiclesByFuelType());
                        break;

                    case 8:
                        fleetManager.printAllReportables();
                        break;

                    case 9:
                        viewAllCustomers();
                        break;

                    case 10:
                        viewAllRentalAgreements();
                        break;

                    case 0:
                        loggedInUser = null;
                        System.out.println("Logged out successfully.");
                        break;

                    default:
                        System.out.println("Invalid option.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private FuelType readFuelType(String prompt) {
        FuelType[] types = FuelType.values();

        System.out.println();
        System.out.println("Select Fuel Type:");
        for (int i = 0; i < types.length; i++) {
            System.out.println("[" + (i + 1) + "] " + types[i]);
        }

        int option = readChoice(prompt, 1, types.length);
        return types[option - 1];
    }

    private void addVehicle() {

        System.out.println();
        System.out.println("========== ADD VEHICLE ==========");

        String registrationNumber = readRequired("Registration Number: ");
        String brand = readRequired("Brand: ");
        String model = readRequired("Model: ");
        double dailyRate = readPositiveDouble("Daily Rate: ");

        System.out.println();
        System.out.println("Select Vehicle Type:");
        System.out.println("[1] Car");
        System.out.println("[2] Van");
        System.out.println("[3] Truck");
        int vehicleType = readChoice("Enter option: ", 1, 3);

        FuelType fuelType = readFuelType("Enter option: ");

        Vehicle vehicle;

        switch (vehicleType) {
            case 1:
                vehicle = new Car(registrationNumber, brand, model, dailyRate, fuelType);
                break;

            case 2:
                int passengerCapacity = readPositiveInt("Passenger Capacity: ");
                vehicle = new Van(registrationNumber, brand, model, dailyRate,
                        fuelType, passengerCapacity);
                break;

            default:
                double cargoCapacityTons = readPositiveDouble("Cargo Capacity (tons): ");
                vehicle = new Truck(registrationNumber, brand, model, dailyRate,
                        fuelType, cargoCapacityTons);
                break;
        }

        fleetManager.addVehicle(vehicle);

        System.out.println();
        System.out.println("Vehicle added successfully: " + vehicle.getVehicleId());
    }

    private void addCustomer() {

        System.out.println();
        System.out.println("========== ADD CUSTOMER ==========");

        String fullName = readRequired("Full Name: ");
        String contactNumber = readRequired("Contact Number: ");

        System.out.println();
        System.out.println("[1] Individual Customer");
        System.out.println("[2] Corporate Customer");
        int customerType = readChoice("Enter customer type: ", 1, 2);

        Customer customer;

        if (customerType == 1) {
            int loyaltyPoints = readNonNegativeInt("Loyalty Points: ");
            customer = new IndividualCustomer(fullName, contactNumber, loyaltyPoints);
        } else {
            double creditLimit = readPositiveDouble("Credit Limit: ");
            customer = new CorporateCustomer(fullName, contactNumber, creditLimit);
        }

        fleetManager.addCustomer(customer);

        System.out.println();
        System.out.println("Customer added successfully: " + customer.getCustomerId());
    }

    private void viewAvailableVehicles() {

        System.out.println();
        System.out.println("======= AVAILABLE VEHICLES =======");

        List<Vehicle> vehicles = fleetManager.getAvailableVehicles();

        if (vehicles.isEmpty()) {
            System.out.println("No vehicles are currently available.");
            return;
        }

        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle);
        }
    }

    private void createRentalAgreement() {

        System.out.println();
        System.out.println("====== CREATE RENTAL AGREEMENT ======");

        String vehicleId = readRequired("Vehicle ID: ");
        String customerId = readRequired("Customer ID: ");
        int rentalDays = readPositiveInt("Rental Days: ");

        try {
            RentalAgreement agreement =
                    fleetManager.createRentalAgreement(vehicleId, customerId, rentalDays);

            if (agreement != null) {
                System.out.println();
                System.out.println(agreement);
                printPriceBreakdown(agreement);
            }

        } catch (VehicleNotAvailableException | InsufficientCreditException e) {
            System.out.println("Rental failed: " + e.getMessage());
        }
    }

    /** Shows how the discount was applied (the receipt itself only shows the final total). */
    private void printPriceBreakdown(RentalAgreement agreement) {
        double subtotal = agreement.getVehicle().calculateRentalCost(agreement.getRentalDays());
        double total = agreement.getTotalCost();
        double discount = subtotal - total;
        double percent = subtotal > 0 ? (discount / subtotal) * 100 : 0;

        System.out.println("Price breakdown");
        System.out.printf("  Subtotal : %,.2f%n", subtotal);
        System.out.printf("  Discount : %,.2f (%.0f%%)%n", discount, percent);
        System.out.printf("  Total    : %,.2f%n", total);
    }

    private void returnVehicle() {

        System.out.println();
        System.out.println("========= RETURN VEHICLE =========");

        String agreementId = readRequired("Rental Agreement ID: ");

        fleetManager.returnVehicle(agreementId);
    }

    private void viewRevenueReport() {

        System.out.println();
        System.out.println("================================================================");
        System.out.println("FLEET REVENUE REPORT");
        System.out.println("================================================================");
        System.out.printf("Total Revenue: %,.2f%n", fleetManager.generateRevenueReport());
        System.out.println("----------------------------------------------------------------");
    }

    private void viewVehiclesByFuelType() {

        System.out.println();
        System.out.println("======= VEHICLES BY FUEL TYPE =======");

        FuelType fuelType = readFuelType("Select fuel type: ");

        List<Vehicle> vehicles = fleetManager.getVehiclesByFuelType(fuelType);

        System.out.println();
        System.out.println("Fuel Type: " + fuelType);
        System.out.println("-------------------------------------");

        if (vehicles.isEmpty()) {
            System.out.println("No vehicles found for this fuel type.");
            return;
        }

        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle);
        }
    }

    private void viewAllCustomers() {

        System.out.println();
        System.out.println("========== ALL CUSTOMERS ==========");

        List<Customer> customers = fleetManager.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }

        for (Customer customer : customers) {
            System.out.println(customer);
        }
    }

    private void viewAllRentalAgreements() {

        System.out.println();
        System.out.println("======= ALL RENTAL AGREEMENTS =======");

        List<RentalAgreement> agreements = fleetManager.getAllAgreements();

        if (agreements.isEmpty()) {
            System.out.println("No rental agreements found.");
            return;
        }

        for (RentalAgreement agreement : agreements) {
            System.out.println(agreement);
        }
    }
}
