package com.geneflow.app.model;

/**
 * Patient entity. Mirrors the "patients" table in {@link com.geneflow.app.db.DatabaseHelper}.
 * Status codes used by the dashboard dots:
 *   0 = red  (positive / pathogenic)
 *   1 = grey (no VCF / pending)
 *   2 = green(negative / benign)
 *   3 = blue (VUS / in progress)
 */
public class Patient {

    public static final int STATUS_RED = 0;
    public static final int STATUS_GREY = 1;
    public static final int STATUS_GREEN = 2;
    public static final int STATUS_BLUE = 3;

    private long id;
    private String name;
    private String gender;
    private int age;
    private String health;
    private String diseasePanel;
    private int status;            // see constants above
    private String acmgStatus;     // e.g. "POSITIVE | BRCA1 Pathogenic"
    private String variantGene;    // e.g. "BRCA1"
    private String variantClass;   // e.g. "Pathogenic"
    private String alteration;     // e.g. "c.5266dupC"
    private int lifetimeRisk;      // 0..100, produced by the ML model
    private boolean vcfLoaded;
    private String reason;         // referral reason text
    private String allergies;
    private String medications;
    private String bloodGroup;
    private String contact;
    private String city;
    private String address;
    private long familyId;         // groups family members together
    private String relation;       // "Primary", "Father", "Mother", "Son" ...

    public Patient() { }

    public Patient(String name, String gender, int age, String health, String diseasePanel) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.health = health;
        this.diseasePanel = diseasePanel;
        this.status = STATUS_GREY;
        this.relation = "Primary";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getHealth() { return health; }
    public void setHealth(String health) { this.health = health; }

    public String getDiseasePanel() { return diseasePanel; }
    public void setDiseasePanel(String diseasePanel) { this.diseasePanel = diseasePanel; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getAcmgStatus() { return acmgStatus; }
    public void setAcmgStatus(String acmgStatus) { this.acmgStatus = acmgStatus; }

    public String getVariantGene() { return variantGene; }
    public void setVariantGene(String variantGene) { this.variantGene = variantGene; }

    public String getVariantClass() { return variantClass; }
    public void setVariantClass(String variantClass) { this.variantClass = variantClass; }

    public String getAlteration() { return alteration; }
    public void setAlteration(String alteration) { this.alteration = alteration; }

    public int getLifetimeRisk() { return lifetimeRisk; }
    public void setLifetimeRisk(int lifetimeRisk) { this.lifetimeRisk = lifetimeRisk; }

    public boolean isVcfLoaded() { return vcfLoaded; }
    public void setVcfLoaded(boolean vcfLoaded) { this.vcfLoaded = vcfLoaded; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getMedications() { return medications; }
    public void setMedications(String medications) { this.medications = medications; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public long getFamilyId() { return familyId; }
    public void setFamilyId(long familyId) { this.familyId = familyId; }

    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
}
