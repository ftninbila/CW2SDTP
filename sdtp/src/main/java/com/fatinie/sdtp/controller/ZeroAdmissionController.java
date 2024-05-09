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
import java.util.ArrayList;
import java.util.List;

@RestController
public class ZeroAdmissionController {
    private final String apiUrlAllocations = "https://web.socem.plymouth.ac.uk/COMP2005/api/Allocations";
    private final String apiUrlEmployees = "https://web.socem.plymouth.ac.uk/COMP2005/api/Employees";

    @GetMapping("/f4")
    public ResponseEntity<List<Employees>> getEmployeesWithZeroAdmissions() {
        try {
            // Fetch allocations data
            JSONArray allocationsJsonArray = fetchJsonArray(apiUrlAllocations);

            // Fetch employees data
            JSONArray employeesJsonArray = fetchJsonArray(apiUrlEmployees);

            List<Employees> employeesWithZeroAdmissions = new ArrayList<>();

            // Initialize employee counts with zero for all employees
            JSONObject employeeCounts = new JSONObject();
            for (int i = 0; i < employeesJsonArray.length(); i++) {
                JSONObject employeeJsonObject = employeesJsonArray.getJSONObject(i);
                employeeCounts.put(String.valueOf(employeeJsonObject.getInt("id")), 0);
            }

            // Count admissions for each employee
            for (int i = 0; i < allocationsJsonArray.length(); i++) {
                JSONObject allocationJsonObject = allocationsJsonArray.getJSONObject(i);
                int employeeId = allocationJsonObject.getInt("employeeID");
                int admissionsCount = employeeCounts.getInt(String.valueOf(employeeId));
                employeeCounts.put(String.valueOf(employeeId), admissionsCount + 1);
            }

            // Find employees with zero admissions
            for (int i = 0; i < employeesJsonArray.length(); i++) {
                JSONObject employeeJsonObject = employeesJsonArray.getJSONObject(i);
                int employeeId = employeeJsonObject.getInt("id");
                int admissionsCount = employeeCounts.getInt(String.valueOf(employeeId));
                if (admissionsCount == 0) {
                    Employees  employee = new Employees(
                            employeeId,
                            employeeJsonObject.getString("forename"),
                            employeeJsonObject.getString("surname")
                    );
                    employeesWithZeroAdmissions.add(employee);
                }
            }

            if (employeesWithZeroAdmissions.isEmpty()) {
                return ResponseEntity.notFound().build(); // No employees with zero admissions found
            } else {
                return ResponseEntity.ok().body(employeesWithZeroAdmissions);
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
}
