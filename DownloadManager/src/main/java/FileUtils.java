import java.io.*;
import java.util.*;



public class FileUtils{
	InputStream in = null;
	int concatcomplete=0;
	RandomAccessFile raf;
	public void concat(String args[],String au) throws IOException{
	String[] getname = au.split("/");
	String outfile=URLHandler.getFilename(getname[getname.length-1]);
	File f = new File(outfile);
	
	OutputStream out = new FileOutputStream("C:\\Users\\wcwra\\Videos\\Desktop\\"+f);
	
	
    byte[] buf = new byte[1024*4];
    for (String file : args) {
        
		InputStream in = new FileInputStream("C:\\Users\\wcwra\\Videos\\Desktop\\"+file);
        int b = 0;
        while ( (b = in.read(buf)) >= 0) {
            out.write(buf, 0, b);
            out.flush();
        }
        in.close();
    }
    out.close();
    
	concatcomplete=1;
	}



 	
  	public void delete(String filename) throws IOException {
  	String name=URLHandler.getFilename(filename);
    File f = new File("C:\\Users\\wcwra\\Videos\\Desktop\\"+name);
    f.delete();
  }
  	

}

