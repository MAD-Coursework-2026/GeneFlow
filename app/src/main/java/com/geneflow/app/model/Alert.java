package com.geneflow.app.model;

/** Recent-alert entry shown at the bottom of the dashboard. */
public class Alert {
    public long id;
    public String patientName;
    public String message;
    public int type; 

    public Alert(String patientName, String message, int type) {
        this.patientName = patientName;
        this.message = message;
        this.type = type;
    }
}
