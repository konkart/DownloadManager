package gr.konkart.dm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window.Type;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;

public class FileUtils{
	String location;
	InputStream in = null;
	
	// concatenates the temporary files in order to create the final wanted file
	public void concat(String files[],String outPut,String location) throws IOException{
		String outfile = outPut;
		this.location = location;
		File f = new File(outfile);
		
		OutputStream out = new FileOutputStream(location+f);
		byte[] buf = new byte[4096];
		for (String file : files) {
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
				deleteFiles(f);
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
		} else {
			Files.move(Paths.get(src.getPath()), Paths.get(dest.getPath()), StandardCopyOption.REPLACE_EXISTING);
		}
	}
  	
  	
  	/*image conversion method by creating a buffered image and writing on it
  	*	t variable is the extension type that was chose on context menu
  	*/
	public void imgConv(String input , String extension) {
		BufferedImage bufferedImage;
		File inputFile = new File(input);
		String split[] = input.split("\\.");
		String keepName = split[0];
		File outputFile = new File(keepName+"."+extension);
		int imgtype;
		try (InputStream is = new FileInputStream(inputFile)){
			
			bufferedImage = ImageIO.read(is);
			try (OutputStream os = new FileOutputStream(outputFile)){
				
				if (extension.equals("bmp") || extension.equals("jpg")) {
					imgtype = BufferedImage.TYPE_INT_RGB;
				} else {
					imgtype = BufferedImage.TYPE_INT_ARGB;
				}
				BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
						bufferedImage.getHeight(), imgtype);
				newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, new Color(0,0,0,0),null);
				ImageIO.write(newBufferedImage, extension, os);

	  				
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
	public void videoConv(String input,String extension) {
		String keepName[] = input.split("\\.");
		String file = keepName[0];
		command = new String[] {"cmd.exe", "/c", "ffmpeg -y -i "+input+" -crf 14 -threads 2 -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" "+file+"."+extension};
		commandFallback = new String[] {"cmd.exe", "/c", "\""+System.getProperty("user.dir")+"\\ffmpeg.exe\" -y -i "+input+" -crf 14 -speed fast -threads 4 -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" "+file+"."+extension};
		String nameOfFile = file+"."+extension;
		ffmpegConvert(input,nameOfFile);
	}
	//audio conversion using ffmpeg
	public void audioConv(String input,String extension) {
		String keepName[] = input.split("\\.");
		String file = keepName[0];
		command = new String[] {"cmd.exe", "/c", "ffmpeg -y -i "+input+" -threads 4 "+file+"."+extension};
		commandFallback = new String[] {"cmd.exe", "/c", "\""+System.getProperty("user.dir") + "\\ffmpeg.exe\" -y -i "+input+" -speed fast -threads 4 "+file+"."+extension};
		String nameOfFile = file+"."+extension;
		ffmpegConvert(input,nameOfFile);
	}
	
	public void ffmpegConvert(String input , String output) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		//executes cmd command and reads the output
		FfmpegRender ff = new FfmpegRender(input.substring(input.lastIndexOf('\\')+1),output.substring(output.lastIndexOf('\\')+1));
		Runnable task = () -> {
			int erCode = 0;
			try {
				Process proc=Runtime.getRuntime().exec(command);
				in = proc.getErrorStream();
				String c;
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				while (( c = reader.readLine()) != null)	{
					System.out.println(c);
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
		public FfmpegRender(String from,String to) {
			ffmpegProgressFrame = new JFrame();
			ffmpegProgressFrame.setResizable(false);
	  			
			progressBar = new JProgressBar();
	  			
			JLabel fromLabel = new JLabel("New label");
			fromLabel.setHorizontalAlignment(SwingConstants.CENTER);
			fromLabel.setText(from);
			fromLabel.setToolTipText(from);
	  			
			JLabel toLabel = new JLabel("New label");
			toLabel.setHorizontalAlignment(SwingConstants.CENTER);
			toLabel.setText(to);
			toLabel.setToolTipText(to);
	  			
			JLabel lblTo = new JLabel("To");
			lblTo.setHorizontalAlignment(SwingConstants.CENTER);
			GroupLayout groupLayout = new GroupLayout(ffmpegProgressFrame.getContentPane());
			groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(groupLayout.createSequentialGroup()
						.addGap(10)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
							.addComponent(fromLabel, GroupLayout.PREFERRED_SIZE, 414, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblTo, GroupLayout.PREFERRED_SIZE, 414, GroupLayout.PREFERRED_SIZE)
							.addComponent(toLabel, GroupLayout.PREFERRED_SIZE, 414, GroupLayout.PREFERRED_SIZE)
							.addComponent(progressBar, 390 , 414 , Short.MAX_VALUE))
						.addGap(10))
			);
			groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(groupLayout.createSequentialGroup()
						.addGap(11)
						.addComponent(fromLabel)
						.addComponent(lblTo, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
						.addGap(2)
						.addComponent(toLabel)
						.addGap(11)
						.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
						.addContainerGap())
			);
			ffmpegProgressFrame.getContentPane().setLayout(groupLayout);
			ffmpegProgressFrame.setVisible(true);
	  		
			Dimension dimension = new Dimension(450,150);
			ffmpegProgressFrame.setPreferredSize(dimension);
			ffmpegProgressFrame.setSize(dimension);
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
