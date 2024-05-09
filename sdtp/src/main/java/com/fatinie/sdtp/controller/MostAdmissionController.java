package com.fatinie.sdtp.controller;

import com.fatinie.sdtp.model.Employees;
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
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MostAdmissionController {
    private final String apiUrl = "https://web.socem.plymouth.ac.uk/COMP2005/api/Allocations";

    @GetMapping("/f3")
    public ResponseEntity<Employees> getEmployeeWithMostAdmissions() {
        try {
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

                JSONArray jsonArray = new JSONArray(response.toString());
                JSONObject employeeCounts = new JSONObject();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int staffId = jsonObject.getInt("employeeID");

                    if (employeeCounts.has(String.valueOf(staffId))) {
                        employeeCounts.put(String.valueOf(staffId), employeeCounts.getInt(String.valueOf(staffId)) + 1);
                    } else {
                        employeeCounts.put(String.valueOf(staffId), 1);
                    }
                }

                // Find employee with maximum admissions
                int maxAdmissions = 0;
                int mostAdmittedEmployeeId = -1;

                Iterator<String> keys = employeeCounts.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    int admissionsCount = employeeCounts.getInt(key);
                    if (admissionsCount > maxAdmissions) {
                        maxAdmissions = admissionsCount;
                        mostAdmittedEmployeeId = Integer.parseInt(key);
                    }
                }

                if (mostAdmittedEmployeeId == -1) {
                    return ResponseEntity.notFound().build(); // No admissions found
                } else {
                    // Fetch employee data
                    String employeeUrl = "https://web.socem.plymouth.ac.uk/COMP2005/api/Employees/" + mostAdmittedEmployeeId;
                    JSONObject employeeJsonObject = fetchJsonObject(employeeUrl);

                    // Create Employee object
                    Employees mostAdmittedEmployee = new Employees(
                            employeeJsonObject.getInt("id"),
                            employeeJsonObject.getString("forename"),
                            employeeJsonObject.getString("surname")
                    );
                    return ResponseEntity.ok().body(mostAdmittedEmployee);
                }
            } else {
                System.err.println("HTTP request failed with response code: " + responseCode);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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