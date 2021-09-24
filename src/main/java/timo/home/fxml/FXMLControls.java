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
import javafx.geometry.Bounds;
import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;	//Enable waiting for Platform.runLater..

import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;

//Extract frame
import javax.imageio.ImageIO;

import timo.home.jcodec.VideoReader;
import timo.home.jcodec.BIWithMeta;
import timo.home.tracking.TrackPoint;
import timo.home.tracking.DigitisedPoints;
import timo.home.tracking.MarkerSet;

public class FXMLControls{
	//FXML-defined stuff

	//Sliders
	@FXML Slider frameSlider;
	@FXML Label frameLabel;
	
	@FXML Slider colourSlider;
	@FXML Label colourLabel;
	
	@FXML Slider radiusSlider;
	@FXML Label radiusLabel;
	
	//VideoView
	@FXML ImageView videoView;
	
	//Track Button
	@FXML Button trackButton;
	
	@FXML ComboBox markerBox;
	@FXML ToggleButton trackToggle;
	//JCodec videoreader
	private VideoReader videoReader = null;
	private BIWithMeta currentFrame;
	
	//For UI
	public int currentFrameNo = 0;
	public boolean trackOn = false;
	public int colourTolerance = 10;
	public int searchRadius = 50;
	public double[] digitisedCoordinates = new double[2];
	public double[] refinedCoordinates = null;
	
	public MarkerSet mSet = null;
	ArrayList<String> markerLabels = null;
	
   public TrackPoint tp;// = new TrackPoint(20);
   private DigitisedPoints dp;
   private Thread trackingThread = null;
   private TrackingRunnable trackingRunnable = null;
   private File currentFile;
   
