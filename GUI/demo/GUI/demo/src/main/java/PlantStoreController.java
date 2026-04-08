package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class PlantStoreController {

    private GameController gameController;
    private Stage stage;

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleTurnip() {
        buyPlant("Turnip", 10, 2, 3, "loam", 6);
    }

    @FXML
    private void handleWheat() {
        buyPlant("Wheat", 15, 4, 5, "loam", 5);
    }

    @FXML
    private void handleThyme() {
        buyPlant("Thyme", 25, 6, 10, "gravel", 7);
    }

    @FXML
    private void handlePotato() {
        buyPlant("Potato", 20, 3, 7, "sand", 8);
    }

    @FXML
    private void handleTomato() {
        buyPlant("Tomato", 10, 9, 15, "sand", 3);
    }

    private void buyPlant(String name, int price, int yield, int maxGrowth, String preferredSoil, int cropPrice) {
        boolean planted = gameController.plantSelectedTile(name, price, yield, maxGrowth, preferredSoil, cropPrice);

        if (planted && stage != null) {
            stage.close();
        } else if (!planted) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Plant Failed");
            alert.setContentText("Could not plant on the selected tile.");
            alert.showAndWait();
        }
    }
}