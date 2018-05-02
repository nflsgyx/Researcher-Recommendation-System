package researcherInfoSys;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * A utility class with several static methods to load Excel file and get user
 * input.
 *
 * @author james
 */
public class Input {

    /**
     * String for formatting the output.
     */
    private static final String S1 = "  o     ", S2 = "  ✔     ", S3 = "        ";

    /**
     * Constants which store the column number of each required attribute in the
     * Excel file.
     */
    private static final int NAME_COL = 2, UNIVERSITY_COL = 0, DEPARTMENT_COL = 1, TOPIC_COL = 10, SKILL_COL = 11;

    /**
     * Missing policy parameter used when reading Excel file.
     */
    private static final Row.MissingCellPolicy MP = Row.MissingCellPolicy.RETURN_BLANK_AS_NULL;

    /**
     * Scanner for keyboard input.
     */
    private static final Scanner KB = new Scanner(System.in);

    /**
     * Read data from a specified Excel file and store into
     * {@link Researcher#repository repository of researcher} after processing.
     *
     * @param filePath the path of the Excel file needed to be loaded
     */
    public static void readFile(String filePath) {
        System.out.println(S1 + "Loading file \"" + filePath + "\" into memory......");
        Timer.start();
        DataFormatter formatter = new DataFormatter();
        try {
            OPCPackage pkg = OPCPackage.open(new File(filePath));
            Sheet sheet = new XSSFWorkbook(pkg).getSheetAt(0);
            pkg.close();
            System.out.println(S2 + "File loaded. " + Timer.getTime());
            System.out.println(S1 + "Processing and building up repository......");
            Timer.start();
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                String name = formatter.formatCellValue(row.getCell(NAME_COL, MP));
                String university = formatter.formatCellValue(row.getCell(UNIVERSITY_COL, MP));
                String department = formatter.formatCellValue(row.getCell(DEPARTMENT_COL, MP));
                String topics = formatter.formatCellValue(row.getCell(TOPIC_COL, MP));
                String skills = formatter.formatCellValue(row.getCell(SKILL_COL, MP));
                Researcher newResearcher = new Researcher(name, university, department, row.getRowNum());
                Researcher r = Researcher.add(newResearcher);
                r.addToInterests(topics);
                r.addToInterests(skills);
            }
        } catch (InvalidFormatException e) {
            System.err.println("Invalid file format - " + e.getMessage());
        } catch (IOException e) {
            System.err.println("File not found - " + e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        System.out.println(S2 + "Repository built. " + Timer.getTime());
        System.out.println(Researcher.getWarningInfo());
    }

    /**
     * Display tips and Get a String input from keyboard.
     *
     * @param tipStr the tips displayed in the command line
     * @return the valid String input from the user
     */
    public static String getString(String tipStr) {
        System.out.print(tipStr + ": ");
        String str = KB.nextLine();
        while (!tipStr.contains("can be blank") && str.isEmpty()) {
            System.out.print(S3 + "Blank is not allowed here, enter again please: ");
            str = KB.nextLine();
        }
        return str;
    }
}
