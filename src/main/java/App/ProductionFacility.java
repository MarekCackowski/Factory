package App;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


//monitor
public class ProductionFacility {
    private final int dumpCapacity, carCapacity, lowerLimit1, lowerLimit2;
    private int[] productsOnLine = new int[5];
    private static boolean isEnded = false;
    private boolean isCarOnRoad;
    private boolean[] waitingLines = new boolean[5];
    private boolean[] isFree = new boolean[20];
    public Label[] dump = new Label[5];
    private Warehouses warehouses;
    private Label material1, material2;
    private final Lock[] line = new ReentrantLock[5];
    private final Condition[] runLine = new Condition[5];
    private final Lock[] car = new ReentrantLock[2];
    private final Condition[] runCar = new Condition[2];

    public ProductionFacility(int dumpCapacity, int carCapacity, int lowerLimit1, int lowerLimit2, Warehouses warehouses) {
        this.dumpCapacity = dumpCapacity;
        this.carCapacity = carCapacity;
        this.lowerLimit1 = lowerLimit1;
        this.lowerLimit2 = lowerLimit2;
        this.warehouses = warehouses;

        //stating that every stage on each line is free and there is no car on the road
        for (int i = 0; i < 20; i++)
            this.isFree[i] = true;
        this.isCarOnRoad = false;

        //constructing locks and conditions
        for (int i = 0; i < 5; i++) {
            this.productsOnLine[i] = 0;
            this.waitingLines[i] = false;
            this.line[i] = new ReentrantLock();
            this.runLine[i] = line[i].newCondition();
        }
        for (int i = 0; i < 2; i++) {
            this.car[i] = new ReentrantLock();
            this.runCar[i] = car[i].newCondition();
        }

        //green label
        material1 = new Label(Integer.toString(warehouses.getMaterial1()));
        material1.setLayoutX(123);
        material1.setLayoutY(154);
        material1.setMaxWidth(34);
        material1.setTextFill(Color.web("#00ff0d"));

        //blue label
        material2 = new Label(Integer.toString(warehouses.getMaterial2()));
        material2.setLayoutX(123);
        material2.setLayoutY(168);
        material2.setMaxWidth(34);
        material2.setTextFill(Color.web("#008cff"));

        //labels on each dump
        for (int i = 0; i < 5; i++) {
            dump[i] = new Label("0");
            dump[i].setLayoutX(381);
            dump[i].setLayoutY(50 + 55 * i);
            dump[i].setMaxWidth(34);
            dump[i].setTextFill(Color.web("#ff0000"));
        }
    }

    //adding all labels
    public void startFacility() {
        Platform.runLater(() -> {
            Application.anchor.getChildren().add(material1);
            Application.anchor.getChildren().add(material2);
            for (int i = 0; i < 5; i++)
                Application.anchor.getChildren().add(dump[i]);
        });
    }

    //removing all labels
    public void stopFacility() {
        Platform.runLater(() -> {
            Application.anchor.getChildren().remove(material1);
            Application.anchor.getChildren().remove(material2);
            for (int i = 0; i < 5; i++)
                Application.anchor.getChildren().remove(dump[i]);
        });
    }
    public synchronized void setIsFree(boolean c, int nr) { isFree[nr] = c; }
    public synchronized boolean getIsFree(int nr) { return isFree[nr]; }
    public synchronized void setWaitingLines(int nr, boolean x) { waitingLines[nr] = x; }
    public synchronized boolean getRunLine(int nr) { return waitingLines[nr]; }
    public void setEnd() { isEnded = true; }
    public boolean end() { return isEnded; }

