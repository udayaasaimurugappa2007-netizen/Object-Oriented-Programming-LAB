/*
 * =====================================================================
 * EXERCISE 2: TEMPERATURE CONVERTER UTILITY CLASS
 * =====================================================================
 * OBJECTIVE: Provide temperature conversion methods between different
 *            scales (Celsius, Fahrenheit, Kelvin).
 * CONCEPTS:  Packages, Static Methods, Mathematical Formulas
 * =====================================================================
 */

package temperature;

// Utility class containing static conversion methods for temperature scales
public class Converter {
    // Utility class with static methods for temperature conversions
    // All methods are static, so no object instantiation is needed
    
    // Converts Celsius to Fahrenheit using formula: F = (C × 9/5) + 32
    public static double celsiusToFahrenheit(double c) {
        return (c * 9 / 5) + 32;
    }

    // Converts Fahrenheit to Celsius using formula: C = (F - 32) × 5/9
    public static double fahrenheitToCelsius(double f) {
        return (f - 32) * 5 / 9;
    }

    // Converts Celsius to Kelvin: K = C + 273.15 (absolute zero offset)
    public static double celsiusToKelvin(double c) {
        return c + 273.15;
    }

    // Converts Kelvin to Celsius: C = K - 273.15
    public static double kelvinToCelsius(double k) {
        return k - 273.15;
    }

    // Converts Fahrenheit to Kelvin: combines F→C then C→K
    public static double fahrenheitToKelvin(double f) {
        return (f - 32) * 5 / 9 + 273.15;
    }

    // Converts Kelvin to Fahrenheit: combines K→C then C→F
    public static double kelvinToFahrenheit(double k) {
        return (k - 273.15) * 9 / 5 + 32;
    }
}
