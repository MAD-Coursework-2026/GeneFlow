package com.geneflow.app.util;

import android.content.Context;

import com.geneflow.app.db.DatabaseHelper;
import com.geneflow.app.model.Alert;
import com.geneflow.app.model.Patient;

/**
 * Seeds the demo data exactly as it appears in the Figma dashboard so the app
 * looks populated on first launch. Runs only once (guarded by {@link Session}).
 */
public class SeedData {

    public static void seedIfNeeded(Context context) {
        Session session = new Session(context);
        if (session.isSeeded()) return;

        DatabaseHelper db = DatabaseHelper.get(context);

        // Demo clinician account (matches the login screen).
        db.insertUser(new com.geneflow.app.model.User(
                "Dr. Sana Irfan", "sana@geneflow.com", "12345678"));

        // ---- Patients exactly as in dashboard screen 4 (status dots) ----
        addFullProfilePatient(db);                                   // Ahmed Khan (red)
        add(db, "Hafsa Usman",  "Female", 41, Patient.STATUS_GREY);
        add(db, "Mutahir Bhatti","Male",  29, Patient.STATUS_RED);
        add(db, "Riaz Ahmed",   "Male",   55, Patient.STATUS_RED);
        add(db, "Mateen Safi",  "Male",   38, Patient.STATUS_GREY);
        add(db, "Haroon Sheikh","Male",   47, Patient.STATUS_RED);
        add(db, "Minahil Bari", "Female", 33, Patient.STATUS_GREEN);

        // A few extra so the colour filters (screens 9-12) have content.
        add(db, "Ayesha Iqbal", "Female", 27, Patient.STATUS_RED);
        add(db, "Fatima Nasir", "Female", 36, Patient.STATUS_GREEN);
        add(db, "Sajida Arshad","Female", 60, Patient.STATUS_GREEN);
        add(db, "Burhan",       "Male",   19, Patient.STATUS_GREY);
        add(db, "Muhammad Ali", "Male",   44, Patient.STATUS_GREY);
        add(db, "Awais Tariq",  "Male",   31, Patient.STATUS_BLUE);
        add(db, "Rehan Tariq",  "Male",   23, Patient.STATUS_BLUE);

        // ---- Recent alerts (dashboard footer) ----
        db.insertAlert(new Alert("Haroon Sheikh", "VCF file uploaded", 0));
        db.insertAlert(new Alert("Minahil Bari", "Status change -ve", 2));

        session.setSeeded(true);
    }

    private static void addFullProfilePatient(DatabaseHelper db) {
        Patient p = new Patient("Ahmed Khan", "Male", 32, "Affected", "HBOC");
        p.setStatus(Patient.STATUS_RED);
        p.setAcmgStatus("POSITIVE | BRCA1 Pathogenic");
        p.setVariantGene("BRCA1");
        p.setVariantClass("Pathogenic");
        p.setAlteration("c.5266dupC");
        p.setLifetimeRisk(72);
        p.setVcfLoaded(true);
        p.setReason("Heavy maternal family history of early onset breast-cancer.");
        p.setAllergies("None Known");
        p.setMedications("Baseline supplements");
        p.setBloodGroup("B+");
        p.setContact("0300-XXXXXXX");
        p.setCity("Lahore");
        p.setAddress("Izmir Town, Lahore");
        p.setFamilyId(1);
        p.setRelation("Primary");
        db.insertPatient(p);
    }

    private static void add(DatabaseHelper db, String name, String gender, int age, int status) {
        Patient p = new Patient(name, gender, age, "—", "—");
        p.setStatus(status);
        p.setFamilyId(0);
        p.setRelation("Primary");
        if (status == Patient.STATUS_RED) {
            p.setAcmgStatus("POSITIVE | Pathogenic");
            p.setLifetimeRisk(60);
            p.setVcfLoaded(true);
        } else if (status == Patient.STATUS_GREEN) {
            p.setAcmgStatus("NEGATIVE | Benign");
            p.setLifetimeRisk(8);
            p.setVcfLoaded(true);
        } else if (status == Patient.STATUS_BLUE) {
            p.setAcmgStatus("UNCERTAIN | VUS");
            p.setLifetimeRisk(40);
            p.setVcfLoaded(true);
        } else {
            p.setAcmgStatus("NO FILE");
            p.setLifetimeRisk(0);
            p.setVcfLoaded(false);
        }
        db.insertPatient(p);
    }
}
