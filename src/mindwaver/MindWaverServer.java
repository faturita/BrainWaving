package mindwaver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MindWaverServer extends MindWaver {

	static int port = 13855;
	
	ServerSocket server ;
	
	public MindWaverServer() throws Exception {
		super();
		// TODO Auto-generated constructor stub
		
		server = new ServerSocket(port);
		
		
		
	}
	
	public static void main(String[] args) throws Exception {
		final MindWaverServer md = new MindWaverServer();

		int i = 0;
		
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true)
					try {
						md.updateMindSet();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			}
			
		}).start();
		
		md.server();

	}
	
	
	void updateMindSet() throws Exception {
		
		String line = reader.readLine();
				
		if (line != null && line.contains("\"rawEeg\":"))
		{
			System.out.println(line);
			dataBuffer.buffer(line);	
		}
	}
	
	int idCounter = 0;
	
	DataBuffer dataBuffer = new DataBuffer();
	
	public void server()
	{
		while(true)
		{
			try
			{
				System.out.println ("Waiting for connection....");
				
				final Socket s = server.accept();
				
				final int id = idCounter++;
			
				(new Thread( new Runnable() {
				
					public void run()
					{
						try
						{
							
							BufferedReader socketReader = new BufferedReader( new InputStreamReader(s.getInputStream()));
							
							
							BufferedWriter socketWriter = new BufferedWriter( new OutputStreamWriter(s.getOutputStream()));
							
							socketWriter.write("{\"isAuthorized\": true}");
						
							
							dataBuffer.register(id);
							
							while (true)
							{
								String msg = dataBuffer.consume(id);
								
								if (msg == null)
								{
									msg = "No signal received...";
									
									
								}
								
								System.out.println (msg);
								
								socketWriter.write(msg);
							}
							
							
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						finally 
						{
							dataBuffer.unregister(id);
						}
						
					}
				
				})).start();
			} catch (Exception e)
			{
				e.printStackTrace();
				
				if (server.isClosed()) return;
			}
			
		}
	}

	
	
	
}
