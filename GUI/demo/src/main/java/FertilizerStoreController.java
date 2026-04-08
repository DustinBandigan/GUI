package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class FertilizerStoreController {

    private GameController gameController;
    private Stage stage;

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleQuick() {
        buyFertilizer("Quick Fertilizer", 100, 2);
    }

    @FXML
    private void handleLasting() {
        buyFertilizer("Lasting Fertilizer", 150, 3);
    }

    @FXML
    private void handlePremium() {
        buyFertilizer("Premium Fertilizer", 200, 6);
    }

    private void buyFertilizer(String name, int price, int days) {
        boolean applied = gameController.fertilizeSelectedTile(name, price, days);

        if (applied && stage != null) {
            stage.close();
        } else if (!applied) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Fertilizer Failed");
            alert.setContentText("Could not apply fertilizer on the selected tile.");
            alert.showAndWait();
        }
    }
}