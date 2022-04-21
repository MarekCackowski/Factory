package App;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//warehouse store amount of materials, product on each dump and product stored in magazine
public class Warehouses {
    private int[] product = new int[5];
    private int material1, material2, productInWarehouse;
    public Label[] dump = new Label[5];
    final Lock[] line = new ReentrantLock[5];
    public Warehouses(int material1, int material2, int productInWarehouse) {
        for (int i = 0; i < 5; i++)
            this.product[i] = 0;
        for (int i = 0; i < 5; i++)
            this.line[i] = new ReentrantLock();
        this.material1 = material1;
        this.material2 = material2;
        this.productInWarehouse = productInWarehouse;

        for (int i = 0; i < 5; i++) {
            dump[i] = new Label("0");
            dump[i].setLayoutX(381);
            dump[i].setLayoutY(50 + 55 * i);
            dump[i].setMaxWidth(34);
            dump[i].setTextFill(Color.web("#ff0000"));
        }
    }
    public synchronized int getDump(int nr) { return product[nr]; }
    public synchronized void setDump(int nr, int ile) { product[nr] = ile; }
    public synchronized void setProduct(int nr) { product[nr]++; }
    public synchronized int getMaterial1() { return material1; }
    public synchronized void setMaterial1(int ile) { material1 = ile; }
    public synchronized int getMaterial2() { return material2; }
    public synchronized void setMaterial2(int ile) { material2 = ile; }
    public synchronized int getProductInWarehouse() { return productInWarehouse; }
    public synchronized void setProductInWarehouse(int ile) { productInWarehouse = ile; }
}