    //Initialise gets called when the controller is instantiated
    public void initialize(){
    	//Create TrackPoint
    	tp = new TrackPoint(searchRadius);
    	
		//Attach Slider listener for colour slider 
		colourSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
				 Number old_val, Number new_val) {
				 		//Update tolerance in real time
				 		colourTolerance = new_val.intValue();
				 		colourLabel.setText(String.format("Colour tolerance %02d", colourTolerance));
				}
		});
		
		//Attach Slider listener for search radius slider 
		radiusSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
				 Number old_val, Number new_val) {
				 		//Update tolerance in real time
				 		searchRadius = new_val.intValue();
				 		radiusLabel.setText(String.format("Search radius %03d", searchRadius));
				}
		});

    }
    
	//Implement to read a marker file for marker labels
	@FXML protected void handleMarkerButtonAction(ActionEvent event){
		//Read a file in here
		//Browse for a file
        FileChooser fc = new FileChooser();
		 fc.setTitle("Select marker set");
		 fc.getExtensionFilters().addAll(
				   new ExtensionFilter("Marker set files", Arrays.asList(new String[]{"*.txt","*.TXT"})));
		 File selectedFile = fc.showOpenDialog(frameLabel.getScene().getWindow());
		  if (selectedFile != null) {
				try{
					BufferedReader br=new BufferedReader(new FileReader(selectedFile));  //creates a buffering character input stream  
					String line;  
					markerLabels = new ArrayList<String>();
					while((line=br.readLine())!=null){  
						markerLabels.add(line);      //appends line to string buffer  
					}  
					br.close();    //closes the stream and release the resources  
				}catch(Exception e){System.out.println("Could not read markers "+e.toString());}
				if (markerLabels != null){
					mSet = new MarkerSet(markerLabels);
					//Create the dropdown menu here into markerBox
					ObservableList<String> options = FXCollections.observableArrayList(markerLabels);
					markerBox.setItems(options);
					markerBox.setValue(mSet.set.get(0).label);
					markerBox.setOnAction(new EventHandler<ActionEvent>(){
						@Override
						public void handle(ActionEvent e){
							trackToggle.setSelected(mSet.set.get(markerLabels.indexOf(markerBox.getValue())).trackOn);
						}
					});
				}
		  }
	}
	
	//Implement to toggle marker on/off
	@FXML protected void handleTrackToggle(ActionEvent event){
		mSet.set.get(markerLabels.indexOf(markerBox.getValue())).trackOn = trackToggle.isSelected();
	}
	
    
    @FXML protected void handleLoadButtonAction(ActionEvent event) {
        //Test reading, and displaying a frame here
        //System.out.println("Got LoadButton click");
        
        //Browse for a file
        FileChooser fc = new FileChooser();
		 fc.setTitle("Select MP4 videofile");
		 fc.getExtensionFilters().addAll(
				   new ExtensionFilter("Video files", Arrays.asList(new String[]{"*.mp4","*.MP4","*.MOV","*.mov"})));
		 File selectedFile = fc.showOpenDialog(frameLabel.getScene().getWindow());
		 
		 if (selectedFile != null) {
			 currentFile = selectedFile;
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
						  		//System.out.println(String.format("Start searching frame %d",val));
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
				 			//System.out.println(String.format("MOUSE Start searching frame %d current %d",val,currentFrameNo));
					 		getFrame(val);
					  }
		         }
		
			
			});	
			 
			 //Display the first image
			 currentFrame = null;
        try{
        		long beforeMillis = System.currentTimeMillis();
				currentFrame = videoReader.nextFrame();
				long afterMillis = System.currentTimeMillis();
				//System.out.println(String.format("got frame, time stamp %.2f took %.2f s to decpde",currentFrame.getTimeStamp(),((double) (afterMillis-beforeMillis))/1000d));
        }catch (Exception ex){
            System.err.println("Could not read frame.");
        }
        videoView.setImage(SwingFXUtils.toFXImage(currentFrame, null));
			
			//Add DigitisedPoints for digitisation
			dp = new DigitisedPoints();
			
			//Attach mouse released listener on the videoView to digitise markers
			videoView.setOnMouseReleased(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						//Scale into frame pixels from view pixels
						Bounds bounds = videoView.getBoundsInParent();
						double xScale = bounds.getWidth()/((double) currentFrame.getWidth());
						double yScale = bounds.getHeight()/((double) currentFrame.getHeight());
					
						digitisedCoordinates[0] = e.getX()/xScale;
						digitisedCoordinates[1] = e.getY()/yScale;
						
						//System.out.println(String.format("Digitised X %.1f Y %.1f scaledX %.1f scaledY %.1f"
						//	,e.getX(),e.getY()
						//	,digitisedCoordinates[0],digitisedCoordinates[1])
						//);
						
						//Set the colour to lookg for
						tp.setSearchRadius(searchRadius);	//Set radius search radius
						tp.setColourToLookFor(currentFrame,digitisedCoordinates);	//Update colour to search for						
						digitiseMarker(digitisedCoordinates);
						
						//System.out.println(String.format("Digitised X %.1f Y %.1f scaledX %.1f scaledY %.1f"
						//,e.getX(),e.getY()
						//,digitisedCoordinates[0],digitisedCoordinates[1])
						//);
						
		         }
		
			
			});
		 
		 }
        
                
    }
    
    private boolean digitiseMarker(double[] digitisedCoordinates){
    	if (digitisedCoordinates != null){
			refinedCoordinates = tp.searchMarker(currentFrame,digitisedCoordinates, colourTolerance);
			if (refinedCoordinates != null){
				//System.out.println(String.format("Frame %d tStamp %.2f Refined X %.1f Y %.1f",currentFrameNo,currentFrame.getTimeStamp(),refinedCoordinates[0],refinedCoordinates[1]));
				System.out.println(String.format("%d\t%f\t%f\t%f",currentFrameNo,currentFrame.getTimeStamp(),refinedCoordinates[0],refinedCoordinates[1]));
				
				dp.addPoint(refinedCoordinates, currentFrameNo,currentFrame.getTimeStamp());
				//Highlight the digitised pixels
				videoView.setImage(SwingFXUtils.toFXImage(tp.getColoured(), null));	//Update the view
				return true;
			}else{
				System.out.println("Could not digitise marker");
				return false;
			}
		}
		return false;						
    }
    
     @FXML protected void handleFrameButtonAction(ActionEvent event) {
        //Test reading, and displaying a frame here
        //System.out.println("Got Frame button click");
        frameSlider.increment();
     }
     
     @FXML protected void handleExtractButtonAction(ActionEvent event) {
        System.out.println("Extract");
        System.out.println("Got File "+currentFile.toString());
        try{
        	ImageIO.write(currentFrame, "png", new File(String.format("frameExport_%s_%05d",currentFile.getName(), currentFrameNo)));
        }catch (Exception e){
        	System.out.println("Could not save Image");
        }
     }
    
     @FXML protected void handleTrackButtonAction(ActionEvent event) {
        toggleTrackButton();
        
        if (trackOn){
        	//Start tracking thread
        	trackingRunnable = new TrackingRunnable(this);
        	trackingThread = new Thread(trackingRunnable);
        	trackingThread.start(); 
        }else{
        	if (trackingThread != null){
        		//System.out.println("Calling trackingRunnable stop");
        		trackingRunnable.stop();
        		try{
        			//System.out.println("Waiting trackingThread join");
        			trackingThread.join(1l);
        			//trackingRunnable = null;
        			//trackingThread = null;
        		}catch (Exception e){
        			//System.out.println("Could not join trackingThread "+e.toString());
        		}
        		//System.out.println("trackingThread join worked");
        	}
        }
        
     }
     
    public void toggleTrackButton(){
    		trackOn ^= true;
		  //System.out.println(String.format("Auto Track %s",trackOn ? "On" : "Off"));
		  trackButton.setText(String.format("Auto Track %s",trackOn ? "On" : "Off"));
    }
    //Helper runnable
    public class TrackingRunnable implements Runnable{
    	private final FXMLControls parentObject;
    	private boolean keepgoing = true;
    	boolean success;
    	public TrackingRunnable(FXMLControls parentObject){
    		this.parentObject = parentObject;
    	}
    	public void setSuccess(boolean a){
    		this.success = a;
    	}
    	public void run(){
    		while (keepgoing){
    			//Load next frame in runLater call, and wait for the call to finish
    			CountDownLatch waitLatch = new CountDownLatch(1);
    			Platform.runLater(new Runnable(){
		 				CountDownLatch waitLatch;
		 				public Runnable init(CountDownLatch waitLatch){
		 					this.waitLatch = waitLatch;
		 					return this;
		 				}
		 				@Override 
		 				public void run(){
		 					parentObject.frameSlider.increment();	//This will get the next frame
		 					boolean success = parentObject.digitiseMarker(parentObject.refinedCoordinates);
		 					setSuccess(success);
		 					waitLatch.countDown();
		 				}
		 			}.init(waitLatch)
    			);
    			try{
    				//System.out.println("Awaiting");
	    			waitLatch.await();  			
    			}catch (Exception e){
    				System.out.println("Await failed "+e.toString());
    			}
    			//Check if we lost the point
	 			if (!success){
	 				stop();
	 				//Notify user that tracking failed
	 				Platform.runLater(new Runnable(){
    					@Override public void run(){
    						parentObject.toggleTrackButton();
    					}
    				});
	 				//System.out.println("Could not digitise marker");
	 				//break;
	 			}
    		}
    	}
    	public void stop(){
    		keepgoing = false;
    		//System.out.println("TrackingRunnable stop called");
    	}
    }
    
     @FXML protected void handleCloseButtonAction(ActionEvent event) {
		  //System.out.println("Got Close button click");
		  
		  closeVideo();
	  }
	  
	  public void closeVideo(){
	  		if (videoReader != null){
	  			videoReader.close();
	  		}
	  		videoReader = null;		
	  }
	 
	 //Cleanup here
	 public void shutdown(){
	 	//System.out.println("Called controls shutdown()");
	 	closeVideo();
	 }
	 
	 //Helper functions
	 public void getNextFrame(){
	 	currentFrame = null;
			//Get next frame here
        try{
        		long beforeMillis = System.currentTimeMillis();
				currentFrame = videoReader.nextFrame();
				++currentFrameNo;
				long afterMillis = System.currentTimeMillis();
				//System.out.println(String.format("got next frame, time stamp %.2f took %.2f s to decpde",currentFrame.getTimeStamp(),((double) (afterMillis-beforeMillis))/1000d));
								
        }catch (Exception ex){
            System.err.println("Could not read frame.");
        }
        videoView.setImage(SwingFXUtils.toFXImage(currentFrame, null));
	 }
	 
	 public void getFrame(int frameNo){
	 	//Do nothing if the requested frame is already up
	 	if (frameNo == currentFrameNo){
	 		//System.out.println("Already the current frame");
	 		return;
	 	}
	 	 	
	 	if (frameNo == currentFrameNo+1){
	 		//Load next frame if incremented by one	
	 		getNextFrame();
	 	}else{
	 		//Have to search for the frame
	 		currentFrameNo = frameNo;
	 		frameSlider.setValue(currentFrameNo);
			  //Update the VideoView
			  currentFrame = null;
			  try{
			  		long beforeMillis = System.currentTimeMillis();
					currentFrame = videoReader.readFrame(frameNo);
				
					long afterMillis = System.currentTimeMillis();
					//System.out.println(String.format("got frame, time stamp %.2f took %.2f s to decpde",currentFrame.getTimeStamp(),((double) (afterMillis-beforeMillis))/1000d));
			  }catch (Exception ex){
					System.err.println("Could not read frame.");
			  }
			  videoView.setImage(SwingFXUtils.toFXImage(currentFrame, null));
	 	}
	 }
	 
}
