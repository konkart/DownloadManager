package gr.konkart.dm;

import java.net.*;
import java.text.NumberFormat;
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
	float Counter=0;
	String home = System.getProperty("user.home");
public SubDownload(String aSubDownloadId,String aFileLoc,long aFileStartPos,long aFileEndPos,int aBufferSize,int aDownloadID){

		FileLoc=aFileLoc;
		FileStartPos=aFileStartPos;
		FileEndPos=aFileEndPos;
		Buffer = new byte[1024*aBufferSize];	
		BytesDownloaded=0;
		BytesDownloadedP=0;
		SubDownloadId=aSubDownloadId;
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

			//int li_bytesRead;
			File f = new File(home+"\\Downloads\\"+SubDownloadId);
			if (f.exists()) {
				
				BytesDownloadedP = f.length();
				FileStartPos=FileStartPos+f.length();
				uc.setRequestProperty("Range","bytes=" +(FileStartPos) + "-"+ FileEndPos);
				System.out.println("ALREADY");
				outputStream = new FileOutputStream(f,true);
				Paused=false;
				}
			else {
				uc.setRequestProperty("Range","bytes=" + FileStartPos + "-"+ FileEndPos);
				outputStream = new FileOutputStream(f);
				}
			InputStream inputStream =  uc.getInputStream();
			byte[] buffer = Buffer;
			while(BytesDownloaded < (FileEndPos - FileStartPos) && Paused==false)
			{	StartTime = System.currentTimeMillis();
				
				
					
				while (Paused==false && (bytesRead = inputStream.read(buffer)) != -1) {
						
						Counter+=bytesRead;
		            	outputStream.write(buffer, 0, bytesRead);
		            
		            	
		            	BytesDownloaded += bytesRead;
		            	BytesDownloadedP += bytesRead;
		            	if (r!=0 && Counter>(r*1000)) {
		            	inc = (int) (r*10/(r/100));
		            	Counter=0;
		            	Thread.sleep(inc);
		            	}

		        	
					} 
				
				
			}
			outputStream.close();
	        inputStream.close();
	        System.out.println("IsCLosed");
			//Finished Downloading
	        //System.out.println(f.getName() +" "+ f.length()+"size // "+SubDownloadId+" // "+(FileStartPos)+" - "+FileEndPos);
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

	public synchronized void setResume() {
		Paused = false;
		
	}
	public synchronized boolean getResume() {
		return Paused;
	}

	public void RateLimit(float x) {
		this.r = x;
		
	}
}
