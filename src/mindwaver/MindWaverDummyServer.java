package mindwaver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class MindWaverDummyServer {

	InputStream str;
	BufferedReader reader;

	Socket s;
	
	static int port = 13855;
	
	public static void main(String[] args) throws Exception {
		MindWaverDummyServer md = new MindWaverDummyServer();
		
		md.serve();
		
	}
	
	public void serve() throws Exception
	{
		ServerSocket s = new ServerSocket(port);
		
		Socket ss = s.accept();
		
		str = ss.getInputStream();
		
		reader = new BufferedReader( new InputStreamReader(str));
		
		String msg;
		
		BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(ss.getOutputStream()));
		
		//msg = reader.readLine();
		//System.out.println(msg);
		
		writer.write("{\"isAuthorized\": true}");
		
		/**
		while ( ( msg = reader.readLine())!= null)
		{
			System.out.println(msg);

		}
		**/
		
		int i=0;
		
		while (true)
		{
			writer.write("{\"rawEeg\":"+Math.sin(2 * Math.PI * i++ / 8000) * 127+"}");
		}

	}

}
