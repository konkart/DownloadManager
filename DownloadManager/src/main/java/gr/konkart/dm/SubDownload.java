package gr.konkart.dm;

import java.net.*;
import java.io.*;


public class SubDownload implements Runnable{
	double elapsedtime=0;
    long totalDownload = 0;
    int bytesRead = -1;
    public long StartTime;
	public  String SubDownloadId;
	public  String FileLoc;
	public  long FileStartPos;
	public  long FileEndPos;
	public  long BytesDownloaded;
	public 	long BytesDownloadedP;
	public  byte Buffer[];
	public  byte NewBufferminus[];
	public  byte NewBufferplus[];
	public  int Complete=0;
	int DownloadID;
	public volatile float r = 0;
	boolean Aborted = false;
	int inc = 50;
	private volatile boolean Paused = false;
	FileOutputStream outputStream = null;
	private boolean isNotPartial=false;
	float Counter=0;
	String home = System.getProperty("user.home");
public SubDownload(String aSubDownloadId,String aFileLoc,long aFileStartPos,long aFileEndPos,int aBufferSize,int aDownloadID){

		FileLoc=aFileLoc;//URL to file
		FileStartPos=aFileStartPos;//start byte of the "to-download" range
		FileEndPos=aFileEndPos;//end byte of the range
		Buffer = new byte[1024*aBufferSize];	
		BytesDownloaded=0;
		BytesDownloadedP=0;
		SubDownloadId=aSubDownloadId;//the temp file name
		DownloadID=aDownloadID;
		Complete=0;

		}

public int SubDownloadStart(){ return 1;}

public void run(){

		try{
			
			Paused=false;
			URL url = new URL(FileLoc);
			URLConnection uc = url.openConnection();
			uc.setRequestProperty("connection","Keep-Alive");

			//partial file save location
			File f = new File(home+"\\Downloads\\"+SubDownloadId);
			//if file exists already it reads its size in bytes and adds it to the initial number-byte to start download from
			if (f.exists()) {
				/*if a non-partial download has been previously stopped, it will instead start over when it is to be resumed*/
				if(isNotPartial==false) {
					BytesDownloadedP = f.length();
					FileStartPos=FileStartPos+f.length();
					
					//check to avoid error:416 on requesting property
					if(FileStartPos>=FileEndPos) {
						FileStartPos--;
					}
					uc.setRequestProperty("Range","bytes=" +(FileStartPos) + "-"+ FileEndPos);
					outputStream = new FileOutputStream(f,true);
				}
				else {
					
					uc.setRequestProperty("Range","bytes=" +(FileStartPos) + "-"+ FileEndPos);
					outputStream = new FileOutputStream(f);
					
				}
				Paused=false;
				}
			
			//else if file not exists the start and end of the bytes to be downloaded are not changed
			else {
				uc.setRequestProperty("Range","bytes=" + FileStartPos + "-"+ FileEndPos);
				outputStream = new FileOutputStream(f);
				}
			System.out.println(FileStartPos+"  :  "+FileEndPos);
			//gets the bytes stream
			InputStream inputStream =  uc.getInputStream();
			byte[] buffer = Buffer;
			if(FileStartPos<=FileEndPos) {
			while(BytesDownloaded < (FileEndPos - FileStartPos) && Paused==false)
			{
				
				
					
				while (Paused==false && (bytesRead = inputStream.read(buffer)) != -1) {
						
						Counter+=bytesRead;
		            	outputStream.write(buffer, 0, bytesRead);
		            
		            	
		            	BytesDownloaded += bytesRead;
		            	BytesDownloadedP += bytesRead;
		            	
		            	//speed rate check and limit
		            	if (r!=0 && Counter>(r*1000)) {
		            	inc = (int) (r*10/(r/100));
		            	Counter=0;
		            	Thread.sleep(inc);
		            	}

		        	
					} 
				
				
			}
			}
			outputStream.close();
	        inputStream.close();
	        System.out.println("IsCLosed");
	        if(Paused==false){
	        Complete=1;
	        
			}


		}catch(Exception e){
			System.out.println(e);
		}
			

		}

	public synchronized void setPause() {
		Paused = true;
		
	}
	public synchronized boolean getPause() {
		return Paused;
	}
	public void RateLimit(float x) {
		this.r = x;
		
	}
	public void setIsNotPartial() {
		isNotPartial=true;
	}
}
