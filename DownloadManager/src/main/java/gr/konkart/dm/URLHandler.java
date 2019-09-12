package gr.konkart.dm;
import java.io.IOException;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class URLHandler {
	//method to identify if a path is a URL or a magnet link
	public static String isUrl(String u) {
		String isit = null;
		
		try {
			URL urlObj = new URL(u);
			URLConnection con = urlObj.openConnection();
			if(con.getContentType()!="content/unknown") {
				isit = "URL";
			}
		} catch (Exception e) {
			isit=null;
			e.printStackTrace();
		}
		final String MAGNET_REGEX= "^magnet:\\?xt=urn:btih:[a-zA-Z0-9]*.*";
		Pattern t = Pattern.compile(MAGNET_REGEX);
		Matcher mag = t.matcher(u);
		if (mag.find()){isit="Torrent";}
		return isit;
	}
	//gets the filename from the link(url)
	public static String getFilename(String pathfilename){
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
			if (contentDisposition==false){
					String urlPath = urlObj.getPath();
					String fileName = urlPath.substring(urlPath.lastIndexOf('/')+1).trim();
					String ext = con.getContentType();
					try {
						ext = ext.substring(ext.lastIndexOf('/')+1);
					} catch (Exception e) {ext = null;}
					if(ext!=null && !fileName.contains('.'+ext) && !ext.equals("unknown")) {
							if (fileName.contains("jpg")) {
								nameOfTheFile = fileName;
							}
							else {
								nameOfTheFile = fileName+'.'+ext;
							}
					}
					else {
						nameOfTheFile = fileName;
					}
			}
		}catch(Exception e) {e.printStackTrace();}
		return nameOfTheFile;
	}
}