    public void beginProduction(int nr) {
        line[nr].lock();
        for (int i = 0; i < 2; i++)
            car[i].lock();
        try {

            //if product on the line + product in dump = dump capacity or there is no material
            //line stop producing till everything is okay
            if (warehouses.getDump(nr) == dumpCapacity - productsOnLine[nr]
                    || warehouses.getMaterial1() == 0 || warehouses.getMaterial2() == 0) {
                setWaitingLines(nr, true);
                runLine[nr].await();
                setWaitingLines(nr, false);
            }

            //updating materials amount on labels
            warehouses.setMaterial1(warehouses.getMaterial1() - 1);
            Platform.runLater(() -> material1.setText(Integer.toString(warehouses.getMaterial1())));
            warehouses.setMaterial2(warehouses.getMaterial2() - 1);
            Platform.runLater(() -> material2.setText(Integer.toString(warehouses.getMaterial2())));

            //if material amount fall below lower limit car is signalled
            if (warehouses.getMaterial1() < lowerLimit1 && !isCarOnRoad) {
                runCar[0].signal();
                System.out.println("1");
            }
            else if (warehouses.getMaterial2() < lowerLimit2 && !isCarOnRoad) {
                runCar[1].signal();
                System.out.println("2");
            }

            //product in making is going to the line
            productsOnLine[nr]++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            line[nr].unlock();
            for (int i = 0; i < 2; i++)
                car[i].unlock();
        }
    }
    public void endProduction(int nr) {
        line[nr].lock();
        try {

            //product is taken from the line to the dump
            productsOnLine[nr]--;
            warehouses.setProduct(nr);

            //updating dump label
            Platform.runLater(() -> dump[nr].setText(Integer.toString(warehouses.getDump(nr))));
        } finally {
            line[nr].unlock();
        }
    }

    public void startDelivery(int nr) {
        car[nr].lock();
        try {

            //if there is car on the road wait till it ends its route and start own route
            if (nr == 0)
                if (isCarOnRoad || warehouses.getMaterial1() >= lowerLimit1) {
                    System.out.println("1 wait");
                    runCar[nr].await();
                }
            else
                if (isCarOnRoad || warehouses.getMaterial2() >= lowerLimit2) {
                    System.out.println("2 wait");
                    runCar[nr].await();
                }
            isCarOnRoad = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            car[nr].unlock();
        }
    }
    public void endDelivery(int nr) {

        //endDelivery is locking every production line and a car
        for (int i = 0; i < 5; i++)
            line[i].lock();
        for (int i = 0; i < 2; i++)
            car[i].lock();
        try {
            //updating material label
            if (nr == 0) {
                warehouses.setMaterial1(warehouses.getMaterial1() + carCapacity);
                Platform.runLater(() -> material1.setText(Integer.toString(warehouses.getMaterial1())));
            } else {
                warehouses.setMaterial2(warehouses.getMaterial2() + carCapacity);
                Platform.runLater(() -> material2.setText(Integer.toString(warehouses.getMaterial2())));
            }

            //informing that there is no longer car on the road
            isCarOnRoad = false;

            //if there is no car on the road and material fall below lower limit signal the car
            if (warehouses.getMaterial2() < lowerLimit2 && !isCarOnRoad)
                runCar[1].signal();
            else if (warehouses.getMaterial1() < lowerLimit1 && !isCarOnRoad)
                runCar[0].signal();

            //if there is a line that stopped production
            //check if there is material and dump is not full and signal it to start again
            for (int i = 0; i < 5; i++)
                if (getRunLine(i) && warehouses.getMaterial1() > 0 &&
                        warehouses.getMaterial2() > 0 && warehouses.getDump(i) < dumpCapacity)
                    runLine[i].signal();
        } finally {
            for (int i = 0; i < 5; i++)
                line[i].unlock();
            for (int i = 0; i < 2; i++)
                car[i].unlock();
        }
    }

    public void collect(int nr, int howMany) {
        line[4 - nr].lock();
        try {

            //update dump label
            warehouses.setProductInWarehouse(warehouses.getProductInWarehouse() + howMany);
            Platform.runLater(() -> dump[4 - nr].setText(Integer.toString(warehouses.getDump(4 - nr))));

            //if the dump is no longer full and there is material
            //signal line to start producing
            if (getRunLine(4 - nr) && warehouses.getMaterial1() > 0 &&
                    warehouses.getMaterial2() > 0 && warehouses.getDump(4 - nr) < dumpCapacity)
                runLine[4 - nr].signal();
        } finally {
            line[4 - nr].unlock();
        }
    }
}

