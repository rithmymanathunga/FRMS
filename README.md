# Fleet Rental Management System (FRMS)

**Module:** ITS1033 Object-Oriented Programming HDSE 77
**Student Name :** Rithmi Manathunga 
**Student ID :** 252771009


This is my OOP coursework. It's a console app for a small vehicle rental company where you can register, log in, add vehicles and customers, create rentals, return them and look at a few reports. Two background threads watch the fleet while the menu is running.

Everything is kept in memory, so the data is gone when the program closes. File saving wasn't part of the coursework so I didn't add it.

## Requirements

JDK 25 or newer. I used IntelliJ, but a terminal with javac works too. There are no external libraries.

## How to run

All the classes are in the package lk.ijse.fleet under src/lk/ijse/fleet/.

**Linux / macOS / Git Bash:** 

javac -d bin $(find src -name "*.java")
java -cp bin lk.ijse.fleet.Main

**Windows PowerShell:**

javac -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp bin lk.ijse.fleet.Main

In IntelliJ just mark src as Sources Root, do Build > Rebuild Project and run Main.

## A note about the background messages

The threads print [FLEET STATUS] and [OVERDUE ALERT] every 5 seconds, which used to land in the middle of whatever I was typing. So now:

In a real terminal (or IntelliJ with "Emulate terminal in console" ticked in the run configuration) those two messages stay pinned on the top two lines.
In the normal IntelliJ Run window they are held back while you fill in a form and printed when you finish. At the menu they print straight away.

It picks the mode on its own, so there is nothing to set up.

## Using it

Register a user first (usernames must be unique), then log in. After that the main menu has everything: add vehicle, add customer, view available vehicles, create rental, return vehicle, the reports, and logout. For a rental you type the vehicle ID and customer ID (like VEH-1001 and CUS-1001) and the number of days. To return a vehicle you type the agreement ID, like AGR-1001.

Bad input like letters, negative numbers or empty text just gets rejected and it asks again.

## Business rules

Rental cost:

Car is dailyRate * days * fuelMultiplier.
Van is the car cost plus dailyRate * 0.20 once. It isn't multiplied by the days.
Truck is the car cost plus cargoTons * 10 * days, so this one is per day.

Fuel multipliers are PETROL 1.00, DIESEL 0.90, ELECTRIC 0.75 and HYBRID 0.85.

Discounts:

Individual customers get 0% under 100 loyalty points, 5% from 100 to 299 and 10% from 300.
Corporate customers always get 15%, and outstandingBalance + cost can't be more than their credit limit.

If the vehicle is already rented, VehicleNotAvailableException is thrown. If a corporate customer can't afford it, InsufficientCreditException is thrown. Both are checked exceptions, thrown in FleetManager.createRentalAgreement(...) and caught in Main, which prints the message. A wrong vehicle or customer ID just prints "not found" and returns null.

IDs come from private static counters (VEH-1001, CUS-1001, AGR-1001...).

## Where the OOP parts are
Vehicle and Customer are abstract. Car, Van, Truck, IndividualCustomer and CorporateCustomer extend them and override calculateRentalCost() / applyDiscount().
Fields are private with getters and setters. The ID fields have no setters.
RentalAgreement has a Vehicle and a Customer. FleetManager owns the lists and Main holds the FleetManager and UserManager.
FuelType is an enum with a multiplier, RentalStatus is a plain enum.
FleetSnapshot is a non-static inner class of FleetManager.
RentalEventListener is a functional interface and I register it in Main with a lambda.
Vehicle and Customer both implement Reportable, and printAllReportables() loops over one List<Reportable> with both of them in it.
The stream methods are getAvailableVehicles(), getVehiclesByFuelType(), getCustomersSortedByName() and generateRevenueReport().
VehicleStatusMonitor and OverdueReminderWorker are Runnables running as daemon threads. The FleetManager methods that touch the lists are synchronized so the threads don't clash with the menu.
Background threads

Both sleep 5 seconds in a loop. The status monitor prints the available count, rented count and total revenue. The overdue worker treats every tick as one day passing: it adds 1 to daysElapsed for each active agreement and prints an alert once that goes past rentalDays. When a vehicle is returned its agreement stops counting, so it doesn't keep alerting.

## Things worth knowing
Passwords are plain text, as the coursework said. It's not a real login system.
A corporate customer's outstandingBalance isn't updated after a rental because the spec doesn't ask for it.
The [EVENT] line prints just before the receipt because the listener fires inside createRentalAgreement(...), before it returns.
The "Price breakdown" under the receipt is only extra output from Main. The receipt itself shows the final total, as in the specification.

## Submission

ZIP file with the source and this README on Google Classroom, plus the public GitHub repo: https://github.com/rithmymanathunga/FRMS
