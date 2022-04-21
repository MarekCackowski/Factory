package App;

import java.util.Random;

public class ProductionLine extends Thread {
    private final int nr, productionTimeMin, productionTimeMax ,x , y;
    private double mod = 1;
    private final ProductionFacility productionFacility;

    public ProductionLine(int nr, int productionTimeMin, int productionTimeMax,
                          ProductionFacility productionFacility, int x, int y) {
        this.nr = nr;
        this.productionTimeMin = productionTimeMin;
        this.productionTimeMax = productionTimeMax;
        this.productionFacility = productionFacility;
        this.x = x;
        this.y = y;
    }
    public void set_mod1(double m) { mod = m; }

    @Override
    public void run() {
        double productionTime;
        Random random = new Random();
        while (!productionFacility.end()) {
            if (productionFacility.getIsFree(4 * nr)) {

                //generate production time, construct and run produced unit
                productionTime = (random.nextDouble(productionTimeMax - productionTimeMin + 1) + productionTimeMin) * mod / 8;
                ProducedUnit producedUnit = new ProducedUnit(nr, productionTime, productionFacility, x, y);
                producedUnit.start();

                //informing that first stage on the line is taken
                productionFacility.setIsFree(false, 4 * nr);
            }
        }
    }
}
