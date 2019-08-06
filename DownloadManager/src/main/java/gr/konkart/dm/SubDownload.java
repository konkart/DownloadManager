package gr.konkart.dm;

import java.net.*;
import java.io.*;


public class SubDownload implements Runnable{
	double elapsedtime=0;
    long totalDownload = 0;
    int bytesRead = -1;
    public long startTime;
	public String subDownloadId;
	public String fileLoc;
	public long fileStartPos;
	public long fileEndPos;
	public long bytesDownloaded;
	public byte Buffer[];
	public int complete=0;
	int downloadID;
	public volatile double r = 0;
	private volatile boolean paused = false;
	FileOutputStream outputStream = null;
	private boolean isNotPartial=false;
	String home = System.getProperty("user.home");
public SubDownload(String subDownloadId,String fileLoc,long fileStartPos,long fileEndPos,int bufferSize,int downloadID){

		this.fileLoc=fileLoc;//URL to file
		this.fileStartPos=fileStartPos;//start byte of the "to-download" range
		this.fileEndPos=fileEndPos;//end byte of the range
		Buffer = new byte[bufferSize];	
		bytesDownloaded=0;
		this.subDownloadId=subDownloadId;//the temp file name
		this.downloadID=downloadID;
		complete=0;

		}

public int SubDownloadStart(){ return 1;}

public void run(){
	System.out.println(subDownloadId+" "+fileLoc);
		try{
			
			paused=false;
			URL url = new URL(fileLoc);
			URLConnection uc = url.openConnection();
			uc.setRequestProperty("connection","Keep-Alive");
			
			//partial file save location
			File f = new File(home+"\\Downloads\\"+subDownloadId);
			//if file exists already it reads its size in bytes and adds it to the initial number-byte to start download from
			if (f.exists()) {
				/*if a non-partial download has been previously stopped, it will instead start over when it is to be resumed*/
				if(isNotPartial==false) {
					bytesDownloaded = f.length();
					fileStartPos=fileStartPos+f.length();
					
					//check to avoid error:416 on requesting property
					if(fileStartPos>=fileEndPos) {
						fileStartPos--;
					}
					uc.setRequestProperty("Range","bytes=" +(fileStartPos) + "-"+ fileEndPos);
					outputStream = new FileOutputStream(f,true);
				}
				else {
					
					uc.setRequestProperty("Range","bytes=" +(fileStartPos) + "-"+ fileEndPos);
					outputStream = new FileOutputStream(f);
					
				}
				paused=false;
				}
			
			//else if file not exists the start and end of the bytes to be downloaded are not changed
			else {
				uc.setRequestProperty("Range","bytes=" + fileStartPos + "-"+ fileEndPos);
				outputStream = new FileOutputStream(f);
				}
			//gets the bytes stream
			InputStream inputStream =  uc.getInputStream();
			Long ltest = 1000L;
			byte[] buffer = Buffer;
			Long oldtime =System.currentTimeMillis();
			long now;
			long downed = 0L;
			if(fileStartPos<=fileEndPos) {
			while(bytesDownloaded < (fileEndPos - fileStartPos) && paused==false)
			{
				
				
					
				while (paused==false && (bytesRead = inputStream.read(buffer)) != -1) {
		            	outputStream.write(buffer, 0, bytesRead);
		            	bytesDownloaded += bytesRead;
		            	
		            	//speed rate check and limit
		            	if (r!=0 && downed>(r*1000) && ((now=System.currentTimeMillis())-oldtime)<ltest) {
		            	Thread.sleep(ltest-(now-oldtime));
		            	oldtime=System.currentTimeMillis();
		            	downed=0;
		            	}
		            	if(bytesRead>=0) {
		            		downed=(long) (downed+bytesRead);
		            	}
					} 
				
				
			}
			}
			outputStream.close();
	        inputStream.close();
	        System.out.println("IsCLosed");
	        if(paused==false){
	        complete=1;
	        
			}


		}catch(Exception e){
			try {
				outputStream.close();
				paused=true;
				complete=0;
				System.out.println("IsCLosed");
			} catch (IOException e1) {
				e1.printStackTrace();
				
			}
	        
		}
			

		}

	public synchronized void setPause() {
		paused = true;
		
	}
	public synchronized boolean getPause() {
		return paused;
	}
	public void RateLimit(double r2) {
		this.r = r2;
		
	}
	public void setIsNotPartial() {
		isNotPartial=true;
	}
	
}
