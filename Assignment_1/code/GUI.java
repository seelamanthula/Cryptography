package com.cryptography.assignment1;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

public class GUI extends JFrame implements ActionListener {

	JPanel panel;
	JTextField text;
	ImageCanvas img;
	AudioImageSteganography steganography = null;
	String audioInput = "C:/Users/Harish/Desktop/Crypto/Assignments/01/Trial/input/president_speech.wav";
	String imageInput = "C:/Users/Harish/Desktop/Crypto/Assignments/01/Trial/input/host_image.bmp";

	public GUI() {
	    super("Stegnography");
	    setLayout(new BorderLayout());
	    this.panel = new JPanel();
	    this.panel.setLayout(new FlowLayout());
	    add(panel, BorderLayout.CENTER);

	    JButton bplus = new JButton("+");
	    this.panel.add(bplus);
	    this.text = new JTextField("0",3);
	    this.panel.add(text);
	    JButton bminus = new JButton("-");	 
	    this.panel.add(bminus);

	    buttonActionPlus(bplus);
	    buttonActionMinus(bminus);	    
	    
	    byte b = 0;
   	 	img = new ImageCanvas(doPaint(b));
	    this.panel.add(img);
   	 
	    JButton button = new JButton("PLAY");
	    add(button, BorderLayout.SOUTH);
	    button.addActionListener(this);
	    
	    JButton bstop = new JButton("STOP");
	    add(bstop, BorderLayout.EAST);
	    buttonAudioStop(bstop);
	    
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setSize(500, 500);
	    setVisible(true);
	}
	
	public void buttonAudioStop(JButton btn) {

		btn.addActionListener(new ActionListener()   {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("In STOP Audio");
				steganography.stopAudioPlay();
			}
		
		});
	}
	
	public void buttonActionPlus(JButton btn) {
		
		btn.addActionListener(new ActionListener()   {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				  String s = text.getText();
		    	  System.out.println("clicked Btn -- : "+s);
		    	  
		    	  byte bit = Byte.parseByte(s);
		    	  if(bit >= 0 && bit < 8) {
		    		 bit += 1;  
			    	 text.setText(bit+"");
			    	 
			    	 img.setImage(doPaint(bit));
			    	 JPanel pan1 = new JPanel();
			    	 pan1.add(img);
			    	 panel.add(pan1);
			  	     panel.revalidate();
			      }
		    	  
		    	  
			}
			
		});
	}

	public void buttonActionMinus(JButton btn) {
		
		btn.addActionListener(new ActionListener()   {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				  String s = text.getText();
		    	  System.out.println("clicked Btn -- : "+s);
		    	  
		    	  byte bit = Byte.parseByte(s);
		    	  if(bit > 0 && bit < 9) {
		    		 bit -= 1;  
			    	 text.setText(bit+"");
			    	 img.setImage(doPaint(bit));
			    	 JPanel pan1 = new JPanel();
			    	 pan1.add(img);
			    	 panel.add(pan1);
			  	     panel.revalidate();
			      }
			}
			
		});
	}

	 private Image doPaint(byte bits) {	

			BufferedImage bImage = null;
			AudioInputStream audioStream = null;
			BufferedImage image = null;
			AudioFormat audioFormat = null;
		 
			try {
					audioStream = AudioSystem.getAudioInputStream(new File(audioInput));
					audioFormat = audioStream.getFormat();
					bImage =  ImageIO.read(new File(imageInput));
				} catch (UnsupportedAudioFileException | IOException e) {
					e.printStackTrace();
				}
				
			this.steganography = new AudioImageSteganography(bImage, audioStream);
			
			if(bits == 0) {
				return steganography.getImage();
			}
			
			steganography.getMaskedImage(bits);
			
			byte[] data = steganography.readAudioFileFromSource();
			byte[] aOutput = steganography.maskAudioBits(data, bits);
			steganography.encodeAudioInImage(data, bits);
			this.audio = aOutput;
			
			return steganography.getImage();
		 }
		
	 private byte[] audio;
	 private byte[] getAudioData() {
		 return audio;
	 }
	 
	 public class ImageCanvas extends JPanel	{ 
		  Image img;
	
		  public void paintComponent(Graphics g)  {
			  g.drawImage(img, 0, 0, this);
		  }
	
		  public void setImage(Image img)  {
			  this.img = img;
		  }
	
		  public ImageCanvas(Image img)  {
			  this.img = img;
			  this.setPreferredSize(new Dimension(img.getWidth(this), img.getHeight(this)));
		  }
	 }
	
	 public void actionPerformed(ActionEvent evt) {
		 try {
			 steganography.playAudioFromByteArray(getAudioData());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new GUI();
	}
}