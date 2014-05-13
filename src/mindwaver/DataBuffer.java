package mindwaver;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DataBuffer {

	
	ConcurrentMap<Integer,Integer> indexList = new ConcurrentHashMap<Integer,Integer>();
	
	ConcurrentMap<Integer,String> indexedBuffer = new ConcurrentHashMap<Integer,String>();
	
	int baseIndex = 0;
	
	int writeIndex = 0;
	
	Object lock = new Object();
	
	
	
	public synchronized void register(int id) {
		indexList.put(id, baseIndex);
		
	}

	
	public synchronized String consume(int id) {
		StringBuffer str = new StringBuffer();
		
		synchronized(lock)
		{
			int readIndex = indexList.get(id);
		
			
			while( indexedBuffer.containsKey(readIndex))
			{
				str.append( indexedBuffer.get(readIndex++));
			}
			
			indexList.put(id, readIndex);
		}
		
		return str.toString();
	}

	public synchronized void unregister(int id) {
		indexList.remove(id);
		
	}


	public synchronized void buffer(String line) {
		// TODO Auto-generated method stub
		
		synchronized (lock)
		{
			// Store
			indexedBuffer.put(writeIndex++, line);
			
			
			// Prune
			int max = writeIndex;
			for (int value : indexList.values())
			{
				if (value < max) max = value;
			}
			
			for (int index : indexedBuffer.keySet())
			{
				if (index < max)
				{
					indexedBuffer.remove(index);
				}
			}
			
			baseIndex = max;
		
		}
		
		
	}

}
