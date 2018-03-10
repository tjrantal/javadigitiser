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

import timo.home.jcodec.VideoReader;
import timo.home.jcodec.BIWithMeta;


public class FXMLControls{
	//FXML-defined stuff

	//Sliders
	@FXML Slider frameSlider;
	@FXML Label frameLabel;
	
	//VideoView
	@FXML ImageView videoView;
	
	//JCodec videoreader
	private VideoReader videoReader;
	private BIWithMeta currentFrame;
	
	//For UI
	public int currentFrameNo = 0;
    
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
    }
    

    
    @FXML protected void handleLoadButtonAction(ActionEvent event) {
        //Test reading, and displaying a frame here
        System.out.println("Got LoadButton click");
        BIWithMeta currentFrame = null;
        try{
        		long beforeMillis = System.currentTimeMillis();
				currentFrame = videoReader.nextFrame();
				long afterMillis = System.currentTimeMillis();
				System.out.println(String.format("got frame %.2f",((double) (afterMillis-beforeMillis))/1000d));
        }catch (Exception ex){
            System.err.println("Could not read frame.");
        }
        videoView.setImage(SwingFXUtils.toFXImage(currentFrame, null));        
    }
    
     @FXML protected void handleFrameButtonAction(ActionEvent event) {
        //Test reading, and displaying a frame here
        System.out.println("Got Frame button click");
        frameSlider.increment();
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
				System.out.println(String.format("FrameButton  got frame %.2f",((double) (afterMillis-beforeMillis))/1000d));
				
				
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
					System.out.println(String.format("got frame %.2f",((double) (afterMillis-beforeMillis))/1000d));
			  }catch (Exception ex){
					System.err.println("Could not read frame.");
			  }
			  videoView.setImage(SwingFXUtils.toFXImage(currentFrame, null));
	 	}
	 }
	 
}
