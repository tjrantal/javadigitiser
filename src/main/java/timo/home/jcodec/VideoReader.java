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
import org.jcodec.common.model.ColorSpace;
import org.jcodec.scale.RgbToBgr;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;
//import org.jcodec.scale.AWTUtil;
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
	
	//To enable extending
	public VideoReader(){}

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
			//System.out.println(String.format("Got picture tStamp %.2f duration %.2f", picture.getTimestamp(),picture.getDuration()));
			//return new BIWithMeta(AWTUtil.toBufferedImage(picture.getPicture()),picture.getTimestamp(),picture.getDuration());
			return new BIWithMeta(toBufferedImage(picture.getPicture()),picture.getTimestamp(),picture.getDuration());
			
		}catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}
	
	//Try to work around jcodec and jcodec.javase module conflicts, copied from org.jcodec.scale.AWTUtil
	
	public BufferedImage toBufferedImage(Picture src) {
		if (src.getColor() != ColorSpace.BGR) {
			Picture bgr = Picture.createCropped(src.getWidth(), src.getHeight(), ColorSpace.BGR, src.getCrop());
			if (src.getColor() == ColorSpace.RGB) {
				new RgbToBgr().transform(src, bgr);
			} else {
				Transform transform = ColorUtil.getTransform(src.getColor(), ColorSpace.RGB);
				transform.transform(src, bgr);
				new RgbToBgr().transform(bgr, bgr);				
			}
			src = bgr;
		}

        BufferedImage dst = new BufferedImage(src.getCroppedWidth(), src.getCroppedHeight(),
                BufferedImage.TYPE_3BYTE_BGR);

        if (src.getCrop() == null)
            toBufferedImage(src, dst);
        else
            toBufferedImageCropped(src, dst);

        return dst;
    }
	
	private void toBufferedImageCropped(Picture src, BufferedImage dst) {
        byte[] data = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
        byte[] srcData = src.getPlaneData(0);
        int dstStride = dst.getWidth() * 3;
        int srcStride = src.getWidth() * 3;
        for (int line = 0, srcOff = 0, dstOff = 0; line < dst.getHeight(); line++) {
            for (int id = dstOff, is = srcOff; id < dstOff + dstStride; id += 3, is += 3) {
                // Unshifting, since JCodec stores [0..255] -> [-128, 127]
                data[id] = (byte) (srcData[is] + 128);
                data[id + 1] = (byte) (srcData[is + 1] + 128);
                data[id + 2] = (byte) (srcData[is + 2] + 128);
            }
            srcOff += srcStride;
            dstOff += dstStride;
        }
    }

    public void toBufferedImage(Picture src, BufferedImage dst) {
        byte[] data = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
        byte[] srcData = src.getPlaneData(0);
        for (int i = 0; i < data.length; i++) {
            // Unshifting, since JCodec stores [0..255] -> [-128, 127]
            data[i] = (byte) (srcData[i] + 128);
        }
    }

	

}
