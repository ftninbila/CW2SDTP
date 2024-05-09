package com.fatinie.sdtp.controller;

import com.fatinie.sdtp.model.Admission;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AdmissionController {

    @GetMapping("/f1/{patientId}")
    public List<Admission> getAdmissionsId(@PathVariable final String patientId) {
        List<Admission> admissions = new ArrayList<>();

        try {
            // URL for the API endpoint
            String apiUrl = "https://web.socem.plymouth.ac.uk/COMP2005/api/Admissions";

            // Create URL object
            URL url = new URL(apiUrl);

            // Create HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method
            connection.setRequestMethod("GET");

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response
                JSONArray jsonArray = new JSONArray(response.toString());
                System.out.println(jsonArray);

                // Iterate through JSON array
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // Check if patientID matches
                    if (jsonObject.getInt("patientID") == Integer.parseInt(patientId)) {
                        Admission admission = new Admission(
                                jsonObject.getInt("id"),
                                jsonObject.getString("admissionDate"),
                                jsonObject.getString("dischargeDate"),
                                jsonObject.getInt("patientID")
                        );
                        admissions.add(admission);
                    }
                }
            }else {
                // Handle non-OK response
                System.err.println("HTTP request failed with response code: " + responseCode);
            }

            // Close connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return admissions;
    }


}
