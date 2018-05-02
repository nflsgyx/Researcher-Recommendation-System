package researcherInfoSys;

/**
 * A utility class which provide different kind of indicators to measure
 * similarity between two instances, including cosineSimilarity and KL
 * divergence currently.
 *
 * @author james
 */
public class Similarity {

    /**
     * Calculate the cosine similarity value of two instances
     *
     * @param a an array of values of different attributes of one instance
     * @param b an array of values of different attributes of one instance
     * @return the cosine similarity value of two arrays
     */
    public static double calCosineSimilarity(double[] a, double[] b) {
        double ab = 0, aa = 0, bb = 0;

        for (int i = 0; i < a.length; i++) {
            double x = -1 + a[i] * 2;
            double y = -1 + b[i] * 2;
            ab += x * y;
            aa += x * x;
            bb += y * y;
        }
        if (aa == 0 || bb == 0) {
            return -2;
        }
        aa = Math.sqrt(aa);
        bb = Math.sqrt(bb);
        return ab / (aa * bb);
    }

    /**
     * Calculate the KL divergence value of two instances
     *
     * @param p an array of values of different attributes of one instance
     * @param q an array of values of different attributes of one instance
     * @return the KL divergence value of two arrays
     */
    public static double calKL(double[] p, double[] q) {
        double kl = 0;
        for (int i = 0; i < p.length; i++) {
            kl += p[i] * Math.log(p[i] / q[i]) / Math.log(2);
        }
        return kl;
    }
}
