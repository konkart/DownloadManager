import java.io.IOException;
import java.net.*;
public class getContentTypeA {
	static String cont;
	public getContentTypeA() {
		getContentTypeA.cont = "ERROR";
	}
	public static String gContentTypeA(String a) throws IOException {
		
		URL newurl = new URL(a);
		URLConnection con = newurl.openConnection();
		cont = con.getContentType();
		String[] contentA = cont.split("/");
		String content = contentA[1];
		return content;
	}
}
