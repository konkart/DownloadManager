/* 
SOURCE USED SHOULD CARRY AUTHOR CREDITS
ORIGINAL AUTHOR:MAHESH KAREKAR
AUTHOR:KONSTANTINOS KARTOFIS
*/
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
	private boolean isPartial=false;
	boolean failed = false;
	long oldtime;
	long now;
	long downed = 0L;
	public SubDownload(String subDownloadId,String fileLoc,long fileStartPos,long fileEndPos,int bufferSize,int downloadID,String location,boolean partial){

		this.fileLoc=fileLoc;//URL to file
		this.fileStartPos=fileStartPos;//start byte of the "to-download" range
		this.fileEndPos=fileEndPos;//end byte of the range
		Buffer = new byte[bufferSize];	
		this.location=location;
		this.subDownloadId=subDownloadId;//the temp file name
		this.downloadID=downloadID;
		complete=false;
		this.isPartial=partial;
		}

	public void run(){
	
		try{
			
			paused=false;
			URL url = new URL(fileLoc);
			URLConnection uc = url.openConnection();
			uc.setRequestProperty("connection","Keep-Alive");
			
			//partial file save location
			File f = new File(location+subDownloadId);

			
			/*
			 * if a non-partial download has been previously stopped, it will instead start over when it is to be resumed
			 * 
			 * @author KONSTANTINOS KARTOFIS
			 */
			if(isPartial==true) {
			//if file exists already it reads its size in bytes and adds it to the initial number-byte to start download from
				if (f.exists()) {
					bytesDownloaded = f.length();
					fileStartPos=fileStartPos+f.length();
					
					//check to avoid error:416 on requesting property
					if(fileStartPos>=fileEndPos) {
						fileStartPos--;
					}
					uc.setRequestProperty("Range","bytes=" +(fileStartPos) + "-"+ fileEndPos);
					outputStream = new FileOutputStream(f,true);
				} else {
					uc.setRequestProperty("Range","bytes=" +(fileStartPos) + "-"+ fileEndPos);
					outputStream = new FileOutputStream(f);
				}
			}

			
			
			//gets the bytes stream
			InputStream inputStream =  uc.getInputStream();
			byte[] buffer = Buffer;
			if(fileStartPos<=fileEndPos) {
			
				while(bytesDownloadedSession < (fileEndPos - fileStartPos) && paused==false){
					oldtime =System.currentTimeMillis();
					while (paused==false && (bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
						bytesDownloaded += bytesRead;
						bytesDownloadedSession = bytesDownloaded;
						speedLimitCheck();
					} 
				}
			}
			outputStream.close();
			inputStream.close();
			if(paused==false){
				complete=true;
			}


		} catch (Exception e){
			paused=true;
			complete=false;
			failed=true;
			e.printStackTrace();
			try {
				outputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		}
	
	/*
	 * speedLimitCheck(),getPause(),setIsNotPartial(),
	 * setPause(),RateLimit()
	 * 
	 * @author KONSTANTINOS KARTOFIS
	 */
	private void speedLimitCheck() throws InterruptedException {
		if (r!=0 && downed>r) {
			if ( ((now = System.currentTimeMillis()) - oldtime) <=1000L) {
				Thread.sleep(1000L-(now-oldtime));
			}
			oldtime=System.currentTimeMillis();
			downed=0;
		}
		if(bytesRead>=0) {
			downed=(long) (downed+bytesRead);
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
