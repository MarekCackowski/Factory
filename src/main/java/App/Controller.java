package App;

import javafx.fxml.FXML;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Slider;
import java.util.Objects;

public class Controller {
    private int[] baseValuesInQuantity = new int[8], quantity = new int[8],
            baseValuesInDuration = new int[10], duration = new int[10], durationCheck = new int[10];
    private String[] quantityString = new String[8], durationString = new String[10];
    private int i;
    private boolean isStarted = false;
    private Warehouses warehouses;
    private Simulation simulation;
    private ProductionFacility productionFacility;
    private Trolley trolley;
    private Route[] route = new Route[2];
    private ProductionLine[] productionLine = new ProductionLine[5];

    @FXML
    private Button stop1, stop2, start1, start2, save1, save2;
    @FXML
    private TextField material1, material2, trolleyCapacity, carCapacity, lowerLimit1, lowerLimit2, dumpCapacity, packageCapacity;
    @FXML
    private TextField productionTimeMin, productionTimeMax, trolleyWaitingTimeMin, trolleyWaitingTimeMax,
            collectionTimeMin, collectionTimeMax, firstRouteTimeMin, firstRouteTimeMax, secondRouteTimeMin, secondRouteTimeMax;
    @FXML
    private Slider productionTimeModifier, waitingTimeModifier;

    //gets base values for text fields from file
    @FXML
    void initialize() throws FileNotFoundException {
        String line;
        File data = new File("data.txt");
        Scanner scanner = new Scanner(data);
        line = scanner.nextLine();
        material1.setText(line);
        baseValuesInQuantity[0] = Integer.parseInt(material1.getText());
        line = scanner.nextLine();
        material2.setText(line);
        baseValuesInQuantity[1] = Integer.parseInt(material2.getText());
        line = scanner.nextLine();
        trolleyCapacity.setText(line);
        baseValuesInQuantity[2] = Integer.parseInt(trolleyCapacity.getText());
        line = scanner.nextLine();
        carCapacity.setText(line);
        baseValuesInQuantity[3] = Integer.parseInt(carCapacity.getText());
        line = scanner.nextLine();
        lowerLimit1.setText(line);
        baseValuesInQuantity[4] = Integer.parseInt(lowerLimit1.getText());
        line = scanner.nextLine();
        lowerLimit2.setText(line);
        baseValuesInQuantity[5] = Integer.parseInt(lowerLimit2.getText());
        line = scanner.nextLine();
        dumpCapacity.setText(line);
        baseValuesInQuantity[6] = Integer.parseInt(dumpCapacity.getText());
        line = scanner.nextLine();
        packageCapacity.setText(line);
        baseValuesInQuantity[7] = Integer.parseInt(packageCapacity.getText());
        line = scanner.nextLine();
        productionTimeMin.setText(line);
        baseValuesInDuration[0] = Integer.parseInt(productionTimeMin.getText());
        line = scanner.nextLine();
        productionTimeMax.setText(line);
        baseValuesInDuration[1] = Integer.parseInt(productionTimeMax.getText());
        line = scanner.nextLine();
        trolleyWaitingTimeMin.setText(line);
        baseValuesInDuration[2] = Integer.parseInt(trolleyWaitingTimeMin.getText());
        line = scanner.nextLine();
        trolleyWaitingTimeMax.setText(line);
        baseValuesInDuration[3] = Integer.parseInt(trolleyWaitingTimeMax.getText());
        line = scanner.nextLine();
        collectionTimeMin.setText(line);
        baseValuesInDuration[4] = Integer.parseInt(collectionTimeMin.getText());
        line = scanner.nextLine();
        collectionTimeMax.setText(line);
        baseValuesInDuration[5] = Integer.parseInt(collectionTimeMax.getText());
        line = scanner.nextLine();
        firstRouteTimeMin.setText(line);
        baseValuesInDuration[6] = Integer.parseInt(firstRouteTimeMin.getText());
        line = scanner.nextLine();
        firstRouteTimeMax.setText(line);
        baseValuesInDuration[7] = Integer.parseInt(firstRouteTimeMax.getText());
        line = scanner.nextLine();
        secondRouteTimeMin.setText(line);
        baseValuesInDuration[8] = Integer.parseInt(secondRouteTimeMin.getText());
        line = scanner.nextLine();
        secondRouteTimeMax.setText(line);
        baseValuesInDuration[9] = Integer.parseInt(secondRouteTimeMax.getText());
        scanner.close();
        save();
    }

