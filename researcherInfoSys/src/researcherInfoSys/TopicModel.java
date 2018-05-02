package researcherInfoSys;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * A helper class to implement Probabilistic Topic Model and recommend similar
 * researchers to a specified researcher.
 *
 * @author james
 */
public class TopicModel {

    /**
     * Map between the instance name to the distribution of each topic of this
     * instance.
     */
    private HashMap<String, double[]> topicDistribution = new HashMap<String, double[]>();

    /**
     * List of different topics' distributions of interests. Each element of the
     * list maps from each related interest name under this topic to the
     * probabilistic value of this interest.
     */
    private ArrayList<HashMap<String, Double>> interestDistribution = new ArrayList<HashMap<String, Double>>();

    /**
     * List of all instances read from data file.
     */
    private InstanceList instancesList;

    /**
     * DataAlphabet of all instances, used to map each index of the interest to
     * its name.
     */
    private Alphabet dataAlphabet;

    /**
     * Instance of the Topic Model.
     */
    private ParallelTopicModel model;

    /**
     * An array of sorted interests under each topic.
     */
    private ArrayList<TreeSet<IDSorter>> topicSortedInterests;

    /**
     * The number of topicsNum assigned.
     */
    private final int topicsNum;

    /**
     * String for formatting the output.
     */
    private static final String S1 = "  o     ", S2 = "  ✔     ", S3 = "        ";

