package gr.konkart.dm;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class FileUtils{
	String home = System.getProperty("user.home");
	InputStream in = null;
	int concatcomplete=0;
	RandomAccessFile raf;
	public void concat(String args[],String au) throws IOException{//concatenates the temporary files in order to create the final wanted file
	String[] getname = au.split("/");
	String outfile=URLHandler.getFilename(getname[getname.length-1]);
	File f = new File(outfile);
	
	OutputStream out = new FileOutputStream(home+"\\Downloads\\"+f);
	
	
    byte[] buf = new byte[1024*4];
    for (String file : args) {
        
		InputStream in = new FileInputStream(home+"\\Downloads\\"+file);
        int b = 0;
        while ( (b = in.read(buf)) >= 0) {
            out.write(buf, 0, b);
            out.flush();
        }
        in.close();
    }
    out.close();
    
	concatcomplete=1;
	System.out.println("concat complete");
	
	}



 	
  	public void delete(String filename) throws IOException {//deletes the file
  	String name=URLHandler.getFilename(filename);
    File f = new File(home+"\\Downloads\\"+name);
    f.delete();
  	}
  	
  	public void imgConv(String n , String t) {//image conversion method by creating a buffered image and writing on it
  		BufferedImage bufferedImage;
  		File inputFile = new File(n);
  		String split[] = n.split("\\.");
  		String keepName = split[0]; 
  		File outputFile = new File(keepName+"."+t);//t variable is the extension type that was chose on context menu

  		try (InputStream is = new FileInputStream(inputFile)){
			
  		 
  			bufferedImage = ImageIO.read(is);
	  		try (OutputStream os = new FileOutputStream(outputFile)){ // create a blank, RGB, same width and height, and a white background
	  		 
	  		  BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
	  				bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
	  		  newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
	  		  ImageIO.write(newBufferedImage, t, os);//write our image with the new extension (t)
	
	  		  System.out.println("Done");
	  				
		  		} catch (IOException e) {
		
		  		  e.printStackTrace();
		
		  		}
  		}catch (FileNotFoundException e1) {
			System.out.println("e1");
		}catch (IOException e1) {
			System.out.println("e1");
		}
  	}
  	//was audio
  	public void videoConv(String n , String t) {//video conversion using ffmpeg
  		//File ffmpeg = new File("ffmpeg.exe");
  		String keepName[] = n.split("\\.");
  		pause();
  		/*String absolutePath = ffmpeg.getAbsolutePath();
  		String ffmpegPath = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
  		File ffmpegPath1 = new File(ffmpegPath);*/
  		System.out.println(n + keepName.length);
  		String nameOfFile = keepName[0];
  		File outputFile = new File(keepName+"."+t);
  		String[] command = new String[] {"cmd.exe", "/c", "ffmpeg -i "+n+" -crf 14 -speed fast -threads 4 "+nameOfFile+"."+t};
  		Runnable task = () -> {//executes cmd command and reads the output
  			try {
  		
  				Process process2=Runtime.getRuntime().exec(command);
  				InputStream in = process2.getErrorStream();
  				int c;
  				while ((c = in.read()) != -1)
  				{
  				    System.out.print((char)c);
  				}
  				in.close();

  				
  				
		  		}
  			 	catch (FileNotFoundException e1) {
  					System.out.println("e1");
  			 	}catch(Exception Io) {
		  			System.out.println(Io);
		  		}
  			
  				finally {
  					JFrame frame = new JFrame();
  					frame.setAlwaysOnTop( true );
  					JOptionPane.showMessageDialog(frame, "File conversion complete!");
  					frame.toFront();
  					frame.setAlwaysOnTop( true );
  				}
  		};
  		Thread thread = new Thread(task);                                                
  		thread.start(); 
  	}
  	public void audioConv(String n , String t) {
  		String keepName[] = n.split("\\.");
  		pause();
  		System.out.println(n + keepName.length);
  		String nameOfFile = keepName[0];
  		File outputFile = new File(keepName+"."+t);
  		String[] command = new String[] {"cmd.exe", "/c", "ffmpeg -i "+n+" -speed fast -threads 4 "+nameOfFile+"."+t};
  		Runnable task = () -> {//executes cmd command and reads the output
  			try {
  		
  				Process process2=Runtime.getRuntime().exec(command);
  				InputStream in = process2.getErrorStream();
  				int c;
  				while ((c = in.read()) != -1)
  				{
  				    System.out.print((char)c);
  				}
  				in.close();

  				
  				
		  		}catch (FileNotFoundException e1) {
  					System.out.println("e1");
  			 	}catch(Exception Io) {
		  			System.out.println(Io);
		  		}
  				finally {
					JFrame frame = new JFrame();
					frame.setAlwaysOnTop( true );
					JOptionPane.showMessageDialog(frame, "File conversion complete!");
					frame.toFront();
					frame.setAlwaysOnTop( true );
				}
  		};
  		Thread thread = new Thread(task);                                                
  		thread.start(); 
  	}
  	
  	static void pause(){
  	    long Time0 = System.currentTimeMillis();
  	    long Time1;
  	    long runTime = 0;
  	    while(runTime<1000){
  	        Time1 = System.currentTimeMillis();
  	        runTime = Time1 - Time0;
  	    }
  	}

}

