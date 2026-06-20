package com.geneflow.app;

import android.content.Intent;
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

/** Screen 6 - patient profile view. */
public class ProfileViewActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private Patient patient;
    private TextView txtAcmg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        db = DatabaseHelper.get(this);
        long id = getIntent().getLongExtra("patient_id", -1);
        patient = db.getPatient(id);

        TextView txtName = findViewById(R.id.txtName);
        TextView txtPatientId = findViewById(R.id.txtPatientId);
        TextView txtAge = findViewById(R.id.txtAge);
        TextView txtGender = findViewById(R.id.txtGender);
        TextView txtBlood = findViewById(R.id.txtBlood);
        TextView txtContact = findViewById(R.id.txtContact);
        TextView txtCity = findViewById(R.id.txtCity);
        TextView txtAddress = findViewById(R.id.txtAddress);
        TextView txtReason = findViewById(R.id.txtReason);
        TextView txtAllergies = findViewById(R.id.txtAllergies);
        TextView txtMeds = findViewById(R.id.txtMeds);
        txtAcmg = findViewById(R.id.txtAcmg);

        View btnBack = findViewById(R.id.btnBack);
        View btnUploadVcf = findViewById(R.id.btnUploadVcf);
        View btnViewPedigree = findViewById(R.id.btnViewPedigree);

        if (patient != null) {
            txtName.setText(safeUpper(patient.getName()));
            txtPatientId.setText("ID #" + (patient.getId() == 0 ? "01637357747" : patient.getId()));
            txtAge.setText("Age: " + patient.getAge());
            txtGender.setText("Gender:  " + orDash(patient.getGender()));
            txtBlood.setText("Blood Group:  " + orDash(patient.getBloodGroup()));
            txtContact.setText("Contact:  " + orDash(patient.getContact()));
            txtCity.setText("City:  " + orDash(patient.getCity()));
            txtAddress.setText("Address:  " + orDash(patient.getAddress()));
            txtReason.setText("Reason:  " + orDash(patient.getReason()));
            txtAllergies.setText("Allergies:  " + orDash(patient.getAllergies()));
            txtMeds.setText("Medications:  " + orDash(patient.getMedications()));
            renderAcmg();
        }

        btnBack.setOnClickListener(v -> finish());

        btnViewPedigree.setOnClickListener(v -> {
            Intent i = new Intent(this, PedigreeActivity.class);
            if (patient != null) i.putExtra("patient_id", patient.getId());
            startActivity(i);
        });

        btnUploadVcf.setOnClickListener(v -> runAnalysis());
    }

    private void runAnalysis() {
        if (patient == null) return;
        try {
            String vcf = VcfReader.readSample(this);
            VcfParser parser = new VcfParser();
            GeneticRiskClassifier classifier = new GeneticRiskClassifier();
            GeneticRiskClassifier.Result r = classifier.analyse(parser.parse(vcf));

            patient.setStatus(r.statusColorCode);
            patient.setAcmgStatus(r.acmgStatusLine);
            patient.setLifetimeRisk(r.lifetimeRiskPercent);
            patient.setVcfLoaded(true);
            if (r.topVariant != null) {
                patient.setVariantGene(r.topVariant.gene);
                patient.setVariantClass(r.classification);
            }
            db.updatePatient(patient);
            renderAcmg();
            Toast.makeText(this, "VCF analysed (risk " + r.lifetimeRiskPercent + "%)",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not analyse VCF file", Toast.LENGTH_SHORT).show();
        }
    }

    private void renderAcmg() {
        String line = patient.getAcmgStatus();
        if (line == null || line.isEmpty()) line = "PENDING | No VCF analysed";
        txtAcmg.setText(line);
        int color = line.contains("POSITIVE") ? R.color.status_red
                : line.contains("NEGATIVE") ? R.color.text_active
                : R.color.status_blue;
        txtAcmg.setTextColor(getColor(color));
    }

    private String orDash(String s) { return (s == null || s.isEmpty()) ? "—" : s; }

    private String safeUpper(String s) { return s == null ? "" : s.toUpperCase(); }
}
