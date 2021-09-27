package timo.home.fxml;
//FXML-defined stuff
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

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
import javafx.scene.control.TextArea;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import java.util.Locale;

//Extract frame
import javax.imageio.ImageIO;

import timo.home.jcodec.VideoReader;
import timo.home.jcodec.BIWithMeta;
//import timo.home.tracking.TrackPoint;
//import timo.home.tracking.DigitisedPoints;
import timo.home.tracking.MarkerSet;
import timo.home.tracking.Point;

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
	private BIWithMeta colouredFrame;
	
	//For UI
	public int currentFrameNo = 0;
	public boolean trackOn = false;
	public int colourTolerance = 10;
	public int searchRadius = 50;
	public double[] digitisedCoordinates = new double[2];
	
	public MarkerSet mSet = null;
	ArrayList<String> markerLabels = null;
	
   //public TrackPoint tp;// = new TrackPoint(20);
   //private DigitisedPoints dp;
   private Thread trackingThread = null;
   private TrackingRunnable trackingRunnable = null;
   private File currentFile;
   
    //Initialise gets called when the controller is instantiated
    public void initialize(){
    	//Create TrackPoint
    	//tp = new TrackPoint(searchRadius);
    	
		//Attach Slider listener for colour slider 
		colourSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
				 Number old_val, Number new_val) {
				 		//Update tolerance in real time
				 		colourTolerance = new_val.intValue();
				 		colourLabel.setText(String.format("Colour tolerance %02d", colourTolerance));
						
						//Set tp colourTolerance
						mSet.set.get(markerLabels.indexOf(markerBox.getValue())).tp.setTolerance(colourTolerance);
				}
		});
		
		//Attach Slider listener for search radius slider 
		radiusSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
				 Number old_val, Number new_val) {
				 		//Update tolerance in real time
				 		searchRadius = new_val.intValue();
				 		radiusLabel.setText(String.format("Search radius %03d", searchRadius));
						//Set tp searchRadius
						mSet.set.get(markerLabels.indexOf(markerBox.getValue())).tp.setSearchRadius(searchRadius);
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
							//Set sliders
							colourSlider.setValue(mSet.set.get(markerLabels.indexOf(markerBox.getValue())).tp.getTolerance());
							radiusSlider.setValue(mSet.set.get(markerLabels.indexOf(markerBox.getValue())).tp.getSearchRadius());
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
		colouredFrame = new BIWithMeta(currentFrame);
        videoView.setImage(SwingFXUtils.toFXImage(currentFrame, null));
			
			//Add DigitisedPoints for digitisation
			//dp = new DigitisedPoints();
			
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
						
						//Get the current marker, set searchRadius, set marker indice
						int markerIndex = markerLabels.indexOf(markerBox.getValue());
						
						mSet.set.get(markerIndex).tp.setSearchRadius(searchRadius);	//Set radius search radius
						mSet.set.get(markerIndex).tp.setColourToLookFor(currentFrame,digitisedCoordinates);	//Set radius search radius
						
						mSet.set.get(markerIndex).dp.lastKnown = new double[]{digitisedCoordinates[0],digitisedCoordinates[1]};
						//Set the colour to lookg for
						//tp.setSearchRadius(searchRadius);	//Set radius search radius
						//tp.setColourToLookFor(currentFrame,digitisedCoordinates);	//Update colour to search for						
						digitiseMarker(digitisedCoordinates,markerIndex);
						
						//System.out.println(String.format("Digitised X %.1f Y %.1f scaledX %.1f scaledY %.1f"
						//,e.getX(),e.getY()
						//,digitisedCoordinates[0],digitisedCoordinates[1])
						//);
						
		         }
		
			
			});
		 
		 }
        
                
    }
    
    private boolean digitiseMarker(double[] digitisedCoordinates, int markerIndex){
    	if (digitisedCoordinates != null){
			
			mSet.set.get(markerIndex).dp.lastKnown  = mSet.set.get(markerIndex).tp.searchMarker(currentFrame,digitisedCoordinates, mSet.set.get(markerIndex).tp.getTolerance());
			if (mSet.set.get(markerIndex).dp.lastKnown  != null){
				//System.out.println(String.format("Frame %d tStamp %.2f Refined X %.1f Y %.1f",currentFrameNo,currentFrame.getTimeStamp(),refinedCoordinates[0],refinedCoordinates[1]));
				System.out.println(String.format("%d\t%s\t%f\t%f\t%f",currentFrameNo,markerLabels.get(markerIndex),currentFrame.getTimeStamp(),mSet.set.get(markerIndex).dp.lastKnown[0],mSet.set.get(markerIndex).dp.lastKnown[1]));
				
				mSet.set.get(markerIndex).dp.addPoint(mSet.set.get(markerIndex).dp.lastKnown, currentFrameNo,currentFrame.getTimeStamp());
				
				
				//Highlight the digitised pixels, add the colouring of the digitised marker	
				colouredFrame = colourMask(colouredFrame,mSet.set.get(markerIndex).tp.getColourCoordinates(),new int[]{255,0,0});
				
				videoView.setImage(SwingFXUtils.toFXImage(colouredFrame, null));	//Update the view
				return true;
			}else{
				System.out.println("Could not digitise marker");
				return false;
			}
		}
		return false;						
    }
	
	private BIWithMeta colourMask(BIWithMeta im, ArrayList<int[]> coords, int[] colour){
		//System.out.println(String.format("colourMask coords.size() %d",coords.size()));
		for(int i = 0;i<coords.size();++i){
			int pixelcolour = 0xff<<24 | colour[0] << 16 | colour[1]<<8 | colour[2];
			im.setRGB(coords.get(i)[0],coords.get(i)[1],pixelcolour);
		}
		return im;
	}
    
     @FXML protected void handleFrameButtonAction(ActionEvent event) {
        //Test reading, and displaying a frame here
        //System.out.println("Got Frame button click");
        frameSlider.increment();
     }
     
	 @FXML protected void handleDisplayCoordsAction(ActionEvent event){
		String output = "LABEL\tFrameNo\ttStamp\tX\tY\n";
		for (int m = 0; m<mSet.set.size();++m){
			for (int p = 0; p<mSet.set.get(m).dp.points.size();++p){
				Point tempPoint = mSet.set.get(m).dp.points.get(p);
				output+=String.format(Locale.ROOT,"%s\t%d\t%f\t%f\t%f\n",mSet.set.get(m).label,tempPoint.frameNo,tempPoint.timeStamp,tempPoint.x,tempPoint.y);
			}
		}
		
		//Pop up a text view, and show the coordinate string -> copy paste to a text editor of your choice
		Scene scene = new Scene(new VBox(new TextArea(output)),400,400);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setTitle("Coordinates");
		stage.show();

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
		int currentMarker = -1;
    	public TrackingRunnable(FXMLControls parentObject){
    		this.parentObject = parentObject;
    	}
    	public void setSuccess(boolean a){
    		this.success = a;
    	}
		public void setCurrentMarker(int currentMarker){
			this.currentMarker = currentMarker;
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
							//Run through markers here
							int currentMarker = 0;
							boolean success = true;
							while (currentMarker < parentObject.markerLabels.size() && success){
								if (parentObject.mSet.set.get(currentMarker).trackOn){
									success = parentObject.digitiseMarker(parentObject.mSet.set.get(currentMarker).dp.lastKnown,currentMarker);
								}
								++currentMarker;
							}
							
		 					setSuccess(success);
							--currentMarker;	//Decrease currentMarker by one to be within range
		 					setCurrentMarker(currentMarker);
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
		colouredFrame = new BIWithMeta(currentFrame);
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
			  colouredFrame = new BIWithMeta(currentFrame);
	 	}
	 }
	 
}
