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
//Cleanup
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import timo.home.fxml.FXMLControls;

public class Digitiser extends Application{

	@Override
	public void start(Stage stage) throws Exception{
		final FXMLLoader loader  = new FXMLLoader(getClass().getResource("main.fxml"));
		Parent root = loader.load();
		Scene scene = new Scene(root, 800, 500);
		stage.setTitle("Digitiser");
		stage.setScene(scene);
		
		
		//Attach a listener for onHidden to cleanup
		stage.setOnHidden(new EventHandler<WindowEvent>(){
				@Override
				public void handle(WindowEvent e) {
					System.out.println("Got stage onHidden event");
					if (loader.getController() instanceof timo.home.fxml.FXMLControls){
						((FXMLControls) loader.getController()).shutdown();
					}
				}
			}
		);
		
		//Show the application
		stage.show();
	}



	//MAIN launch JavaFX
	public static void main(String[] a){
		launch(a);
	}

}
