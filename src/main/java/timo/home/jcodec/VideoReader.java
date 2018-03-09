/*
	JCodec video reader interface
*/
package timo.home.jcodec;

//Java SE
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
//import javax.imageio.ImageIO;
import java.io.File;


//jcodec
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
//import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.common.Demuxer;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.JCodecUtil;
import org.jcodec.common.Format;
import org.jcodec.common.model.Size;
import org.jcodec.api.PictureWithMetadata;

public class VideoReader{
	BufferedImage bi;
	String fileIn;
	FrameGrab fg = null;
	FileChannelWrapper ch = null;
	double duration;
	int frames;
	double fps;
	int width;
	int height;

	public VideoReader(String fileIn){
		this.fileIn = fileIn;
		try{
			
			//Get duration, and number of frames
			File tempFile = new File(fileIn);
			//Format f = JCodecUtil.detectFormat(tempFile);
			Demuxer d = JCodecUtil.createDemuxer(JCodecUtil.detectFormat(tempFile), tempFile);
			DemuxerTrack video_track = d.getVideoTracks().get(0);
			
			
			/*
				Picture size
				import org.jcodec.common.model.Size;
				import org.jcodec.common.VideoCodecMeta;
				Size pictureSize = vcMeta.getSize();
				
				Format format = JCodecUtil.detectFormat(tempFile);
        		Codec videoCodec = getVideoCodec(format, tempFile);
        		CodecMeta = videoCodec.getMeta();
        		
        		DemuxerTrack.getMeta()
        		getVideoCodecMeta()
			*/
			//MP4Demuxer demuxer = new MP4Demuxer(ch); 
			//DemuxerTrack video_track = demuxer.getVideoTrack(); 
			duration = video_track.getMeta().getTotalDuration();
			frames = video_track.getMeta().getTotalFrames();
			fps = ((double) frames)/duration;
			Size pictureSize = video_track.getMeta().getVideoCodecMeta().getSize();
			width = pictureSize.getWidth();
	 		height = pictureSize.getHeight();
			
			ch = NIOUtils.readableChannel(new File(fileIn));
			fg = FrameGrab.createFrameGrab(ch);
			//fg = new FrameGrab(video_track, detectDecoder(video_track));
        	//fg.decodeLeadingFrames();
			
		   //fg = FrameGrab.createFrameGrab(ch);
      }catch(Exception e){
			System.out.println(e.toString());
		}
	}
	
	public void close(){
		try{
			NIOUtils.closeQuietly(ch);
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}

	public double getDuration(){
		return duration;
	}
	
	public double getFPS(){
		return fps;
	}
	
	public int getTotalFrames(){
		return frames;
	}
   
	public int getType(){
		return bi.getType();
	}

	public int getWidth(){
		return width; //bi.getWidth();
	}

	public int getHeight(){
		return height; //widthbi.getHeight();
	}
	
	public BufferedImage readFrame(int frameNumber){
		try{
			fg = fg.seekToFramePrecise(frameNumber);
			return nextFrame();
		}catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}
	
	public BufferedImage nextFrame() throws Exception{
		try{
			PictureWithMetadata picture = fg.getNativeFrameWithMetadata();
			return AWTUtil.toBufferedImage(picture.getPicture);
		}catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}

}
