package gr.konkart.dm;

import java.io.IOException;
import java.net.*;
import java.text.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadFile implements Runnable{
	private String fileLoc;     		//File URL 
	public String FilePath;
	private String location;		//location on disk
	private long fileSize;	 	    //File Size
	private long bytesDownloaded;	//Bytes Downloaded
	private int totConnections;		//Total Connections
	private int bufferSize;			//Buffer Size
	private SubDownload sd[];		//Subdownloads array
	private int downloadID;
	private int complete;			//Completion flag
	private int activeSubConn;		//Active SubConnections(Subdownloads)counter
	private long startTime;
	private boolean isPartial;		//Flag to check if webhost supports partial(multipart) download
	int r=0;
	
	volatile double R=0;
	public URL url;
	ExecutorService pool = Executors.newCachedThreadPool();
	public DownloadFile(int downloadID,String fileLoc,int totConnections,int bufferSize,String location){
		this.fileLoc = fileLoc;
		this.totConnections = totConnections;
		this.bufferSize = bufferSize;
		this.downloadID = downloadID;
		this.location = location;
		activeSubConn=0;
			try{
			url = new URL(this.fileLoc);
			URLConnection uc = url.openConnection();
			fileSize = uc.getContentLength();
			FilePath = URLHandler.getFilename(url.toString());
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
			}
			else {
			current_speed = 0;
			}
			
			NumberFormat formatter = NumberFormat.getNumberInstance();
			formatter.setMaximumFractionDigits(2);
			if (current_speed>1000) {
				current_speed=current_speed/1000;
				return " "+formatter.format(current_speed)+" MB/s ";
			}
			else {
				return " "+formatter.format(current_speed)+" KB/s ";
			}
			
		}

		
		public void StartDownload() throws IOException{ 
			int conn=0;//liveConnections
			long fStartPos,fEndPos,partsize;
			String partname;
			complete=0;
			String nameofFile = URLHandler.getFilename(fileLoc);
			//Download initialization
			if (isPartial==true) {
				//Multipart Download initialization
				sd = new SubDownload[totConnections];
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
					partname = nameofFile + String.valueOf(downloadID) + String.valueOf(conn) + ".dat";
					//Subdownload creation
					sd[conn] = new SubDownload(partname,fileLoc,fStartPos,fEndPos,bufferSize/5,downloadID,location);
					startTime=System.currentTimeMillis();
					pool.execute(sd[conn]);
							
					activeSubConn = activeSubConn + 1;
				}

				
			}
			else {
				//Single part download initialization
				totConnections = 1;
				sd = new SubDownload[totConnections];
					
				conn=0;
				partsize= fileSize;
				fStartPos = 0;
				fEndPos= fileSize;
				partname = nameofFile + String.valueOf(downloadID) + String.valueOf(conn) + ".dat";
				sd[0] = new SubDownload(partname,fileLoc,fStartPos,fEndPos,bufferSize,downloadID,location);
				startTime=System.currentTimeMillis();
				sd[0].setIsNotPartial();
				pool.execute(sd[0]);
					
				activeSubConn = activeSubConn + 1;
				}
			pool.shutdown();
			
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
		
		//get Sub download ID
		public String getSubDownId(int id){
			return sd[id].getSubDownloadId();
		}
		
		//downloaded bytes calculation
		public void calcBytesDownloaded(){
			bytesDownloaded=0;	
				for (int conn=0;conn<totConnections;conn++){
					bytesDownloaded=bytesDownloaded + sd[conn].getBytesDownloaded();
				}
		}
		//rate limits all Sub Download
		public void setRateLimit(double rateper) {
			R = rateper;
			boolean done = false;
			double r = rateper/totConnections;
			while(done==false) {
				try {
					for (int conn=0;conn<totConnections;conn++){
						sd[conn].RateLimit(r);
					}
					done=true;
				}catch(Exception e) {}
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
			for (int i=0;i<sd.length;i++) {
				if(sd[i].getPause()==false && !pool.isTerminated()) {
					p = false;
					
					break;
				}
			}
			return p;	
		}
		
		// method to check if download is complete
		public boolean isDownloadComplete() {
			boolean downloadcomplete = true;
			for(int subDown = 0; subDown < activeSubConn ; subDown ++){
				if(isSubDownComplete(subDown) == false){
					downloadcomplete = false; //Download Incomplete
					break;
				}
			}
			return downloadcomplete;
		}
		
		// method to check if download is failed
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
		
		public String getLocation() {
			return location;
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