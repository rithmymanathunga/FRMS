package lk.ijse.fleet;

import java.util.List;
import java.util.Scanner;

public class Main {

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

        // Rental event listener
        fleetManager.setRentalEventListener(
                agreement -> System.out.println(
                        "[EVENT] New rental created: "
                                + agreement.getAgreementId()
                )
        );

        showAuthMenu();
    }

    private void showAuthMenu() {

        while (true) {

            System.out.println();
            System.out.println("=================================");
            System.out.println("       FLEET RENTAL SYSTEM");
            System.out.println("=================================");
            System.out.println("[1] Login");
            System.out.println("[2] Register New User");
            System.out.println("[0] Exit");
            System.out.print("Enter option: ");

            String input = scanner.nextLine();

            try {

                int option = Integer.parseInt(input);

                switch (option) {

                    case 1:
                        login();
                        break;

                    case 2:
                        registerUser();
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
        System.out.println("========== REGISTER ==========");

        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine();

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        userManager.registerUser(
                username,
                password,
                fullName
        );
    }

    private void login() {

        System.out.println();
        System.out.println("============ LOGIN ============");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        SystemUser user = userManager.login(
                username,
                password
        );

        if (user != null) {

            loggedInUser = user;

            System.out.println();
            System.out.println("Login successful!");
            System.out.println(
                    "Welcome, " + loggedInUser.getUsername() + "!"
            );

            showMainMenu();

        } else {

            System.out.println("Invalid username or password");
        }
    }

    private void showMainMenu() {

        while (loggedInUser != null) {

            System.out.println();
            System.out.println("=================================");
            System.out.println("       FLEET RENTAL SYSTEM");
            System.out.println("=================================");
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
            System.out.print("Enter option: ");

            String input = scanner.nextLine();

            try {

                int option = Integer.parseInt(input);

                switch (option) {

                    case 1:
                        addVehicle();
                        break;

                    case 2:
                        addCustomer();
                        break;

                    case 3:
                        viewAvailableVehicles();
                        break;

                    case 4:
                        createRentalAgreement();
                        break;

                    case 5:
                        returnVehicle();
                        break;

                    case 6:
                        System.out.println("Coming soon");
                        break;

                    case 7:
                        System.out.println("Coming soon");
                        break;

                    case 8:
                        System.out.println("Coming soon");
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

    private void addVehicle() {

        System.out.println();
        System.out.println("========== ADD VEHICLE ==========");

        System.out.print("Registration Number: ");
        String registrationNumber = scanner.nextLine();

        System.out.print("Brand: ");
        String brand = scanner.nextLine();

        System.out.print("Model: ");
        String model = scanner.nextLine();

        System.out.print("Daily Rate: ");

        try {

            double dailyRate =
                    Double.parseDouble(scanner.nextLine());

            System.out.println();
            System.out.println("Select Vehicle Type:");
            System.out.println("[1] Car");
            System.out.println("[2] Van");
            System.out.println("[3] Truck");
            System.out.print("Enter option: ");

            int vehicleType =
                    Integer.parseInt(scanner.nextLine());

            System.out.println();
            System.out.println("Select Fuel Type:");
            System.out.println("[1] PETROL");
            System.out.println("[2] DIESEL");
            System.out.println("[3] ELECTRIC");
            System.out.println("[4] HYBRID");
            System.out.print("Enter option: ");

            int fuelOption =
                    Integer.parseInt(scanner.nextLine());

            FuelType fuelType;

            switch (fuelOption) {

                case 1:
                    fuelType = FuelType.PETROL;
                    break;

                case 2:
                    fuelType = FuelType.DIESEL;
                    break;

                case 3:
                    fuelType = FuelType.ELECTRIC;
                    break;

                case 4:
                    fuelType = FuelType.HYBRID;
                    break;

                default:
                    System.out.println("Invalid fuel type.");
                    return;
            }

            Vehicle vehicle;

            switch (vehicleType) {

                case 1:

                    vehicle = new Car(
                            registrationNumber,
                            brand,
                            model,
                            dailyRate,
                            fuelType
                    );

                    break;

                case 2:

                    System.out.print("Passenger Capacity: ");

                    int passengerCapacity =
                            Integer.parseInt(scanner.nextLine());

                    vehicle = new Van(
                            registrationNumber,
                            brand,
                            model,
                            dailyRate,
                            fuelType,
                            passengerCapacity
                    );

                    break;

                case 3:

                    System.out.print(
                            "Cargo Capacity (tons): "
                    );

                    double cargoCapacityTons =
                            Double.parseDouble(scanner.nextLine());

                    vehicle = new Truck(
                            registrationNumber,
                            brand,
                            model,
                            dailyRate,
                            fuelType,
                            cargoCapacityTons
                    );

                    break;

                default:
                    System.out.println("Invalid vehicle type.");
                    return;
            }

            fleetManager.addVehicle(vehicle);

            System.out.println();
            System.out.println(
                    "Vehicle added successfully: "
                            + vehicle.getVehicleId()
            );

        } catch (NumberFormatException e) {

            System.out.println("Invalid number entered.");
        }
    }

    private void addCustomer() {

        System.out.println();
        System.out.println("========== ADD CUSTOMER ==========");

        System.out.print("Full Name: ");
        String fullName = scanner.nextLine();

        System.out.print("Contact Number: ");
        String contactNumber = scanner.nextLine();

        System.out.println();
        System.out.println("[1] Individual Customer");
        System.out.println("[2] Corporate Customer");
        System.out.print("Enter customer type: ");

        try {

            int customerType =
                    Integer.parseInt(scanner.nextLine());

            Customer customer;

            switch (customerType) {

                case 1:

                    customer = new IndividualCustomer(
                            fullName,
                            contactNumber,
                            0
                    );

                    break;

                case 2:

                    System.out.print("Credit Limit: ");

                    double creditLimit =
                            Double.parseDouble(scanner.nextLine());

                    customer = new CorporateCustomer(
                            fullName,
                            contactNumber,
                            creditLimit
                    );

                    break;

                default:
                    System.out.println("Invalid customer type.");
                    return;
            }

            fleetManager.addCustomer(customer);

            System.out.println();
            System.out.println(
                    "Customer added successfully: "
                            + customer.getCustomerId()
            );

        } catch (NumberFormatException e) {

            System.out.println("Invalid number entered.");
        }
    }

    private void viewAvailableVehicles() {

        System.out.println();
        System.out.println("======= AVAILABLE VEHICLES =======");

        List<Vehicle> vehicles =
                fleetManager.getAvailableVehicles();

        if (vehicles.isEmpty()) {

            System.out.println(
                    "No vehicles are currently available."
            );

            return;
        }

        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle);
        }
    }

    private void createRentalAgreement() {

        System.out.println();
        System.out.println(
                "====== CREATE RENTAL AGREEMENT ======"
        );

        System.out.print("Vehicle ID: ");
        String vehicleId = scanner.nextLine();

        System.out.print("Customer ID: ");
        String customerId = scanner.nextLine();

        System.out.print("Rental Days: ");

        try {

            int rentalDays =
                    Integer.parseInt(scanner.nextLine());

            RentalAgreement agreement =
                    fleetManager.createRentalAgreement(
                            vehicleId,
                            customerId,
                            rentalDays
                    );

            if (agreement != null) {

                System.out.println();
                System.out.println(
                        "Rental agreement created successfully."
                );

                System.out.println(agreement);
            }

        } catch (VehicleNotAvailableException
                 | InsufficientCreditException e) {

            System.out.println(
                    "Rental failed: " + e.getMessage()
            );

        } catch (NumberFormatException e) {

            System.out.println(
                    "Please enter a valid number for rental days."
            );
        }
    }

    private void returnVehicle() {

        System.out.println();
        System.out.println("========= RETURN VEHICLE =========");

        System.out.print("Rental Agreement ID: ");
        String agreementId = scanner.nextLine();

        fleetManager.returnVehicle(agreementId);
    }

    private void viewAllCustomers() {

        System.out.println();
        System.out.println("========== ALL CUSTOMERS ==========");

        List<Customer> customers =
                fleetManager.getAllCustomers();

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
        System.out.println(
                "======= ALL RENTAL AGREEMENTS ======="
        );

        List<RentalAgreement> agreements =
                fleetManager.getAllAgreements();

        if (agreements.isEmpty()) {

            System.out.println(
                    "No rental agreements found."
            );

            return;
        }

        for (RentalAgreement agreement : agreements) {
            System.out.println(agreement);
        }
    }
}