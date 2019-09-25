package gr.konkart.dm;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHandler {
	String database = "jdbc:sqlite:Downloads.db";	// the location the database file will be created at
	private int newDownID,newTorID;					// the id number of the download entry
	private int counterDow,counterTor;				// the ammout of downloads

	// creates the database file if it doesnt exist
	public void createDB() {
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			if (conn != null) {
				String sql = "CREATE TABLE IF NOT EXISTS direct (id int NOT NULL,name text NOT NULL,URL text NOT NULL,savedLocation text NOT NULL,date text);";
				stmt.execute(sql);
				sql = "CREATE TABLE IF NOT EXISTS torrent (id int NOT NULL,name text NOT NULL,URL text NOT NULL,savedLocation text NOT NULL,date text);";
				stmt.execute(sql);
				stmt.close();
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// inserts the direct download information to the "direct" table
	public void insertDownloadDir(ArrayList<Download> d,int id) {
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			if (conn != null) {
				String sql = "INSERT INTO direct (id,name,URL,savedLocation,date) VALUES ("+d.get(id).getDownloadID()+",\""+d.get(id).getNameOfFile()+"\",\""+d.get(id).getFileLoc()+"\",\""+d.get(id).getLocation()+"\",\""+getDateTime()+"\");";
				stmt.execute(sql);
				stmt.close();
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// updates the location of the direct download file save location
	public void updateLocDir(String oldLoc, String newLoc, int id) {
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			if (conn != null) {
				String sql = "UPDATE direct SET savedLocation == \""+newLoc+"\\"+"\" WHERE savedLocation == \""+oldLoc+"\" AND id == "+id+";";
				stmt.execute(sql);
				stmt.close();
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/* imports the direct downloads from the database,creates the objects and returns an array with the items 
	 * that will fill the jtable
	 */
	public ArrayList<String[]> importDownloadDir(DownloadManager window) {
		ArrayList<String[]> t = new ArrayList<String[]>();
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			if (conn != null) {
				String sql = "SELECT * FROM direct";
				ResultSet results = stmt.executeQuery(sql);
				while (results.next()) {
					String[] item;
					int id = Integer.parseInt(results.getString(1));
					String f = results.getString(4)+"\\"+results.getString(2);
					File file;
					if ( (file = new File(f)).exists()) {
						long size = file.length();
						String[] item1 = {results.getString(1).toString(),results.getString(2),"100%  0 KB/s",getFileSize(size),results.getString(5),results.getString(3)};
						item = item1;
					} else if (new File(f+"single.dat").exists() || new File(f+"1.dat").exists()) {
						String[] item1 = {results.getString(1).toString(),results.getString(2),"0%  0 KB/s","Paused",results.getString(5),results.getString(3)};
						item = item1;
					} else {
						String[] item1 = {results.getString(1).toString(),results.getString(2),"Not Found","Not Found",results.getString(5),results.getString(3)};
						item = item1;
					}
					if (id>newDownID) {
						newDownID = id;
					} else if (id == newDownID) {
						newDownID = id+1;
					}
					
					counterDow = counterDow + 1;
					Download download = new Download(id, results.getString(3), results.getString(2), results.getString(4), -1);
					window.addDown(download);
					
					t.add(item);
					
				}
				stmt.close();
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return t;
	}
	
	// updates the location the torrent file has been saved on local drive
	public void updateLocTor(String oldLoc, String newLoc, int id) {
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			String newLo = newLoc;
			String old = oldLoc;
			if (conn != null) {
				String sql = "UPDATE torrent SET savedLocation == \""+newLo+"\\"+"\" WHERE savedLocation == \""+old+"\" AND id == "+id+";";
				
				stmt.execute(sql);
				stmt.close();
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// inserts the torrent download information to the "torrent" table
	public void insertDownloadTor(ArrayList<Torrent> t,int id) {
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			if (conn != null) {
				String sql = "INSERT INTO torrent (id,name,URL,savedLocation,date) VALUES ("+t.get(id).getDownloadID()+",\""+t.get(id).getFolderName()+"\",\""+t.get(id).getMagnetURI()+"\",\""+t.get(id).getLocation()+"\",\""+getDateTime()+"\");";
				stmt.execute(sql);
				stmt.close();
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// gets the torrent downloads, creates the objects and returns an array of the items that will go to the Jtable
	public ArrayList<String[]> importDownloadTor(DownloadManager window) {
		ArrayList<String[]> t = new ArrayList<String[]>();
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			if (conn != null) {
				String sql = "SELECT * FROM torrent";
				ResultSet results = stmt.executeQuery(sql);
				while (results.next()) {
					String[] item;
					String f = results.getString(4)+"\\"+results.getString(2);
					if (new File(f).exists()) {
						String[] item1 = {results.getString(1).toString(),results.getString(2),"00%  0 KB/s","Stopped",results.getString(5),results.getString(3)};
						item = item1;
					} else {
						String[] item1 = {results.getString(1).toString(),results.getString(2),"0%  0 KB/s","0",results.getString(5),results.getString(3)};
						item = item1;
					}
					if (Integer.parseInt(results.getString(1))>newTorID) {
						newTorID = Integer.parseInt(results.getString(1));
					} else if (Integer.parseInt(results.getString(1)) == newTorID) {
						newTorID = Integer.parseInt(results.getString(1))+1;
					}
					counterTor = counterTor + 1;
					Torrent tr = new Torrent(Integer.parseInt(results.getString(1)), results.getString(3), results.getString(2), results.getString(4), -1);
					window.addTor(tr,Integer.parseInt(results.getString(1)));
					t.add(item);
					
				}
				stmt.close();
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return t;
	}
	
	// deletes torrent download entry
	public void deleteTor(int id) {
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			if (conn != null) {
				String sql = "DELETE FROM torrent WHERE id=="+id+"";
				stmt.execute(sql);
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// deletes direct download entry
	public void deleteDir(int id) {
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			if (conn != null) {
				String sql = "DELETE FROM direct WHERE id=="+id+"";
				stmt.execute(sql);
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String getFileSize(long size) {
		float sizeTmp = size;
		String sizeR = null;
		if (sizeTmp>=1073741824) {
			sizeR = String.format("%.1fGB",sizeTmp/1073741824);
		} else if (sizeTmp>=1048576) {
			sizeR = String.format("%.1fMB",sizeTmp/1048576);
		} else if (sizeTmp>1024){
			sizeR = ( (long) sizeTmp/1024 ) + "KB";
		} else {
			sizeR = ( (long) sizeTmp ) + "B";
		}
		return sizeR;
	}
	private String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("hh:mm dd-MM-yy");
		Date date = new Date();
		return dateFormat.format(date);
	}
	public int getDownID() {
		return newDownID;
	}
	public int getDownCount() {
		return counterDow;
	}
	public int getTorID() {
		return newTorID;
	}
	public int getTorCount() {
		return counterTor;
	}
}
