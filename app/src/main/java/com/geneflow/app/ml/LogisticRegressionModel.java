package com.geneflow.app.ml;

/**
 * A simple binary classifier built completely from code — no external library,
 * no pre-trained weights, nothing downloaded from Hugging Face.
 *
 * It is a LOGISTIC REGRESSION model:  p = sigmoid(w·x + b)
 * trained with batch gradient descent on a small synthetic dataset of
 * genetic-variant features. The trained weights are then used to predict the
 * pathogenic probability of new variants extracted from a patient's VCF file.
 *
 * Features (see {@link com.geneflow.app.model.Variant#features()}):
 *   0 rarity            (rare variant -> more suspicious)
 *   1 inSilicoScore     (computational deleteriousness)
 *   2 conservation      (evolutionary conservation)
 *   3 clinvarPathogenic (reported pathogenic flag)
 *   4 lossOfFunction    (nonsense / frameshift / splice)
 *   5 geneRiskWeight    (known disease-gene weighting)
 */
public class LogisticRegressionModel {

    private static final int N_FEATURES = 6;
    private final double[] weights = new double[N_FEATURES];
    private double bias = 0.0;
    private boolean trained = false;

    /** Train once with the built-in synthetic dataset. */
    public void train() {
        double[][] x = trainingFeatures();
        int[] y = trainingLabels();
        train(x, y, 4000, 0.30);
        trained = true;
    }

    /** Batch gradient descent. */
    public void train(double[][] x, int[] y, int epochs, double learningRate) {
        int m = x.length;
        for (int epoch = 0; epoch < epochs; epoch++) {
            double[] gradW = new double[N_FEATURES];
            double gradB = 0.0;
            for (int i = 0; i < m; i++) {
                double pred = predictProba(x[i]);
                double error = pred - y[i];
                for (int j = 0; j < N_FEATURES; j++) {
                    gradW[j] += error * x[i][j];
                }
                gradB += error;
            }
            for (int j = 0; j < N_FEATURES; j++) {
                weights[j] -= learningRate * gradW[j] / m;
            }
            bias -= learningRate * gradB / m;
        }
    }

    /** Probability that a feature vector is pathogenic (0..1). */
    public double predictProba(double[] features) {
        double z = bias;
        for (int j = 0; j < N_FEATURES; j++) {
            z += weights[j] * features[j];
        }
        return sigmoid(z);
    }

    public boolean isTrained() { return trained; }

    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    /* -------------------------------------------------------------------- */
    /* Built-in synthetic training data.                                    */
    /* Hand-authored examples of pathogenic (1) and benign (0) variants.    */
    /* -------------------------------------------------------------------- */

    private double[][] trainingFeatures() {
        return new double[][] {
                // rarity, inSilico, conservation, clinvar, LoF, geneWeight
                {0.98, 0.95, 0.92, 1, 1, 0.95},  // classic pathogenic LoF in disease gene
                {0.96, 0.90, 0.88, 1, 0, 0.90},  // pathogenic missense
                {0.93, 0.85, 0.80, 1, 1, 0.85},
                {0.90, 0.88, 0.90, 0, 1, 0.92},
                {0.95, 0.80, 0.85, 1, 0, 0.80},
                {0.88, 0.78, 0.75, 0, 1, 0.88},
                {0.92, 0.92, 0.95, 1, 1, 0.70},
                {0.85, 0.70, 0.72, 1, 0, 0.78},
                {0.97, 0.96, 0.94, 1, 1, 0.99},
                {0.80, 0.65, 0.68, 0, 1, 0.82},

                {0.10, 0.10, 0.20, 0, 0, 0.10},  // common benign polymorphism
                {0.05, 0.15, 0.18, 0, 0, 0.05},
                {0.20, 0.22, 0.30, 0, 0, 0.20},
                {0.15, 0.18, 0.25, 0, 0, 0.12},
                {0.30, 0.25, 0.28, 0, 0, 0.22},
                {0.08, 0.12, 0.15, 0, 0, 0.08},
                {0.25, 0.30, 0.35, 0, 0, 0.30},
                {0.12, 0.20, 0.22, 0, 0, 0.18},
                {0.18, 0.10, 0.12, 0, 0, 0.14},
                {0.35, 0.32, 0.30, 0, 0, 0.25},

                // a few borderline / VUS-like rows to sharpen the boundary
                {0.60, 0.55, 0.50, 0, 0, 0.55},
                {0.55, 0.48, 0.45, 0, 0, 0.50},
                {0.70, 0.60, 0.58, 0, 1, 0.62},
                {0.40, 0.38, 0.40, 0, 0, 0.40}
        };
    }

    private int[] trainingLabels() {
        return new int[] {
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 0, 1, 0
        };
    }
}
