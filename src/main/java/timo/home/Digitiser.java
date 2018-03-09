/*
	Built with Maven, depends on jcodec, which depends on several other libs. Maven takes care of the dependencies
*/
package timo.home;

//Imports to create FXML application main
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class Digitiser extends Application{

	@Override
	public void start(Stage stage) throws Exception{
		Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
		Scene scene = new Scene(root, 800, 500);
		stage.setTitle("Digitiser");
		stage.setScene(scene);
		stage.show();
	}

	//MAIN launch JavaFX
	public static void main(String[] a){
		launch(a);
	}

}