    /**
     * Constructor with parameter, constructing a new <code>TopicModel</code>
     * instance with the number of topics. build up the topic model using the
     * data from text file, and organize the result into {@link TopicModel#topicDistribution topicDistribution},
     * {@link TopicModel#interestDistribution interestDistribution} for further
     * processing.
     *
     * @param topicsNum the assigned topics number
     */
    public TopicModel(int topicsNum) {
        ArrayList<Pipe> pipeList = new ArrayList<>();
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add(new TokenSequence2FeatureSequence());

        instancesList = new InstanceList(new SerialPipes(pipeList));
        dataAlphabet = instancesList.getDataAlphabet();
        try {
            Reader fileReader;
            fileReader = new InputStreamReader(new FileInputStream(new File("data/malletData.txt")), "UTF-8");
            instancesList.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1));
        } catch (FileNotFoundException e) {
            System.err.println("File not found.");
        } catch (UnsupportedEncodingException e) {
            System.err.println("Failed loading file.");
        }

        model = new ParallelTopicModel(topicsNum, 1.0, 0.01);
        model.addInstances(instancesList);
        model.setNumThreads(1);
        model.setNumIterations(2000);
        try {
            model.estimate();
            topicSortedInterests = model.getSortedWords();
        } catch (IOException e) {
            System.err.println("Failed building topic model ");
        }

        for (int i = 0; i < instancesList.size(); i++) {
            topicDistribution.put(instancesList.get(i).getName().toString(), model.getTopicProbabilities(i));
        }

        this.topicsNum = topicsNum;
        for (int i = 0; i < topicsNum; i++) {
            double s = 0;
            for (IDSorter idCountPair : topicSortedInterests.get(i)) {
                s += idCountPair.getWeight();
            }
            interestDistribution.add(new HashMap<>());
            for (IDSorter idCountPair : topicSortedInterests.get(i)) {
                interestDistribution.get(i).put(dataAlphabet.lookupObject(idCountPair.getID()).toString(), idCountPair.getWeight() / s);
            }
        }
    }

    /**
     * Get the topic distribution of a specified instance, providing its
     * researcher name and researcher id, and print the distribution if
     * required.
     *
     * @param name the researcher name of a instance
     * @param id the researcher id number of a instance
     * @param print is <code>true</code> if choose to print out the topic
     * distributuin
     * @return the topic distribution of a specified instance
     */
    public double[] getTopicDistribution(String name, int id, boolean print) {
        return TopicModel.this.getTopicDistribution(toInstanceName(name, id), print);
    }

    /**
     * Get the topic distribution of a specified instance, providing its
     * instanceName and print the distribution if required.
     *
     * @param instanceName the name of a instance, which is a combination of
     * researcher name and id
     * @param print is <code>true</code> if choose to print out the topic
     * distribution
     * @return the topic distribution of a specified instance
     */
    public double[] getTopicDistribution(String instanceName, boolean print) {
        if (!print) {
            return topicDistribution.get(instanceName);
        }
        double dist[] = topicDistribution.get(instanceName);
        int[] mainTopics = {-1, -1, -1, -1, -1};
        for (int i = 0; i < dist.length; i++) {
            double x = dist[i];
            int k = 4;
            while (k >= 0 && (mainTopics[k] == -1 || x > dist[mainTopics[k]])) {
                k--;
            }
            k++;
            for (int j = 4; j > k; j--) {
                mainTopics[j] = mainTopics[j - 1];
            }
            if (k <= 4) {
                mainTopics[k] = i;
            }
        }
        for (int i = 0; i < 5; i++) {
            String sFormat = (i == 0) ? "Composition Analysis\t= " : "\t\t\t+ ";
            System.out.format(S3 + sFormat + "%5.2f%% Topic%d (%s ......)\n", dist[mainTopics[i]] * 100, mainTopics[i], getHottestInterest(mainTopics[i]));
        }
        return topicDistribution.get(instanceName);
    }

    /**
     * Get the hottest 10 interests under a specified topic, providing the topic
     * index.
     *
     * @param topicIndex the index of a topic
     * @return the top 10 interests under one topic, as a String
     */
    private String getHottestInterest(int topicIndex) {
        StringBuilder hottestInterests = new StringBuilder();
        int rank = 0;
        Iterator<IDSorter> iterator = topicSortedInterests.get(topicIndex).iterator();
        while (iterator.hasNext() && rank < 10) {
            IDSorter idCountPair = iterator.next();
            hottestInterests.append(dataAlphabet.lookupObject(idCountPair.getID()) + " (" + String.format("%.0f", idCountPair.getWeight()) + ") ");
            rank++;
        }
        return hottestInterests.toString();
    }

    /**
     * Convert the name and id of a researcher to the instance name.
     *
     * @param name the name of a researcher
     * @param id the id number of a researcher
     * @return the corresponding instance name of this researcher
     */
    private static String toInstanceName(String name, int id) {
        return (name + "/" + id).replaceAll(" ", "@").toLowerCase();
    }

    /**
     * Get the {@link Researcher researcher} instance by its instance name.
     *
     * @param str the instanceName of the researcher
     * @return the {@link Researcher researcher} instance
     */
    private static Researcher getResearcherByInstance(String str) {
        String name = str.substring(0, str.indexOf("/")).replaceAll("@", " ");
        int id = Integer.parseInt(str.substring(str.indexOf("/") + 1));
        LinkedList<Researcher> researcherList = Researcher.getResearcherInfo(name);
        if (researcherList != null) {
            for (Researcher r : researcherList) {
                if (r.getId() == id) {
                    return r;
                }
            }
        }
        return null;
    }

    /**
     * Recommend and print out similar researchers of a specified researcher,
     * measure similarity using predictive conditional probability.
     *
     * @param researcher the specified researcher requiring recommendation.
     */
    public void recommend(Researcher researcher) {
        LinkedHashSet<String> interestList = researcher.getInterests();
        RecommendResult result = new RecommendResult(researcher, false);
        for (HashMap.Entry<String, double[]> topicDistEntry : topicDistribution.entrySet()) {
            double p = 1;
            for (String interest : interestList) {
                interest = interest.toLowerCase().replaceAll(" ", "");
                double s = 1E-8;
                double[] dist = topicDistEntry.getValue();
                for (int j = 0; j < dist.length; j++) {
                    if (interestDistribution.get(j).get(interest) != null) {
                        s += interestDistribution.get(j).get(interest) * topicDistribution.get(topicDistEntry.getKey())[j];
                    }
                }
                p = p * s;
            }
            result.add(getResearcherByInstance(topicDistEntry.getKey()), p);
        }
        result.output("predictive conditional probability", "Probability");
    }

    /**
     * Recommend and print out similar researchers of a specified researcher,
     * measure similarity using KL divergence.
     *
     * @param researcher the specified researcher requiring recommendation
     */
    public void recommend_KL(Researcher researcher) {
        double[] p = topicDistribution.get(toInstanceName(researcher.getName(), researcher.getId()));
        RecommendResult result = new RecommendResult(researcher, true);
        for (HashMap.Entry<String, double[]> topicDistEntry : topicDistribution.entrySet()) {
            double kl = Similarity.calKL(p, topicDistEntry.getValue());
            result.add(getResearcherByInstance(topicDistEntry.getKey()), kl);
        }
        result.output("KL divergence (asymmetric)", "KL value");
    }

    /**
     * Recommend and print out similar researchers of a specified researcher,
     * measure similarity using Cosine similarity.
     *
     * @param researcher the specified researcher requiring recommendation
     */
    public void recommend_Cosine(Researcher researcher) {
        double[] p = topicDistribution.get(toInstanceName(researcher.getName(), researcher.getId()));
        RecommendResult result = new RecommendResult(researcher, false);
        for (HashMap.Entry<String, double[]> topicDistEntry : topicDistribution.entrySet()) {
            double kl = Similarity.calCosineSimilarity(p, topicDistEntry.getValue());
            result.add(getResearcherByInstance(topicDistEntry.getKey()), kl);
        }
        result.output("cosine similarity", "Cos-similarity");
    }

    /**
     * Write the information containing in the topic model into an Arff file, in
     * the format required by the weka API for further processing.
     *
     * @param fileName the path of the Arff file needed to be written data into
     * @return A catalog which helps other APIs to retrieve researchers
     * conveniently.
     */
    public Researcher[] createArff(String fileName) {
        Researcher[] researcherList = new Researcher[topicDistribution.entrySet().size()];
        try {
            File file = new File(fileName);
            file.createNewFile();
            BufferedWriter fw = new BufferedWriter(new FileWriter(file));
            fw.write("@RELATION topicDistribution\n");
            for (int i = 0; i < topicsNum; i++) {
                fw.write("@ATTRIBUTE " + i + " REAL\n");
            }
            fw.write("@DATA\n");
            int i = 0;
            for (HashMap.Entry<String, double[]> topicDistEntry : topicDistribution.entrySet()) {
                researcherList[i] = getResearcherByInstance(topicDistEntry.getKey());
                i++;
                for (double k : topicDistEntry.getValue()) {
                    fw.write(k + " ");
                }
                fw.write("\n");
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            System.err.println("Failed saving to Arff file");
        }
        return researcherList;
    }

    /**
     * An inner class which mainly stores the recommended researchers list and
     * indicator value of each researcher.
     */
    private class RecommendResult {

        /**
         * It is <code> true </code> if the recommend list is arranged in the way
         * that indicator values are ascending.
         */
        private boolean isAscend;

        /**
         * The specified researcher needing for recommendation.
         */
        private Researcher owner;

        /**
         * The number of researchers to be recommended.
         */
        private static final int LEN = 5;

        /**
         * List of researchers to be recommended.
         */
        private ArrayList<Researcher> recommendList = new ArrayList<>();

        /**
         * The indicator value of each researcher.
         */
        private ArrayList<Double> valueList = new ArrayList<>();

        /**
         * Constructor with parameter, constructing a new
         * <code>RecommendResult</code> instance with the specified researcher
         * and whether it is ascending.
         *
         * @param r the specified researcher
         * @param isAscend is <code>true</code> if the recommed list is arranged
         * in the way that indicator values are ascending
         */
        public RecommendResult(Researcher r, boolean isAscend) {
            owner = r;
            this.isAscend = isAscend;
        }

        /**
         * Insert a researcher to the appropriate position in the list.
         *
         * @param researcher the researcher to be inserted
         * @param value the indicator value of the researcher
         */
        public void add(Researcher researcher, double value) {
            if (valueList.size() < LEN || (!isAscend && value > valueList.get(LEN - 1)) || (isAscend && value < valueList.get(LEN - 1))) {
                int indexInsert = LEN - 1;
                if (valueList.size() == LEN) {
                    while (indexInsert >= 0 && ((!isAscend && value > valueList.get(indexInsert)) || (isAscend && value < valueList.get(indexInsert)))) {
                        indexInsert--;
                    }
                    indexInsert++;
                    valueList.remove(LEN - 1);
                    recommendList.remove(LEN - 1);
                } else {
                    indexInsert = valueList.size();
                }
                valueList.add(indexInsert, value);
                recommendList.add(indexInsert, researcher);
            }
        }

        /**
         * Print out the recommend list to the command line window.
         *
         * @param algorithmStr the algorithm title to be displayed
         * @param indicatorStr the indicator title to be displayed
         */
        public void output(String algorithmStr, String indicatorStr) {
            System.out.println("\n" + S3 + "\t\t\t\t----Measure similarity using " + algorithmStr + "----");
            System.out.println(S1 + "Your Information");
            System.out.format(S3 + "%s (%s - %s)\n" + S3 + "Interests\t\t%s\n", owner.getName(), owner.getUniversity(), owner.getDepartment(), owner.getInterestsStr());
            getTopicDistribution(owner.getName(), owner.getId(), true);
            System.out.println(S2 + "Recommendation List");
            for (int i = 0; i < LEN; i++) {
                Researcher r = recommendList.get(i);
                System.out.format(" [%d]    %s (%s - %s)\n", i + 1, r.getName(), r.getUniversity(), r.getDepartment());
                System.out.println(S3 + indicatorStr + "\t\t" + valueList.get(i));
                System.out.println(S3 + "Interests\t\t" + r.getInterestsStr());
                getTopicDistribution(r.getName(), r.getId(), true);
                System.out.println();
            }
        }
    }
}
