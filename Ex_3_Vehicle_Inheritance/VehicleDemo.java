/*
 * =====================================================================
 * EXERCISE 3: VEHICLE INHERITANCE AND POLYMORPHISM
 * =====================================================================
 * OBJECTIVE: Demonstrate inheritance by creating a vehicle hierarchy
 *            where different vehicle types inherit from base class.
 * CONCEPTS:  Inheritance, Method Overriding, Polymorphism
 * =====================================================================
 */

// Base class: Represents a generic vehicle with common attributes and billing logic
class Vehicle {
    // Attributes common to all vehicle types
    String vehicleNumber;    // Unique registration number of the vehicle
    String model;            // Model name/variant
    String manufacturer;     // Manufacturing brand or company
    double price;            // Base purchase price

    // Base Constructor: Initializes common vehicle properties via super call in derived classes
    Vehicle(String number, String model, String manufacturer, double price) {
        this.vehicleNumber = number;
        this.model = model;
        this.manufacturer = manufacturer;
        this.price = price;
    }

    // Method: Calculate and display bill with tax and insurance
    void printBill(double taxRate, double insuranceRate) {
        // Calculate tax and insurance amounts as percentages
        double tax = price * taxRate / 100;
        double insurance = price * insuranceRate / 100;
        double total = price + tax + insurance;

        // Display itemized bill
        System.out.println("Vehicle Number : " + vehicleNumber);
        System.out.println("Model : " + model);
        System.out.println("Manufacturer : " + manufacturer);
        System.out.println("Price : " + price);
        System.out.println("Road Tax : " + tax);
        System.out.println("Insurance : " + insurance);
        System.out.println("Total Cost : " + total);
        System.out.println();
    }
}

// Car class: Inherits from Vehicle, applies car-specific tax and insurance rates
class Car extends Vehicle {
    Car(String number, String model, String manufacturer, double price) {
        super(number, model, manufacturer, price);
    }

    // Override method with car-specific rates: 5% tax, 3% insurance
    void calculateBill() {
        printBill(5, 3);
    }
}

// Bike class: Inherits from Vehicle, applies bike-specific rates
class Bike extends Vehicle {
    Bike(String number, String model, String manufacturer, double price) {
        super(number, model, manufacturer, price);
    }

    // Bike rates: 10% tax, 2% insurance
    void calculateBill() {
        printBill(10, 2);
    }
}

// Truck class: Inherits from Vehicle, applies truck-specific rates
class Truck extends Vehicle {
    Truck(String number, String model, String manufacturer, double price) {
        super(number, model, manufacturer, price);
    }

    // Truck rates: 10% tax, 4% insurance
    void calculateBill() {
        printBill(10, 4);
    }
}

// Main class: Demonstrates vehicle inheritance and bill calculation
public class VehicleDemo {
    public static void main(String[] args) {
        // Create different vehicle objects with sample data
        Car car = new Car("TN01AB1234", "Swift", "Maruti", 800000);
        Bike bike = new Bike("TN02CD5678", "R15", "Yamaha", 200000);
        Truck truck = new Truck("TN03EF9012", "Tata 407", "Tata", 1500000);

        System.out.println("CAR BILL");
        car.calculateBill();

        System.out.println("BIKE BILL");
        bike.calculateBill();

        System.out.println("TRUCK BILL");
        truck.calculateBill();
    }
}

/*
OUTPUT:

CAR BILL
Vehicle Number : TN01AB1234
Model : Swift
Manufacturer : Maruti
Price : 800000.0
Road Tax : 40000.0
Insurance : 24000.0
Total Cost : 864000.0

BIKE BILL
Vehicle Number : TN02CD5678
Model : R15
Manufacturer : Yamaha
Price : 200000.0
Road Tax : 20000.0
Insurance : 4000.0
Total Cost : 224000.0

TRUCK BILL
Vehicle Number : TN03EF9012
Model : Tata 407
Manufacturer : Tata
Price : 1500000.0
Road Tax : 150000.0
Insurance : 60000.0
Total Cost : 1710000.0
*/
