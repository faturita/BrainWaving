package mindwaver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import mindwaver.SawFaders;

// http://www.proxyarch.com/studio/mindset.html

// Typical data string from NeuroSky Device:
// aaaa0200 04390500 812036ec 237b3687 87c73734 edc73870 c7f136f2 af4f3690 bf783513 2d0535bd ab96
// aaaa sync
// 0200 signal (0-200, 0 is good, 200 is off-head)
// 0439 attention  (0 is non
// 0500 meditation
// 8120 wave code + length (20 is Hexidecimal for 32, 8 numbers x 4 bytes each (for a float)
// 36ec 237b3687 87c73734 edc73870 c7f136f2 af4f3690 bf783513 2d0535bd ab96	

public class MindWaver {

	int meditation, attention;
	float[] waves;

	float eeg;

	static int port = 13854;
	String ip = "127.0.0.1";
	PrintStream output;

	InputStream str;
	BufferedReader reader;

	Socket s;

	BufferedWriter wr;
	
	StringBuffer stringBuffer;
	
	int poorSignalLevel;
	
	SawFaders sawFaders;
	double offset = 1000.0;
	double amplification = 1;

	int updateFreq=0;
	
	
	
	public static void main(String[] args) throws Exception {
		MindWaver md = new MindWaver();

		int i = 0;

		try {
			while (i++ < 100000)
				md.updateMindSet();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			md.close();
		}

	}

	public void close() throws Exception {
		str.close();
		s.close();

		wr.flush();
		wr.close();
		
		sawFaders.finishRecording();
		
		sawFaders.stop();
	}

	public  MindWaver() throws Exception {
		output = System.out;

		waves = new float[8];

		s = new Socket(ip, port);


		wr = new BufferedWriter(new FileWriter("file.mat"));
		
		
		BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(s.getOutputStream()));
		
		//writer.write("{\"appName\": \"Apps\", \"appKey\": \"9f54141b4b6c567c558d3a76cb8d715cbde03096\"}\n");

		//System.out.println("Response:"+reader.readLine());
		//{"enableRawOutput": true, "format": "Json"}
		String message = "{\"enableRawOutput\": true, \"format\": \"JSON\"}\r\n";
		writer.write(message);
		
		System.out.println( message );
		
		str = s.getInputStream();
		
		reader = new BufferedReader( new InputStreamReader(str));

		System.out.println("Response:"+reader.readLine());
		
		sawFaders = new SawFaders();
		
		//sawFaders.initSynthesizer();
		//sawFaders.sdtart();
		// OR
		
		sawFaders.initSawFadersApplet(sawFaders, "Mosquito");
		
		sawFaders.initRecording("brainsounds.wav");
		
	}


	
	void updateMindSet() throws Exception {
		
		String line = reader.readLine();
		System.out.println(line);
		
		if (line != null && line.contains("\"rawEeg\":"))
		{
			System.out.println(line);
			String numericValue = line.split(":")[1];
			int eeg = new Integer( numericValue.substring(0,numericValue.length()-1)).intValue();
			
			
			System.out.println("EEG:"+eeg);
			
			double calculatedFrequency = Math.abs(eeg*amplification+offset);
			
			System.out.println("Freq:"+calculatedFrequency);
			
			sawFaders.changeFrequency(calculatedFrequency);
			
			/**if (updateFreq++ % 1000 == 0)
				player.play(player.notes.tone(index, 1000));
			**/
			
			wr.write(new Integer(eeg).toString()+"\n");
			wr.flush();
			
		}
		
	}
	
	
	void updateMindSetRaw() throws Exception {
		byte[] buffer = new byte[42];
		
		int byteCount = str.read(buffer);
		if (byteCount > 0 ) {
			
			
			if (buffer[0] == -86 && buffer[1] == -86)
			{
				System.out.println ("SYNC("+byteCount+")");
				
				for (int k=0;k<byteCount;k++)
				{
					System.out.print(  "("+k+")"+ (short)buffer[k] +":" );
					
					if (buffer[k]==-128)
					{
						
						
						byte[] floatBuffer = new byte[4];
						for (int j = 0; j < 4; j++) {
							floatBuffer[j] = buffer[k + j];
						}
						
						//eeg = byteArrayToFloat(floatBuffer) * 10000;
						
						short eeg = (short)((buffer[k+2]<<8) | buffer[k+1]);
						
						System.out.println("EEG:"+eeg);
						
						wr.write(new Integer(eeg).toString()+"\n");
						wr.flush();
					}
				}
				
				System.out.println ("");
				
			}
			

			
			//short rawValue = (hiByte<<8) | loByte;
			
			/**
			poorSignalLevel = (int)buffer[0];

			byte[] floatBuffer = new byte[4];
			for (int j = 0; j < 4; j++) {
				floatBuffer[j] = buffer[10 + j];
			}
			
			eeg = byteArrayToFloat(floatBuffer) * 10000;

			//eeg = (float)3.4;
			
			System.out.println (eeg);
			
			meditation = buffer[7];
			attention = buffer[5];
			for (int i = 0; i < 8; i++) {
				floatBuffer = new byte[4];
				for (int j = 0; j < 4; j++) {
					floatBuffer[j] = buffer[10 + i * 4 + j];
				}
				waves[i] = byteArrayToFloat(floatBuffer) * 10000;
				//wr.write(new Float(waves[i]).toString() + "\n");
			}
			
			**/
		}

		if (byteCount > 0) {
			
			
			String recvData = new String(buffer);
			
			//System.out.println(recvData)	;		
			
			for (int k=0;k<byteCount;k++)
			{
				System.out.print(  "("+k+")"+ (short)buffer[k] +":" );
			}System.out.println ("");
			// output.println(attention+","+meditation+","+waves[0]+","+waves[1]+","+waves[2]+","+waves[3]+","+waves[4]+","+waves[5]+","+waves[6]+","+waves[7]);
			// // Write the coordinate to the file

			
			//output.println(poorSignalLevel);
			//wr.write(new Float(eeg).toString()+"\n");

		} else {
			System.out.println("No Esense Values!");
		}

	}

	int i=0;
	
	float byteArrayToFloat(byte test[]) {
		int bits = 0;
		int i = 0;
		for (int shifter = 3; shifter >= 0; shifter--) {
			bits |= ((int) test[i] & 0xff) << (shifter * 8);
			i++;
		}
		return Float.intBitsToFloat(bits);
	}

}
