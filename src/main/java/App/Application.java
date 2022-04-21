package App;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;

public class Application extends javafx.application.Application {

    static SplitPane root;

    //right side of the scene
    static AnchorPane anchor;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("view.fxml"));
        root = fxmlLoader.load();
        anchor = (AnchorPane)root.getItems().get(1);
        Scene scene = new Scene(root, 800, 500);
        stage.setTitle("Factory simulator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(); }
}