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

	FrameGrab fg = null;
	FileChannelWrapper ch = null;
	double duration;
	int frames;
	double fps;
	int width;
	int height;

	public VideoReader(String fileIn){
		this(new File(fileIn));
	}

	public VideoReader(File tempFile){
		try{
			
			//Format f = JCodecUtil.detectFormat(tempFile);
			Demuxer d = JCodecUtil.createDemuxer(JCodecUtil.detectFormat(tempFile), tempFile);
			//Get info from the track; duration, number of frames, size
			DemuxerTrack video_track = d.getVideoTracks().get(0);
			duration = video_track.getMeta().getTotalDuration();
			frames = video_track.getMeta().getTotalFrames();
			fps = ((double) frames)/duration;
			Size pictureSize = video_track.getMeta().getVideoCodecMeta().getSize();
			width = pictureSize.getWidth();
	 		height = pictureSize.getHeight();
			ch = NIOUtils.readableChannel(tempFile);
			fg = FrameGrab.createFrameGrab(ch);
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
	
	public BIWithMeta readFrame(int frameNumber){
		try{
			fg = fg.seekToFramePrecise(frameNumber);
			return nextFrame();
		}catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}
	
	public BIWithMeta nextFrame() throws Exception{
		try{
			PictureWithMetadata picture = fg.getNativeFrameWithMetadata();
			return new BIWithMeta(AWTUtil.toBufferedImage(picture.getPicture()),picture.getTimestamp(),picture.getDuration());
		}catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}

}
