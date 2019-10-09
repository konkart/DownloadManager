/* 
SOURCE USED SHOULD CARRY AUTHOR CREDITS
ORIGINAL AUTHOR:MAHESH KAREKAR
AUTHOR:KONSTANTINOS KARTOFIS
*/
package gr.konkart.dm;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Download implements Runnable{
	private String fileLoc;			//File URL
	private String location;		//location on disk
	private long fileSize = 1;		//File Size
	private long bytesDownloaded;	//Bytes Downloaded
	private int totConnections = 5;	//Total Connections
	private int bufferSize = 2024;	//Buffer Size
	private SubDownload sd[];		//Subdownloads array
	private int downloadID;
	private int complete = -2;		//Completion flag
	private int activeSubConn;		//Active SubConnections(Subdownloads)counter
	private long startTime;
	private Boolean isPartial = null;		//Flag to check if webhost supports partial(multipart) download
	int r=0;
	private long bytesDownloadedSession = 0;
	private String[] files;
	private FileUtils futils = new FileUtils();
	public URL url;
	int conn=0;//liveConnections
	long fStartPos,fEndPos,partsize;
	String partname,nameOfFile;
	private int trayRow;
	private URLConnection uc;
	public Download(int downloadID,String fileLoc,String filename,String location,int trayRow) {
		this.fileLoc = fileLoc;
		this.downloadID = downloadID;
		this.location = location;
		this.trayRow = trayRow;
		this.nameOfFile = filename;
		activeSubConn=0;
	}
	
	public String getBytesDownloaded() {
		float downedFloat = bytesDownloaded;
		if (downedFloat>=1073741824) {
			return String.format("%.1fGB",downedFloat/1073741824);
		} else if(downedFloat>=1048576) {
			return String.format("%.1fMB",downedFloat/1048576);
		} else if (downedFloat>=1024) {
			return ( (long) (downedFloat/1024) ) +"KB";
		} else {
			return ( (long) downedFloat )+"B";
		}
	}
	
	public long getFileSize() {
		return fileSize;
	}
	//calculates and returns the download speed
	public String getDownloadSpeed() { 

		float current_speed;

		if (bytesDownloadedSession > 0 ) {
		current_speed = (float)( bytesDownloadedSession / (System.currentTimeMillis() - startTime));
		} else {
		current_speed = 0;
		}
			
		if (current_speed>1000) {
			current_speed=current_speed/1000;
			return " "+String.format("%.1f",current_speed)+" MB/s ";
		} else {
			return " "+String.format("%.0f",current_speed)+" KB/s ";
		}
			
	}

		
	public void StartDownload() throws IOException{ 
		complete = 0;
		activeSubConn = 0;
		pool = Executors.newCachedThreadPool();
		try {
			url = new URL(fileLoc);
			uc = url.openConnection();
			fileSize = uc.getContentLength();
			if(isPartial==null) {
				getPartial();
			}
		} catch (Exception e) {e.printStackTrace();}
		//Download initialization
		if (isPartial==true) {
			doMultipart();	
		} else {
			doSinglePart();
		}
		pool.shutdown();
	}
	//Multipart Download initialization
	private void doMultipart() {

		sd = new SubDownload[totConnections];
			
		files =  new String[totConnections];
			
		partsize= (long)(fileSize/totConnections);

		//Part initialization
		for (conn=0;conn<totConnections;conn++){
				
			if ( conn == (totConnections - 1)) {
				fStartPos=conn*partsize;
				fEndPos= fileSize;
			}
			else {
				fStartPos=conn*partsize;
				fEndPos= fStartPos + partsize - 1;
			}
			partname = nameOfFile + String.valueOf(conn) + ".dat";
			sd[conn] = new SubDownload(partname,fileLoc,fStartPos,fEndPos,bufferSize/5,downloadID,location);
			files[conn] = partname;
			startTime=System.currentTimeMillis();
			pool.execute(sd[conn]);
					
			activeSubConn = activeSubConn + 1;
		}
	}
		
	//check if a subdownload is completed
	public boolean isSubDownComplete(int id){
		return sd[id].getCompleted();
	}
	//checks if subdownload failed
	public boolean isSubDownFailed(int id) {
		return sd[id].getFailed();
	}
	//download progress calc
	public int DownloadProgress() {
		int pcount=0;
		calcBytesDownloaded();
		if ( bytesDownloaded > 0 && fileSize > 0 ) {
			pcount = (int)((( bytesDownloaded * 100 ) / fileSize)) ;
		}
		return pcount;
	}
		
	//downloaded bytes calculation
	public void calcBytesDownloaded() {
		bytesDownloaded=0;
		bytesDownloadedSession = 0;	
			for (int conn=0;conn<totConnections;conn++){
				bytesDownloaded = bytesDownloaded + sd[conn].getBytesDownloaded();
				bytesDownloadedSession = bytesDownloadedSession + sd[conn].getBytesDownloadedSession();
			}
	}
		
		
	/*
	 * setRateLimit(),concatSub(),getTrayRow()
	 * deleteSubFiles(),PauseDownload(),getPause(),
	 * isDownloadComplete(),isDownloadFailed(),
	 * doSinglePart(),getPartial()....
	 * 
	 * @author KONSTANTINOS KARTOFIS
	 */
	ExecutorService pool ;
	//Single part download initialization
	private void doSinglePart() {
		totConnections = 1;
		sd = new SubDownload[totConnections];
		files =  new String[totConnections];
		conn = 0;
		partsize = fileSize;
		fStartPos = 0;
		fEndPos = fileSize;
		partname = nameOfFile + String.valueOf(conn) + "single.dat";
		sd[0] = new SubDownload(partname,fileLoc,bufferSize,downloadID,location);
		startTime=System.currentTimeMillis();
		pool.execute(sd[0]);
		files[conn] = partname;
		activeSubConn = activeSubConn + 1;
		
	}
		
	//sets the rate limit
	public void setRateLimit(double rateper) {
		double r = rateper/totConnections;
		long time = System.currentTimeMillis();
		while ((System.currentTimeMillis()-time)<600) {
			try {
				for (int conn=0;conn<totConnections;conn++){
					sd[conn].RateLimit(r);
				}
				break;
			} catch(Exception e) {
				//e.printStackTrace();
			}
		}
		
	}
	
	public void setPartial(boolean isPartial) {
		this.isPartial = isPartial;
	}
	
	public void concatSub() {
		   try {
			   futils.concat(files,nameOfFile,location);
			   complete = 1;
		   } catch(Exception e){}
		   deleteSubFiles();
	}
		
	//deletes the uneeded files
	public void deleteSubFiles() {
		if (files == null) {
			if(isPartial) {
				files =  new String[totConnections];
				for (conn=0;conn<totConnections;conn++){
					partname = nameOfFile + String.valueOf(conn) + ".dat";
					files[conn] = partname;
				}
			} else {
				files =  new String[1];
				for (conn=0;conn<1;conn++){
					partname = nameOfFile + String.valueOf(conn) + "single.dat";
					files[conn] = partname;
				}
			}
		}
		if (pool==null || pool.isTerminated()) {
			for(int fileid=0;fileid < totConnections;fileid++) {
				futils.deleteFiles(new File(location+files[fileid]));
			}
		}
	}
	//pauses Sub Downloads
	public void PauseDownload() {
		try {
			for (int i=0;i<sd.length;i++) {
				sd[i].setPause();
			}
		} catch (Exception e) {
			
		}
	}
	//checks if SubDownloads are paused
	public boolean getPause() {
		boolean p = true;
		try {
			for (int i=0;i<sd.length;i++) {
				if(sd[i].getPause()==false && !pool.isTerminated()) {
					p = false;
					break;
				}
			}
		}catch(Exception e) {
			p = true;
		}
		return p;	
	}
		
	//  method to check if download is complete
	public boolean isDownloadComplete() {
		boolean downloadcomplete = true;
		
		for(int subDown = 0; subDown < activeSubConn ; subDown ++){
			if(isSubDownComplete(subDown) == false){
				
				downloadcomplete = false; //Download Incomplete
				break;
			}
		}
		if (downloadcomplete == true){
			complete = 1;
		} else {
			complete = 0;
		}
		return downloadcomplete;
	}
		
	//method to check if download is failed
	public boolean isDownloadFailed() {
		boolean failed = false;
		for(int subDown = 0; subDown < activeSubConn ; subDown ++){
			   if(isSubDownFailed(subDown) == true){
				   failed = true;
				   complete = -1;
				   break;
			   }
		}
		return failed;
	}
	
	public void getPartial() {
		isPartial = "bytes".equals(uc.getHeaderField("Accept-Ranges"));
		DatabaseHandler db = new DatabaseHandler();
		db.updateDirPartial(isPartial,downloadID);
	}
		
	public int getComplete() {
		return complete;
	}
	public void setComplete(int x) {
		this.complete = x;
	}
	
	public int getActiveSubConn() {
		return activeSubConn;
	}
	
	public int getTotConnections() {
		return totConnections;
	}
	public int getTrayRow(){
		return trayRow;
	}
	public void setTrayRow(int tRow){
		this.trayRow = tRow;
	}
	public String getFileLoc() {
		return fileLoc;
	}
	public String getLocation() {
		return location;
	}
	public String getNameOfFile() {
		return nameOfFile;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public int getDownloadID() {
		return downloadID;
	}
	public void run(){
		if ( fileSize >= 0 ) {
			try {
				StartDownload();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}