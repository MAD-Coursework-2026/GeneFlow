package com.geneflow.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.geneflow.app.adapter.PatientAdapter;
import com.geneflow.app.db.DatabaseHelper;
import com.geneflow.app.ml.GeneticRiskClassifier;
import com.geneflow.app.model.Alert;
import com.geneflow.app.model.Patient;
import com.geneflow.app.util.VcfReader;
import com.geneflow.app.vcf.VcfParser;

import java.util.List;

/** Screen 4 - clinician dashboard. */
public class DashboardActivity extends AppCompatActivity implements PatientAdapter.Listener {

    private DatabaseHelper db;
    private PatientAdapter adapter;
    private RecyclerView recycler;
    private TextView txtTotal;
    private LinearLayout alertsContainer;

    // -1 means "no status filter applied"
    private int activeFilter = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = DatabaseHelper.get(this);

        recycler = findViewById(R.id.recyclerPatients);
        txtTotal = findViewById(R.id.txtTotalPatients);
        alertsContainer = findViewById(R.id.alertsContainer);

        adapter = new PatientAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        // Search
        final android.widget.EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                String term = s.toString().trim();
                if (term.isEmpty()) {
                    applyFilter(activeFilter);
                } else {
                    adapter.setItems(db.searchPatients(term));
                }
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        // New patient
        findViewById(R.id.btnNewPatient).setOnClickListener(v ->
                startActivity(new Intent(this, PatientRegistrationActivity.class)));

        // Status filters (tap a dot to filter, tap again to clear)
        findViewById(R.id.filterRed).setOnClickListener(v -> toggleFilter(Patient.STATUS_RED));
        findViewById(R.id.filterGrey).setOnClickListener(v -> toggleFilter(Patient.STATUS_GREY));
        findViewById(R.id.filterGreen).setOnClickListener(v -> toggleFilter(Patient.STATUS_GREEN));
        findViewById(R.id.filterBlue).setOnClickListener(v -> toggleFilter(Patient.STATUS_BLUE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFilter(activeFilter);
        renderAlerts();
    }

    private void toggleFilter(int status) {
        activeFilter = (activeFilter == status) ? -1 : status;
        applyFilter(activeFilter);
    }

    private void applyFilter(int status) {
        List<Patient> list = (status == -1) ? db.getAllPatients() : db.getPatientsByStatus(status);
        adapter.setItems(list);
        txtTotal.setText(String.valueOf(db.countPatients()));
    }

    private void renderAlerts() {
        alertsContainer.removeAllViews();
        List<Alert> alerts = db.getAlerts();
        LayoutInflater inf = LayoutInflater.from(this);
        for (Alert a : alerts) {
            TextView tv = new TextView(this);
            tv.setText(a.patientName + "  —  " + a.message);
            tv.setTextSize(13f);
            tv.setPadding(0, 8, 0, 8);
            tv.setTextColor(a.type == Patient.STATUS_RED
                    ? getColor(R.color.status_red)
                    : getColor(R.color.text_active));
            alertsContainer.addView(tv);
        }
    }

    /* ---------------- PatientAdapter.Listener ---------------- */

    @Override
    public void onOpenProfile(Patient p) {
        Intent i = new Intent(this, ProfileViewActivity.class);
        i.putExtra("patient_id", p.getId());
        startActivity(i);
    }

    @Override
    public void onUpload(Patient p) {
        // Run the from-scratch ML pipeline on the bundled sample VCF.
        try {
            String vcf = VcfReader.readSample(this);
            VcfParser parser = new VcfParser();
            GeneticRiskClassifier classifier = new GeneticRiskClassifier();
            GeneticRiskClassifier.Result r = classifier.analyse(parser.parse(vcf));

            p.setStatus(r.statusColorCode);
            p.setAcmgStatus(r.acmgStatusLine);
            p.setLifetimeRisk(r.lifetimeRiskPercent);
            p.setVcfLoaded(true);
            if (r.topVariant != null) {
                p.setVariantGene(r.topVariant.gene);
                p.setVariantClass(r.classification);
            }
            db.updatePatient(p);
            db.insertAlert(new Alert(p.getName(), "VCF file uploaded", Patient.STATUS_RED));

            Toast.makeText(this,
                    "Analysis complete: " + r.acmgStatusLine + "  (risk " + r.lifetimeRiskPercent + "%)",
                    Toast.LENGTH_LONG).show();
            applyFilter(activeFilter);
            renderAlerts();
        } catch (Exception e) {
            Toast.makeText(this, "Could not analyse VCF file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFamily(Patient p) {
        Intent i = new Intent(this, PedigreeActivity.class);
        i.putExtra("patient_id", p.getId());
        startActivity(i);
    }

    @Override
    public void onDelete(final Patient p) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_message, null);
        TextView msg = view.findViewById(R.id.txtMessage);
        ImageView img = view.findViewById(R.id.imgDialog);
        TextView ok = view.findViewById(R.id.btnDialogOk);
        msg.setText(R.string.patient_deleted);
        img.setImageResource(R.drawable.ic_trash);

        final AlertDialog dlg = new AlertDialog.Builder(this).setView(view).create();
        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Delete immediately, then confirm via the success dialog (matches screen 8).
        db.deletePatient(p.getId());
        applyFilter(activeFilter);

        ok.setOnClickListener(v -> dlg.dismiss());
        dlg.show();
    }
}
