package researcherInfoSys;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Stores data for a researcher, and contains a repository of all researcher as
 * well, by which a series of functionalities can be realized. Main
 * functionalities includes:
 * <ul>
 * <li>get number of distinct researchers</li>
 * <li>get number of distinct interests</li>
 * <li>get number of researchers with a specified interest</li>
 * <li>get detailed information of a specified researcher</li>
 * <li>get number of times two interests con-occur</li>
 * </ul>
 * Instances of this class store:
 * <ul>
 * <li>the name of a researcher, as a String</li>
 * <li>the university of a researcher, as a String</li>
 * <li>the department of the university, as a String</li>
 * <li>the list of interest of a researcher</li>
 * </ul>
 * Researchers with the same name, department and university attributes are
 * generally considered as the same people, with their interests combined.
 * Otherwise, they're considered as different people.
 *
 * @author james
 */
public class Researcher {

    /**
     * A repository mapping each researcher name to its corresponding Researcher
     * Object.
     */
    private static HashMap<String, LinkedList<Researcher>> repository = new HashMap<>();

    /**
     * A repository mapping each interest to a set of unique researcherID of
     * each researcher.
     */
    private static HashMap<String, TreeSet<Integer>> interestRepository = new HashMap<>();

    /**
     * The number of distinct researchers in the repository.
     */
    private static int researcherNum = 0;

    /**
     * The numbers of recored whose names occur at least twice in the dataset.
     */
    private static int sameNameNum_diffDep = 0, sameNameNum_sameDep = 0;

    /**
     * The set which stores the name of the researchers who have occur at least
     * twice in the dataset.
     */
    private static LinkedHashSet<String> sameNameList_diffDep = new LinkedHashSet<>();

    /**
     * The sets which stores the names of different researchers who has a common
     * name with others.
     */
    private static LinkedHashSet<String> sameNameList_sameDep = new LinkedHashSet<>();

    /**
     * The name, university and department information of a researcher, as a
     * String.
     */
    private final String name, university, department;

    /**
     * The unique id of a researcher.
     */
    private int id;

    /**
     * The set of interests of a researcher.
     */
    private final LinkedHashSet<String> interests = new LinkedHashSet<>();

    /**
     * Constructor with parameter, constructing a new <code>Researcher</code>
     * instance with the name, university, department and id information.
     *
     * @param name the name of a researcher
     * @param university the university of a researcher
     * @param department the department of a researcher
     * @param id the id of a researcher, initially the row number of the record
     * in Excel.
     */
    public Researcher(String name, String university, String department, int id) {
        this.name = name.trim().replaceAll(" +", " ");
        this.university = university.trim();
        this.department = department.trim();
        this.id = id;
    }

    /**
     * Get the name of this researcher.
     *
     * @return the researcher name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the university name of this researcher.
     *
     * @return the researcher's university name
     */
    public String getUniversity() {
        return university.isEmpty() ? "not given" : university;
    }

    /**
     * Get the department name of the researcher.
     *
     * @return the researcher's department name.
     */
    public String getDepartment() {
        return department.isEmpty() ? "not given" : department;
    }

    /**
     * Get the id number of the researcher.
     *
     * @return the researcher's id number
     */
    public int getId() {
        return id;
    }

    /**
     * Get the interests list of the researcher, as a
     * <code>LinkedHashSet</code>.
     *
     * @return the researcher's interests set
     */
    public LinkedHashSet<String> getInterests() {
        return interests;
    }

    /**
     * Get the interests list of the researcher, as a String. The String will be
     * formatted into 5 interests per line.
     *
     * @return the researcher's interest as a String
     */
    public String getInterestsStr() {
        String str = "";
        int i = 1;
        for (String interest : interests) {
            if (i % 5 == 0) {
                str += "\n\t\t\t\t";
            }
            str += interest + ", ";
            i++;
        }
        if (str.length() != 0) {
            return str.substring(0, str.length() - 2);
        } else {
            return "None";
        }
    }

    /**
     * Get the number of distinct researchers in the researcher repository.
     *
     * @return the distinct researchers number
     */
    public static int getResearcherNum() {
        return researcherNum;
    }

    /**
     * Get the number of distinct interests in the interests repository.
     *
     * @return the distinct interests number
     */
    public static int getInterestNum() {
        return interestRepository.size();
    }

    /**
     * Get the distinct number of researchers with a specified interest.
     *
     * @param interest the specified interest for enquiry
     * @return the distinct number of researchers with a specified interest
     */
    public static int getResearcherNumByInterest(String interest) {
        TreeSet<Integer> researcherList = interestRepository.get(interest.trim().replaceAll(" +", " ").toLowerCase());
        return researcherList == null ? 0 : researcherList.size();
    }

    /**
     * Get a list of {@link Researcher researchers} instances of a specified
     * name. Each instance contains detailed information of this researcher.
     *
     * @param name the specified researcher's name
     * @return a list of {@link Researcher researchers} instances of a specified
     * name, each containing detailed information of a researcher.
     */
    public static LinkedList<Researcher> getResearcherInfo(String name) {
        return repository.get(name.trim().replaceAll(" +", " ").toLowerCase());
    }

