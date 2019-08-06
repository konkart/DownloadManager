package gr.konkart.dm;

import java.io.IOException;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class URLHandler {
	static String cont;
	public URLHandler() {
	URLHandler.cont = "ERROR";
	}
	//gets the type name of file
	public static String gContentTypeA(String a) throws IOException {
		
		URL newurl = new URL(a);
		URLConnection con = newurl.openConnection();
		cont = con.getContentType();
		System.out.println(con.getHeaderField("Connection"));
		String[] contentA = cont.split("/");
		String content = contentA[1];
		return content;
	}
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
		int p = Math.max(pathfilename.lastIndexOf('/'), pathfilename.lastIndexOf("\\"));
		if(pathfilename.substring(p + 1).contains("?")) {
			String parts[]=pathfilename.substring(p + 1).split("\\?");
			nameOfTheFile=parts[0];
		}
		else {
			nameOfTheFile=pathfilename.substring(p + 1);
		}
			if (p >= 0) {
				return nameOfTheFile;
			} 
			else {
				return pathfilename;
		}
		}
}
