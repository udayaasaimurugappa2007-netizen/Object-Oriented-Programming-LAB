/*
 * =====================================================================
 * EXERCISE 10: FILE HANDLING AND DIRECTORY OPERATIONS
 * =====================================================================
 * OBJECTIVE: Understand file handling by listing all files in a
 *            directory without displaying subdirectories.
 * CONCEPTS:  File Operations, Directory Listing, File Filtering
 * =====================================================================
 */

import java.io.File;      // For file operations
import java.util.Scanner;  // For user input

// Main program: Prompts user for directory path and lists only files while excluding directories
public class File_Handling_ListFiles {

    // Entry point: Validates directory path and iterates over listFiles()
    public static void main(String[] args) {
        // Create scanner for user input
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter directory path: ");
        String path = sc.nextLine();

        // Create File object for the given path
        File directory = new File(path);

        // Check if path exists and is a directory
        if (directory.exists() && directory.isDirectory()) {

            // Get all files and subdirectories in this directory
            File[] files = directory.listFiles();

            System.out.println("\nFiles in the directory:");

            // Check if listFiles() returned results
            if (files != null) {
                // Iterate through all items
                for (File file : files) {
                    // Display only files (not directories)
                    if (file.isFile()) {
                        System.out.println(file.getName());
                    }
                }
            }

        } else {
            System.out.println("Invalid directory path.");
        }

        sc.close();  // Close scanner resource
    }
}

/*
 * =====================================================================
 * SAMPLE OUTPUT
 * =====================================================================
 *
 * Enter directory path: C:\Users\Documents
 *
 * Files in the directory:
 * file1.txt
 * file2.txt
 * program.java
 * notes.docx
 *
 * =====================================================================
 * OUTPUT EXPLANATION
 * =====================================================================
 *
 * 1. The program asks the user to enter a directory path.
 *
 * 2. A File object is created using the entered path.
 *
 * 3. exists() checks whether the specified path exists.
 *
 * 4. isDirectory() checks whether the specified path is a directory.
 *
 * 5. listFiles() retrieves the files and directories contained in
 *    the specified directory.
 *
 * 6. isFile() ensures that only files are displayed and directories
 *    are ignored.
 *
 * 7. getName() retrieves and displays the name of each file.
 *
 * 8. If the specified path is invalid or is not a directory, the
 *    program displays "Invalid directory path."
 *
 * =====================================================================
 * RESULT
 * =====================================================================
 *
 * Thus, the Java program for implementing file handling was
 * successfully executed, and the files present in the specified
 * directory were displayed.
 *
 * =====================================================================
 * END OF EXPERIMENT 10
 * =====================================================================
 */
