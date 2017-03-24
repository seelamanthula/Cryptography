package com.cryptography.assignment1;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class AudioImageSteganography {

	AudioInputStream audioInputStream = null;
	AudioFormat audioFormat = null;
	BufferedImage image = null;
	
	public AudioImageSteganography(BufferedImage image, AudioInputStream audioInputStream) {
		this.audioInputStream = audioInputStream;
		this.audioFormat = audioInputStream.getFormat();
		this.image = image;		
	}
	
	public byte[] readAudioFileFromSource() {
		
			int bpFrame = audioFormat.getFrameSize();
		    int numberBytes = 1024 * bpFrame; 
		    byte[] audioBytes = new byte[numberBytes];
	    	
		    try {
		    	byte[] data = new byte[audioInputStream.available()];

		    	int bytesRead = 0;
		    	int currentBytes = 0;
		
		    	while (bytesRead != -1) {

		    		bytesRead = audioInputStream.read(audioBytes, 0, numberBytes);
		    		currentBytes += bytesRead;
		    		
		    		// reading audio bytes
		    		for(int k = currentBytes-1024, i = 0; k < currentBytes; k++, i++) {
		    			data[k] =  audioBytes[i];
		    			if(data[k] !=  audioBytes[i]) {
		    				System.out.println("B : "+audioBytes[i]+" D : "+data[k]);
		    			}
		    		}
		    	}
		    	
		    	return data;
		    	
		    } catch (Exception ex) { 
		    	System.out.println(ex.getMessage());
		    }
		
		return null;
	}

	public byte[] maskAudioBits(byte[] data, int bits) {

		int[] audioInt = byteArrayToIntArray(data);
		
		int maskBits = (int)(Math.pow(2, bits)) - 1 << (8 - bits);
		int mask = (maskBits << 24) | (maskBits << 16) | (maskBits << 8) | maskBits;

		for (int i = 0; i < audioInt.length; i++)  {
			audioInt[i] = audioInt[i] & mask;
		}

		return intToByteArray(audioInt);
	}

	public void getMaskedImage(int bits) {
		 
		 int[] imageRGB = image.getRGB(0, 0, image.getWidth(null), 
				 							 image.getHeight(null), 
				 							 null, 0, 
				 							 image.getWidth(null));

		 int maskBits = (int)(Math.pow(2, bits)) - 1 << (8 - bits);
		 int mask = (maskBits << 24) | (maskBits << 16) | (maskBits << 8) | maskBits;
	
		 for (int i = 0; i < imageRGB.length; i++)  {
			 imageRGB[i] = imageRGB[i] & mask;
		 }
	
		 image.setRGB(0, 0, image.getWidth(null), 
				 			image.getHeight(null), 
				 			imageRGB, 0, 
				 			image.getWidth(null));	 
	}

	public Image getImage() {
	  return image;
	}
	 
	public void encodeAudioInImage(byte[] data, int encodeBits) {
	
		int[] audioInt = byteArrayToIntArray(data);
		
		int[] imageRGB = image.getRGB(0, 0, image.getWidth(null), 
					 image.getHeight(null), 
					 null, 0, 
					 image.getWidth(null));

		System.out.println("Encode Bits : "+encodeBits);

		int k = (int)(Math.pow(2, encodeBits)) - 1;
		int encodeByteMask = k << (8 - encodeBits);	
		int encodeMask = (encodeByteMask << 24) | (encodeByteMask << 16) | (encodeByteMask << 8) | encodeByteMask;

		int decodeByteMask = ~(encodeByteMask >>> (8 - encodeBits)) & 0xFF; 
	//	int decodeByteMask = ~(k); 
		System.out.println("D : "+Integer.toBinaryString(decodeByteMask));
		
		int hostMask = (decodeByteMask << 24) | (decodeByteMask << 16) | (decodeByteMask << 8) | decodeByteMask;

		for (int i = 0; i < imageRGB.length && i < audioInt.length; i++)	  {
			int encodeData = (audioInt[i] & encodeMask) >>> (8 - encodeBits);
			imageRGB[i] = (imageRGB[i] & hostMask) | (encodeData & ~hostMask);
		}
		
		image.setRGB(0, 0, image.getWidth(null), image.getHeight(null), 
		imageRGB, 0, image.getWidth(null));
	}
	
	private byte[] intToByteArray(int[] intData) {

		ByteBuffer byteBuffer = ByteBuffer.allocate(intData.length * 4);        
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(intData);
        
	    return byteBuffer.array();      
	}
	
	private int[] byteArrayToIntArray(byte[] byteData) {
		
		IntBuffer intBuffer = ByteBuffer.wrap(byteData)
				     		.order(ByteOrder.BIG_ENDIAN)
				     		.asIntBuffer();
		
		int[] array = new int[intBuffer.remaining()];
		intBuffer.get(array);
		
		return array;
	}	
	
	public void playAudioFromByteArray(byte[] audio) throws Exception {
		
		AudioFormat format = audioFormat;

		final AudioInputStream aIStream = new AudioInputStream(new ByteArrayInputStream(audio), 
														  format, 
														  audio.length / format.getFrameSize());
		
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(format);
		line.start();

		Runnable runner = new Runnable() {
			int bSize = (int) format.getSampleRate() * format.getFrameSize();
			byte playBuffer[] = new byte[bSize];

			public void run() {
				try {
					boolean play = true;
					while (count != -1) {						
						count = aIStream.read(playBuffer, 0, playBuffer.length);
						
						if (count > 0) {
							line.write(playBuffer, 0, count);
						}
						if (!play)
							break;
					}
					play = false;
					line.drain();
					line.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		playThread = new Thread(runner);
		playThread.start();

	}
	
	Thread playThread = null;
	int count;

	public void stopAudioPlay(){
	//	count = 0;
	//	System.out.println("count : "+count);
		playThread.stop();
	}
	
}
