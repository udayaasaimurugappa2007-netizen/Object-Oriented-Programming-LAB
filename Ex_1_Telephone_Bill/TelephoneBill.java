/*
 * =====================================================================
 * EXERCISE 1: TELEPHONE BILL CALCULATOR
 * =====================================================================
 * OBJECTIVE: Understand class and object concepts by calculating
 *            telephone bills based on usage minutes and plan type.
 * CONCEPTS:  Classes, Objects, Constructors, Methods
 * =====================================================================
 */

// Represents a customer's telephone bill account and calculates bill based on plan tier
public class TelephoneBill {
    // Instance variables to store customer information
    int customerNo;                // Customer identification number
    String customerName;           // Name of the customer
    int previousMinutes;           // Call duration minutes at previous billing period
    int currentMinutes;            // Call duration minutes at current billing period
    String type;                   // Tariff plan type: "prepaid" or "postpaid"

    // Constructor: Initializes customer data when object is created
    TelephoneBill(int no, String name, int prev, int curr, String type) {
        this.customerNo = no;
        this.customerName = name;
        this.previousMinutes = prev;
        this.currentMinutes = curr;
        this.type = type;
    }

    // Method: Calculate bill amount based on minutes used and plan type
    // Returns the total bill amount in currency units
    double calculateBill() {
        int minutes = currentMinutes - previousMinutes;  // Calculate total minutes used
        double bill;

        // Check if customer has prepaid plan
        if (type.equalsIgnoreCase("prepaid")) {
            if (minutes <= 100)
                bill = minutes * 1;
            else if (minutes <= 200)
                bill = 100 + (minutes - 100) * 1.5;
            else
                bill = 100 + 100 * 1.5 + (minutes - 200) * 2;
        } else {
            // Postpaid plan - different pricing structure
            if (minutes <= 100)
                bill = minutes * 0.75;  // First 100 minutes at lower rate
            else if (minutes <= 200)
                bill = 100 * 0.75 + (minutes - 100) * 1.25;  // Next 100 at higher rate
            else
                bill = 100 * 0.75 + 100 * 1.25 + (minutes - 200) * 1.75;  // Beyond 200
        }

        return bill;
    }

    // Main method: Entry point - creates and tests the TelephoneBill object
    public static void main(String[] args) {
        // Create a new customer object with sample data
        TelephoneBill t = new TelephoneBill(
                201,              // Customer ID
                "Anu",           // Customer Name
                300,             // Previous reading (minutes)
                550,             // Current reading (minutes)
                "postpaid"       // Plan type
        );

        System.out.println("Customer No: " + t.customerNo);
        System.out.println("Customer Name: " + t.customerName);
        System.out.println("Minutes: " +
                (t.currentMinutes - t.previousMinutes));
        System.out.println("Bill Amount: Rs." + t.calculateBill());
    }
}

/*
OUTPUT:

Customer No: 201
Customer Name: Anu
Minutes: 250
Bill Amount: Rs.287.5
*/