    @FXML
    protected void start() {
        if (isStarted) {
            productionFacility.stopFacility();
            trolley.stopTrolley();
        }
        warehouses = new Warehouses(quantity[0], quantity[1], 0);
        productionFacility = new ProductionFacility(quantity[6], quantity[3], quantity[4], quantity[5], warehouses);
        trolley = new Trolley(quantity[2], quantity[7], duration[2], duration[3],
                duration[4], duration[5], productionFacility, warehouses);
        for (int i = 0; i < 2; i++) {
            route[i] = new Route(i, quantity[i + 4], duration[6], duration[7],
                    duration[8], duration[9], warehouses, productionFacility);
        }
        for (int i = 0; i < 5; i++) {
            productionLine[i] = new ProductionLine(i, duration[0], duration[1],
                    productionFacility, 240, 45 + 55 * i);
        }
        simulation = new Simulation(productionFacility, route, trolley, productionLine);
        simulation.start();
        productionFacility.startFacility();
        trolley.startTrolley();
        isStarted = true;
        start1.setDisable(true);
        start2.setDisable(true);
        stop1.setDisable(false);
        stop2.setDisable(false);
        productionTimeModifier.setDisable(false);
        waitingTimeModifier.setDisable(false);
    }
    @FXML
    protected void stop() {
        simulation.end = true;
        stop1.setDisable(true);
        stop2.setDisable(true);
        start1.setDisable(false);
        start2.setDisable(false);
        productionTimeModifier.setValue(1);
        waitingTimeModifier.setValue(1);
        productionTimeModifier.setDisable(true);
        waitingTimeModifier.setDisable(true);
    }
    @FXML
    protected void save() {

        //checks if value typed in quantity text field is not empty
        for (i = 0; i < 8; i++) {
            if (!Objects.equals(quantityString[i], "") && quantityString[i] != null)
                quantity[i] = Integer.parseInt(quantityString[i]);
            else
                quantity[i] = baseValuesInQuantity[i];
        }
        for (i = 0; i < 10; i++) {
            if (!Objects.equals(durationString[i], "") && durationString[i] != null)
                durationCheck[i] = Integer.parseInt(durationString[i]);
            else
                durationCheck[i] = duration[i];
        }

        //set both values in time text field to empty if the min value is greater
        if (durationCheck[0] > durationCheck[1]) {
            productionTimeMin.setText("");
            productionTimeMax.setText("");
            durationString[0] = "";
            durationString[1] = "";
        }
        if (durationCheck[2] > durationCheck[3]) {
            trolleyWaitingTimeMin.setText("");
            trolleyWaitingTimeMax.setText("");
            durationString[2] = "";
            durationString[3] = "";
        }
        if (durationCheck[4] > durationCheck[5]) {
            collectionTimeMin.setText("");
            collectionTimeMax.setText("");
            durationString[4] = "";
            durationString[5] = "";
        }
        if (durationCheck[6] > durationCheck[7]) {
            firstRouteTimeMin.setText("");
            firstRouteTimeMax.setText("");
            durationString[6] = "";
            durationString[7] = "";
        }
        if (durationCheck[8] > durationCheck[9]) {
            secondRouteTimeMin.setText("");
            secondRouteTimeMax.setText("");
            durationString[8] = "";
            durationString[9] = "";
        }
        //checks if value typed in time text field is not empty
        for (i = 0; i < 10; i++) {
            if (!Objects.equals(durationString[i], "") && durationString[i] != null)
                duration[i] = durationCheck[i];
            else
                duration[i] = baseValuesInDuration[i];
        }
        save1.setDisable(true);
        save2.setDisable(true);
    }

