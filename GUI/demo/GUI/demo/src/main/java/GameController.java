package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class GameController {

    @FXML private Label playerNameLabel;
    @FXML private Label dayLabel;
    @FXML private Label savingsLabel;
    @FXML private Label waterLabel;

    @FXML private Label plantTypeLabel;
    @FXML private Label stageLabel;
    @FXML private Label wateredLabel;
    @FXML private Label readyHarvestLabel;

    @FXML private GridPane farmGrid;
    @FXML private VBox plantInfoBox;
    @FXML private Button excavateButton;

    private int currentDay = 1;
    private int savings = 1000;
    private int water = 10;
    private final int maxWater = 10;
    private boolean meteorTriggered = false;

    private boolean excavateMode = false;
    private int excavationsToday = 0;

    private static final int SIZE = 10;
    private static final int MAX_EXCAVATIONS_PER_DAY = 5;
    private static final int EXCAVATE_COST = 500;
    private static final int REFILL_COST = 100;

    private final Button[][] tileButtons = new Button[SIZE][SIZE];

    private final char[][] soil = {
            {'l','l','l','l','l','l','l','l','l','l'},
            {'g','s','g','g','s','s','g','g','s','g'},
            {'l','l','l','l','l','l','l','l','l','l'},
            {'s','s','s','g','g','g','g','s','s','s'},
            {'s','l','s','g','g','g','g','s','l','s'},
            {'s','l','s','g','g','g','g','s','l','s'},
            {'s','s','s','g','g','g','g','s','s','s'},
            {'l','l','l','l','l','l','l','l','l','l'},
            {'g','s','g','g','s','s','g','g','s','g'},
            {'l','l','l','l','l','l','l','l','l','l'}
    };

    private final boolean[][] meteorTiles = new boolean[SIZE][SIZE];
    private final boolean[][] permanentFertilizer = new boolean[SIZE][SIZE];
    private final boolean[][] wateredTiles = new boolean[SIZE][SIZE];

    private final String[][] plantedName = new String[SIZE][SIZE];
    private final int[][] plantedYield = new int[SIZE][SIZE];
    private final int[][] plantedMaxGrowth = new int[SIZE][SIZE];
    private final String[][] plantedPreferredSoil = new String[SIZE][SIZE];
    private final int[][] plantedCropPrice = new int[SIZE][SIZE];
    private final int[][] plantedGrowth = new int[SIZE][SIZE];
    private final String[][] plantedState = new String[SIZE][SIZE];

    private int selectedRow = -1;
    private int selectedCol = -1;

    public void initialize() {
        currentDay = 1;
        savings = 1000;
        water = 10;
        meteorTriggered = false;
        excavateMode = false;
        excavationsToday = 0;

        selectedRow = -1;
        selectedCol = -1;

        dayLabel.setText("Day " + currentDay);
        savingsLabel.setText("Savings: " + savings);
        waterLabel.setText("Water: " + water + "/" + maxWater);

        plantInfoBox.setVisible(false);

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                meteorTiles[r][c] = false;
                permanentFertilizer[r][c] = false;
                wateredTiles[r][c] = false;

                plantedName[r][c] = "";
                plantedYield[r][c] = 0;
                plantedMaxGrowth[r][c] = 0;
                plantedPreferredSoil[r][c] = "";
                plantedCropPrice[r][c] = 0;
                plantedGrowth[r][c] = 0;
                plantedState[r][c] = "";
            }
        }

        createGrid();
        updateExcavateButtonState();
    }

    public void setPlayerName(String name) {
        playerNameLabel.setText(name);
    }

    private void createGrid() {
        farmGrid.getChildren().clear();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Button tile = new Button();

                tile.setPrefSize(70, 70);
                tile.setMinSize(70, 70);
                tile.setMaxSize(70, 70);

                final int r = row;
                final int c = col;

                tile.setOnMouseEntered(e -> showTileInfo(r, c));
                tile.setOnMouseExited(e -> plantInfoBox.setVisible(false));

                tile.setOnAction(e -> {
                    if (excavateMode) {
                        handleTileExcavate(r, c);
                    } else {
                        selectTile(r, c);
                    }
                });

                tileButtons[row][col] = tile;
                farmGrid.add(tile, col, row);
            }
        }

        refreshAllTiles();
    }

    private void refreshAllTiles() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                updateTileAppearance(r, c, tileButtons[r][c]);
            }
        }
    }

    private void selectTile(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        refreshAllTiles();
    }

    private boolean isSelected(int row, int col) {
        return selectedRow == row && selectedCol == col;
    }

    private boolean hasPlant(int row, int col) {
        return plantedName[row][col] != null && !plantedName[row][col].isEmpty();
    }

    private void showTileInfo(int row, int col) {
        plantInfoBox.setVisible(true);

        if (meteorTiles[row][col]) {
            plantTypeLabel.setText("Soil Type: Meteorite");
        } else {
            String soilName = switch (soil[row][col]) {
                case 's' -> "Sand";
                case 'g' -> "Gravel";
                default -> "Loam";
            };

            if (permanentFertilizer[row][col]) {
                plantTypeLabel.setText("Soil Type: " + soilName + " (" + soil[row][col] + ") - Permanently Fertilized");
            } else {
                plantTypeLabel.setText("Soil Type: " + soilName + " (" + soil[row][col] + ")");
            }
        }

        stageLabel.setText("Tile: (" + (col + 1) + "," + (row + 1) + ")");
        wateredLabel.setText("Watered?: " + (wateredTiles[row][col] ? "Y" : "N"));

        if (hasPlant(row, col)) {
            readyHarvestLabel.setText(
                    plantedName[row][col] +
                            " | Stage: " + plantedState[row][col] +
                            " (" + plantedGrowth[row][col] + "/" + plantedMaxGrowth[row][col] + ")"
            );
        } else {
            readyHarvestLabel.setText("Plant: None");
        }
    }

    private String getTileText(int row, int col) {
        String baseText;

        if (hasPlant(row, col)) {
            String name = plantedName[row][col];
            baseText = name.substring(0, Math.min(2, name.length())).toLowerCase();
        } else if (permanentFertilizer[row][col]) {
            baseText = "(" + soil[row][col] + ")";
        } else {
            baseText = String.valueOf(soil[row][col]);
        }

        if (wateredTiles[row][col]) {
            baseText = "[" + baseText + "]";
        }

        return baseText;
    }

    private void updateTileAppearance(int row, int col, Button tile) {
        tile.setText(getTileText(row, col));

        if (meteorTiles[row][col]) {
            applyMeteorTileStyle(tile, isSelected(row, col));
        } else if (permanentFertilizer[row][col]) {
            applyPermanentFertilizedStyle(tile, isSelected(row, col));
        } else {
            applyNormalTileStyle(tile, isSelected(row, col));
        }
    }

    private void applyNormalTileStyle(Button tile, boolean selected) {
        String borderWidth = selected ? "3" : "1";
        tile.setStyle(
                "-fx-background-color: #f2f2f2;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: " + borderWidth + ";" +
                        "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: black;"
        );
    }

    private void applyMeteorTileStyle(Button tile, boolean selected) {
        String borderWidth = selected ? "3" : "1";
        tile.setStyle(
                "-fx-background-color: #bfbfbf;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: " + borderWidth + ";" +
                        "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: black;"
        );
    }

    private void applyPermanentFertilizedStyle(Button tile, boolean selected) {
        String borderWidth = selected ? "3" : "2";
        tile.setStyle(
                "-fx-background-color: #f2f2f2;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: " + borderWidth + ";" +
                        "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: black;"
        );
    }

    private void updateExcavateButtonState() {
        if (!meteorTriggered) {
            excavateButton.setDisable(true);
            excavateButton.setStyle(
                    "-fx-font-size: 30;" +
                            "-fx-border-color: gray;" +
                            "-fx-border-width: 2;" +
                            "-fx-background-color: #efefef;" +
                            "-fx-text-fill: gray;"
            );
            return;
        }

        if (excavationsToday >= MAX_EXCAVATIONS_PER_DAY) {
            excavateButton.setDisable(true);
            excavateButton.setStyle(
                    "-fx-font-size: 30;" +
                            "-fx-border-color: gray;" +
                            "-fx-border-width: 2;" +
                            "-fx-background-color: #efefef;" +
                            "-fx-text-fill: gray;"
            );
        } else {
            excavateButton.setDisable(false);
            excavateButton.setStyle(
                    "-fx-font-size: 30;" +
                            "-fx-border-color: black;" +
                            "-fx-border-width: 2;" +
                            "-fx-background-color: #efefef;" +
                            "-fx-text-fill: black;"
            );
        }
    }

    private void triggerMeteoriteEvent() {
        int[][] affectedTiles = {
                {1,1},{1,4},{1,5},{1,8},
                {3,3},{3,4},{3,5},{3,6},
                {4,1},{4,3},{4,4},{4,5},{4,6},{4,8},
                {5,1},{5,3},{5,4},{5,5},{5,6},{5,8},
                {6,3},{6,4},{6,5},{6,6},
                {8,1},{8,4},{8,5},{8,8}
        };

        for (int[] pos : affectedTiles) {
            int row = pos[0];
            int col = pos[1];

            meteorTiles[row][col] = true;
            permanentFertilizer[row][col] = false;
            wateredTiles[row][col] = false;

            plantedName[row][col] = "";
            plantedYield[row][col] = 0;
            plantedMaxGrowth[row][col] = 0;
            plantedPreferredSoil[row][col] = "";
            plantedCropPrice[row][col] = 0;
            plantedGrowth[row][col] = 0;
            plantedState[row][col] = "";
        }

        refreshAllTiles();
    }

    private void handleTileExcavate(int row, int col) {
        if (!meteorTiles[row][col]) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("You can only excavate grey meteor tiles.");
            alert.showAndWait();
            return;
        }

        if (excavationsToday >= MAX_EXCAVATIONS_PER_DAY) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Excavate Limit Reached");
            alert.setContentText("You have already used all 5 excavations for today.");
            alert.showAndWait();
            excavateMode = false;
            updateExcavateButtonState();
            return;
        }

        if (savings < EXCAVATE_COST) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Not Enough Savings");
            alert.setContentText("You need 500 savings to excavate this tile.");
            alert.showAndWait();
            excavateMode = false;
            return;
        }

        savings -= EXCAVATE_COST;
        savingsLabel.setText("Savings: " + savings);

        meteorTiles[row][col] = false;
        permanentFertilizer[row][col] = true;
        wateredTiles[row][col] = false;
        excavationsToday++;
        excavateMode = false;

        updateTileAppearance(row, col, tileButtons[row][col]);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Excavation Successful");
        alert.setContentText(
                "Tile excavated.\n" +
                        "Cost: " + EXCAVATE_COST + "\n" +
                        "This tile is now permanently fertilized.\n" +
                        "Remaining excavations today: " + (MAX_EXCAVATIONS_PER_DAY - excavationsToday)
        );
        alert.showAndWait();

        updateExcavateButtonState();
    }

    public boolean plantSelectedTile(String name, int price, int yield, int maxGrowth, String preferredSoil, int cropPrice) {
        if (selectedRow == -1 || selectedCol == -1) {
            return false;
        }

        if (meteorTiles[selectedRow][selectedCol]) {
            return false;
        }

        if (hasPlant(selectedRow, selectedCol)) {
            return false;
        }

        if (savings < price) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Not Enough Savings");
            alert.setContentText("You need " + price + " savings to buy " + name + ".");
            alert.showAndWait();
            return false;
        }

        savings -= price;
        savingsLabel.setText("Savings: " + savings);

        plantedName[selectedRow][selectedCol] = name;
        plantedYield[selectedRow][selectedCol] = yield;
        plantedMaxGrowth[selectedRow][selectedCol] = maxGrowth;
        plantedPreferredSoil[selectedRow][selectedCol] = preferredSoil;
        plantedCropPrice[selectedRow][selectedCol] = cropPrice;
        plantedGrowth[selectedRow][selectedCol] = 0;
        plantedState[selectedRow][selectedCol] = "Seedling";
        wateredTiles[selectedRow][selectedCol] = false;

        updateTileAppearance(selectedRow, selectedCol, tileButtons[selectedRow][selectedCol]);
        return true;
    }

    @FXML
    private void handlePlant() {
        if (selectedRow == -1 || selectedCol == -1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("No Tile Selected");
            alert.setContentText("Click a tile first before planting.");
            alert.showAndWait();
            return;
        }

        if (meteorTiles[selectedRow][selectedCol]) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Invalid Tile");
            alert.setContentText("You cannot plant on a meteorite tile.");
            alert.showAndWait();
            return;
        }

        if (hasPlant(selectedRow, selectedCol)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Tile Occupied");
            alert.setContentText("This tile already has a plant.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PlantStore.fxml"));
            Scene scene = new Scene(loader.load(), 1500, 850);

            PlantStoreController controller = loader.getController();

            Stage storeStage = new Stage();
            controller.setGameController(this);
            controller.setStage(storeStage);

            storeStage.setTitle("Plant Store");
            storeStage.initModality(Modality.APPLICATION_MODAL);
            storeStage.setScene(scene);
            storeStage.showAndWait();

            refreshAllTiles();

        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error");
            alert.setContentText("Could not open Plant Store.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleWater() {
        if (selectedRow == -1 || selectedCol == -1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("No Tile Selected");
            alert.setContentText("Click a tile first before watering.");
            alert.showAndWait();
            return;
        }

        if (meteorTiles[selectedRow][selectedCol]) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Invalid Tile");
            alert.setContentText("You cannot water a meteorite tile.");
            alert.showAndWait();
            return;
        }

        if (!hasPlant(selectedRow, selectedCol)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("No Plant");
            alert.setContentText("There is no plant on this tile.");
            alert.showAndWait();
            return;
        }

        if (wateredTiles[selectedRow][selectedCol]) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Already Watered");
            alert.setContentText("This tile is already watered.");
            alert.showAndWait();
            return;
        }

        if (water <= 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Watering Can Empty");
            alert.setContentText("Refill your watering can first.");
            alert.showAndWait();
            return;
        }

        water--;
        wateredTiles[selectedRow][selectedCol] = true;

        waterLabel.setText("Water: " + water + "/" + maxWater);
        updateTileAppearance(selectedRow, selectedCol, tileButtons[selectedRow][selectedCol]);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Tile Watered");
        alert.setContentText("Tile (" + (selectedCol + 1) + "," + (selectedRow + 1) + ") has been watered.");
        alert.showAndWait();
    }

    @FXML
    private void handleRefillWater() {
        if (water == maxWater) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Already Full");
            alert.setContentText("Your watering can is already full.");
            alert.showAndWait();
            return;
        }

        if (savings < REFILL_COST) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Not Enough Savings");
            alert.setContentText("You need 100 savings to refill the watering can.");
            alert.showAndWait();
            return;
        }

        savings -= REFILL_COST;
        water = maxWater;

        savingsLabel.setText("Savings: " + savings);
        waterLabel.setText("Water: " + water + "/" + maxWater);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Water Refilled");
        alert.setContentText("Your watering can has been refilled for 100.");
        alert.showAndWait();
    }

    @FXML
    private void handleFertilizer() {
        System.out.println("Fertilizer clicked");
    }

    @FXML
    private void handleHarvest() {
        System.out.println("Harvest clicked");
    }

    @FXML
    private void handleNextDay() {
        if (currentDay >= 20) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("The planting season is over.");
            alert.showAndWait();
            return;
        }

        savings += 50;
        savingsLabel.setText("Savings: " + savings);

        currentDay++;
        dayLabel.setText("Day " + currentDay);

        excavationsToday = 0;
        excavateMode = false;

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                wateredTiles[r][c] = false;
            }
        }

        if (currentDay == 16 && !meteorTriggered) {
            meteorTriggered = true;
            triggerMeteoriteEvent();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Meteorite Event");
            alert.setContentText("A meteorite has struck the field!");
            alert.showAndWait();
        }

        refreshAllTiles();
        updateExcavateButtonState();

        if (currentDay == 20) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Final Day");
            alert.setContentText("You have reached Day 20.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleExcavate() {
        if (!meteorTriggered) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Excavate is not available yet.");
            alert.showAndWait();
            return;
        }

        if (excavationsToday >= MAX_EXCAVATIONS_PER_DAY) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Excavate Limit Reached");
            alert.setContentText("You have already used all 5 excavations for today.");
            alert.showAndWait();
            updateExcavateButtonState();
            return;
        }

        int remaining = MAX_EXCAVATIONS_PER_DAY - excavationsToday;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Excavate Meteorite");
        alert.setContentText(
                "Cost per tile: " + EXCAVATE_COST + "\n" +
                        "Remaining excavations today: " + remaining + "\n\n" +
                        "Press OK, then click a grey tile to excavate."
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            excavateMode = true;
        }
    }
}