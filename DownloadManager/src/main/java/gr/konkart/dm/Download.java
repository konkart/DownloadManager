package gr.konkart.dm;

import java.io.IOException;
import java.net.*;
import java.text.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Download implements Runnable{
	private String fileLoc;     		//File URL 
	public String FilePath;
	private String location;		//location on disk
	private long fileSize;	 	    //File Size
	private long bytesDownloaded;	//Bytes Downloaded
	private int totConnections;		//Total Connections
	private int bufferSize = 2024;			//Buffer Size
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
	
	ExecutorService pool = Executors.newCachedThreadPool();
	public Download(int downloadID,String fileLoc,int totConnections,String location){
		this.fileLoc = fileLoc;
		this.totConnections = totConnections;
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
			
			if (current_speed>1000) {
				current_speed=current_speed/1000;
				return " "+String.format("%.3f",current_speed)+" MB/s ";
			}
			else {
				return " "+String.format("%.0f",current_speed)+" KB/s ";
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
					partname = nameofFile + String.valueOf(downloadID) + String.valueOf(conn) + ".dat";
					//Subdownload creation
					sd[conn] = new SubDownload(partname,fileLoc,fStartPos,fEndPos,bufferSize/5,downloadID,location);
					files[conn] = sd[conn].getSubDownloadId();
					startTime=System.currentTimeMillis();
					pool.execute(sd[conn]);
							
					activeSubConn = activeSubConn + 1;
				}

				
			}
			else {
				//Single part download initialization
				totConnections = 1;
				sd = new SubDownload[totConnections];
				files =  new String[totConnections];
				conn=0;
				partsize= fileSize;
				fStartPos = 0;
				fEndPos= fileSize;
				partname = nameofFile + String.valueOf(downloadID) + String.valueOf(conn) + ".dat";
				sd[0] = new SubDownload(partname,fileLoc,fStartPos,fEndPos,bufferSize,downloadID,location);
				startTime=System.currentTimeMillis();
				sd[0].setIsNotPartial();
				pool.execute(sd[0]);
				files[conn] = sd[conn].getSubDownloadId();
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
		
		//downloaded bytes calculation
		public void calcBytesDownloaded(){
			bytesDownloaded=0;	
				for (int conn=0;conn<totConnections;conn++){
					bytesDownloaded=bytesDownloaded + sd[conn].getBytesDownloaded();
				}
		}
		//rate limits all Sub Download
		public void setRateLimit(double rateper) {
			double r = rateper/totConnections;
			long time = System.currentTimeMillis();
			while((System.currentTimeMillis()-time)<600) {
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
				   futils.concat(files,FilePath,location);
				   complete = 1;
			   } catch(Exception e){}
			   deleteSubFiles();
		}
		
		//deletes the uneeded files
		public void deleteSubFiles() {
			if (pool.isTerminated()) {
				for(int fileid=0;fileid < totConnections;fileid++) {
					try {
						futils.delete(files[fileid],location);
						System.out.println("ok");
					} catch (IOException e) {
						e.printStackTrace();
					}
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
		
		// method to check if download is complete
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
			}
			else {
				complete = 0;
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