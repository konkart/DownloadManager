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
	public  byte Buffer[];
	public  byte NewBufferminus[];
	public  byte NewBufferplus[];
	public  int Complete=0;
	boolean Aborted = false;
	FileOutputStream outputStream = null;

public SubDownload(String aSubDownloadId,String aFileLoc,long aFileStartPos,long aFileEndPos,int aBufferSize){

		FileLoc=aFileLoc;
		FileStartPos=aFileStartPos;
		FileEndPos=aFileEndPos;
		Buffer = new byte[1024*aBufferSize];	
		BytesDownloaded=0;
		SubDownloadId=aSubDownloadId;
		Complete=0;

		}

public int SubDownloadStart(){ return 1;}

public void run(){

		try{

			URL url = new URL(FileLoc);
			URLConnection uc = url.openConnection();
			uc.setRequestProperty("Range","bytes=" + FileStartPos + "-"+ FileEndPos);

			//int li_bytesRead;
			File f = new File("\\DownloadedFiles\\"+SubDownloadId);
			
			outputStream = new FileOutputStream(f);
			InputStream inputStream =  uc.getInputStream();
			byte[] buffer = Buffer;
			while(BytesDownloaded < (FileEndPos - FileStartPos))
			{
				
			while ((bytesRead = inputStream.read(buffer)) != -1) {
		        	
						System.out.println(bytesRead);
		            	outputStream.write(buffer, 0, bytesRead);
		            
		            	
		            	BytesDownloaded += bytesRead;
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
			outputStream.close();
	        inputStream.close();

			//Finished Downloading
			Complete=1;

		}catch(Exception e){}


		}
}

