package App;

class Simulation extends Thread {
    public volatile boolean end = false;
    private final ProductionFacility productionFacility;
    private final Route[] route;
    private final Trolley trolley;
    private final ProductionLine[] productionLine;
    public Simulation(ProductionFacility productionFacility, Route[] route,
                      Trolley trolley, ProductionLine[] productionLine) {
        this.productionFacility = productionFacility;
        this.route = route;
        this.trolley = trolley;
        this.productionLine = productionLine;
    }
    @Override
    public void run() {
        for (int i = 0; i < 2; i++)
            route[i].start();
        trolley.start();
        for (int i = 0; i < 5; i++)
            productionLine[i].start();

        //waiting till end button is pressed
        while (!end);

        //informing threads to end work after ending current executing this loop
        productionFacility.setEnd();
    }
}
