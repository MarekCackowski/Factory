package App;

import javafx.animation.PathTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import java.util.Random;

public class Trolley extends Thread {
    private final int trolleyCapacity, packageCapacity, trolleyWaitingTimeMin, TrolleyWaitingTimeMax, collectionTimeMax, collectionTimeMin;
    private int howManyVisible, allPackages;
    private double trolleyWaitingTime, collectionTime;
    private double modifier = 1;
    private final ProductionFacility productionFacility;
    private final Warehouses warehouses;
    private final Label productInWarehouse;
    private AnchorPane trolley;
    private Rectangle rectangle, chargingBar;
    private final Rectangle[] packagesOnTrolley = new Rectangle[5];
    private final Line line1;
    private Line line2, line;
    private PathTransition transition1, transition2, transition;
    private final ScaleTransition scale;

    public Trolley(int trolleyCapacity, int packageCapacity, int trolleyWaitingTimeMin, int TrolleyWaitingTimeMax,
                   int collectionTimeMin, int collectionTimeMax, ProductionFacility productionFacility, Warehouses warehouses) {
        this.trolleyCapacity = trolleyCapacity;
        this.packageCapacity = packageCapacity;
        this.trolleyWaitingTimeMin = trolleyWaitingTimeMin;
        this.TrolleyWaitingTimeMax = TrolleyWaitingTimeMax;
        this.collectionTimeMin = collectionTimeMin;
        this.collectionTimeMax = collectionTimeMax;
        this.productionFacility = productionFacility;
        this.warehouses = warehouses;
        this.allPackages = 0;
        this.howManyVisible = 0;

        //this line is used in transition everytime (from magazine to first dump)
        line1 = new Line();
        line1.setStartX(425);
        line1.setStartY(380);
        line1.setEndX(425);
        line1.setEndY(280);

        //transition for charging bar
        scale = new ScaleTransition();
        scale.setToX(40);
        scale.setAutoReverse(false);
        scale.setRate(1);

        //label on red rectangle
        productInWarehouse = new Label("0");
        productInWarehouse.setLayoutX(395);
        productInWarehouse.setLayoutY(410);
        productInWarehouse.setMaxWidth(60);
        productInWarehouse.setPrefWidth(60);
        productInWarehouse.setTextFill(Color.web("#000000"));
        productInWarehouse.setAlignment(Pos.CENTER);
    }

    public void set_mod2(double m) { modifier = m; }
    public void startTrolley() {
        Platform.runLater(() -> Application.anchor.getChildren().add(productInWarehouse));
    }
    public void stopTrolley() {
        Platform.runLater(() -> Application.anchor.getChildren().remove(productInWarehouse));
    }

