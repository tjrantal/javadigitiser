package timo.home.fxml;
//FXML-defined stuff
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

//UI stuff
import javafx.scene.chart.*;		//Chart
import javafx.scene.control.*;	//Slider
import javafx.beans.value.*;	//ChangeListener

import timo.home.jcodec.VideoReader;
import timo.home.jcodec.BIWithMeta;

public class FXMLControls{
	//FXML-defined stuff
	@FXML Text actiontarget;
	
	//Charts
	@FXML LineChart<Number,Number> lineChart;
	@FXML NumberAxis xAxis = new NumberAxis();
	@FXML	NumberAxis yAxis = new NumberAxis();
	XYChart.Series series1;
	XYChart.Series series2;
	
	@FXML LineChart<Number,Number> zLineChart;
	@FXML NumberAxis zxAxis = new NumberAxis();
	@FXML	NumberAxis zyAxis = new NumberAxis();
	XYChart.Series zseries;
	XYChart.Series zseries2;
		
	//Sliders
	@FXML Slider aSlider;
	@FXML Slider vSlider;
	@FXML Slider sSlider;
	@FXML Slider rSlider;
	@FXML Label aLabel;
	@FXML Label vLabel;
	@FXML Label sLabel;
	@FXML Label rLabel;
	
	//JCodec videoreader
	private VideoReader videoReader;
	private BIWithMeta currentFrame;
    
    //Initialise gets called when the controller is instantiated
    public void initialize(){
    	System.out.println("INITIALISE");
		videoReader = new VideoReader("VID-20180304-WA0000.mp4");
		double duration = videoReader.getDuration();
		int	frames = videoReader.getTotalFrames();
		double fps = videoReader.getFPS();
		int width = videoReader.getWidth();
		int height = videoReader.getHeight();
		System.out.println(String.format("Duration %.1f frames %d fps %.2f width %d height %d",duration,frames,fps,width,height));		
    }
    

    
    @FXML protected void handleSubmitButtonAction(ActionEvent event) {
        //Test reading, and displaying a frame here
        System.out.println("Got button click");
        try{
        		long beforeMillis = System.currentTimeMillis();
				BIWithMeta currentFrame = videoReader.nextFrame();
				long afterMillis = System.currentTimeMillis();
				System.out.println(String.format("got frame %.2f",((double) (afterMillis-beforeMillis))/1000d));
        }catch (Exception ex){
            System.err.println("Could not read frame.");
        }
        videoReader.close();
        
    }
}
