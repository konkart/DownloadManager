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
	
	//concatenates the temporary files in order to create the final wanted file
	public void concat(String args[],String au) throws IOException{
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



	//deletes the file
  	public void delete(String filename) throws IOException {
  	String name=URLHandler.getFilename(filename);
    File f = new File(home+"\\Downloads\\"+name);
    f.delete();
  	}
  	
  	/*image conversion method by creating a buffered image and writing on it
  	*	t variable is the extension type that was chose on context menu
  	*/
  	public void imgConv(String n , String t) {
  		BufferedImage bufferedImage;
  		File inputFile = new File(n);
  		String split[] = n.split("\\.");
  		String keepName = split[0];
  	
  		File outputFile = new File(keepName+"."+t);

  		try (InputStream is = new FileInputStream(inputFile)){
			
  		 
  			bufferedImage = ImageIO.read(is);
  			// create a blank, RGB, same width and height, and a white background
	  		try (OutputStream os = new FileOutputStream(outputFile)){ 
	  		 
	  		  BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
	  				bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
	  		  newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
	  		//write our image with the new extension (t)
	  		  ImageIO.write(newBufferedImage, t, os);
	
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
  	//video conversion using ffmpeg
  	public void videoConv(String n , String t) {
  		//File ffmpeg = new File("ffmpeg.exe");
  		String keepName[] = n.split("\\.");
  		pause();
  		System.out.println(n + keepName.length);
  		String nameOfFile = keepName[0];
  		File outputFile = new File(keepName+"."+t);
  		String[] command = new String[] {"cmd.exe", "/c", "ffmpeg -i "+n+" -crf 14 -speed fast -threads 4 "+nameOfFile+"."+t};
  		//executes cmd command and reads the output
  		Runnable task = () -> {
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
  	//audio conversion using ffmpeg
  	public void audioConv(String n , String t) {
  		String keepName[] = n.split("\\.");
  		pause();
  		System.out.println(n + keepName.length);
  		String nameOfFile = keepName[0];
  		File outputFile = new File(keepName+"."+t);
  		String[] command = new String[] {"cmd.exe", "/c", "ffmpeg -i "+n+" -speed fast -threads 4 "+nameOfFile+"."+t};
  		//executes cmd command and reads the output
  		Runnable task = () -> {
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
  	
  	//a simple method that will pause for 1 second
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

