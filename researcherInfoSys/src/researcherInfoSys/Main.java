package researcherInfoSys;

/**
 * Entrance of the program. Initialize the data and provide command line
 * interface for testing.
 *
 * @author james
 */
public class Main {

    /**
     * Enter into the program.
     *
     * @param args the command line args
     */
    public static void main(String[] args) {
        init();
        Test.showMenu();
    }

    /**
     * Initialize the data. Display welcome message and load specified Excel
     * file.
     */
    public static void init() {
        System.out.println("\t\t------Welcome to Researcher Recommended System------\n");
        Input.readFile("data/Dataset_RG.xlsx");
    }
}
