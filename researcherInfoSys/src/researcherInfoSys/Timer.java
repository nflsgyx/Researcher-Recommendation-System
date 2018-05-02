package researcherInfoSys;

/**
 * A utility class for measuring the time elapsed.
 *
 * @author james
 */
public class Timer {

    /**
     * The start time of the timer.
     */
    private static Long startTime = -1L;

    /**
     * Reset the timer.
     */
    public static void start() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Get the time elapsed.
     *
     * @return the time elapsed
     */
    public static String getTime() {
        Double cost = (System.currentTimeMillis() - startTime.doubleValue()) / 1000;
        if (startTime > 0) {
            startTime = -1L;
            return "Time cost: " + String.format("%.3f", cost).toString() + "s";
        } else {
            return "RESET Timer before use";
        }
    }
}
