import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ProfileVector 
{
	public int[] values;

	public ProfileVector(int length)
	{
		values = new int[length];
	}
	
	public static HashMap<String, ProfileVector> LoadProfiles(String file) throws IOException
	{
		HashMap<String, ProfileVector> map = new HashMap<String,ProfileVector>();
		LoadProfiles(file, map);		
		return map;
	}
	
	public static void LoadProfiles(String file, HashMap<String, ProfileVector> map) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String[] headerCells = reader.readLine().split("\t"); 
		int vectorLength = headerCells.length - 1;
		
		for(String workingLine = reader.readLine(); workingLine != null; workingLine = reader.readLine())
		{
			String[] cells = workingLine.split("\t");
			ProfileVector vector = new ProfileVector(vectorLength);
			
			for(int i = 0; i < vectorLength; i++)
				vector.values[i] = Integer.parseInt(cells[i + 1]);
			
			for(int i = 0; i < vectorLength; i++)
				vector.values[i] = Integer.parseInt(cells[i + 1]);
			
			map.put(cells[0], vector);
		}
		reader.close();
	}

	public static int GetProfileLength(String file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String[] headerCells = reader.readLine().split("\t"); 
		int vectorLength = headerCells.length - 1;
		reader.close();
		return vectorLength;
	}
}