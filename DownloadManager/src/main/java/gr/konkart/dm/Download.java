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
	private long fileSize;			//File Size
	private long bytesDownloaded;	//Bytes Downloaded
	private int totConnections = 5;	//Total Connections
	private int bufferSize = 2024;	//Buffer Size
	private SubDownload sd[];		//Subdownloads array
	private int downloadID;
	private int complete;			//Completion flag
	private int activeSubConn;		//Active SubConnections(Subdownloads)counter
	private long startTime;
	private boolean isPartial;		//Flag to check if webhost supports partial(multipart) download
	int r=0;
	private String[] files;
	private FileUtils futils = new FileUtils();
	public URL url;
	int conn=0;//liveConnections
	long fStartPos,fEndPos,partsize;
	String partname;
	String nameOfFile;
	private int trayRow;
	public Download(int downloadID,String fileLoc,String filename,String location,int trayRow){
		this.fileLoc = fileLoc;
		this.downloadID = downloadID;
		this.location = location;
		this.trayRow = trayRow;
		this.nameOfFile = filename;
		activeSubConn=0;
		complete=0;
			try{
			url = new URL(this.fileLoc);
			URLConnection uc = url.openConnection();
			fileSize = uc.getContentLength();
			isPartial = uc.getHeaderField("Accept-Ranges").equals("bytes");
			}catch(Exception e){}

		};
		public String getBytesDownloaded(){
			return String.valueOf((bytesDownloaded/1024)/1024);
		}
		
		public long getFileSize(){
			return fileSize;
		}
		//calculates and returns the download speed
		public String getDownloadSpeed(){ 

			float current_speed;

			if (bytesDownloaded > 0 ) {
			current_speed = (float)( bytesDownloaded / (System.currentTimeMillis() - startTime));
			} else {
			current_speed = 0;
			}
			
			if (current_speed>1000) {
				current_speed=current_speed/1000;
				return " "+String.format("%.3f",current_speed)+" MB/s ";
			} else {
				return " "+String.format("%.0f",current_speed)+" KB/s ";
			}
			
		}

		
		public void StartDownload() throws IOException{ 
			//Download initialization
			if (isPartial==true) {
				doMultipart();	
			} else {
				doSinglePart();
			}
			pool.shutdown();
		}
		
		private void doMultipart() {
			//Multipart Download initialization
			sd = new SubDownload[totConnections];
			
			files =  new String[totConnections];
			
			//Dividing the file size to get size for each sub part
			partsize= (long)(fileSize/totConnections);

			//Part initialization
			for (conn=0;conn<totConnections;conn++){
				
				//Last part initialization
				if ( conn == (totConnections - 1)) {
					fStartPos=conn*partsize;
					fEndPos= fileSize;
				}
				//Parts initialization
				else {
					fStartPos=conn*partsize;
					fEndPos= fStartPos + partsize - 1;
				}
				//part name and temporary extension
				partname = nameOfFile + String.valueOf(downloadID) + String.valueOf(conn) + ".dat";
				//Subdownload creation
				sd[conn] = new SubDownload(partname,fileLoc,fStartPos,fEndPos,bufferSize/5,downloadID,location,true);
				files[conn] = sd[conn].getSubDownloadId();
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
		public int DownloadProgress(){
			int pcount=0;
			calcBytesDownloaded();
			if ( bytesDownloaded > 0 && fileSize > 0 )
			pcount = (int)((( bytesDownloaded * 100 ) / fileSize)) ;
			return pcount;
		}
		
		//downloaded bytes calculation
		public void calcBytesDownloaded(){
			bytesDownloaded=0;	
				for (int conn=0;conn<totConnections;conn++){
					bytesDownloaded=bytesDownloaded + sd[conn].getBytesDownloaded();
				}
		}
		
		
		/*
		 * setRateLimit(),concatSub(),getTrayRow()
		 * deleteSubFiles(),PauseDownload(),getPause(),
		 * isDownloadComplete(),isDownloadFailed(),
		 * doSinglePart()....
		 * 
		 * @author KONSTANTINOS KARTOFIS
		 */
		ExecutorService pool = Executors.newCachedThreadPool();
		//Single part download initialization
		private void doSinglePart() {
			totConnections = 1;
			sd = new SubDownload[totConnections];
			files =  new String[totConnections];
			conn=0;
			partsize= fileSize;
			fStartPos = 0;
			fEndPos= fileSize;
			partname = nameOfFile + String.valueOf(downloadID) + String.valueOf(conn) + "single.dat";
			sd[0] = new SubDownload(partname,fileLoc,fStartPos,fEndPos,bufferSize,downloadID,location,false);
			startTime=System.currentTimeMillis();
			pool.execute(sd[0]);
			files[conn] = sd[conn].getSubDownloadId();
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
		
		
		public void concatSub() {
			   try {
				   futils.concat(files,nameOfFile,location);
				   complete = 1;
			   } catch(Exception e){}
			   deleteSubFiles();
		}
		
		//deletes the uneeded files
		public void deleteSubFiles() {
			if (pool.isTerminated()) {
				for(int fileid=0;fileid < totConnections;fileid++) {
					futils.deleteFiles(new File(location+files[fileid]));
					System.out.println("ok");
				}
			}
		}
		//pauses Sub Downloads
		public void PauseDownload() {
			for (int i=0;i<sd.length;i++) {
				sd[i].setPause();
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
					   failed = true; //Download Failed
					   break;
				   }
			}
			return failed;
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
		public void run(){
			if ( fileSize > 0 ) {
				try {
					StartDownload();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
}