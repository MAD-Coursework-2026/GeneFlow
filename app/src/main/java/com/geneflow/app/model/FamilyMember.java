package com.geneflow.app.model;

/** Lightweight holder for a member row in the multi-member family registration screen. */
public class FamilyMember {
    public String relation = "Father";
    public String name = "";
    public String gender = "";
    public String age = "";
    public String health = "";
    public String diseasePanel = "";
    public boolean vcfLoaded = false;

    public FamilyMember() { }

    public FamilyMember(String relation) {
        this.relation = relation;
    }
}
