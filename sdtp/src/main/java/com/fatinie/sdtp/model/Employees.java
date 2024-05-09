package com.fatinie.sdtp.model;

public class Employees {
        private int id;
        private String forename;
        private String surname;

        // Constructor
        public Employees(int id, String forename, String surname) {
            this.id = id;
            this.forename = forename;
            this.surname = surname;
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
    }

