import java.net.*;
import java.io.*;


public class SubDownload extends Thread{
	double elapsedtime=0;
    long totalDownload = 0;
    int bytesRead = -1;
	public  String SubDownloadId;
	public  String FileLoc;
	public  long FileStartPos;
	public  long FileEndPos;
	public  long BytesDownloaded;
	public long BytesDownloadedP;
	public  byte Buffer[];
	public  byte NewBufferminus[];
	public  byte NewBufferplus[];
	public  int Complete=0;
	boolean Aborted = false;
	public boolean Paused = false;
	FileOutputStream outputStream = null;

public SubDownload(String aSubDownloadId,String aFileLoc,long aFileStartPos,long aFileEndPos,int aBufferSize){

		FileLoc=aFileLoc;
		FileStartPos=aFileStartPos;
		FileEndPos=aFileEndPos;
		Buffer = new byte[1024*aBufferSize];	
		BytesDownloaded=0;
		BytesDownloadedP=0;
		SubDownloadId=aSubDownloadId;
		Complete=0;

		}

public int SubDownloadStart(){ return 1;}

public void run(){

		try{

			URL url = new URL(FileLoc);
			URLConnection uc = url.openConnection();
			

			//int li_bytesRead;
			File f = new File("C:\\Users\\wcwra\\Videos\\Desktop\\"+SubDownloadId);
			if (f.exists()) {
			outputStream = new FileOutputStream(f,true);
			BytesDownloadedP = f.length();
			long newStartPos = f.length();
			FileStartPos = newStartPos+FileStartPos;
			uc.setRequestProperty("Range","bytes=" + FileStartPos + "-"+ FileEndPos);
			}
			else {
			outputStream = new FileOutputStream(f,true);
			}
			InputStream inputStream =  uc.getInputStream();
			byte[] buffer = Buffer;
			System.out.println(FileEndPos+"   "+FileStartPos);
			while(BytesDownloaded < (FileEndPos - FileStartPos) && Paused==false)
			{
				while ((bytesRead = inputStream.read(buffer)) != -1 && Paused==false) {
		        	
						
		            	outputStream.write(buffer, 0, bytesRead);
		            
		            	
		            	BytesDownloaded += bytesRead;
		            	BytesDownloadedP += bytesRead;
		            	//DOWNLOAD SPEED LIMIT TEST,TODO INTERVAL AND LIMIT CHECK
		            	/*if (bytesRead>500) {
		            		NewBufferminus = new byte [3];
		            		buffer = NewBufferminus;
		            	}
		            	else {
		            		NewBufferplus = new byte [500];
		            		buffer = NewBufferplus;
		            	}*/
		            	//buffer=NewBufferplus;
		            	
		        	
		        //--------------------  
		        	
				}     
			}
			System.out.println(BytesDownloaded);
			outputStream.close();
	        inputStream.close();

			//Finished Downloading
			Complete=1;

		}catch(Exception e){
			System.out.println(e);
		}
			

		}

	public void setPause() {
		Paused = true;
		System.out.println(Paused);
	}
	public boolean getPause() {
		return Paused;
	}
}

