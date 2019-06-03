package gr.konkart.dm;

import java.io.File;
import java.security.Security;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTModule;
import bt.peerexchange.PeerExchangeModule;
import bt.protocol.crypto.EncryptionPolicy;

import java.nio.file.*;
import bt.dht.*;
import bt.runtime.BtClient;
import bt.runtime.Config;
import bt.torrent.selector.PieceSelector;
import bt.torrent.selector.SequentialSelector;
import bt.tracker.http.HttpTrackerModule;




public class Torrent implements Runnable{
	String FileLoc;
	long StartTime;
	int Complete;
	int DownloadId;
	public Torrent(String aFileLoc,int DownloadID){
		FileLoc = aFileLoc;
		DownloadId=DownloadID;
		Complete=0;

		}
	volatile boolean Paused=false;
	int once = 0;
	long downloaded;
	long downloadedS;
	int total;
	int piece;
	int perce;
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
		Duration dur = Duration.ofSeconds(60);
		config.setPeerConnectionTimeout(dur);
		config.setPeerConnectionRetryCount(1);
		config.setMaxPieceReceivingTime(Duration.ofSeconds(240)); 
		config.setEncryptionPolicy(EncryptionPolicy.REQUIRE_ENCRYPTED);
		config.setTimeoutedAssignmentPeerBanDuration(Duration.ofMinutes(15));
		config.setMaxPeerConnections(100);
		config.setNumberOfPeersToRequestFromTracker(30);
		DHTModule dhtModule = new DHTModule(new DHTConfig() {
		    @Override
		    public boolean shouldUseRouterBootstrap() {
		        return true;
		    }
		});

		// get download directory
		Path targetDirectory = new File(home+"\\Downloads\\").toPath();

		// create file system based backend for torrent data
		Storage storage = new FileSystemStorage(targetDirectory);
		PieceSelector selector= SequentialSelector.sequential();
		// create client with a private runtime
		BtClient client = Bt.client()
		        .config(config)
		        .storage(storage)
		        .magnet(FileLoc)
		        .autoLoadModules()
		        .stopWhenDownloaded()
		        .module(dhtModule)
		        .selector(selector)
		        .build();
		StartTime = System.currentTimeMillis();
		client.startAsync(state -> {
			total = state.getPiecesTotal();
			piece = state.getPiecesComplete();
			perce = (piece*100)/(total);
		    if (state.getPiecesRemaining() == 0) {
		        client.stop();
		        Complete = 1;
		    }
		    else if(Paused==true) {
		    	client.stop();
		    }
		    downloaded=state.getDownloaded();
		}, 1000).join();
		}
	public long getDownloaded() {
		return (downloaded/1024)/1024;
	}
	public String getDownloadSpeed() {
		float current_speed;
		downloadedS=downloaded;
		if (downloadedS > 0 ) {
		current_speed = (float)( downloadedS / (System.currentTimeMillis() - StartTime));
		downloadedS=0;
		}
		else {
		current_speed = 0;
		}
		NumberFormat formatter = NumberFormat.getNumberInstance() ;
		formatter.setMaximumFractionDigits(2);

		return " " + formatter.format(current_speed) + " KB/s ";
	}

	public int getSize() {
		return total;
	}
	public int getPerc() {
		int p=1;
		if (perce!=0) {
		
		return perce;
		}
		else {
			perce = p;
			return perce;
		}
		
	}
	public void setTorPaused() {
		Paused = true;
	}
	public void setTorResume() {
		Paused = false;
	}
	
	
}