    private void charge(Random random) {
        trolleyWaitingTime = Math.round((random.nextDouble(TrolleyWaitingTimeMax - trolleyWaitingTimeMin + 1) + trolleyWaitingTimeMin) * modifier);
        chargingBar = new Rectangle(1, 10);
        chargingBar.setX(345);
        chargingBar.setY(415);
        chargingBar.setFill(Color.web("#000000"));
        chargingBar.setStroke(Color.web("#000000"));
        chargingBar.setStrokeWidth(1);

        scale.setDuration(Duration.seconds(trolleyWaitingTime));
        scale.setNode(chargingBar);
        scale.setOnFinished(e -> {
            synchronized (this) {
                notify();
            }
        });
        Platform.runLater(() -> Application.anchor.getChildren().add(chargingBar));

        Platform.runLater(scale::play);
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void createTrolley() {
        trolley = new AnchorPane();
        rectangle = new Rectangle(20, 20);
        rectangle.setX(0);
        rectangle.setY(0);
        rectangle.setFill(Color.web("#d3d3d3"));
        rectangle.setStroke(Color.web("#000000"));
        rectangle.setStrokeWidth(1);
        trolley.getChildren().add(rectangle);
        packagesOnTrolley[0] = new Rectangle(8, 8);
        packagesOnTrolley[0].setLayoutX(2);
        packagesOnTrolley[0].setLayoutY(2);
        packagesOnTrolley[0].setFill(Color.web("#78680c"));
        packagesOnTrolley[0].setStrokeWidth(0.5);
        packagesOnTrolley[0].setStroke(Color.web("#000000"));
        packagesOnTrolley[0].setVisible(false);
        trolley.getChildren().add(packagesOnTrolley[0]);
        packagesOnTrolley[1] = new Rectangle(8, 8);
        packagesOnTrolley[1].setLayoutX(10);
        packagesOnTrolley[1].setLayoutY(2);
        packagesOnTrolley[1].setFill(Color.web("#78680c"));
        packagesOnTrolley[1].setStrokeWidth(0.5);
        packagesOnTrolley[1].setStroke(Color.web("#000000"));
        packagesOnTrolley[1].setVisible(false);
        trolley.getChildren().add(packagesOnTrolley[1]);
        packagesOnTrolley[2] = new Rectangle(8, 8);
        packagesOnTrolley[2].setLayoutX(2);
        packagesOnTrolley[2].setLayoutY(10);
        packagesOnTrolley[2].setFill(Color.web("#78680c"));
        packagesOnTrolley[2].setStrokeWidth(0.5);
        packagesOnTrolley[2].setStroke(Color.web("#000000"));
        packagesOnTrolley[2].setVisible(false);
        trolley.getChildren().add(packagesOnTrolley[2]);
        packagesOnTrolley[3] = new Rectangle(8, 8);
        packagesOnTrolley[3].setLayoutX(10);
        packagesOnTrolley[3].setLayoutY(10);
        packagesOnTrolley[3].setFill(Color.web("#78680c"));
        packagesOnTrolley[3].setStrokeWidth(0.5);
        packagesOnTrolley[3].setStroke(Color.web("#000000"));
        packagesOnTrolley[3].setVisible(false);
        trolley.getChildren().add(packagesOnTrolley[3]);
        packagesOnTrolley[4] = new Rectangle(8, 8);
        packagesOnTrolley[4].setLayoutX(6);
        packagesOnTrolley[4].setLayoutY(6);
        packagesOnTrolley[4].setFill(Color.web("#78680c"));
        packagesOnTrolley[4].setStrokeWidth(0.5);
        packagesOnTrolley[4].setStroke(Color.web("#000000"));
        packagesOnTrolley[4].setVisible(false);
        trolley.getChildren().add(packagesOnTrolley[4]);
    }
    private void takeFirstStep(Random random) {
        collectionTime = (random.nextDouble(collectionTimeMax - collectionTimeMin + 1) + collectionTimeMin);
        transition1 = new PathTransition(Duration.seconds(collectionTime / 25 * 3), line1, trolley);
        transition1.setOnFinished(e1 -> {
            synchronized (this) {
                notify();
            }
        });

        Platform.runLater(() -> {
            Application.anchor.getChildren().remove(chargingBar);
            Application.anchor.getChildren().add(trolley);
            transition1.play();
        });
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    private int MakePackageVisible(int nr) {
        //allPackages is sum of packages taken from whole turn
        //packagesFrom1Dump is number of packages taken in 1 turn
        int packagesFrom1Dump = 0;
        while (warehouses.getDump(4 - nr) >= packageCapacity && trolleyCapacity - allPackages/packageCapacity > 0) {
            packagesFrom1Dump += packageCapacity;
            allPackages += packageCapacity;
            warehouses.setDump(4 - nr, warehouses.getDump(4 - nr) - packageCapacity);

            //change packages to visible for every package taken on the turn
            if (howManyVisible == 0)
                    Platform.runLater(() -> packagesOnTrolley[0].setVisible(true));
            else if (howManyVisible == 1)
                Platform.runLater(() -> packagesOnTrolley[1].setVisible(true));
            else if (howManyVisible == 2)
                Platform.runLater(() -> packagesOnTrolley[2].setVisible(true));
            else if (howManyVisible == 3)
                Platform.runLater(() -> packagesOnTrolley[3].setVisible(true));
            else if (howManyVisible == 4)
                Platform.runLater(() -> packagesOnTrolley[4].setVisible(true));
            howManyVisible++;
        }
        return packagesFrom1Dump;
    }
    private void nextDumpOrComeBack(int nr, int x, int y) {
        //if trolley is not at the end and there is fewer packages than max trolley goes to the next dump
        if (nr != 4 && allPackages / packageCapacity < trolleyCapacity) {
            Platform.runLater(() -> transition.play());
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e3) {
                    e3.printStackTrace();
                }
            }
            collection(nr + 1, x, y - 55, trolley);
        }

        //else trolley starts its way back
        else {
            line2 = new Line();
            line2.setStartX(x);
            line2.setStartY(y);
            line2.setEndX(425);
            line2.setEndY(380);
            transition2 = new PathTransition(Duration.seconds(collectionTime / 25 * 7), line2, trolley);
            transition2.setNode(trolley);
            transition2.setDelay(Duration.seconds(collectionTime / 25 * 2));
            transition2.setOnFinished(e2 -> {
                synchronized (this) {
                    Platform.runLater(() -> productInWarehouse.setText(Integer.toString(warehouses.getProductInWarehouse())));
                    Platform.runLater(() -> Application.anchor.getChildren().remove(trolley));
                    notify();
                }
            });
            Platform.runLater(() -> transition2.play());
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e3) {
                    e3.printStackTrace();
                }
            }
        }
    }

    private void collection(int nr, int x, int y, AnchorPane trolley) {
        line = new Line();
        line.setEndX(x);
        line.setEndY(y - 55);
        line.setStartX(x);
        line.setStartY(y);
        transition = new PathTransition(Duration.seconds(collectionTime / 25), line, trolley);
        transition.setDelay(Duration.seconds(collectionTime / 25 * 2));
        transition.setNode(trolley);
        transition.setOnFinished(e3 -> { synchronized (this) { notify(); } });

        int packagesFrom1Dump = MakePackageVisible(nr);
        productionFacility.collect(nr, packagesFrom1Dump);
        nextDumpOrComeBack(nr, x, y);
    }

    @Override
    public void run() {
        Random random = new Random();
        while (!productionFacility.end()) {
            charge(random);
            createTrolley();
            takeFirstStep(random);

            //recursively making a transition from magazine to last dump and back
            //if amount of packages on trolley is equal to chosen capacity the trolley start moving back
            collection(0, 425, 280, trolley);
        }
    }
}
