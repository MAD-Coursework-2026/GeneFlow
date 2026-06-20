package com.geneflow.app.ml;

import com.geneflow.app.model.Variant;

import java.util.List;

/**
 * Clinical wrapper around {@link LogisticRegressionModel}.
 *
 * It turns the raw model probability into:
 *   - an ACMG-style classification string (Pathogenic / Likely Pathogenic / VUS /
 *     Likely Benign / Benign), and
 *   - a lifetime disease-risk percentage (0..100).
 *
 * The ACMG layer applies a few well-known rule signals on top of the model
 * (loss-of-function = PVS1-like, rarity = PM2-like, in-silico = PP3-like) so the
 * output resembles the criteria a clinician would expect.
 */
public class GeneticRiskClassifier {

    private final LogisticRegressionModel model;

    public GeneticRiskClassifier() {
        model = new LogisticRegressionModel();
        model.train();
    }

    public static class Result {
        public Variant topVariant;          // most significant variant found
        public String classification;       // Pathogenic / VUS / Benign ...
        public int lifetimeRiskPercent;     // 0..100
        public String acmgStatusLine;       // e.g. "POSITIVE | BRCA1 Pathogenic"
        public int statusColorCode;         // matches Patient.STATUS_*
    }

    /** Analyse a whole patient (all their variants) and summarise the result. */
    public Result analyse(List<Variant> variants) {
        Result r = new Result();

        if (variants == null || variants.isEmpty()) {
            r.classification = "No variants";
            r.lifetimeRiskPercent = 0;
            r.acmgStatusLine = "NO FILE";
            r.statusColorCode = 1; // grey
            return r;
        }

        // Score every variant; keep the most pathogenic one.
        Variant top = null;
        double topProb = -1;
        for (Variant v : variants) {
            v.pathogenicProbability = model.predictProba(v.features());
            if (v.pathogenicProbability > topProb) {
                topProb = v.pathogenicProbability;
                top = v;
            }
        }

        r.topVariant = top;
        r.classification = classify(topProb, top);
        r.lifetimeRiskPercent = toLifetimeRisk(topProb, top);
        r.acmgStatusLine = buildAcmgLine(top, r.classification);
        r.statusColorCode = colorFor(r.classification);
        return r;
    }

    /** Map probability + rule signals onto the 5-tier ACMG vocabulary. */
    private String classify(double p, Variant v) {
        boolean lof = v.lossOfFunction >= 1.0;
        boolean clinvar = v.clinvarPathogenic >= 1.0;

        if (p >= 0.90 || (lof && clinvar)) return "Pathogenic";
        if (p >= 0.75 || (lof && p >= 0.6)) return "Likely Pathogenic";
        if (p >= 0.40) return "VUS";
        if (p >= 0.20) return "Likely Benign";
        return "Benign";
    }

    /**
     * Convert pathogenic probability into a lifetime risk %.
     * The probability is scaled by the gene's penetrance weight so a pathogenic
     * variant in a high-penetrance gene yields a higher lifetime risk.
     */
    private int toLifetimeRisk(double p, Variant v) {
        double penetrance = 0.4 + 0.6 * v.geneRiskWeight; // 0.4 .. 1.0
        double risk = p * penetrance * 100.0;
        risk = Math.max(0, Math.min(99, risk));
        return (int) Math.round(risk);
    }

    private String buildAcmgLine(Variant v, String classification) {
        boolean positive = classification.contains("Pathogenic");
        String prefix = positive ? "POSITIVE" : (classification.equals("VUS") ? "UNCERTAIN" : "NEGATIVE");
        String gene = (v.gene == null || v.gene.isEmpty()) ? "—" : v.gene;
        return prefix + " | " + gene + " " + classification;
    }

    private int colorFor(String classification) {
        if (classification.contains("Pathogenic")) return 0;   // red
        if (classification.equals("VUS")) return 3;            // blue
        if (classification.contains("Benign")) return 2;       // green
        return 1;                                              // grey
    }
}
