/*
 * =====================================================================
 * EXERCISE 5: CIRCULAR QUEUE WITH EXCEPTION HANDLING
 * =====================================================================
 * OBJECTIVE: Understand Abstract Data Types (ADT) by implementing
 *            a circular queue and handling overflow/underflow errors.
 * CONCEPTS:  Queues, Interfaces, Exception Handling, BufferedReader
 * =====================================================================
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;

// Interface: Defines contract for queue operations
interface MyQueue {
    void enqueue();   // Add element to queue
    void dequeue();   // Remove element from queue
    void display();   // Display all queue elements
}

// Concrete class: Implements circular queue using fixed-size array
class CircularQueue implements MyQueue {

    // Queue properties
    final int SIZE = 5;         // Maximum queue capacity
    int[] queue = new int[SIZE];  // Array to store queue elements
    int front = -1;             // Pointer to front element
    int rear = -1;              // Pointer to rear element

    // Method: Add element to queue (throws exception handling)
    public void enqueue() {
        // Try-catch to handle input and queue overflow exceptions
        try {
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(System.in));

            // Check for queue overflow condition
            if ((front == 0 && rear == SIZE - 1)
                    || (front == rear + 1)) {
                System.out.println("Queue Overflow");  // All spaces filled
                return;
            }

            System.out.print("Enter the element: ");
            int item = Integer.parseInt(br.readLine());

            if (front == -1) {
                front = 0;
                rear = 0;
            } else if (rear == SIZE - 1) {
                rear = 0;
            } else {
                rear++;
            }

            queue[rear] = item;

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Method: Remove and display front element from queue
    public void dequeue() {
        // Check if queue is empty
        if (front == -1) {
            System.out.println("Queue Underflow");  // No elements to remove
            return;
        }

        System.out.println("Deleted Element: " + queue[front]);

        if (front == rear) {
            front = -1;
            rear = -1;
        } else if (front == SIZE - 1) {
            front = 0;
        } else {
            front++;
        }
    }

    // Method: Display all elements currently in the queue
    public void display() {
        // Check if queue is empty
        if (front == -1) {
            System.out.println("Queue is Empty");
            return;
        }

        System.out.println("Queue Elements:");

        if (front <= rear) {
            for (int i = front; i <= rear; i++) {
                System.out.print(queue[i] + " ");
            }
        } else {
            for (int i = front; i < SIZE; i++) {
                System.out.print(queue[i] + " ");
            }

            for (int i = 0; i <= rear; i++) {
                System.out.print(queue[i] + " ");
            }
        }

        System.out.println();
    }
}

// Main class: Driver program for circular queue operations
public class Main {
    // Main method: Interactive menu for queue operations
    public static void main(String[] args) throws Exception {
        // Input reader for user choices
        BufferedReader br =
                new BufferedReader(new InputStreamReader(System.in));

        CircularQueue q = new CircularQueue();  // Create queue object
        int ch;  // User choice variable

        do {
            System.out.println("\n*** CIRCULAR QUEUE ***");
            System.out.println("1. Enqueue");
            System.out.println("2. Dequeue");
            System.out.println("3. Display");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            ch = Integer.parseInt(br.readLine());

            switch (ch) {

                case 1:
                    q.enqueue();
                    break;

                case 2:
                    q.dequeue();
                    break;

                case 3:
                    q.display();
                    break;

                case 4:
                    System.out.println("Program Ended");
                    break;

                default:
                    System.out.println("Invalid Choice");
            }

        } while (ch != 4);
    }
}

/*
SAMPLE OUTPUT:

*** CIRCULAR QUEUE ***
1. Enqueue
2. Dequeue
3. Display
4. Exit
Enter your choice: 1
Enter the element: 10

*** CIRCULAR QUEUE ***
1. Enqueue
2. Dequeue
3. Display
4. Exit
Enter your choice: 1
Enter the element: 20

*** CIRCULAR QUEUE ***
1. Enqueue
2. Dequeue
3. Display
4. Exit
Enter your choice: 1
Enter the element: 30

*** CIRCULAR QUEUE ***
1. Enqueue
2. Dequeue
3. Display
4. Exit
Enter your choice: 3
Queue Elements:
10 20 30

*** CIRCULAR QUEUE ***
1. Enqueue
2. Dequeue
3. Display
4. Exit
Enter your choice: 2
Deleted Element: 10

*** CIRCULAR QUEUE ***
1. Enqueue
2. Dequeue
3. Display
4. Exit
Enter your choice: 3
Queue Elements:
20 30

*** CIRCULAR QUEUE ***
1. Enqueue
2. Dequeue
3. Display
4. Exit
Enter your choice: 4
Program Ended
*/
