import java.io.IOException;
import java.net.*;
import java.text.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadFile implements Runnable{
	public String FileLoc;     		//File URL 
	public String FilePath;
	public long FileSize;	 	    //File Size
	public long BytesDownloaded;   //Bytes Downloaded
	public int TotConnections;       //Total Connections
	public int BufferSize;			//Buffer Size
	public SubDownload sd[];
	public int DownloadID;
	public int Complete;
	public int ActiveSubConn;
	public long StartTime;
	public boolean IsPartial;
	public volatile boolean Paused = false;
	int r=0;
	volatile float R=0;
	public URL url;
	ExecutorService pool = Executors.newCachedThreadPool();
	public DownloadFile(int aDownloadID,String aFileLoc,int aTotConnections,int aBufferSize){
		FileLoc = aFileLoc;
		TotConnections = aTotConnections;
		BufferSize = aBufferSize;
		DownloadID= aDownloadID;
		ActiveSubConn=0;
			try{
			url = new URL(aFileLoc);
			URLConnection uc = url.openConnection();
			FileSize = uc.getContentLength();
			FilePath = url.getPath();
			IsPartial = uc.getHeaderField("Accept-Ranges").equals("bytes");
			
			//System.out.println("Filesize: " + FileSize);
			}catch(Exception e){}
		};
public String getBytesDownloaded(){
		return String.valueOf(BytesDownloaded);}
		
public String getFileSize(){
		return String.valueOf(FileSize);}
		
public String numberofConnections(){
		return String.valueOf(ActiveSubConn);}
		
public String getDownloadSpeed(){ 

			float current_speed;

			if (BytesDownloaded > 0 ) {
			current_speed = (float)( BytesDownloaded / (System.currentTimeMillis() - StartTime));
			}
			else {
			current_speed = 0;
			}
			NumberFormat formatter = NumberFormat.getNumberInstance() ;
			formatter.setMaximumFractionDigits(2);

			return " " + formatter.format(current_speed) + " KB/s ";
			
		}

		
public int StartDownload() throws IOException{ 
			int li_conn=0;//liveConnections
			long ld_FStartPos,ld_FEndPos,ld_partsize;
			String partname;
			Complete=0;
			
			//Multipart Download
			if (IsPartial==true) {
			sd = new SubDownload[TotConnections];
			ld_partsize= (long)(FileSize/TotConnections);


			for (li_conn=0;li_conn < TotConnections ;li_conn++){

						if ( li_conn == (TotConnections - 1))	{
							ld_FStartPos=li_conn*ld_partsize;
							ld_FEndPos= FileSize;
						}
						else	{
							ld_FStartPos=li_conn*ld_partsize;
							ld_FEndPos= ld_FStartPos + ld_partsize - 1;
						}
	
						partname = "DFL" +  String.valueOf(DownloadID) + String.valueOf(li_conn) + ".dat";
						sd[li_conn] = new SubDownload(partname,FileLoc,ld_FStartPos,ld_FEndPos,BufferSize,DownloadID);
						StartTime=System.currentTimeMillis();
						
						pool.execute(sd[li_conn]);
						//sd[li_conn].start();
						
						ActiveSubConn = ActiveSubConn + 1;
					}

				
				}
				else {
					//SingleDownload
					TotConnections = 1;
					sd = new SubDownload[TotConnections];
					
					li_conn=1;
					ld_partsize= FileSize;
					ld_FStartPos = 0;
					ld_FEndPos= FileSize;
					String[] nameof = FileLoc.split("/");
					String nameofFile = nameof[nameof.length-1]+"dat";
					sd[0] = new SubDownload(nameofFile,FileLoc,ld_FStartPos,ld_FEndPos,BufferSize,DownloadID);
					StartTime=System.currentTimeMillis();
					pool.execute(sd[0]);
					//sd[0].start();
					ActiveSubConn = ActiveSubConn + 1;
				}
			return li_conn;
			}
			
public int isSubDownComplete(int id){
				return sd[id].Complete;
			}
public int isSimpleDownComplete(int id){
				return sd[id].Complete;
			}
			
public int DownloadProgress(){ 
				int pcount=0;
				calcBytesDownloaded();
				if ( BytesDownloaded > 0 && FileSize > 0 )
				pcount = (int)((( BytesDownloaded * 100 ) / FileSize)) ;
				return pcount;
			}
			
public String getSubDownId(int id)
			{
				return sd[id].SubDownloadId;
			}
			
public void calcBytesDownloaded(){
				BytesDownloaded=0;	
				for (int li_conn=0;li_conn < TotConnections  ;li_conn++){
					BytesDownloaded=BytesDownloaded + sd[li_conn].BytesDownloadedP;
					}
			}
public  synchronized void OneSubIsCompleted() {
	TotConnections =- 1;
	if (TotConnections!=0) {
	setRateLimit(R);
	}
}
public void setRateLimit(float x) {
				R = x;
				float r = x/TotConnections;
				for (int li_conn=0;li_conn < TotConnections  ;li_conn++){
					sd[li_conn].RateLimit(r);
					
				}	
			}

public void PauseDownload() {
	for (int i=0;i<sd.length;i++) {
		sd[i].setPause();
	}
}
public  boolean getPause() {
	boolean p = false;
	for (int i=0;i<sd.length;i++) {
		p = sd[i].getPause();
	}
	return p;
	
}
public void ResumeDownload() {
	for (int i=0;i<sd.length;i++) {
		sd[i].setResume();
	}
}
public  boolean getResume() {
	boolean p = false;
	for (int i=0;i<sd.length;i++) {
		p = sd[i].getResume();
	}
	return p;
	
}
		
public void run(){
				if ( FileSize > 0 )
					try {
						StartDownload();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		}

		
}