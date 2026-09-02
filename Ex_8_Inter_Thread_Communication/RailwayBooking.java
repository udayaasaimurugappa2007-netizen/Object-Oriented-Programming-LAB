/*
 * =====================================================================
 * EXERCISE 8: INTER-THREAD COMMUNICATION
 * =====================================================================
 * OBJECTIVE: Demonstrate inter-thread communication using wait() and
 *            notifyAll() for synchronized booking system.
 * CONCEPTS:  Synchronized Methods, wait(), notifyAll(), Thread Communication
 * =====================================================================
 */

// Shared resource class managing seat count with synchronized monitor methods
class Railway {

    int seats = 2;  // Available seat capacity shared among concurrent threads

    // Synchronized method acquiring object monitor lock before booking
    synchronized void bookTicket(String name) {

        // Loop condition protects against spurious wakeups while waiting for seats
        while (seats == 0) {
            try {
                System.out.println(name + " waiting for a seat.");
                wait();  // Temporarily releases monitor lock and enters WAITING state
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }

        seats--;  // Decrement available seats

        System.out.println(name + " booked a ticket.");
        System.out.println("Available seats: " + seats);
    }

    // Synchronized method: Cancel a ticket and notify waiting threads
    synchronized void cancelTicket(String name) {
        seats++;  // Increase available seats

        System.out.println(name + " cancelled a ticket.");
        System.out.println("Available seats: " + seats);

        notifyAll();  // Wake up all waiting threads
    }
}

// Booking thread: Attempts to book a ticket
class Booking extends Thread {

    Railway railway;  // Shared railway object
    String name;      // Passenger name

    Booking(Railway railway, String name) {
        this.railway = railway;
        this.name = name;
    }

    // Run method: Execute booking operation
    public void run() {
        railway.bookTicket(name);
    }
}

// Cancellation thread: Cancels a ticket after delay
class Cancellation extends Thread {

    Railway railway;  // Shared railway object
    String name;      // Passenger name

    Cancellation(Railway railway, String name) {
        this.railway = railway;
        this.name = name;
    }

    // Run method: Wait 1 second then cancel ticket
    public void run() {

        try {
            Thread.sleep(1000);  // Delay 1000 ms (1 second)
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        railway.cancelTicket(name);
    }
}

// Main class: Demonstrates inter-thread communication
public class RailwayBooking {

    // Main method: Create threads for booking and cancellation
    public static void main(String[] args) {
        Railway railway = new Railway();  // Shared resource

        // Create booking threads for 3 passengers
        Booking b1 = new Booking(railway, "Passenger 1");
        Booking b2 = new Booking(railway, "Passenger 2");
        Booking b3 = new Booking(railway, "Passenger 3");

        Cancellation c1 =
                new Cancellation(railway, "Passenger 1");

        b1.start();
        b2.start();
        b3.start();
        c1.start();
    }
}

/*
SAMPLE OUTPUT:

Passenger 1 booked a ticket.
Available seats: 1
Passenger 2 booked a ticket.
Available seats: 0
Passenger 3 waiting for a seat.
Passenger 1 cancelled a ticket.
Available seats: 1
Passenger 3 booked a ticket.
Available seats: 0

NOTE:
Thread scheduling can cause the order of Passenger 1 and Passenger 2
to vary, but the synchronization and final seat count remain correct.
*/
