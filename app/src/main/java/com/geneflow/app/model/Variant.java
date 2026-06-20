package com.geneflow.app.model;

/**
 * A single genetic variant parsed out of a VCF file plus the feature values
 * the ML model uses. Each numeric feature is normalised to roughly 0..1.
 */
public class Variant {
    public String chrom;
    public int pos;
    public String ref;
    public String alt;
    public String gene;

    // --- features fed to the logistic-regression model ---
    public double alleleFrequency;   // population frequency (rare variants -> higher risk)
    public double inSilicoScore;     // REVEL-style deleteriousness 0..1
    public double conservation;      // evolutionary conservation 0..1
    public double clinvarPathogenic; // 1 if reported pathogenic in ClinVar, else 0
    public double lossOfFunction;    // 1 if nonsense / frameshift / splice, else 0
    public double geneRiskWeight;    // known disease-gene weighting 0..1

    // --- model output ---
    public double pathogenicProbability;

    public Variant(String chrom, int pos, String ref, String alt, String gene) {
        this.chrom = chrom;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
        this.gene = gene;
    }

    /** Feature vector in the fixed order expected by the model. */
    public double[] features() {
        // Rarity is more informative than raw frequency, so invert AF.
        double rarity = 1.0 - Math.min(1.0, alleleFrequency * 100.0);
        return new double[] {
                rarity,
                inSilicoScore,
                conservation,
                clinvarPathogenic,
                lossOfFunction,
                geneRiskWeight
        };
    }
}
