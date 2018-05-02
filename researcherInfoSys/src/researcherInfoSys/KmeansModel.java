package researcherInfoSys;

import java.io.File;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * A helper class to realize K-Means Clustering algorithm, and recommend similar
 * researchers to a specified researcher.
 *
 * @author james
 */
public class KmeansModel {

    /**
     * A catalog which helps to retrieve {@link Researcher researcher}
     * conveniently. It stores the corresponding {@link Researcher researcher}
     * of each index of instance,
     */
    private Researcher[] catalog;

    /**
     * List of all instances retrieved from the Arff file.
     */
    private Instances instanceList;

    /**
     * Instance of the K-Means Model.
     */
    private SimpleKMeans KM;

    /**
     * Constructor with parameter, constructing a new <code>KmeansModel</code>
     * instance with the number of clusters and a researcher catalog.
     *
     * @param num the specified number of the clusters
     * @param catalog the catalog which helps to retrieve
     * {@link Researcher researcher}, it stores the corresponding
     * {@link Researcher researcher} of each index of instance
     */
    public KmeansModel(int num, Researcher[] catalog) {
        this.catalog = catalog;
        try {
            File file = new File("data/topicDistribution.arff");
            ArffLoader loader = new ArffLoader();
            loader.setFile(file);
            instanceList = loader.getDataSet();
            KM = new SimpleKMeans();
            KM.setNumClusters(num);
            KM.buildClusterer(instanceList);
        } catch (Exception e) {
            System.err.println("Failed clustering instances - " + e.getMessage());
        }
    }

    /**
     * Recommend and output similar researchers who are in a same cluster with a
     * specified researcher.
     *
     * @param owner the specified researcher needing recommendations
     */
    public void recommend(Researcher owner) {
        try {
            int ownerClusterNo = -1;
            for (int i = 0; i < catalog.length; i++) {
                if (catalog[i]!=null && catalog[i].equals(owner)) {
                    ownerClusterNo = KM.clusterInstance(instanceList.instance(i));
                    break;
                }
            }
            for (int i = 0; i < catalog.length; i++) {
                if (catalog[i]!=null && KM.clusterInstance(instanceList.instance(i)) == ownerClusterNo) {
                    System.out.format("        %s (%s - %s)\n", catalog[i].getName(), catalog[i].getUniversity(), catalog[i].getDepartment());
                    System.out.println("        Interests\t\t" + catalog[i].getInterestsStr() + "\n");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed during recommendation - "+e.getMessage());
        }
    }
}
