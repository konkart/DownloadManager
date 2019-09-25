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
	private long fileStartPos = 0;
	private long fileEndPos = 1;
	private long bytesDownloaded=0;
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
	private URLConnection uc;
	
	/*
	 * Partial subDownload constructor
	 */
	public SubDownload(String subDownloadId,String fileLoc,long fileStartPos,long fileEndPos,int bufferSize,int downloadID,String location){

		this.fileLoc=fileLoc;//URL to file
		this.fileStartPos=fileStartPos;//start byte of the "to-download" range
		this.fileEndPos=fileEndPos;//end byte of the range
		Buffer = new byte[bufferSize];	
		this.location=location;
		this.subDownloadId=subDownloadId;//the temp file name
		this.downloadID=downloadID;
		complete=false;
		this.isPartial=true;
	}
	
	// Single part subDownload constructor
	public SubDownload(String subDownloadId,String fileLoc,int bufferSize,int downloadID,String location){
		this.fileLoc=fileLoc;//URL to file
		Buffer = new byte[bufferSize];	
		this.location=location;
		this.subDownloadId=subDownloadId;//the temp file name
		this.downloadID=downloadID;
		complete=false;
		this.isPartial=false;
	}

	public void run(){
	
		try {
			
			paused=false;
			URL url = new URL(fileLoc);
			uc = url.openConnection();
			uc.setRequestProperty("connection","Keep-Alive");
			uc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
			//partial file save location
			File f = new File(location+subDownloadId);

			checkAndResume(f);
			
			//gets the bytes stream
			InputStream inputStream = uc.getInputStream();
			byte[] buffer = Buffer;
			
			while ((bytesRead = inputStream.read(buffer))!=-1 && paused==false){
				outputStream.write(buffer, 0, bytesRead);
				bytesDownloaded += bytesRead;
				speedLimitCheck();
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
	 * setPause(),RateLimit(),checkAndResume()
	 * 
	 * @author KONSTANTINOS KARTOFIS
	 */
	
	/*
	 * if a non-partial download has been previously stopped, it will instead start over when it is to be resumed
	 * else it will restart the download of a non partial download
	 */
	private void checkAndResume(File file) throws FileNotFoundException {
		if(isPartial==true) {
			//if file exists already it reads its size in bytes and adds it to the initial number-byte to start download from
			if (file.exists()) {
				bytesDownloaded = file.length();
				fileStartPos=fileStartPos+file.length();
					
				//check to avoid error:416 on requesting property
				if(fileStartPos>=fileEndPos) {
					fileStartPos--;
				}
				outputStream = new FileOutputStream(file,true);
			} else {
				outputStream = new FileOutputStream(file);
			}
			uc.setRequestProperty("Range","bytes=" +(fileStartPos) + "-"+ fileEndPos);
		} else {
			outputStream = new FileOutputStream(file);
		}
	}
	/*
	 * checks if the limit has been exceeded and sleeps the download
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
