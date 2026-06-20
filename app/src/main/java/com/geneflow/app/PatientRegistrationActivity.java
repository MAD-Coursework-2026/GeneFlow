package com.geneflow.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.geneflow.app.db.DatabaseHelper;
import com.geneflow.app.model.Patient;

/** Screen 5 - primary patient registration plus multi-member family. */
public class PatientRegistrationActivity extends AppCompatActivity {

    private DatabaseHelper db;

    private android.widget.EditText etName, etAge, etMemberName, etMemberAge;
    private Spinner spGender, spHealth, spDiseasePanel, spMemberGender, spMemberHealth, spMemberPanel;
    private TextView txtUploadPrimary, txtUploadMember;
    private ImageView relFather, relMother, relChild;

    private boolean primaryVcfLoaded = false;
    private boolean memberVcfLoaded = false;
    private String selectedRelation = "Father";

    // Family id shared by the primary patient and any saved members of this session.
    private long familyId = System.currentTimeMillis();

    private static final String[] GENDERS = {"Gender", "Male", "Female", "Other"};
    private static final String[] HEALTH = {"Health", "Healthy", "Symptomatic", "Under Treatment"};
    private static final String[] PANELS = {"Disease Panel", "HBOC (BRCA)", "Lynch / CRC", "Cardio", "Whole Exome"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_registration);

        db = DatabaseHelper.get(this);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etMemberName = findViewById(R.id.etMemberName);
        etMemberAge = findViewById(R.id.etMemberAge);

        spGender = findViewById(R.id.spGender);
        spHealth = findViewById(R.id.spHealth);
        spDiseasePanel = findViewById(R.id.spDiseasePanel);
        spMemberGender = findViewById(R.id.spMemberGender);
        spMemberHealth = findViewById(R.id.spMemberHealth);
        spMemberPanel = findViewById(R.id.spMemberPanel);

        txtUploadPrimary = findViewById(R.id.txtUploadPrimary);
        txtUploadMember = findViewById(R.id.txtUploadMember);

        relFather = findViewById(R.id.relFather);
        relMother = findViewById(R.id.relMother);
        relChild = findViewById(R.id.relChild);

        bind(spGender, GENDERS);
        bind(spHealth, HEALTH);
        bind(spDiseasePanel, PANELS);
        bind(spMemberGender, GENDERS);
        bind(spMemberHealth, HEALTH);
        bind(spMemberPanel, PANELS);

        // Relation selection
        relFather.setOnClickListener(v -> selectRelation("Father"));
        relMother.setOnClickListener(v -> selectRelation("Mother"));
        relChild.setOnClickListener(v -> selectRelation("Son"));
        selectRelation("Father");

        // Upload source dialogs
        findViewById(R.id.btnUploadPrimary).setOnClickListener(v ->
                showUploadSource(true));
        findViewById(R.id.btnUploadMember).setOnClickListener(v ->
                showUploadSource(false));

        // Member delete / save
        findViewById(R.id.btnDeleteMember).setOnClickListener(v -> {
            clearMemberFields();
            showMessage(getString(R.string.patient_deleted), R.drawable.ic_trash);
        });
        findViewById(R.id.btnSaveMember).setOnClickListener(v -> saveMember());

        // Back / Proceed
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnProceed).setOnClickListener(v -> proceed());
    }

    private void bind(Spinner s, String[] items) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a);
    }

    private void selectRelation(String rel) {
        selectedRelation = rel;
        float dim = 0.4f, full = 1f;
        relFather.setAlpha(rel.equals("Father") ? full : dim);
        relMother.setAlpha(rel.equals("Mother") ? full : dim);
        relChild.setAlpha(rel.equals("Son") ? full : dim);
    }

    private void showUploadSource(final boolean isPrimary) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_upload_source, null);
        final AlertDialog dlg = new AlertDialog.Builder(this).setView(view).create();
        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        View.OnClickListener pick = v -> {
            if (isPrimary) {
                primaryVcfLoaded = true;
                txtUploadPrimary.setText("sample.vcf selected");
            } else {
                memberVcfLoaded = true;
                txtUploadMember.setText("sample.vcf selected");
            }
            dlg.dismiss();
        };

        view.findViewById(R.id.rowLocal).setOnClickListener(pick);
        view.findViewById(R.id.rowGoogleDrive).setOnClickListener(pick);
        view.findViewById(R.id.rowOneDrive).setOnClickListener(pick);
        view.findViewById(R.id.rowSecureLink).setOnClickListener(pick);
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dlg.dismiss());
        dlg.show();
    }

    private void saveMember() {
        String name = etMemberName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter the family member's name", Toast.LENGTH_SHORT).show();
            return;
        }
        Patient m = new Patient(name,
                valueOf(spMemberGender, GENDERS),
                parseAge(etMemberAge),
                valueOf(spMemberHealth, HEALTH),
                valueOf(spMemberPanel, PANELS));
        m.setRelation(selectedRelation);
        m.setFamilyId(familyId);
        m.setStatus(memberVcfLoaded ? Patient.STATUS_RED : Patient.STATUS_GREY);
        m.setVcfLoaded(memberVcfLoaded);
        db.insertPatient(m);

        showMessage(getString(R.string.patient_saved), R.drawable.ic_saved_check);
        clearMemberFields();
    }

    private void proceed() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter the patient's name", Toast.LENGTH_SHORT).show();
            return;
        }
        Patient p = new Patient(name,
                valueOf(spGender, GENDERS),
                parseAge(etAge),
                valueOf(spHealth, HEALTH),
                valueOf(spDiseasePanel, PANELS));
        p.setRelation("Primary");
        p.setFamilyId(familyId);
        p.setStatus(primaryVcfLoaded ? Patient.STATUS_RED : Patient.STATUS_GREY);
        p.setVcfLoaded(primaryVcfLoaded);
        if (primaryVcfLoaded) {
            p.setAcmgStatus("POSITIVE | BRCA1 Pathogenic");
            p.setVariantGene("BRCA1");
            p.setVariantClass("Pathogenic");
            p.setLifetimeRisk(72);
        }
        db.insertPatient(p);

        Toast.makeText(this, "Patient registered", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, DashboardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    private void showMessage(String message, int iconRes) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_message, null);
        ((TextView) view.findViewById(R.id.txtMessage)).setText(message);
        ((ImageView) view.findViewById(R.id.imgDialog)).setImageResource(iconRes);
        final AlertDialog dlg = new AlertDialog.Builder(this).setView(view).create();
        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.btnDialogOk).setOnClickListener(v -> dlg.dismiss());
        dlg.show();
    }

    private void clearMemberFields() {
        etMemberName.setText("");
        etMemberAge.setText("");
        spMemberGender.setSelection(0);
        spMemberHealth.setSelection(0);
        spMemberPanel.setSelection(0);
        memberVcfLoaded = false;
        txtUploadMember.setText(getString(R.string.upload_vcf_file));
    }

    private String valueOf(Spinner s, String[] items) {
        int pos = s.getSelectedItemPosition();
        // index 0 is the placeholder label; treat it as empty.
        return (pos <= 0) ? "" : items[pos];
    }

    private int parseAge(android.widget.EditText e) {
        try {
            return Integer.parseInt(e.getText().toString().trim());
        } catch (Exception ex) {
            return 0;
        }
    }
}
