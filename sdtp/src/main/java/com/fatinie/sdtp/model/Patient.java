package com.fatinie.sdtp.model;
public class Patient {
    private int id;
    private String forename;
    private String surname;
    private String nhsNumber;

    // Constructor
    public Patient(int id, String forename, String surname, String nhsNumber) {
        this.id = id;
        this.forename = forename;
        this.surname = surname;
        this.nhsNumber = nhsNumber;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getForename() {
        return forename;
    }

    public String getSurname() {
        return surname;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }
}


