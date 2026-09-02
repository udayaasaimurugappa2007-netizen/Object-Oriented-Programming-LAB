/*
 * =====================================================================
 * EXERCISE 9: STRING OPERATIONS USING ARRAYLIST
 * =====================================================================
 * OBJECTIVE: Understand ArrayList and string operations by implementing
 *            a menu-driven program for string manipulation.
 * CONCEPTS:  ArrayList, String Methods, Collections, User Input
 * =====================================================================
 */

import java.util.ArrayList;  // For dynamic list
import java.util.Scanner;     // For user input

// Interactive menu-driven application for ArrayList string manipulations
public class StringMenu {

    // Main execution loop: Displays options and dispatches user actions
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ArrayList<String> list = new ArrayList<>();  // Dynamic array list to store strings

        int choice;  // User's menu choice

        do {
            // Display menu options
            System.out.println("\n--- STRING OPERATIONS ---");
            System.out.println("1. Append");
            System.out.println("2. Insert");
            System.out.println("3. Search");
            System.out.println("4. Display Starting Letter");
            System.out.println("5. Display All");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            choice = sc.nextInt();
            sc.nextLine();  // Consume newline character

            switch (choice) {
                // Case 1: Add string to end of list
                case 1:
                    System.out.print("Enter string: ");
                    list.add(sc.nextLine());
                    System.out.println("String added successfully.");
                    break;

                // Case 2: Insert string at specific position
                case 2:
                    System.out.print("Enter index: ");
                    int index = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter string: ");
                    String value = sc.nextLine();

                    if (index >= 0 && index <= list.size()) {
                        list.add(index, value);
                        System.out.println("String inserted successfully.");
                    } else {
                        System.out.println("Invalid index.");
                    }
                    break;

                // Case 3: Search for a string in the list
                case 3:
                    System.out.print("Enter string to search: ");
                    String search = sc.nextLine();

                    if (list.contains(search)) {
                        System.out.println("String found.");
                    } else {
                        System.out.println("String not found.");
                    }
                    break;

                // Case 4: Display strings starting with specific letter
                case 4:
                    System.out.print("Enter starting letter: ");
                    char ch = sc.nextLine().charAt(0);

                    System.out.println("Matching strings:");

                    // Iterate through list and check first character
                    for (String s : list) {
                        if (!s.isEmpty()
                                && Character.toLowerCase(s.charAt(0))
                                == Character.toLowerCase(ch)) {
                            System.out.println(s);
                        }
                    }
                    break;

                // Case 5: Display all strings in the list
                case 5:
                    System.out.println("ArrayList: " + list);
                    break;

                // Case 6: Exit program
                case 6:
                    System.out.println("Program terminated.");
                    break;

                // Default: Invalid choice
                default:
                    System.out.println("Invalid choice.");
            }

        } while (choice != 6);

        sc.close();
    }
}

/*
 * =====================================================================
 * OUTPUT:
 * =====================================================================
 *
 * --- STRING OPERATIONS ---
 * 1. Append
 * 2. Insert
 * 3. Search
 * 4. Display Starting Letter
 * 5. Display All
 * 6. Exit
 * Enter your choice: 1
 * Enter string: Apple
 * String added successfully.
 *
 * --- STRING OPERATIONS ---
 * 1. Append
 * 2. Insert
 * 3. Search
 * 4. Display Starting Letter
 * 5. Display All
 * 6. Exit
 * Enter your choice: 1
 * Enter string: Banana
 * String added successfully.
 *
 * --- STRING OPERATIONS ---
 * 1. Append
 * 2. Insert
 * 3. Search
 * 4. Display Starting Letter
 * 5. Display All
 * 6. Exit
 * Enter your choice: 2
 * Enter index: 1
 * Enter string: Orange
 * String inserted successfully.
 *
 * --- STRING OPERATIONS ---
 * 1. Append
 * 2. Insert
 * 3. Search
 * 4. Display Starting Letter
 * 5. Display All
 * 6. Exit
 * Enter your choice: 5
 * ArrayList: [Apple, Orange, Banana]
 *
 * --- STRING OPERATIONS ---
 * 1. Append
 * 2. Insert
 * 3. Search
 * 4. Display Starting Letter
 * 5. Display All
 * 6. Exit
 * Enter your choice: 3
 * Enter string to search: Banana
 * String found.
 *
 * --- STRING OPERATIONS ---
 * 1. Append
 * 2. Insert
 * 3. Search
 * 4. Display Starting Letter
 * 5. Display All
 * 6. Exit
 * Enter your choice: 4
 * Enter starting letter: A
 * Matching strings:
 * Apple
 *
 * --- STRING OPERATIONS ---
 * 1. Append
 * 2. Insert
 * 3. Search
 * 4. Display Starting Letter
 * 5. Display All
 * 6. Exit
 * Enter your choice: 6
 * Program terminated.
 *
 * =====================================================================
 * END OF EXPERIMENT 9
 * =====================================================================
 */
