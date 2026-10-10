package lk.ijse.fleet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FleetManager {

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<RentalAgreement> agreements = new ArrayList<>();

    private RentalEventListener rentalEventListener;

    // ---- Console display control (keeps background output from breaking input) ----
    private static final String ESC = "\u001B";

    /** True when the console understands ANSI cursor control (a real terminal). */
    private final boolean ansiMode = detectAnsi();

    private String statusLine = "[FLEET STATUS] waiting for first update...";
    private String alertLine = "[OVERDUE ALERT] none";

    // Fallback mode only: the prompt currently waiting for the user's input
    private String activePrompt = null;

    // Fallback mode only: while a data-entry form is open, background output
    // is held back and shown as one tidy block when the form ends.
    private boolean formActive = false;
    private String pendingStatus = null;
    private final List<String> pendingAlerts = new ArrayList<>();

    private static boolean detectAnsi() {
        try {
            if ("true".equalsIgnoreCase(System.getProperty("frms.ansi"))) {
                return true;
            }
            java.io.Console console = System.console();
            return console != null && console.isTerminal();
        } catch (Throwable t) {
            return false;
        }
    }

    // ==================== CONSOLE OUTPUT CONTROL ====================

    /**
     * Terminal mode: reserves the top two lines of the screen for the live
     * fleet status and the latest overdue alert. Everything else (menus,
     * prompts, typing) scrolls underneath, so background updates never touch
     * what the user is typing. Does nothing in fallback mode.
     */
    public synchronized void initDisplay() {
        if (!ansiMode) {
            return;
        }
        // clear screen, limit scrolling to line 3 downwards, put cursor on line 3
        System.out.print(ESC + "[2J" + ESC + "[3;r" + ESC + "[3;1H");
        System.out.flush();
        drawBars();

        // give the terminal its normal scrolling back when the program ends
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(ESC + "[r");
            System.out.flush();
        }));
    }

    private synchronized void drawBars() {
        // one single write, so nothing can slip in between save and restore
        String sequence = ESC + "7"
                + ESC + "[1;1H" + ESC + "[2K" + statusLine
                + ESC + "[2;1H" + ESC + "[2K" + alertLine
                + ESC + "8";
        System.out.print(sequence);
        System.out.flush();
    }

    /** Prints a prompt and remembers it (used by the fallback mode). */
    public synchronized void showPrompt(String prompt) {
        activePrompt = prompt;
        System.out.print(prompt);
        System.out.flush();
    }

    /** Called once the user has pressed Enter. */
    public synchronized void clearPrompt() {
        activePrompt = null;
    }

    /** Fallback mode: a data-entry form is starting; hold background output. */
    public synchronized void beginForm() {
        formActive = true;
    }

    /** Fallback mode: the form is finished; show everything that was held back. */
    public synchronized void endForm() {
        formActive = false;

        if (pendingStatus == null && pendingAlerts.isEmpty()) {
            return;
        }

        boolean statusWasHeld = (pendingStatus != null);
        pendingStatus = null;

        System.out.println();
        for (String alert : pendingAlerts) {
            System.out.println(alert);
        }
        pendingAlerts.clear();

        // The held status is out of date (the form may have rented or returned
        // a vehicle), so print a fresh snapshot instead of the old one.
        if (statusWasHeld) {
            generateSnapshot().printSummary();
        }
        System.out.flush();
    }

    /** Fallback mode: print on its own line and redraw the prompt underneath. */
    private synchronized void printBackgroundLine(String message) {
        if (activePrompt != null) {
            System.out.println();
            System.out.println(message);
            System.out.print(activePrompt);
            System.out.flush();
        } else {
            System.out.println(message);
        }
    }

    private synchronized void emitStatus(String message) {
        if (ansiMode) {
            statusLine = message;
            drawBars();
        } else if (formActive) {
            pendingStatus = message;            // only the latest status matters
        } else {
            printBackgroundLine(message);
        }
    }

    private synchronized void emitAlerts(List<String> alerts) {
        if (ansiMode) {
            if (alerts.isEmpty()) {
                alertLine = "[OVERDUE ALERT] none";
            } else {
                String last = alerts.get(alerts.size() - 1);
                alertLine = alerts.size() > 1
                        ? last + " (+" + (alerts.size() - 1) + " more)"
                        : last;
            }
            drawBars();
        } else if (formActive) {
            pendingAlerts.addAll(alerts);
        } else {
            for (String alert : alerts) {
                printBackgroundLine(alert);
            }
        }
    }

    // ==================== MISSION 2 ====================

    public synchronized void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    public synchronized void addCustomer(Customer c) {
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

    // Mission 4: Stream-based available vehicle list
    public List<Vehicle> getAvailableVehicles() {
        return vehicles.stream()
                .filter(v -> v.getStatus() == RentalStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public synchronized RentalAgreement createRentalAgreement(
            String vehicleId,
            String customerId,
            int rentalDays)
            throws VehicleNotAvailableException, InsufficientCreditException {

        Vehicle vehicle = findVehicleById(vehicleId);

        if (vehicle == null) {
            System.out.println("Vehicle not found.");
            return null;
        }

        if (vehicle.getStatus() != RentalStatus.AVAILABLE) {
            throw new VehicleNotAvailableException(
                    "Vehicle is not available for rental."
            );
        }

        Customer customer = findCustomerById(customerId);

        if (customer == null) {
            System.out.println("Customer not found.");
            return null;
        }

        double subtotal = vehicle.calculateRentalCost(rentalDays);
        double cost = customer.applyDiscount(subtotal);

        if (customer instanceof CorporateCustomer corporateCustomer) {
            if (!corporateCustomer.hasSufficientCredit(cost)) {
                throw new InsufficientCreditException(
                        "Insufficient credit for this rental."
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

    public synchronized void returnVehicle(String agreementId) {
        for (RentalAgreement agreement : agreements) {
            if (agreement.getAgreementId().equals(agreementId)) {
                agreement.getVehicle().setStatus(RentalStatus.AVAILABLE);
                System.out.println("Vehicle returned successfully.");
                return;
            }
        }

        System.out.println("Rental agreement not found.");
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    public List<RentalAgreement> getAllAgreements() {
        return new ArrayList<>(agreements);
    }

    // ==================== MISSION 3 ====================

    public FleetSnapshot generateSnapshot() {
        return new FleetSnapshot();
    }

    public synchronized void printFleetStatus() {
        generateSnapshot().printSummary();
    }

    public synchronized void checkOverdueAgreements() {
        List<String> alerts = new ArrayList<>();

        for (RentalAgreement agreement : agreements) {

            // A returned agreement is finished: no more days, no more alerts.
            if (!isActive(agreement)) {
                continue;
            }

            agreement.setDaysElapsed(
                    agreement.getDaysElapsed() + 1
            );

            if (agreement.getDaysElapsed() > agreement.getRentalDays()) {
                alerts.add(
                        "[OVERDUE ALERT] Agreement "
                                + agreement.getAgreementId()
                                + " has exceeded its "
                                + agreement.getRentalDays()
                                + "-day rental period."
                );
            }
        }

        emitAlerts(alerts);
    }

    /**
     * An agreement is still running while its vehicle is RENTED and no newer
     * agreement has taken over that same vehicle.
     */
    private boolean isActive(RentalAgreement agreement) {
        Vehicle vehicle = agreement.getVehicle();

        if (vehicle.getStatus() != RentalStatus.RENTED) {
            return false;
        }

        int index = agreements.indexOf(agreement);
        for (int i = index + 1; i < agreements.size(); i++) {
            if (agreements.get(i).getVehicle() == vehicle) {
                return false;
            }
        }
        return true;
    }

    // Non-static inner class
    public class FleetSnapshot {

        private int availableCount;
        private int rentedCount;
        private double totalRevenue;

        public FleetSnapshot() {

            availableCount = 0;
            rentedCount = 0;
            totalRevenue = 0.0;

            for (Vehicle vehicle : vehicles) {
                if (vehicle.getStatus() == RentalStatus.AVAILABLE) {
                    availableCount++;
                } else if (vehicle.getStatus() == RentalStatus.RENTED) {
                    rentedCount++;
                }
            }

            for (RentalAgreement agreement : agreements) {
                totalRevenue += agreement.getTotalCost();
            }
        }

        public void printSummary() {
            emitStatus(String.format(
                    "[FLEET STATUS] Available: %d Rented: %d Total Revenue So Far: %,.2f",
                    availableCount,
                    rentedCount,
                    totalRevenue
            ));
        }
    }

    // ==================== MISSION 4 ====================

    public List<Vehicle> getVehiclesByFuelType(FuelType type) {
        return vehicles.stream()
                .filter(v -> v.getFuelType() == type)
                .collect(Collectors.toList());
    }

    // Optional Stream + Comparator method
    public List<Customer> getCustomersSortedByName() {
        return customers.stream()
                .sorted(Comparator.comparing(c -> c.getFullName()))
                .collect(Collectors.toList());
    }

    public double generateRevenueReport() {
        return agreements.stream()
                .mapToDouble(a -> a.getTotalCost())
                .sum();
    }

    public void printAllReportables() {

        List<Reportable> reportables = new ArrayList<>();

        reportables.addAll(vehicles);
        reportables.addAll(customers);

        System.out.println();
        System.out.println("========== FLEET & CUSTOMER REPORT ==========");

        if (reportables.isEmpty()) {
            System.out.println("No vehicles or customers found.");
            return;
        }

        for (Reportable item : reportables) {
            System.out.println(
                    item.getReportCategory() + ": " + item.getSummary()
            );
        }

        System.out.println("=============================================");
    }
}