package com.fatinie.sdtp.model;

import java.math.BigDecimal;

public class Admission {
    private final int id;
    private final String admissionDate;
    private final String dischargeDate;
    private final int patientID;

    // Constructor
    public Admission(int id, String admissionDate, String dischargeDate, int patientID) {
        this.id = id;
        this.admissionDate = admissionDate;
        this.dischargeDate = dischargeDate;
        this.patientID = patientID;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getAdmissionDate() {
        return admissionDate;
    }

    public String getDischargeDate() {
        return dischargeDate;
    }
    public int getPatientID() {
        return patientID;
    }
}
