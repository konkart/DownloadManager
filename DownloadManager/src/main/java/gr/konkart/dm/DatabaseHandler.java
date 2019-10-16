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

	// creates the database file with the tables if it doesnt exist
	public void createDB() {
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			if (conn != null) {
				String sql = "CREATE TABLE IF NOT EXISTS direct (id int NOT NULL,name text NOT NULL,URL text NOT NULL,savedLocation text NOT NULL,date text,partial int DEFAULT -1);";
				stmt.execute(sql);
				sql = "CREATE TABLE IF NOT EXISTS torrent (id int NOT NULL,name text NOT NULL,URL text NOT NULL,savedLocation text NOT NULL,date text);";
				stmt.execute(sql);
				sql = "CREATE TABLE IF NOT EXISTS defaultFolder (folder TEXT);";
				stmt.execute(sql);
				sql = "INSERT INTO defaultFolder (folder) VALUES (\""+System.getProperty("user.home")+"\\Downloads\\"+"\");";
				stmt.execute(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * saves the new Default folder that user chose
	 */
	public void setDefaultFolder(String folder,String oldFolder) {
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			if (conn != null) {
				String sql = "UPDATE defaultFolder SET folder == \""+folder+"\" WHERE folder == \""+oldFolder+"\";";
				stmt.execute(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * gets the default Download folder that was saved some time on the past
	 */
	public String getDefaultFolder() {
		String folder = null;
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			if (conn != null) {
				String sql = "SELECT * FROM defaultFolder";
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()) {
					folder = rs.getString(1).toString();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return folder;
	}
	
	// inserts the direct download information to the "direct" table
	public void insertDownloadDir(ArrayList<Download> d,int id) {
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			if (conn != null) {
				String sql = "INSERT INTO direct (id,name,URL,savedLocation,date) VALUES ("+d.get(id).getDownloadID()+",\""+d.get(id).getNameOfFile()+"\",\""+d.get(id).getFileLoc()+"\",\""+d.get(id).getLocation()+"\",\""+getDateTime()+"\");";
				stmt.execute(sql);
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
					if (id > newDownID || id == newDownID) {
						newDownID = id;
					}
					if ( (file = new File(f)).exists()) {
						long size = file.length();
						String[] item1 = {String.valueOf(id),results.getString(2),"100%  0 KB/s",getFileSize(size),results.getString(5),results.getString(3)};
						item = item1;
					} else if (new File(f+"single.dat").exists() || new File(f+"1.dat").exists()) {
						String[] item1 = {String.valueOf(id),results.getString(2),"0%  0 KB/s","Paused",results.getString(5),results.getString(3)};
						item = item1;
					} else {
						String[] item1 = {String.valueOf(id),results.getString(2),"Not Found","Not Found",results.getString(5),results.getString(3)};
						item = item1;
					}
					
					counterDow = counterDow + 1;
					Download download = new Download(id, results.getString(3), results.getString(2), results.getString(4), -1);
					int partialValue = Integer.parseInt(results.getString(6));
					if (partialValue==1) {
						download.setPartial(true);
					} else if (partialValue==0) {
						download.setPartial(false);
					} else {
						download.setPartial(null);
					}
					window.addDown(download);
					
					t.add(item);
					
				}
				newDownID = newDownID + 1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return t;
	}
	
	/*
	 *  Updates the partial value so program doesnt have to redetermine if a download is supporting multipart
	 */
	public void updateDirPartial(boolean partial, int id) {
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			int partialValue;
			if (partial==true) {
				partialValue = 1;
			} else {
				partialValue = 0;
			}
			if (conn != null) {
				String sql = "UPDATE direct SET partial == "+partialValue+" WHERE id == "+id+";";
				
				stmt.execute(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// updates the location the torrent file has been saved on local drive
	public void updateLocTor(String oldLoc, String newLoc, int id) {
		try (Connection conn = DriverManager.getConnection(database); Statement stmt = conn.createStatement()) {
			String newLo = newLoc;
			String old = oldLoc;
			if (conn != null) {
				String sql = "UPDATE torrent SET savedLocation == \""+newLo+"\\"+"\" WHERE savedLocation == \""+old+"\" AND id == "+id+";";
				
				stmt.execute(sql);
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
				boolean exists = false;
				String sql = "SELECT * FROM torrent";
				ResultSet results = stmt.executeQuery(sql);
				while (results.next()) {
					String[] item;
					String f = results.getString(4)+"\\"+results.getString(2);
					int id = Integer.parseInt(results.getString(1));
					if (id>newTorID || id == newTorID) {
						newTorID = id;
					}
					if (new File(f).exists()) {
						String[] item1 = {String.valueOf(id),results.getString(2),"00%  0 KB/s","Stopped",results.getString(5),results.getString(3)};
						item = item1;
						exists = true;
					} else {
						String[] item1 = {String.valueOf(id),results.getString(2),"0%  0 KB/s","Not Found",results.getString(5),results.getString(3)};
						item = item1;
					}
					counterTor = counterTor + 1;
					Torrent tr = new Torrent(id, results.getString(3), results.getString(2), results.getString(4), -1);
					if (exists) {
						tr.setSize(2);
					}
					window.addTor(tr);
					t.add(item);
					
				}
				newTorID = newTorID + 1;
				System.out.println(newTorID);
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
