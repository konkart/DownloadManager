package gr.konkart.dm;

import java.net.*;
import java.io.*;


public class SubDownload implements Runnable{
	double elapsedtime=0;
    long totalDownload = 0;
    int bytesRead = -1;
    public long startTime;
	private String subDownloadId;
	private String fileLoc;
	private String location;
	private long fileStartPos;
	private long fileEndPos;
	private long bytesDownloaded=0;
	private long bytesDownloadedSession=0;
	private byte Buffer[];
	private boolean complete=false;
	int downloadID;
	private volatile double r = 0;
	private volatile boolean paused = false;
	FileOutputStream outputStream = null;
	private boolean isNotPartial=false;
	boolean failed = false;
public SubDownload(String subDownloadId,String fileLoc,long fileStartPos,long fileEndPos,int bufferSize,int downloadID,String location){

		this.fileLoc=fileLoc;//URL to file
		this.fileStartPos=fileStartPos;//start byte of the "to-download" range
		this.fileEndPos=fileEndPos;//end byte of the range
		Buffer = new byte[bufferSize];	
		this.location=location;
		this.subDownloadId=subDownloadId;//the temp file name
		this.downloadID=downloadID;
		complete=false;
		}

public int SubDownloadStart(){ return 1;}

public void run(){
	
		try{
			
			paused=false;
			URL url = new URL(fileLoc);
			URLConnection uc = url.openConnection();
			uc.setRequestProperty("connection","Keep-Alive");
			
			//partial file save location
			File f = new File(location+subDownloadId);
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
			
			long now;
			long downed = 0L;
			if(fileStartPos<=fileEndPos) {
			
			while(bytesDownloadedSession < (fileEndPos - fileStartPos) && paused==false){
				Long oldtime =System.currentTimeMillis();
				while (paused==false && (bytesRead = inputStream.read(buffer)) != -1) {
		            	outputStream.write(buffer, 0, bytesRead);
		            	bytesDownloaded += bytesRead;
		            	bytesDownloadedSession = bytesDownloaded;
		            	
		            	//speed rate check and limit
		            	if (r!=0 && downed>r && ((now=System.currentTimeMillis())-oldtime)<ltest) {
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
	        if(paused==false){
	        complete=true;
			}


		}catch(Exception e){
			paused=true;
			complete=false;
			failed=true;
			try {
				outputStream.close();
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
	public boolean getCompleted() {
		return complete;
	}
	public boolean getFailed() {
		return failed;
	}
	public String getSubDownloadId() {
		return subDownloadId;
	}
	public long getBytesDownloaded() {
		return bytesDownloaded;
	}
	
}