    /**
     * Get the number of times two specified interests con-occur in one
     * researcher record.
     *
     * @param ia the name of one interest
     * @param ib the name of another interest
     * @return the number of times two interests con-occur
     */
    public static int getCooccurNum(String ia, String ib) {
        int num = 0, i = 0, j = 0;
        TreeSet<Integer> raList = interestRepository.get(ia.trim().replaceAll(" +", " ").toLowerCase());
        TreeSet<Integer> rbList = interestRepository.get(ib.trim().replaceAll(" +", " ").toLowerCase());
        if (raList == null || rbList == null) {
            return 0;
        }
        raList.add(Integer.MAX_VALUE);
        rbList.add(Integer.MAX_VALUE);
        Iterator<Integer> iterA = raList.iterator(), iterB = rbList.iterator();
        int idA = iterA.next(), idB = iterB.next();
        while (iterA.hasNext() && iterB.hasNext()) {
            while (idA < idB && iterA.hasNext()) {
                idA = iterA.next();
            }
            while (idB < idA && iterB.hasNext()) {
                idB = iterB.next();
            }
            if (idA == idB && idA != Integer.MAX_VALUE) {
                num++;
                idA = iterA.next();
                idB = iterB.next();
            }
        }
        return num;
    }

    /**
     * Add a researcher instance to the repository and offer it a valid id after
     * checking duplication. Check whether the researcher has appeared in
     * previous records. If so, combine two records and update the later record
     * researcher id.
     *
     * @param newResearcher the instance of a new researcher
     * @return the instance of this new researcher with his valid researcher id
     */
    public static Researcher add(Researcher newResearcher) {
        String name_low = newResearcher.name.toLowerCase();
        if (!repository.containsKey(name_low)) {
            LinkedList<Researcher> l = new LinkedList<>();
            l.add(newResearcher);
            repository.put(name_low, l);
            researcherNum++;
        } else {
            for (Researcher r : repository.get(name_low)) {
                if (areSame(newResearcher, r)) {
                    newResearcher.id = r.id;
                    if (sameNameList_sameDep.add(newResearcher.name)) {
                        sameNameNum_sameDep += 2;
                    } else {
                        sameNameNum_sameDep++;
                    }
                    return r;
                }
            }
            repository.get(name_low).add(newResearcher);
            researcherNum++;
            if (sameNameList_diffDep.add(newResearcher.name)) {
                sameNameNum_diffDep += 2;
            } else {
                sameNameNum_diffDep++;
            }
        }
        return newResearcher;
    }

    /**
     * Parese a String of interests list and add them to
     * {@link Researcher#interestRepository interests Repository}.
     *
     * @param interestsStr the interests String to be parsed
     */
    public void addToInterests(String interestsStr) {
        for (String str : interestsStr.trim().split(",")) {
            String interest = str.trim().replaceAll(" +", " ");
            if (!interest.isEmpty()) {
                interests.add(interest);
                interest = interest.toLowerCase();
                TreeSet<Integer> researcherList = interestRepository.get(interest);
                if (researcherList != null) {
                    researcherList.add(id);
                } else {
                    researcherList = new TreeSet<>();
                    researcherList.add(id);
                    interestRepository.put(interest, researcherList);
                }
            }
        }
    }

    /**
     * Check whether two researcher records with same name represent the same
     * person by comparing their university and department information.
     *
     * @param ra one instance of {@link Researcher researcher}
     * @param rb another instance of {@link Researcher researcher}
     * @return <code>true</code> if two record represent the same researcher
     */
    private static boolean areSame(Researcher ra, Researcher rb) {
        return ra.department.toLowerCase().equals(rb.department.toLowerCase()) && ra.university.toLowerCase().equals(rb.university.toLowerCase());

    }

    /**
     * Get a summary of the researcher repository, whose data come from the
     * Excel file. Announce the number of records with same name, and the
     * strategy to handle them, that is, considering records with same
     * department and university name as the same researcher, otherwise as
     * different researchers.
     *
     * @return the summary of the researcher repository read from Excel file
     */
    public static String getWarningInfo() {
        String res = "\tWARNING:\n\t  There are " + String.valueOf(sameNameNum_sameDep)
                + " records sharing same name, university and department information with at least one another.\n";
        res += "\tThey've been considered as the same people by the system and the attributes of theese records with the same name are merged.\n";
        res += "\t" + sameNameList_sameDep.toString() + "\n";
        res += "\t  Another " + String.valueOf(sameNameNum_diffDep)
                + " records sharing same name information with at least one another.\n\tThey've been considered as different people since they come from different deparment and univeristy.\n";
        res += "\t" + sameNameList_diffDep.toString() + "\n";
        return res;
    }

    /**
     * Write the information containing in the repository to a text file, in the
     * format required by the mallet API for further processing.
     */
    public static void createMalletData() {
        try {
            File file = new File("data/malletData.txt");
            file.createNewFile();
            BufferedWriter fw = new BufferedWriter(new FileWriter(file));
            for (LinkedList<Researcher> rList : repository.values()) {
                for (Researcher r : rList) {
                    String str = (r.name + "/" + r.id).replaceAll(" ", "@").toLowerCase() + " " + "X ";
                    for (String i : r.interests) {
                        str += i.replaceAll(" ", "") + " ";
                    }
                    str = str + "\n";
                    fw.write(str);
                }
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            System.err.println("Failed saving to file.");
        }
    }
}
