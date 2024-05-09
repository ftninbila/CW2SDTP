package com.fatinie.sdtp.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.json.*;

public class AdmissionViewer extends JFrame {
    private JTextArea textArea;

    private final String apiUrlAllocations = "https://web.socem.plymouth.ac.uk/COMP2005/api/Allocations";
    private final String apiUrlEmployees = "https://web.socem.plymouth.ac.uk/COMP2005/api/Employees";
    private final String apiUrlAdmissions = "https://web.socem.plymouth.ac.uk/COMP2005/api/Admissions";
    private final String apiUrlPatients = "https://web.socem.plymouth.ac.uk/COMP2005/api/Patients";

    public AdmissionViewer() {
        setTitle("Admission Viewer");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1));

        JButton fetchAllAdmissionsButton = new JButton("List All Admissions");
        fetchAllAdmissionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fetchAllAdmissions();
            }
        });
        buttonPanel.add(fetchAllAdmissionsButton);

        JButton fetchAdmittedPatientsButton = new JButton("List Admitted Patients");
        fetchAdmittedPatientsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fetchAdmittedPatients();
            }
        });
        buttonPanel.add(fetchAdmittedPatientsButton);

        JButton mostAdmittedStaffButton = new JButton("Most Admitted Staff");
        mostAdmittedStaffButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                findMostAdmittedStaff();
            }
        });
        buttonPanel.add(mostAdmittedStaffButton);

        JButton zeroAdmissionsStaffButton = new JButton("Zero Admissions Staff");
        zeroAdmissionsStaffButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                findZeroAdmissionsStaff();
            }
        });
        buttonPanel.add(zeroAdmissionsStaffButton);

        panel.add(buttonPanel, BorderLayout.EAST);
        add(panel);
    }

    private void fetchAllAdmissions() {
        fetchAndDisplayData(apiUrlAdmissions);
    }

    private void fetchAdmittedPatients() {
        fetchAndDisplayData(apiUrlPatients);
    }

    private void findMostAdmittedStaff() {
        try {
            URL url = new URL(apiUrlAllocations);
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

                // Count admissions for each staff
                HashMap<Integer, Integer> staffAdmissions = new HashMap<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int staffId = jsonObject.getInt("employeeID");
                    staffAdmissions.put(staffId, staffAdmissions.getOrDefault(staffId, 0) + 1);
                }

// Find staff with maximum admissions
                int maxAdmissions = 0;
                int mostAdmittedEmployeeId = -1;

                for (Integer staffId : staffAdmissions.keySet()) {
                    int admissionsCount = staffAdmissions.get(staffId);
                    if (admissionsCount > maxAdmissions) {
                        maxAdmissions = admissionsCount;
                        mostAdmittedEmployeeId = staffId;
                    }
                }


                if (mostAdmittedEmployeeId != -1) {
                    // Fetch employee data
                    String employeeUrl = apiUrlEmployees + "/" + mostAdmittedEmployeeId;
                    JSONObject employeeJsonObject = fetchJsonObject(employeeUrl);

                    // Display the most admitted staff member
                    textArea.setText("Most Admitted Staff:\n" +
                            "ID: " + employeeJsonObject.getInt("id") + "\n" +
                            "Forename: " + employeeJsonObject.getString("forename") + "\n" +
                            "Surname: " + employeeJsonObject.getString("surname"));
                } else {
                    textArea.setText("No admissions found.");
                }
            } else {
                textArea.setText("Error: HTTP request failed with response code " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            textArea.setText("Error: " + e.getMessage());
        }
    }

    private void findZeroAdmissionsStaff() {
        try {
            // Fetch allocations data
            JSONArray allocationsJsonArray = fetchJsonArray(apiUrlAllocations);

            // Fetch employees data
            JSONArray employeesJsonArray = fetchJsonArray(apiUrlEmployees);

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
            JSONArray zeroAdmissionsStaff = new JSONArray();
            for (int i = 0; i < employeesJsonArray.length(); i++) {
                JSONObject employeeJsonObject = employeesJsonArray.getJSONObject(i);
                int employeeId = employeeJsonObject.getInt("id");
                int admissionsCount = employeeCounts.getInt(String.valueOf(employeeId));
                if (admissionsCount == 0) {
                    zeroAdmissionsStaff.put(employeeJsonObject);
                }
            }

            if (zeroAdmissionsStaff.length() > 0) {
                // Display employees with zero admissions
                textArea.setText("Zero Admissions Staff:\n" + zeroAdmissionsStaff.toString(4));
            } else {
                textArea.setText("No employees with zero admissions found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            textArea.setText("Error: " + e.getMessage());
        }
    }

    private void fetchAndDisplayData(String apiUrl) {
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
                textArea.setText(jsonArray.toString(4)); // Display JSON data in text area with indentation
            } else {
                textArea.setText("Error: HTTP request failed with response code " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            textArea.setText("Error: " + e.getMessage());
        }
    }

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
            throw new Exception("HTTP request failed with response code: " + responseCode);
        }
    }

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
            throw new Exception("HTTP request failed with response code: " + responseCode);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AdmissionViewer().setVisible(true);
            }
        });
    }
}
