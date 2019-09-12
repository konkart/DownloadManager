package gr.konkart.dm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class FileUtils{
	String location;
	InputStream in = null;
	
	// concatenates the temporary files in order to create the final wanted file
	public void concat(String args[],String outPut,String location) throws IOException{
		String outfile = outPut;
		this.location = location;
		File f = new File(outfile);
		
		OutputStream out = new FileOutputStream(location+f);
		
		
		byte[] buf = new byte[1024*4];
		for (String file : args) {
	        
			InputStream in = new FileInputStream(location+file);
			int b = 0;
			while ( (b = in.read(buf)) >= 0) {
				out.write(buf, 0, b);
				out.flush();
			}
			in.close();
		}
		out.close();
	}
  	
  	//method to delete files or a folder and all its content
	public boolean deleteFiles(File file) {
		File[] contents = file.listFiles();
		if (contents!=null) {
			for (File f : contents) {
				if (Files.isSymbolicLink(f.toPath())==false) {
					deleteFiles(f);
				}
			}
		}
		return file.delete();
	}
  	
  	//method to move a folder and its content to a new folder
	public void moveDir(File src, File dest) throws IOException {
		if(src.isDirectory()){
			dest.mkdir();
			String files[] = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				moveDir(srcFile,destFile);
			}
		}
		else {
			Files.move(Paths.get(src.getPath()), Paths.get(dest.getPath()), StandardCopyOption.REPLACE_EXISTING);
		}
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
						bufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
				newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, new Color(0,0,0,0),null);
	  		//write our image with the new extension (t)
				ImageIO.write(newBufferedImage, t, os);
	
				System.out.println("Done");
	  				
			} catch (IOException e) {
		
				e.printStackTrace();
		
			}
		}catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
  	//video conversion using ffmpeg
	String[] command;
	String[] commandFallback;
	public void videoConv(String n,String t) {
		//File ffmpeg = new File("ffmpeg.exe");
		String keepName[] = n.split("\\.");
		System.out.println(n + keepName.length);
		String file = keepName[0];
		command = new String[] {"cmd.exe", "/c", "ffmpeg -y -i "+n+" -crf 14 -speed fast -threads 4 -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" "+file+"."+t};
		commandFallback = new String[] {"cmd.exe", "/c", "\""+System.getProperty("user.dir")+"\\ffmpeg.exe\" -y -i "+n+" -crf 14 -speed fast -threads 4 -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" "+file+"."+t};
		String nameOfFile = n.substring(n.lastIndexOf('\\')+1)+"."+t;
		ffmpegConvert(file,nameOfFile);
	}
	//audio conversion using ffmpeg
	public void audioConv(String n,String t) {
		//File ffmpeg = new File("ffmpeg.exe");
		String keepName[] = n.split("\\.");
		System.out.println(n + keepName.length);
		String file = keepName[0];
		command = new String[] {"cmd.exe", "/c", "ffmpeg -y -i "+n+" -speed fast -threads 4 "+file+"."+t};
		commandFallback = new String[] {"cmd.exe", "/c", "\""+System.getProperty("user.dir") + "\\ffmpeg.exe\" -y -i "+n+" -speed fast -threads 4 "+file+"."+t};
		String nameOfFile = n.substring(n.lastIndexOf('\\')+1)+"."+t;
		ffmpegConvert(file,nameOfFile);
	}
	
	public void ffmpegConvert(String n , String t) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		//executes cmd command and reads the output
		FfmpegRender ff = new FfmpegRender(n.substring(n.lastIndexOf('\\')+1),t);
		System.out.println("aaaaaaa");
		Runnable task = () -> {
			int erCode = 0;
			try {
				Process proc=Runtime.getRuntime().exec(command);
				in = proc.getErrorStream();
				String c;
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				while (( c = reader.readLine()) != null)	{
					if(c.contains("Duration")) {
						ff.progress(true, sdf.parse("1970-01-01 " + c.split("Duration: ")[1].split(",")[0] ).getTime());
					} else if (c.contains("time=")) {
						ff.progress(false,sdf.parse("1970-01-01 " + c.split("time=")[1].split(" ")[0]).getTime());
					}
				}
				reader.close();
				in.close();
				erCode = proc.waitFor();
				if (erCode!=0) {
					proc=Runtime.getRuntime().exec(commandFallback);
					in = proc.getErrorStream();
					reader = new BufferedReader(new InputStreamReader(in));
					while (( c = reader.readLine()) != null)	{
						if(c.contains("Duration")) {
							ff.progress(true, sdf.parse("1970-01-01 " + c.split("Duration: ")[1].split(",")[0] ).getTime());
						} else if (c.contains("time=")) {
							ff.progress(false,sdf.parse("1970-01-01 " + c.split("time=")[1].split(" ")[0]).getTime());
						}
					}
					reader.close();
  	  				in.close();
  	  				erCode = proc.waitFor();
  				}
			}
			catch (Exception e1) {
				e1.printStackTrace();
			}
			finally {
				ff.ffmpegProgressFrame.dispose();
				JFrame frame = new JFrame();
				frame.setAlwaysOnTop( true );
				if (erCode==0) {
					JOptionPane.showMessageDialog(frame, "File conversion complete!");
				} else {
					JOptionPane.showMessageDialog(frame, "File conversion failed.");
				}
				frame.toFront();
				frame.setAlwaysOnTop( true );
			}
		};
		Thread thread = new Thread(task);                                                
		thread.start(); 
	}

	public class FfmpegRender extends JFrame{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JFrame ffmpegProgressFrame;
		JProgressBar progressBar;
		long max;
		/**
		 * @wbp.parser.entryPoint
		 */
		public FfmpegRender(String from,String to) {
			ffmpegProgressFrame = new JFrame();
			ffmpegProgressFrame.setResizable(false);
			ffmpegProgressFrame.getContentPane().setLayout(null);
	  			
	  			
			progressBar = new JProgressBar();
			progressBar.setBounds(10, 81, 414, 29);
			ffmpegProgressFrame.getContentPane().add(progressBar);
	  			
			JLabel fromLabel = new JLabel("New label");
			fromLabel.setHorizontalAlignment(SwingConstants.CENTER);
			fromLabel.setBounds(10, 11, 414, 14);
			ffmpegProgressFrame.getContentPane().add(fromLabel);
			fromLabel.setText(from);
	  			
			JLabel toLabel = new JLabel("New label");
			toLabel.setHorizontalAlignment(SwingConstants.CENTER);
			toLabel.setBounds(10, 56, 414, 14);
			ffmpegProgressFrame.getContentPane().add(toLabel);
			toLabel.setText(to);
	  			
			JLabel lblTo = new JLabel("To");
			lblTo.setHorizontalAlignment(SwingConstants.CENTER);
			lblTo.setBounds(10, 25, 414, 29);
			ffmpegProgressFrame.getContentPane().add(lblTo);
			ffmpegProgressFrame.setVisible(true);
	  			
			Dimension dimension = new Dimension(440,150);
			ffmpegProgressFrame.setPreferredSize(dimension);
			ffmpegProgressFrame.pack();
			ffmpegProgressFrame.setLocationRelativeTo(null);
			ffmpegProgressFrame.setVisible(true);
		}
		public void progress(boolean getDuration,long en) {
			if (getDuration==true) {
				max = en;
			} else {
				progressBar.setValue((int) ((en*100) / max));
			}
		}
	}
}
