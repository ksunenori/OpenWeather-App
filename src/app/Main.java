/**
 * File: Main.java
 * Author: Kevin Tran
 * Created on: December, 20, 2023
 * Last Modified: February, 04, 2024
 * Description: Main Class of OpenWeather App
 */
package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("weather.fxml"));
        primaryStage.setTitle("Weather Forecast");
        primaryStage.setScene(new Scene(root, 400, 200));

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
