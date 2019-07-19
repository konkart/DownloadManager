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
	int Complete; //Completion flag
	int DownloadId; //Download ID
	public Torrent(String aFileLoc,int DownloadID){//Torrent constructor
		FileLoc = aFileLoc;
		DownloadId=DownloadID;
		Complete=0;

		}
	volatile boolean Paused=false; //Paused Flag
	long downloaded;//Downloaded Bytes
	long downloadedS;//Variable used in download speed(gets 'downloaded's value)
	int total;//total torrent pieces
	int piece;//completed pieces
	int perce;//percentage
	@Override
	public void run() {
			String home = System.getProperty("user.home");
			System.setProperty("java.net.preferIPv4Stack" , "true");
			
		Config config = new Config() {//torrent client config
		    @Override
		    public int getNumOfHashingThreads() {//number of threads to use
		        return Runtime.getRuntime().availableProcessors() * 4;
		    }
		};
		Duration dur = Duration.ofSeconds(60);
		config.setPeerConnectionTimeout(dur);
		config.setPeerConnectionRetryCount(1);//peer connection retry count
		config.setMaxPieceReceivingTime(Duration.ofSeconds(240)); 
		config.setEncryptionPolicy(EncryptionPolicy.REQUIRE_ENCRYPTED);
		config.setTimeoutedAssignmentPeerBanDuration(Duration.ofMinutes(15));//Peer ban duration after failing to connect
		config.setMaxPeerConnections(100);//max active peer connections
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
		client.startAsync(state -> {//start client with callback every 1000ms
			total = state.getPiecesTotal();
			piece = state.getPiecesComplete();
			perce = (piece*100)/(total);
		    if (state.getPiecesRemaining() == 0) {//if no pieces remaining stop client and flag as completed
		        client.stop();
		        Complete = 1;
		    }
		    else if(Paused==true) {
		    	client.stop();
		    }
		    downloaded=state.getDownloaded();//get downloaded bytes
		}, 1000).join();
		}
	public long getDownloaded() {//get downloaded data in Megabytes
		return (downloaded/1024)/1024;
	}
	public String getDownloadSpeed() {//download speed calculation
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

	public int getSize() {//gets the torrent's data size
		return total;
	}
	public int getPerc() {//get percentage completed
		int p=1;
		if (perce!=0) {
		
		return perce;
		}
		else {
			perce = p;
			return perce;
		}
		
	}
	public void setTorPaused() {//funtion that is called to pause/stop the torrent
		Paused = true;
	}
	
}


