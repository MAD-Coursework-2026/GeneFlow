package com.geneflow.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.geneflow.app.model.Alert;
import com.geneflow.app.model.Patient;
import com.geneflow.app.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * GeneFlow local database.
 *
 * IMPORTANT (for the report): MySQL is a *server* database and cannot run on an
 * Android device. The correct mobile choice — and the one taught in MAD — is
 * SQLite, which Android ships with. This class is the whole data/backend layer:
 * it stores clinician accounts, patients and dashboard alerts, and exposes
 * simple CRUD methods to the rest of the app. If the project later needs a
 * central, multi-device database, you would add a remote MySQL server behind a
 * REST API and sync to it from here.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "geneflow.db";
    private static final int DB_VERSION = 1;

    // users table
    public static final String T_USERS = "users";
    // patients table
    public static final String T_PATIENTS = "patients";
    // alerts table
    public static final String T_ALERTS = "alerts";

    private static DatabaseHelper instance;

    /** Singleton keeps one open connection for the whole app (avoids leaks). */
    public static synchronized DatabaseHelper get(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, email TEXT UNIQUE, password TEXT)");

        db.execSQL("CREATE TABLE " + T_PATIENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, gender TEXT, age INTEGER, health TEXT, disease_panel TEXT, " +
                "status INTEGER, acmg_status TEXT, variant_gene TEXT, variant_class TEXT, " +
                "alteration TEXT, lifetime_risk INTEGER, vcf_loaded INTEGER, " +
                "reason TEXT, allergies TEXT, medications TEXT, blood_group TEXT, " +
                "contact TEXT, city TEXT, address TEXT, family_id INTEGER, relation TEXT)");

        db.execSQL("CREATE TABLE " + T_ALERTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_name TEXT, message TEXT, type INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + T_PATIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_ALERTS);
        onCreate(db);
    }

    /* ============================ USERS ============================ */

    public long insertUser(User u) {
        ContentValues cv = new ContentValues();
        cv.put("username", u.username);
        cv.put("email", u.email);
        cv.put("password", u.password);
        return getWritableDatabase().insert(T_USERS, null, cv);
    }

    public boolean validateLogin(String email, String password) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM " + T_USERS + " WHERE email=? AND password=?",
                new String[]{email, password});
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    public boolean emailExists(String email) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM " + T_USERS + " WHERE email=?", new String[]{email});
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    /* ============================ PATIENTS ============================ */

    public long insertPatient(Patient p) {
        return getWritableDatabase().insert(T_PATIENTS, null, toValues(p));
    }

    public void updatePatient(Patient p) {
        getWritableDatabase().update(T_PATIENTS, toValues(p), "id=?",
                new String[]{String.valueOf(p.getId())});
    }

    public void deletePatient(long id) {
        getWritableDatabase().delete(T_PATIENTS, "id=?", new String[]{String.valueOf(id)});
    }

    public List<Patient> getAllPatients() {
        return query("SELECT * FROM " + T_PATIENTS + " ORDER BY id ASC", null);
    }

    public List<Patient> getPatientsByStatus(int status) {
        return query("SELECT * FROM " + T_PATIENTS + " WHERE status=? ORDER BY id ASC",
                new String[]{String.valueOf(status)});
    }

    public List<Patient> searchPatients(String term) {
        return query("SELECT * FROM " + T_PATIENTS + " WHERE name LIKE ? ORDER BY id ASC",
                new String[]{"%" + term + "%"});
    }

    public List<Patient> getFamily(long familyId) {
        return query("SELECT * FROM " + T_PATIENTS + " WHERE family_id=? ORDER BY id ASC",
                new String[]{String.valueOf(familyId)});
    }

    public Patient getPatient(long id) {
        List<Patient> list = query("SELECT * FROM " + T_PATIENTS + " WHERE id=?",
                new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    public int countPatients() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + T_PATIENTS, null);
        int n = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return n;
    }

    /* ============================ ALERTS ============================ */

    public long insertAlert(Alert a) {
        ContentValues cv = new ContentValues();
        cv.put("patient_name", a.patientName);
        cv.put("message", a.message);
        cv.put("type", a.type);
        return getWritableDatabase().insert(T_ALERTS, null, cv);
    }

    public List<Alert> getAlerts() {
        List<Alert> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM " + T_ALERTS + " ORDER BY id DESC", null);
        while (c.moveToNext()) {
            Alert a = new Alert(
                    c.getString(c.getColumnIndexOrThrow("patient_name")),
                    c.getString(c.getColumnIndexOrThrow("message")),
                    c.getInt(c.getColumnIndexOrThrow("type")));
            a.id = c.getLong(c.getColumnIndexOrThrow("id"));
            list.add(a);
        }
        c.close();
        return list;
    }

    /* ============================ helpers ============================ */

    private ContentValues toValues(Patient p) {
        ContentValues cv = new ContentValues();
        cv.put("name", p.getName());
        cv.put("gender", p.getGender());
        cv.put("age", p.getAge());
        cv.put("health", p.getHealth());
        cv.put("disease_panel", p.getDiseasePanel());
        cv.put("status", p.getStatus());
        cv.put("acmg_status", p.getAcmgStatus());
        cv.put("variant_gene", p.getVariantGene());
        cv.put("variant_class", p.getVariantClass());
        cv.put("alteration", p.getAlteration());
        cv.put("lifetime_risk", p.getLifetimeRisk());
        cv.put("vcf_loaded", p.isVcfLoaded() ? 1 : 0);
        cv.put("reason", p.getReason());
        cv.put("allergies", p.getAllergies());
        cv.put("medications", p.getMedications());
        cv.put("blood_group", p.getBloodGroup());
        cv.put("contact", p.getContact());
        cv.put("city", p.getCity());
        cv.put("address", p.getAddress());
        cv.put("family_id", p.getFamilyId());
        cv.put("relation", p.getRelation());
        return cv;
    }

    private List<Patient> query(String sql, String[] args) {
        List<Patient> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(sql, args);
        while (c.moveToNext()) {
            list.add(fromCursor(c));
        }
        c.close();
        return list;
    }

    private Patient fromCursor(Cursor c) {
        Patient p = new Patient();
        p.setId(c.getLong(c.getColumnIndexOrThrow("id")));
        p.setName(c.getString(c.getColumnIndexOrThrow("name")));
        p.setGender(c.getString(c.getColumnIndexOrThrow("gender")));
        p.setAge(c.getInt(c.getColumnIndexOrThrow("age")));
        p.setHealth(c.getString(c.getColumnIndexOrThrow("health")));
        p.setDiseasePanel(c.getString(c.getColumnIndexOrThrow("disease_panel")));
        p.setStatus(c.getInt(c.getColumnIndexOrThrow("status")));
        p.setAcmgStatus(c.getString(c.getColumnIndexOrThrow("acmg_status")));
        p.setVariantGene(c.getString(c.getColumnIndexOrThrow("variant_gene")));
        p.setVariantClass(c.getString(c.getColumnIndexOrThrow("variant_class")));
        p.setAlteration(c.getString(c.getColumnIndexOrThrow("alteration")));
        p.setLifetimeRisk(c.getInt(c.getColumnIndexOrThrow("lifetime_risk")));
        p.setVcfLoaded(c.getInt(c.getColumnIndexOrThrow("vcf_loaded")) == 1);
        p.setReason(c.getString(c.getColumnIndexOrThrow("reason")));
        p.setAllergies(c.getString(c.getColumnIndexOrThrow("allergies")));
        p.setMedications(c.getString(c.getColumnIndexOrThrow("medications")));
        p.setBloodGroup(c.getString(c.getColumnIndexOrThrow("blood_group")));
        p.setContact(c.getString(c.getColumnIndexOrThrow("contact")));
        p.setCity(c.getString(c.getColumnIndexOrThrow("city")));
        p.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
        p.setFamilyId(c.getLong(c.getColumnIndexOrThrow("family_id")));
        p.setRelation(c.getString(c.getColumnIndexOrThrow("relation")));
        return p;
    }
}
