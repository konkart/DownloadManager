package gr.konkart.dm;

import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class URLHandler {
	//method to identify if a path is a URL or a magnet link
	public static String isUrl(String u) {
		String isit = null;
		
		final String URL_REGEX = "^((https?|ftp)://|(www|ftp).)?[a-z0-9-]+(.[a-z0-9-]+)+([/?]*.*)*?$";				
		final String MAGNET_REGEX= "^magnet:\\?xt=urn:btih:[a-zA-Z0-9]*.*";
		Pattern p = Pattern.compile(URL_REGEX);
		Pattern t = Pattern.compile(MAGNET_REGEX);
		Matcher mag = t.matcher(u);
		Matcher m = p.matcher(u);
		if(m.find()) {
		    isit="URL";
		}
		else if (mag.find()){isit="Torrent";}
		return isit;
	}
	//gets the filename from the link(url)
	public static String getFilename(String pathfilename) {
		String nameOfTheFile=pathfilename;
		
		boolean contentDisposition = false;
		String parts[];
		try {
			URL urlObj = new URL(pathfilename);
			URLConnection con = urlObj.openConnection();
			if(con.getHeaderField("Content-Disposition")!=null) {
				parts = con.getHeaderField("Content-Disposition").split("filename=");
				nameOfTheFile = parts[1].replace("\"", "");
				contentDisposition=true;
			}
			
		}catch(Exception e) {
			contentDisposition=false;
		}
		if (contentDisposition==false){
			try {
				URL urlObj = new URL(pathfilename);
				URLConnection con = urlObj.openConnection();
				String urlPath = urlObj.getPath();
				String fileName = urlPath.substring(urlPath.lastIndexOf('/')+1).trim();
				String ext = con.getContentType();
				ext = ext.substring(ext.lastIndexOf('/')+1);
				if(!ext.equals("null") && !fileName.contains('.'+ext)) {
						nameOfTheFile = fileName+'.'+ext;
				}
				else {
					nameOfTheFile = fileName;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return nameOfTheFile;
	}
}
