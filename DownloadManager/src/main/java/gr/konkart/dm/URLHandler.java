package gr.konkart.dm;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class URLHandler {
	//method to identify if a path is a URL or a magnet link
	public static String getUriType(String u) {
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
		String fileName;
		String parts[];
		try {
			URL urlObj = new URL(pathfilename);
			URLConnection con = urlObj.openConnection();
			if(con.getHeaderField("Content-Disposition")!=null) {
				try {
					parts = con.getHeaderField("Content-Disposition").split("filename=");
					nameOfTheFile = parts[1].replace("\"", "");
					contentDisposition=true;
				} catch (Exception e) {e.printStackTrace();}
			}
			if (contentDisposition==false){
					String urlPath = urlObj.getPath();
					if (!urlPath.endsWith("/") && !urlPath.isEmpty()) {
						fileName = urlPath.substring(urlPath.lastIndexOf('/')+1).split(":")[0].trim();
					} else if (urlPath.endsWith("/") && urlPath.length()>21){
						fileName = urlPath.substring(urlPath.lastIndexOf('/')-20).split("/")[0].split(":")[0].trim();
					} else {
						fileName = "index" + urlObj.getHost();
					}
					String ext = con.getContentType();
					System.out.println(fileName);
					try {
						ext = ext.substring(ext.lastIndexOf('/')+1).split(";")[0];
					} catch (Exception e) {ext = null;}
					if(ext!=null && !ext.equals("unknown")) {
						if (!fileName.matches("[a-zA-Z0-9]*[.][a-zA-Z]+")) {
							nameOfTheFile = fileName+"."+ext;
							System.out.println("lol");
						} else {
							nameOfTheFile = fileName;
						}
					}
			}
			System.out.println(nameOfTheFile+" aaaa");
		}catch(Exception e) {e.printStackTrace();}
		return nameOfTheFile;
	}
}
