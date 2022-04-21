package App;

import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.Random;

public class Route extends Thread {
    private final int nr, lowerLimit, firstRouteTimeMin, secondRouteTimeMin, firstRouteTimeMax, secondRouteTimeMax;
    private final Warehouses warehouses;
    private final ProductionFacility productionFacility;
    private final Rectangle rectangle;

    public Route(int nr, int lowerLimit, int firstRouteTimeMin, int firstRouteTimeMax,
                 int secondRouteTimeMin, int secondRouteTimeMax, Warehouses warehouses, ProductionFacility productionFacility) {
        this.nr = nr;
        this.lowerLimit = lowerLimit;
        this.firstRouteTimeMin = firstRouteTimeMin;
        this.firstRouteTimeMax = firstRouteTimeMax;
        this.secondRouteTimeMin = secondRouteTimeMin;
        this.secondRouteTimeMax = secondRouteTimeMax;
        this.warehouses = warehouses;
        this.productionFacility = productionFacility;

        //create car rectangle
        rectangle = new Rectangle(20, 30);
        rectangle.setX(130);
        rectangle.setY(187);
        rectangle.setFill(Color.web("#b6cbdd"));
        rectangle.setStroke(Color.web("000000"));
        rectangle.setStrokeWidth(1);
    }

    //firstOrSecond equals 1 if nr = 0 and -1 if nr = 1
    private void transitionToMagazineAndBack(Random random, int routeTimeMax, int routeTimeMin, int firstOrSecond) {
        int routeTime = random.nextInt(routeTimeMax - routeTimeMin + 1) + routeTimeMin;
        TranslateTransition translateTransition1 = new TranslateTransition(Duration.seconds(routeTime/3), rectangle);
        translateTransition1.setByY(95);
        RotateTransition rotateTransition1 = new RotateTransition(Duration.millis(10), rectangle);
        rotateTransition1.setByAngle(-90);
        TranslateTransition translateTransition2 = new TranslateTransition(Duration.seconds(routeTime/3), rectangle);
        translateTransition2.setByX(-70 * firstOrSecond);
        RotateTransition rotateTransition2 = new RotateTransition(Duration.millis(10), rectangle);
        rotateTransition2.setByAngle(90);
        TranslateTransition translateTransition3 = new TranslateTransition(Duration.seconds(routeTime/3), rectangle);
        translateTransition3.setByY(78);

        routeTime = random.nextInt(routeTimeMax - routeTimeMin + 1) + routeTimeMin;
        TranslateTransition translateTransition4 = new TranslateTransition(Duration.seconds(routeTime/3), rectangle);
        translateTransition4.setByY(-78);
        translateTransition4.setDelay(Duration.seconds(routeTime/10));
        RotateTransition rotateTransition3 = new RotateTransition(Duration.millis(10), rectangle);
        rotateTransition3.setByAngle(90);
        TranslateTransition translateTransition5 = new TranslateTransition(Duration.seconds(routeTime/3), rectangle);
        translateTransition5.setByX(70 * firstOrSecond);
        RotateTransition rotateTransition4 = new RotateTransition(Duration.millis(10), rectangle);
        rotateTransition4.setByAngle(-90);
        TranslateTransition translateTransition6 = new TranslateTransition(Duration.seconds(routeTime/3), rectangle);
        translateTransition6.setByY(-95);
        translateTransition6.setDelay(Duration.seconds(routeTime/10));
        TranslateTransition translateTransition7 = new TranslateTransition(Duration.seconds(routeTime/10), rectangle);
        translateTransition7.setByX(0);

        //create sequentialTransition to magazine and back to factory and play it
        SequentialTransition sequentialTransition = new SequentialTransition(translateTransition1, rotateTransition1, translateTransition2,
                rotateTransition2, translateTransition3, translateTransition4, rotateTransition3, translateTransition5,
                rotateTransition4, translateTransition6, translateTransition7);
        sequentialTransition.setOnFinished(e1 -> { synchronized (this) { notify(); } });
        Platform.runLater(() -> {
            Application.anchor.getChildren().add(rectangle);
            sequentialTransition.play();
        });
        synchronized (this) { try { wait(); } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        }
        //remove car after and end delivery
        Platform.runLater(() -> Application.anchor.getChildren().remove(rectangle));
        productionFacility.endDelivery(nr);
    }

    @Override
    public void run() {
        Random random = new Random();
        double firstRouteTime, secondRouteTime;
        while (!productionFacility.end()) {
            productionFacility.startDelivery(nr);
            if (nr == 0)
                transitionToMagazineAndBack(random, firstRouteTimeMax, firstRouteTimeMin, 1);
            else
                transitionToMagazineAndBack(random, secondRouteTimeMax, secondRouteTimeMin, -1);
        }
    }
}
