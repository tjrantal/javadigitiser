/*
	JCodec video reader interface
*/
package timo.home.jcodec;

//Java SE
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
//import javax.imageio.ImageIO;
import java.io.File;

//File reading
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class RawVideoReader extends VideoReader{

	HeaderInfo headerInfo = null;	//To contain header info
	DataInputStream in = null;	//Pointers for data reading
	FileInputStream fin = null;
	int currentFrame;	//To enable getNextFrame
	int fileLength;
	byte[] imageBytes; 
	public RawVideoReader(String fileIn){
		this(new File(fileIn));
	}

	public RawVideoReader(File tempFile){
		try{
			fin = new FileInputStream(tempFile);
			in = new DataInputStream(fin);
			fileLength = (int) tempFile.length();
			headerInfo = readHeader(in);
			currentFrame = -1;
			duration = (double) headerInfo.frames/(double) headerInfo.frameRate;
			frames = headerInfo.frames;
			fps = headerInfo.frameRate;
			width = headerInfo.width;
	 		height = headerInfo.height;
			imageBytes = new byte[width*height];	//Reserve a buffer for reading frames
			
      }catch(Exception e){
			System.out.println(e.toString());
		}
	}
	
	public HeaderInfo readHeader(DataInputStream in){
		try{
			ArrayList<Integer> values = new ArrayList<Integer>();
			byte[] bytes = new byte[4];
			for (int i = 0;i< 18; ++i){
				in.read(bytes);	//Read bytes into memory
				values.add(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt());
			}
			return new HeaderInfo(values.get(3),values.get(4),values.get(17),values.get(2)+1,values.get(5));
		}catch(Exception e){
			System.out.println("Could not do headerInfo "+e.toString());
			return null;
		}
		
	}
	
	
	@Override
	public void close(){
		try{
			fin.close();
			in.close();
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}
   
	@Override
	public int getType(){
		return -1;
	}

	@Override
	public BIWithMeta readFrame(int frameNumber){
		try{

			fin.getChannel().position((long) (headerInfo.headerLength+(frameNumber-1)*width*height));	//Mark file for reset after reading the header
			currentFrame = frameNumber-1;
			return nextFrame();
		}catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}
	
	/*Will just read from the in DataStream*/
	@Override
	public BIWithMeta nextFrame() throws Exception{
		try{
			byte[] imageBytes = new byte[width*height];
			in.read(imageBytes);
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			
				byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
				for (int i = 0; i < imageBytes.length; i++) {
					// 3BYTE_BGR = 3 successive bytes define a colour
					data[3*i] = imageBytes[i];
					data[3*i+1] = imageBytes[i];
					data[3*i+2] = imageBytes[i];
				}
			++currentFrame;
			
			return new BIWithMeta(bi,(double) currentFrame/fps,1d/fps);
			
		}catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}

	public class HeaderInfo{
		public int width;
		public int height;
		public int frameRate;
		public int frames;
		public int synchFrame;
		public static final int headerLength = 72;
		public HeaderInfo(int width, int height, int frameRate, int frames, int synchFrame ){
			this.width = width;
			this.height = height;
			this.frameRate = frameRate;
			this.frames = frames;
			this.synchFrame = synchFrame;
		}
	}

}