    //all methods below are checking if every sign typed in text fields is numeric (if not whole text field is cleared)
    //every change is being saved to strings checked above
    @FXML
    protected void qMaterial1() {
        for (i = 0; i < material1.getText().length(); i++)
            if (material1.getText().charAt(i) < '0' || material1.getText().charAt(i) > '9') {
                material1.setText("");
            }
        quantityString[0] = material1.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void qMaterial2() {
        for (i = 0; i < material2.getText().length(); i++)
            if (material2.getText().charAt(i) < '0' || material2.getText().charAt(i) > '9') {
                material2.setText("");
            }
        quantityString[1] = material2.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void qTrolleyCapacity() {
        for (i = 0; i < trolleyCapacity.getText().length(); i++)
            if (trolleyCapacity.getText().charAt(i) < '0' || trolleyCapacity.getText().charAt(i) > '9') {
                trolleyCapacity.setText("");
            }
        quantityString[2] = trolleyCapacity.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void qCarCapacity() {
        for (i = 0; i < carCapacity.getText().length(); i++)
            if (carCapacity.getText().charAt(i) < '0' || carCapacity.getText().charAt(i) > '9') {
                carCapacity.setText("");
            }
        quantityString[3] = carCapacity.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void qLowerLimit1() {
        for (i = 0; i < lowerLimit1.getText().length(); i++)
            if (lowerLimit1.getText().charAt(i) < '0' || lowerLimit1.getText().charAt(i) > '9') {
                lowerLimit1.setText("");
            }
        quantityString[4] = lowerLimit1.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void qLowerLimit2() {
        for (i = 0; i < lowerLimit2.getText().length(); i++)
            if (lowerLimit2.getText().charAt(i) < '0' || lowerLimit2.getText().charAt(i) > '9') {
                lowerLimit2.setText("");
            }
        quantityString[5] = lowerLimit2.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void qDumpCapacity() {
        for (i = 0; i < dumpCapacity.getText().length(); i++)
            if (dumpCapacity.getText().charAt(i) < '0' || dumpCapacity.getText().charAt(i) > '9') {
                dumpCapacity.setText("");
            }
        quantityString[6] = dumpCapacity.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void qPackageCapacity() {
        for (i = 0; i < packageCapacity.getText().length(); i++)
            if (packageCapacity.getText().charAt(i) < '0' || packageCapacity.getText().charAt(i) > '9') {
                packageCapacity.setText("");
            }
        quantityString[7] = packageCapacity.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }

    @FXML
    protected void tProductionTimeMin() {
        for (i = 0; i < productionTimeMin.getText().length(); i++)
            if (productionTimeMin.getText().charAt(i) < '0' || productionTimeMin.getText().charAt(i) > '9') {
                productionTimeMin.setText("");
            }
        durationString[0] = productionTimeMin.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void tProductionTimeMax() {
        for (i = 0; i < productionTimeMax.getText().length(); i++)
            if (productionTimeMax.getText().charAt(i) < '0' || productionTimeMax.getText().charAt(i) > '9') {
                productionTimeMax.setText("");
            }
        durationString[1] = productionTimeMax.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void tTrolleyWaitingTimeMin() {
        for (i = 0; i < trolleyWaitingTimeMin.getText().length(); i++)
            if (trolleyWaitingTimeMin.getText().charAt(i) < '0' || trolleyWaitingTimeMin.getText().charAt(i) > '9') {
                trolleyWaitingTimeMin.setText("");
            }
        durationString[2] = trolleyWaitingTimeMin.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void tTrolleyWaitingTimeMax() {
        for (i = 0; i < trolleyWaitingTimeMax.getText().length(); i++)
            if (trolleyWaitingTimeMax.getText().charAt(i) < '0' || trolleyWaitingTimeMax.getText().charAt(i) > '9') {
                trolleyWaitingTimeMax.setText("");
            }
        durationString[3] = trolleyWaitingTimeMax.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void tCollectionTimeMin() {
        for (i = 0; i < collectionTimeMin.getText().length(); i++)
            if (collectionTimeMin.getText().charAt(i) < '0' || collectionTimeMin.getText().charAt(i) > '9') {
                collectionTimeMin.setText("");
            }
        durationString[4] = collectionTimeMin.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void tCollectionTimeMax() {
        for (i = 0; i < collectionTimeMax.getText().length(); i++)
            if (collectionTimeMax.getText().charAt(i) < '0' || collectionTimeMax.getText().charAt(i) > '9') {
                collectionTimeMax.setText("");
            }
        durationString[5] = collectionTimeMax.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void tFirstRouteTimeMin() {
        for (i = 0; i < firstRouteTimeMin.getText().length(); i++)
            if (firstRouteTimeMin.getText().charAt(i) < '0' || firstRouteTimeMin.getText().charAt(i) > '9') {
                firstRouteTimeMin.setText("");
            }
        durationString[6] = firstRouteTimeMin.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void tFirstRouteTimeMax() {
        for (i = 0; i < firstRouteTimeMax.getText().length(); i++)
            if (firstRouteTimeMax.getText().charAt(i) < '0' || firstRouteTimeMax.getText().charAt(i) > '9') {
                firstRouteTimeMax.setText("");
            }
        durationString[7] = firstRouteTimeMax.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void tSecondRouteTimeMin() {
        for (i = 0; i < secondRouteTimeMin.getText().length(); i++)
            if (secondRouteTimeMin.getText().charAt(i) < '0' || secondRouteTimeMin.getText().charAt(i) > '9') {
                secondRouteTimeMin.setText("");
            }
        durationString[8] = secondRouteTimeMin.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }
    @FXML
    protected void tSecondRouteTimeMax() {
        for (i = 0; i < secondRouteTimeMax.getText().length(); i++)
            if (secondRouteTimeMax.getText().charAt(i) < '0' || secondRouteTimeMax.getText().charAt(i) > '9') {
                secondRouteTimeMax.setText("");
            }
        durationString[9] = secondRouteTimeMax.getText();
        save1.setDisable(false);
        save2.setDisable(false);
    }

    @FXML
    protected void productionModifier() {
        for (i = 0; i < 5; i++)
            productionLine[i].set_mod1(productionTimeModifier.getValue());
    }
    @FXML
    protected void waitingModifier() {
        trolley.set_mod2(waitingTimeModifier.getValue());
    }
}