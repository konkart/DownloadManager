import java.io.File;
import java.security.Security;
import java.time.Duration;
import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTModule;
import java.nio.file.*;
import bt.dht.*;
import bt.runtime.BtClient;
import bt.runtime.Config;




public class Torrent implements Runnable{
	String FileLoc;
	int Complete;
	int DownloadId;
	public Torrent(String aFileLoc,int DownloadID){
		FileLoc = aFileLoc;
		DownloadId=DownloadID;
		Complete=0;

		}
	

	@Override
	public void run() {
			String home = System.getProperty("user.home");
			Security.setProperty("policy", "unlimited");
			System.setProperty("java.net.preferIPv4Stack" , "true");
			
		Config config = new Config() {
		    @Override
		    public int getNumOfHashingThreads() {
		        return Runtime.getRuntime().availableProcessors() * 4;
		    }
		};
		Duration dur = Duration.ofSeconds(1000);
		config.setPeerConnectionTimeout(dur);
		config.setPeerConnectionRetryCount(10);
		
		
		
		
		DHTModule dhtModule = new DHTModule(new DHTConfig() {
		    @Override
		    public boolean shouldUseRouterBootstrap() {
		        return true;
		    }
		});

		// get download directory
		Path targetDirectory = new File(home+"/Downloads/").toPath();

		// create file system based backend for torrent data
		Storage storage = new FileSystemStorage(targetDirectory);

		// create client with a private runtime
		BtClient client = Bt.client()
		        .config(config)
		        .storage(storage)
		        .magnet(FileLoc)
		        .autoLoadModules()
		        .stopWhenDownloaded()
		        .build();
		
		client.startAsync();
		System.out.println("started");
		}
		
	
}


