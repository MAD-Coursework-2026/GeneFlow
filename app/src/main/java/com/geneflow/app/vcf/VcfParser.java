package com.geneflow.app.vcf;

import com.geneflow.app.model.Variant;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Minimal, defensive VCF (Variant Call Format) parser.
 *
 * It reads standard VCF text:
 *   #CHROM  POS  ID  REF  ALT  QUAL  FILTER  INFO
 * and pulls feature values out of the INFO column when present:
 *   AF=     allele frequency
 *   GENE=   gene symbol
 *   REVEL=  in-silico deleteriousness
 *   GERP=   conservation (rescaled)
 *   CLNSIG= clinical significance (Pathogenic / Benign ...)
 *   ANN= / EFF=  effect annotation (used to flag loss-of-function)
 *
 * Anything missing falls back to a sensible default, so a sparse or slightly
 * malformed file never crashes the app.
 */
public class VcfParser {

    private static final String[] HIGH_RISK_GENES = {
            "BRCA1", "BRCA2", "TP53", "MLH1", "MSH2", "APC", "PTEN", "CFTR", "HBB"
    };

    private static final String[] LOF_TERMS = {
            "stop_gained", "frameshift", "splice", "nonsense", "start_lost", "stop_lost"
    };

    /** Parse the full text of a VCF file. Returns an empty list on bad input. */
    public List<Variant> parse(String vcfText) {
        List<Variant> variants = new ArrayList<>();
        if (vcfText == null || vcfText.trim().isEmpty()) {
            return variants;
        }

        String[] lines = vcfText.split("\\r?\\n");
        for (String line : lines) {
            if (line == null) continue;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue; // skip headers

            try {
                Variant v = parseLine(line);
                if (v != null) variants.add(v);
            } catch (Exception ignored) {
                // One bad line must never break the whole parse.
            }
        }
        return variants;
    }

    private Variant parseLine(String line) {
        String[] cols = line.split("\\t");
        if (cols.length < 5) {
            cols = line.split("\\s+"); // tolerate space-separated files
        }
        if (cols.length < 5) return null;

        String chrom = cols[0];
        int pos = safeInt(cols[1]);
        String ref = cols[3];
        String alt = cols[4];
        String info = cols.length >= 8 ? cols[7] : "";

        String gene = infoString(info, "GENE", "");
        double af = infoDouble(info, "AF", 0.001);
        double revel = infoDouble(info, "REVEL", -1);
        double gerp = infoDouble(info, "GERP", -1);
        String clnsig = infoString(info, "CLNSIG", "");
        String ann = info.toLowerCase(Locale.ROOT);

        Variant v = new Variant(chrom, pos, ref, alt, gene);
        v.alleleFrequency = af;

        // In-silico score: use REVEL if present, else derive from variant type.
        if (revel >= 0) {
            v.inSilicoScore = clamp(revel);
        } else {
            v.inSilicoScore = looksDamaging(alt, ref, ann) ? 0.8 : 0.3;
        }

        // Conservation: rescale GERP (~ -12..6) to 0..1, else default.
        if (gerp > -100) {
            v.conservation = clamp((gerp + 12.0) / 18.0);
        } else {
            v.conservation = 0.5;
        }

        v.clinvarPathogenic = clnsig.toLowerCase(Locale.ROOT).contains("pathogenic") ? 1.0 : 0.0;
        v.lossOfFunction = isLof(ann) ? 1.0 : 0.0;
        v.geneRiskWeight = geneWeight(gene);

        return v;
    }

    private boolean looksDamaging(String alt, String ref, String ann) {
        if (isLof(ann)) return true;
        // crude: single-base substitution treated as missense candidate
        return ref != null && alt != null && ref.length() == 1 && alt.length() == 1;
    }

    private boolean isLof(String annLower) {
        for (String term : LOF_TERMS) {
            if (annLower.contains(term)) return true;
        }
        return false;
    }

    private double geneWeight(String gene) {
        if (gene == null) return 0.3;
        String g = gene.toUpperCase(Locale.ROOT);
        for (String hr : HIGH_RISK_GENES) {
            if (hr.equals(g)) return 0.95;
        }
        return gene.isEmpty() ? 0.3 : 0.5;
    }

    /* ----------------------------- helpers ----------------------------- */

    private String infoString(String info, String key, String def) {
        for (String token : info.split(";")) {
            String[] kv = token.split("=", 2);
            if (kv.length == 2 && kv[0].equalsIgnoreCase(key)) {
                return kv[1].trim();
            }
        }
        return def;
    }

    private double infoDouble(String info, String key, double def) {
        String s = infoString(info, key, null);
        if (s == null) return def;
        // CLNSIG / AF can be comma lists; take the first number.
        String first = s.split(",")[0].trim();
        try {
            return Double.parseDouble(first);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private int safeInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private double clamp(double x) {
        if (x < 0) return 0;
        if (x > 1) return 1;
        return x;
    }
}
