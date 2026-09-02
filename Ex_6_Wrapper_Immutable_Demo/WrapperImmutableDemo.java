/*
 * =====================================================================
 * EXERCISE 6: WRAPPER CLASSES AND IMMUTABILITY DEMONSTRATION
 * =====================================================================
 * OBJECTIVE: Understand immutability of wrapper classes by showing
 *            that operations create new objects rather than modifying.
 * CONCEPTS:  Wrapper Classes, Immutability, Identity Hash Code
 * =====================================================================
 */

// Main class demonstrating that Java wrapper classes (Integer, Double) are immutable
public class WrapperImmutableDemo {

    public static void main(String[] args) {
        // ===== INTEGER WRAPPER DEMONSTRATION =====
        // Instantiates an Integer object wrapping primitive value 100
        Integer i = Integer.valueOf(100);

        System.out.println("Before Modification");
        System.out.println("Integer Value : " + i);
        // System.identityHashCode() returns default memory-location-based hash code for instance identity comparison
        System.out.println("Hash Code : " +
                System.identityHashCode(i));

        // Arithmetic operation implicitly unboxes Integer to int, computes 150, and boxes to a NEW Integer instance
        i = i + 50;

        System.out.println("\nAfter Modification");
        System.out.println("Integer Value : " + i);
        // Notice: Hash code is DIFFERENT - proves it's a new object
        System.out.println("Hash Code : " +
                System.identityHashCode(i));

        // ===== DOUBLE WRAPPER DEMONSTRATION =====
        // Create a Double wrapper object
        Double d = Double.valueOf(25.5);

        System.out.println("\nBefore Modification");
        System.out.println("Double Value : " + d);
        // Get unique identity hash code for this Double object
        System.out.println("Hash Code : " +
                System.identityHashCode(d));

        // Modification: Multiply by 2 (creates a NEW Double object)
        d = d * 2;

        System.out.println("\nAfter Modification");
        System.out.println("Double Value : " + d);
        System.out.println("Hash Code : " +
                System.identityHashCode(d));
    }
}

/*
OUTPUT:

Before Modification
Integer Value : 100
Hash Code : <runtime-dependent>

After Modification
Integer Value : 150
Hash Code : <runtime-dependent>

Before Modification
Double Value : 25.5
Hash Code : <runtime-dependent>

After Modification
Double Value : 51.0
Hash Code : <runtime-dependent>
*/
