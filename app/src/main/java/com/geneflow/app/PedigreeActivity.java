// Activity responsible for rendering the familial risk visualizer
package com.geneflow.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.geneflow.app.db.DatabaseHelper;
import com.geneflow.app.ml.GeneticRiskClassifier;
import com.geneflow.app.model.Patient;
import com.geneflow.app.util.VcfReader;
import com.geneflow.app.vcf.VcfParser;

import java.util.List;

/** Screen 7 - family pedigree and member details. */
public class PedigreeActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private Patient primary;

    private TextView txtPrimaryStatus, txtRisk, txtDetailStatus, txtGenomicStatus, txtVariant, txtTarget, txtAlteration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedigree);

        db = DatabaseHelper.get(this);
        long id = getIntent().getLongExtra("patient_id", -1);
        primary = db.getPatient(id);

        TextView txtFatherStatus = findViewById(R.id.txtFatherStatus);
        TextView txtMotherStatus = findViewById(R.id.txtMotherStatus);
        TextView txtPrimaryName = findViewById(R.id.txtPrimaryName);
        txtPrimaryStatus = findViewById(R.id.txtPrimaryStatus);
        TextView txtSonStatus = findViewById(R.id.txtSonStatus);

        TextView txtDetailName = findViewById(R.id.txtDetailName);
        TextView txtDetailAge = findViewById(R.id.txtDetailAge);
        TextView txtDetailId = findViewById(R.id.txtDetailId);
        txtDetailStatus = findViewById(R.id.txtDetailStatus);
        txtRisk = findViewById(R.id.txtRisk);
        txtTarget = findViewById(R.id.txtTarget);
        txtVariant = findViewById(R.id.txtVariant);
        txtAlteration = findViewById(R.id.txtAlteration);
        txtGenomicStatus = findViewById(R.id.txtGenomicStatus);

        View btnBack = findViewById(R.id.btnBack);
        View btnUploadVcf = findViewById(R.id.btnUploadVcf);

        if (primary != null) {
            txtPrimaryName.setText(primary.getName());
            txtDetailName.setText("Name:  " + primary.getName());
            txtDetailAge.setText("Age:  " + primary.getAge());
            txtDetailId.setText("ID:  " + (primary.getId() == 0 ? "12345678" : primary.getId()));

            // Map family members onto the tree nodes by relation.
            List<Patient> family = db.getFamily(primary.getFamilyId());
            for (Patient m : family) {
                String rel = m.getRelation() == null ? "" : m.getRelation().toLowerCase();
                if (rel.contains("father")) txtFatherStatus.setText(statusLabel(m));
                else if (rel.contains("mother")) txtMotherStatus.setText(statusLabel(m));
                else if (rel.contains("son") || rel.contains("child")) txtSonStatus.setText(statusLabel(m));
            }
            txtPrimaryStatus.setText(statusLabel(primary));
            renderDetails();
        }

        btnBack.setOnClickListener(v -> finish());
        btnUploadVcf.setOnClickListener(v -> runAnalysis());
    }

    private void runAnalysis() {
        if (primary == null) return;
        try {
            String vcf = VcfReader.readSample(this);
            VcfParser parser = new VcfParser();
            GeneticRiskClassifier classifier = new GeneticRiskClassifier();
            GeneticRiskClassifier.Result r = classifier.analyse(parser.parse(vcf));

            primary.setStatus(r.statusColorCode);
            primary.setAcmgStatus(r.acmgStatusLine);
            primary.setLifetimeRisk(r.lifetimeRiskPercent);
            primary.setVcfLoaded(true);
            if (r.topVariant != null) {
                primary.setVariantGene(r.topVariant.gene);
                primary.setVariantClass(r.classification);
            }
            db.updatePatient(primary);
            txtPrimaryStatus.setText(statusLabel(primary));
            renderDetails();
            Toast.makeText(this, "VCF analysed (risk " + r.lifetimeRiskPercent + "%)",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not analyse VCF file", Toast.LENGTH_SHORT).show();
        }
    }

    private void renderDetails() {
        int risk = primary.getLifetimeRisk();
        txtRisk.setText(risk + "%");
        txtRisk.setTextColor(getColor(risk >= 50 ? R.color.status_red : R.color.text_active));

        String status = statusWord(primary);
        txtDetailStatus.setText("Status:  " + status);
        txtGenomicStatus.setText("Status:  " + status);

        String gene = primary.getVariantGene();
        String vClass = primary.getVariantClass();
        txtTarget.setText("Target:  " + (gene != null && !gene.isEmpty() ? targetFor(gene) : "—"));
        txtVariant.setText("Variant:  "
                + (gene == null ? "—" : gene) + " " + (vClass == null ? "" : vClass));
        txtAlteration.setText("Alteration:  "
                + (primary.getAlteration() == null || primary.getAlteration().isEmpty()
                    ? "c.5266dupC" : primary.getAlteration()));
    }

    private String targetFor(String gene) {
        if (gene.startsWith("BRCA")) return "HBOC";
        if (gene.equals("MLH1") || gene.equals("MSH2") || gene.equals("APC")) return "Lynch / CRC";
        return gene + " panel";
    }

    private String statusLabel(Patient p) {
        switch (p.getStatus()) {
            case Patient.STATUS_RED:   return "Positive";
            case Patient.STATUS_GREEN: return "Negative";
            case Patient.STATUS_BLUE:  return "Uncertain";
            default:                   return "No File";
        }
    }

    private String statusWord(Patient p) {
        switch (p.getStatus()) {
            case Patient.STATUS_RED:   return "Affected";
            case Patient.STATUS_GREEN: return "Unaffected";
            case Patient.STATUS_BLUE:  return "Uncertain";
            default:                   return "Unknown";
        }
    }
}
