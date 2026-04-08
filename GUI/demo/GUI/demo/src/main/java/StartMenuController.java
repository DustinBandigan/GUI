package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class StartMenuController {

    @FXML
    private TextField nameField;

    @FXML
    private void handleConfirm(javafx.event.ActionEvent event) {
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Enter a name.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GameScreen.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 900);

            GameController controller = loader.getController();
            controller.setPlayerName(name);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLeaderboard() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("Leaderboard soon!");
        alert.showAndWait();
    }
}