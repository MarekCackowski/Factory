package App;

import javafx.animation.PathTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import javafx.application.Platform;

public class ProducedUnit extends Thread {
    private final int nrLine, x, y;
    private final double productionTime;
    private final ProductionFacility productionFacility;
    private ScaleTransition scaleTransition;
    private PathTransition pathTransition;

    public ProducedUnit(int nrLine, double productionTime, ProductionFacility productionFacility, int x, int y) {
        this.nrLine = nrLine;
        this.productionTime = productionTime;
        this.productionFacility = productionFacility;
        this.x = x + 18;
        this.y = y + 15;
    }

    //recursively going stage by stage to the end of production line
    private void step(int x, int y, int nr, Circle circle) {

        //inform that current stage on the line is occupied
        productionFacility.setIsFree(false, nr);

        //create path and scale transition from this to next stage
        Line line = new Line();
        line.setStartX(x);
        line.setStartY(y);
        line.setEndX(x + 35);
        line.setEndY(y);
        pathTransition = new PathTransition(Duration.seconds(productionTime), line, circle);
        scaleTransition = new ScaleTransition();
        scaleTransition.setByY(2);
        scaleTransition.setByX(2);
        scaleTransition.setAutoReverse(false);
        scaleTransition.setRate(1);
        scaleTransition.setNode(circle);
        scaleTransition.setDuration(Duration.seconds(productionTime));
        scaleTransition.setOnFinished(e -> {
            synchronized (this) {
                notify();
            }
        });
        pathTransition.setOnFinished(e -> {
            synchronized (this) {
                notify();
            }
        });

        //if it's first stage the circle is added
        if (nr == (4 * nrLine))
            Platform.runLater(() -> Application.anchor.getChildren().add(circle));
        Platform.runLater(() -> scaleTransition.play());
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //if circle has to wait it changes color to red
        if (nr != 3 + (4 * nrLine) && !productionFacility.getIsFree(nr + 1)) {
            Platform.runLater(() -> circle.setFill(Color.web("#ff0000")));
            while (!productionFacility.getIsFree(nr + 1)) ;
            Platform.runLater(() -> circle.setFill(Color.web("#000000")));
        }

        //if it's not the last stage there is path transition
        if (nr != 3 + (4 * nrLine)) {
            Platform.runLater(() -> pathTransition.play());
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //if not last stage go to the next else inform that the last stage is not taken
        if (nr != 3 + (4 * nrLine)) {
            productionFacility.setIsFree(true, nr);
            step(x + 35, y, nr + 1, circle);
        } else {
            Platform.runLater(() -> Application.anchor.getChildren().remove(circle));
            productionFacility.setIsFree(true, nr);
        }
    }

    @Override
    public void run() {
        //create circle (product in making)
        productionFacility.beginProduction(nrLine);
        Circle circle = new Circle(1, 1, 1);
        circle.setFill(Color.web("#000000"));
        circle.setStrokeWidth(0);
        circle.setCenterX(x);
        circle.setCenterY(y);

        step(x, y, 4 * nrLine, circle);
        productionFacility.endProduction(nrLine);
    }
}
