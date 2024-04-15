/**
 * File: Controller.java
 * Author: Kevin Tran
 * Created on: December, 20, 2023
 * Last Modified: February, 04, 2024
 * Description: Controller Class of OpenWeather App
 */
package app;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {

    @FXML
    private ComboBox<String> stateComboBox;
    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private Label resultLabel;

    private Map<String, List<String>> stateCitiesMap = new HashMap<>();

    private final OkHttpClient httpClient = new OkHttpClient();

    public void initialize() {
        loadStates();
        loadCityData();
    }

    // Get weather button, fetches data for the selected City
    @FXML
    private void handleLoadWeatherAction() {
        String state = stateComboBox.getSelectionModel().getSelectedItem();
        String city = cityComboBox.getSelectionModel().getSelectedItem();
        if (state != null && city != null && !city.isEmpty()) {
            // Retrieve the weather data for the selected city
            fetchWeatherData(city, getApiKey());
        } else {
            resultLabel.setText("Please select both a state and a city.");
        }
    }

    // State ComboBox selection handler
    @FXML
    private void handleStateSelection() {
        String selectedState = stateComboBox.getSelectionModel().getSelectedItem();
        if (selectedState != null && stateCitiesMap.containsKey(selectedState)) {
            cityComboBox.getItems().setAll(stateCitiesMap.get(selectedState));
        }
    }

    // Load state from text file
    private void loadStates() {
        try (InputStream is = getClass().getResourceAsStream("/resources/states.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (String state : line.split(",")) {
                    stateComboBox.getItems().add(state.trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultLabel.setText("Failed to load states");
        }
    }

    // Load cities from JSON from selected state
    private void loadCityData() {
        try (InputStream is = getClass().getResourceAsStream("/resources/cities.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                JsonArray cityArray = entry.getValue().getAsJsonArray();
                List<String> cities = new ArrayList<>();
                for (JsonElement cityElem : cityArray) {
                    cities.add(cityElem.getAsString());
                }
                stateCitiesMap.put(entry.getKey(), cities);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultLabel.setText("Failed to load city data");
        }
    }

    // Fetches weather data from city using OpenWeatherAPI
    private void fetchWeatherData(String city, String apiKey) {
        Request request = new Request.Builder()
                .url("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey)
                .build();

        httpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                updateWeatherLabel("Error fetching weather data.");
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    JsonElement jsonElement = gson.fromJson(response.body().charStream(), JsonElement.class);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    // Assuming the structure of the response contains a "main" object with a "temp" field
                    double tempKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();

                    //Convert Kelvin to Fahrenheit
                    double tempFahrenheit = (tempKelvin - 273.15) * 9 / 5 + 32;

                    String weatherInfo = String.format("Temperature in %s: %.2f °F", city, tempFahrenheit);
                    updateWeatherLabel(weatherInfo);
                } else {
                    updateWeatherLabel("Error: " + response.code());
                }
                if (response.body() != null) {
                    response.body().close();
                }
            }
        });
    }


    private void updateWeatherLabel(String weatherInfo) {
        javafx.application.Platform.runLater(() -> resultLabel.setText(weatherInfo));
    }

    // Gets API key from text file
    private String getApiKey() {
        try (InputStream inputStream = getClass().getResourceAsStream("/resources/api_key.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.readLine();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

}
