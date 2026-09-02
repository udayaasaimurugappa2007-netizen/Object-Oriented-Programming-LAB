/*
 * =====================================================================
 * EXERCISE 7: MULTI-THREADING DEMONSTRATION
 * =====================================================================
 * OBJECTIVE: Understand multi-threading by sorting array using
 *            different threads for ascending and descending orders.
 * CONCEPTS:  Threads, Thread.start(), Thread.join(), Concurrency
 * =====================================================================
 */

import java.util.Arrays;  // For array operations
import java.util.Random;   // For random number generation

// Thread 1: Generates random integer numbers and populates shared static array
class ArrayGenerator extends Thread {

    static int[] a = new int[5];  // Shared static array populated by generator thread

    // Entry point for thread execution invoked by start()
    public void run() {

        // Fill array with random numbers (0-99)
        Random r = new Random();

        for (int i = 0; i < a.length; i++) {
            a[i] = r.nextInt(100);
        }

        System.out.println("Original Array: " +
                Arrays.toString(a));
    }
}

// Thread 2: Sorts array in ascending order
class AscendingSort extends Thread {

    int[] a;  // Local reference to array

    AscendingSort(int[] a) {
        this.a = a;  // Initialize with passed array
    }

    // Run method: Sort array in ascending order
    public void run() {

        int[] b = a.clone();  // Create a copy to avoid modifying original
        Arrays.sort(b);       // Sort in ascending order

        System.out.println("Ascending Order: " +
                Arrays.toString(b));
    }
}

// Thread 3: Sorts array in descending order
class DescendingSort extends Thread {

    int[] a;  // Local reference to array

    DescendingSort(int[] a) {
        this.a = a;  // Initialize with passed array
    }

    // Run method: Sort array in descending order
    public void run() {

        int[] b = a.clone();  // Create a copy
        Arrays.sort(b);       // First sort in ascending order

        // Reverse the array to get descending order
        for (int i = 0; i < b.length / 2; i++) {

            // Swap elements to reverse array
            int temp = b[i];
            b[i] = b[b.length - 1 - i];
            b[b.length - 1 - i] = temp;
        }

        System.out.println("Descending Order: " +
                Arrays.toString(b));
    }
}

// Main class: Demonstrates multi-threading with array sorting
public class ArrayThreadDemo {
    // Main method: Creates and manages threads
    public static void main(String[] args)
            throws InterruptedException {

        // Step 1: Create and start array generator thread
        ArrayGenerator g = new ArrayGenerator();

        g.start();      // Start the thread
        g.join();       // Wait for generator to complete before proceeding

        // Step 2: Create sorting threads
        AscendingSort asc =
                new AscendingSort(ArrayGenerator.a);

        DescendingSort desc =
                new DescendingSort(ArrayGenerator.a);

        asc.start();
        desc.start();
    }
}

/*
SAMPLE OUTPUT:

Original Array: [42, 17, 65, 8, 31]
Ascending Order: [8, 17, 31, 42, 65]
Descending Order: [65, 42, 31, 17, 8]

NOTE:
The generated array values are random, so the output values may
differ each time the program is executed.
*/
