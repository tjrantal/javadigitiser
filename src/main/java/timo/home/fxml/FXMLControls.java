package timo.home.fxml;
//FXML-defined stuff
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

//UI stuff
import javafx.scene.chart.*;		//Chart
import javafx.scene.control.*;	//Slider
import javafx.beans.value.*;	//ChangeListener
import javafx.scene.image.*;	//ImageView
import javafx.embed.swing.SwingFXUtils;	//BufferedImage to javaFX Image
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.FileChooser;	//Picking a video file to read
import javafx.stage.FileChooser.ExtensionFilter;	//Picking a video file to read
import java.util.Arrays;
import java.io.File;

import timo.home.jcodec.VideoReader;
import timo.home.jcodec.BIWithMeta;


public class FXMLControls{
	//FXML-defined stuff

	//Sliders
	@FXML Slider frameSlider;
	@FXML Label frameLabel;
	
	@FXML Slider colourSlider;
	@FXML Label colourLabel;
	
	//VideoView
	@FXML ImageView videoView;
	
	//Track Button
	@FXML Button trackButton;
	
	//JCodec videoreader
	private VideoReader videoReader;
	private BIWithMeta currentFrame;
	
	//For UI
	public int currentFrameNo = 0;
	public boolean trackOn = false;
	public int colourTolerance = 10;
	//public Node thisNode;
    
    //Initialise gets called when the controller is instantiated
    public void initialize(){
		//Attach Slider listener for colour slider 
		colourSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
				 Number old_val, Number new_val) {
				 		//Update tolerance in real time
				 		colourTolerance = new_val.intValue();
				 		colourLabel.setText(String.format("Colour tolerance %02d", colourTolerance));
				}
		});
    }
    

    
    @FXML protected void handleLoadButtonAction(ActionEvent event) {
        //Test reading, and displaying a frame here
        System.out.println("Got LoadButton click");
        
        //Browse for a file
        FileChooser fc = new FileChooser();
		 fc.setTitle("Select MP4 videofile");
		 fc.getExtensionFilters().addAll(
				   new ExtensionFilter("Video files", Arrays.asList(new String[]{"*.mp4","*.MP4"})));
		 File selectedFile = fc.showOpenDialog(frameLabel.getScene().getWindow());
		 if (selectedFile != null) {
			 System.out.println("Got File "+selectedFile.toString());
			 videoReader = new VideoReader(selectedFile);
			double duration = videoReader.getDuration();
			int	frames = videoReader.getTotalFrames();
			double fps = videoReader.getFPS();
			int width = videoReader.getWidth();
			int height = videoReader.getHeight();
			System.out.println(String.format("Duration %.1f frames %d fps %.2f width %d height %d",duration,frames,fps,width,height));	
		
			//Prep the slider
			frameSlider.setMin(0);
			frameSlider.setMax(frames-1);
			frameSlider.setBlockIncrement(1d);
		
			//Attach Slider listener for clicking on the slider 
		
			frameSlider.valueProperty().addListener(new ChangeListener<Number>() {
				public void changed(ObservableValue<? extends Number> ov,
					 Number old_val, Number new_val) {
					 		//Update text label while dragging
					 		int val = new_val.intValue();
					 		frameLabel.setText(String.format("Frame No %d", val));
						  
						  //Only update the frame if the slider is no longer moving
						  if (!frameSlider.isValueChanging() & currentFrameNo !=val){
						  		System.out.println(String.format("Start searching frame %d",val));
							  	getFrame(val);
						  }
					}
			});
		
		
			//Try firing end of drag event manually
			frameSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
		             Slider source = (Slider)e.getSource();
				 		int val = (int) source.getValue();
				 		frameLabel.setText(String.format("Frame No %d", val));
				 		if (currentFrameNo != val){
				 			System.out.println(String.format("MOUSE Start searching frame %d current %d",val,currentFrameNo));
					 		getFrame(val);
					  }
		         }
		
			
			});	
			 
			 //Display the first image
			 BIWithMeta currentFrame = null;
        try{
        		long beforeMillis = System.currentTimeMillis();
				currentFrame = videoReader.nextFrame();
				long afterMillis = System.currentTimeMillis();
				System.out.println(String.format("got frame, time stamp %.2f took %.2f s to decpde",currentFrame.getTimeStamp(),((double) (afterMillis-beforeMillis))/1000d));
        }catch (Exception ex){
            System.err.println("Could not read frame.");
        }
        videoView.setImage(SwingFXUtils.toFXImage(currentFrame, null));
			 
		 }
        
                
    }
    
     @FXML protected void handleFrameButtonAction(ActionEvent event) {
        //Test reading, and displaying a frame here
        System.out.println("Got Frame button click");
        frameSlider.increment();
     }
    
     @FXML protected void handleTrackButtonAction(ActionEvent event) {
        //Test reading, and displaying a frame here
        trackOn ^= true;
        System.out.println(String.format("Auto Track %s",trackOn ? "On" : "Off"));
        trackButton.setText(String.format("Auto Track %s",trackOn ? "On" : "Off"));
     }
    
   	@FXML protected void handleCloseButtonAction(ActionEvent event) {
		  System.out.println("Got Close button click");
		  videoReader.close();
	 }
	 
	 //Helper functions
	 public void getNextFrame(){
	 	BIWithMeta currentFrame = null;
			//Get next frame here
        try{
        		long beforeMillis = System.currentTimeMillis();
				currentFrame = videoReader.nextFrame();
				long afterMillis = System.currentTimeMillis();
				System.out.println(String.format("got next frame, time stamp %.2f took %.2f s to decpde",currentFrame.getTimeStamp(),((double) (afterMillis-beforeMillis))/1000d));
								
        }catch (Exception ex){
            System.err.println("Could not read frame.");
        }
        videoView.setImage(SwingFXUtils.toFXImage(currentFrame, null));
	 }
	 
	 public void getFrame(int frameNo){
	 	//Do nothing if the requested frame is already up
	 	if (frameNo == currentFrameNo){
	 		System.out.println("Already the current frame");
	 		return;
	 	}
	 	 	
	 	if (frameNo == currentFrameNo+1){
	 		//Load next frame if incremented by one	
	 		getNextFrame();
	 		currentFrameNo = frameNo;
	 	}else{
	 		//Have to search for the frame
	 		currentFrameNo = frameNo;
	 		frameSlider.setValue(currentFrameNo);
			  //Update the VideoView
			  BIWithMeta currentFrame = null;
			  try{
			  		long beforeMillis = System.currentTimeMillis();
					currentFrame = videoReader.readFrame(frameNo);
				
					long afterMillis = System.currentTimeMillis();
					System.out.println(String.format("got frame, time stamp %.2f took %.2f s to decpde",currentFrame.getTimeStamp(),((double) (afterMillis-beforeMillis))/1000d));
			  }catch (Exception ex){
					System.err.println("Could not read frame.");
			  }
			  videoView.setImage(SwingFXUtils.toFXImage(currentFrame, null));
	 	}
	 }
	 
}
