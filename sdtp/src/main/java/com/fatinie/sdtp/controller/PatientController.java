package com.fatinie.sdtp.controller;

import com.fatinie.sdtp.model.Patient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
public class PatientController {
    @GetMapping("/f2")
public ResponseEntity<List<Patient>> getCurrentlyAdmittedPatients() {
    List<Patient> currentlyAdmittedPatients = new ArrayList<>();

    try {
        // Fetch admissions data
        JSONArray admissionsJsonArray = fetchJsonArray("https://web.socem.plymouth.ac.uk/COMP2005/api/Admissions");

        // Iterate through admissions to find currently admitted patients
        for (int i = 0; i < admissionsJsonArray.length(); i++) {
            JSONObject admissionJsonObject = admissionsJsonArray.getJSONObject(i);
            if (Objects.equals(admissionJsonObject.getString("dischargeDate"), "0001-01-01T00:00:00")) {
                int patientId = admissionJsonObject.getInt("patientID");

                // Fetch patient data
                JSONObject patientJsonObject = fetchJsonObject("https://web.socem.plymouth.ac.uk/COMP2005/api/Patients/" + patientId);

                // Create Patient object and add to the list of currently admitted patients
                Patient patient = new Patient(
                        patientId,
                        patientJsonObject.getString("forename"),
                        patientJsonObject.getString("surname"),
                        patientJsonObject.getString("nhsNumber")
                );
                currentlyAdmittedPatients.add(patient);
            }
        }

        if (currentlyAdmittedPatients.isEmpty()) {
            return ResponseEntity.notFound().build(); // No admitted patients found
        } else {
            return ResponseEntity.ok().body(currentlyAdmittedPatients);
        }
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

    // Helper method to fetch JSONArray from API
    private JSONArray fetchJsonArray(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return new JSONArray(response.toString());
        } else {
            System.err.println("HTTP request failed with response code: " + responseCode);
            throw new Exception("Failed to fetch data from API: " + apiUrl);
        }
    }

    // Helper method to fetch JSONObject from API
    private JSONObject fetchJsonObject(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return new JSONObject(response.toString());
        } else {
            System.err.println("HTTP request failed with response code: " + responseCode);
            throw new Exception("Failed to fetch data from API: " + apiUrl);
        }
    }
}