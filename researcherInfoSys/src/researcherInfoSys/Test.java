package researcherInfoSys;

import java.util.LinkedList;

/**
 * A class providing a command line interface for users to test Task 1-6
 *
 * @author james
 */
public class Test {

    /**
     * String for formatting the output.
     */
    private static final String S1 = "  o     ", S2 = "  ✔     ", S3 = "        ";

    /**
     * A topic model instance used for recommendation.
     */
    private static TopicModel tm = null;

    /**
     * A K-means model instance used for recommendation.
     */
    private static KmeansModel km = null;

    /**
     * Provide a command line menu for testing Task 1-6.
     */
    public static void showMenu() {
        boolean quit = false;
        LinkedList<Researcher> researcherList;
        String name;
        Researcher.createMalletData();
        do {
            System.out.println("\n" + S1 + "Please input your operation such as '3' for task 3, 'Q' to exit: ");
            System.out.println(S3 + "【1】 – calculate the number of distinct researchers in the dataset");
            System.out.println(S3 + "【2】 – calculate the number of distinct interests in the dataset");
            System.out.println(S3 + "【3】 – given a researcher’s name, show detailed information about him/her  (e.g. university, department, interests)");
            System.out.println(S3 + "【4】 - given an interest, calculate the number of researchers who have that interest");
            System.out.println(S3 + "【5】 – given two interests, show the number of times they co-occur");
            System.out.println(S3 + "【6】 – given a researcher, find similar researchers based on their interests");
            System.out.println(S3 + "【Q】 - exit the system");

            String str = Input.getString(S1 + "Enter your command here").trim().toLowerCase();

            switch (str) {
                case "1":
                    Timer.start();
                    System.out.println(S2 + "Number of distinct researchers = " + Researcher.getResearcherNum());
                    System.out.println(S3 + Timer.getTime());
                    break;
                case "2":
                    Timer.start();
                    System.out.println(S2 + "Number of distinct intesrests = " + Researcher.getInterestNum());
                    System.out.println(S3 + Timer.getTime());
                    break;
                case "3":
                    name = Input.getString(S3 + "Enter the researcher name");
                    Timer.start();
                    researcherList = Researcher.getResearcherInfo(name);
                    if (researcherList != null) {
                        System.out.println(S2 + "There're " + researcherList.size() + " researcher named " + name);
                        for (Researcher researcher : researcherList) {
                            System.out.println(S3 + researcher.getName());
                            System.out.println(S3 + "Univeristy\t\t" + researcher.getUniversity());
                            System.out.println(S3 + "Department\t\t" + researcher.getDepartment());
                            System.out.println(S3 + "Interests\t\t" + researcher.getInterests());
                        }
                    } else {
                        System.out.println(S2 + "Not found " + name);
                    }
                    System.out.println(S3 + Timer.getTime());
                    break;
                case "4":
                    String interest = Input.getString(S3 + "Enter the interest");
                    Timer.start();
                    System.out.println(S2 + "Number of distinct researchers with interest \"" + interest + "\" = " + Researcher.getResearcherNumByInterest(interest));
                    System.out.println(S3 + Timer.getTime());
                    break;
                case "5":
                    String interestA = Input.getString(S3 + "Enter the first interest");
                    String interestB = Input.getString(S3 + "Enter the second interest");
                    Timer.start();
                    System.out.println(S2 + "Number of times they co-occur = " + Researcher.getCooccurNum(interestA, interestB));
                    System.out.println(S3 + Timer.getTime());
                    break;
                case "6":
                    System.out.println(S3 + "- a.Probabilistic Topic Model\n" + S3 + "- b.K-Means Clustering\n" + S3 + "- c.Ranking by Cosine Similarity");
                    String algorithmStr;
                    do {
                        algorithmStr = Input.getString(S3 + "Choose an algorithm (a/b/c)").trim().toLowerCase();
                    } while (!algorithmStr.equals("a") && !algorithmStr.equals("b") && !algorithmStr.equals("c"));

                    String ch1 = "y";
                    if (tm != null) {
                        do {
                            ch1 = Input.getString(S3 + "A topic model already existed, do you want to rebuild it? (y/n)").trim().toLowerCase();
                        } while (!ch1.equals("y") && !ch1.equals("n"));
                    }
                    if (ch1.equals("y")) {
                        tm = new TopicModel(30);
                    }

                    if (algorithmStr.equals("b")) {
                        String ch2 = "y";
                        if (!ch1.equals("y") && km != null) {
                            do {
                                ch2 = Input.getString(S3 + "A K-means model already existed, do you want to rebuild it? (y/n)").trim().toLowerCase();
                            } while (!ch2.equals("y") && !ch2.equals("n"));
                        }
                        if (ch2.equals("y")) {
                            Researcher[] catalog = tm.createArff("data/topicDistribution.arff");
                            System.out.println(S2 + "Attributes of each researcher assigned.");
                            System.out.println(S1 + "Clustering using K-Means algorithm......");
                            km = new KmeansModel(100, catalog);
                        }
                    }

                    handleRecommendation(algorithmStr);
                    break;
                case "q":
                    quit = true;
                    System.out.println(S1 + "Bye-Bye");
                    break;
                default:
                    System.out.println(S3 + "Not a valid command");
            }

        } while (!quit);
    }

    /**
     * Call different method using different recommendation algorithms.
     *
     * @param option represents the specified algorithm user wants to use
     */
    private static void handleRecommendation(String option) {
        String name = Input.getString(S1 + "Enter the researcher name");
        LinkedList<Researcher> researcherList = Researcher.getResearcherInfo(name);
        if (researcherList != null) {
            System.out.println(S2 + "There're " + researcherList.size() + " researcher named " + name);
            for (Researcher researcher : researcherList) {
                switch (option) {
                    case "a":
                        tm.recommend(researcher);
                        tm.recommend_KL(researcher);
                        break;
                    case "b":
                        km.recommend(researcher);
                        break;
                    case "c":
                        tm.recommend_Cosine(researcher);
                }
            }
        } else {
            System.out.println(S2 + "Not found " + name);
        }
    }
}
