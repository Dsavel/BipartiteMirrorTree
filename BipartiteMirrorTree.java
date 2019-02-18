
import java.io.*;
import java.util.*;

public class BipartiteMirrorTree 
{
	public static void main(String[] args) 
	{
		if(args.length == 3)
		{
			new BipartiteMirrorTree(args[0], args[1], args[2]);
		}
		else if(args.length == 4)
		{
			new BipartiteMirrorTree(args[0], args[1], args[2], args[3]);
		}
		else if(args.length == 5 && args[2].equalsIgnoreCase("-T") )
		{
			new BipartiteMirrorTree(args[0], args[1], Integer.parseInt(args[3]), args[4]);
		}
		else
		{
			System.out.println("Usage: BipartiteMirrorTree <Class1Profiles> <Class2Profiles> [<pairings>] <output>");
			System.out.println("Multi-threaded All-Pairs Histogram Usage: BipartiteMirrorTree <Class1Profiles> <Class2Profiles> -T <number of threads> <output>");
		}
	}
	
	public BipartiteMirrorTree(String inputClass1, String inputClass2, String output)
	{
		try
		{
			int vectorLength = ProfileVector.GetProfileLength(inputClass1);
			int vectorLength2 = ProfileVector.GetProfileLength(inputClass2);
			
			if(vectorLength != vectorLength2)
			{
				System.out.println("ERROR: Vector Length Mismatch");
				return;
			}
			HashMap<String, ProfileVector> profiles1 = ProfileVector.LoadProfiles(inputClass1);
			HashMap<String, ProfileVector> profiles2 = ProfileVector.LoadProfiles(inputClass2);
			
			PrintWriter printer = new PrintWriter(output);
			
			for(ProfileVector profile1 : profiles1.values())
			{
				for(ProfileVector profile2 : profiles2.values())
				{
					printer.println(GetProfileSparseCorrelation(profile1.values, profile2.values));
				}
			}
			printer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public BipartiteMirrorTree(String inputClass1, String inputClass2, String interactions, String output)
	{
		try
		{
			int vectorLength = ProfileVector.GetProfileLength(inputClass1);
			int vectorLength2 = ProfileVector.GetProfileLength(inputClass2);
			if(vectorLength != vectorLength2)
			{
				System.out.println("ERROR: Vector Length Mismatch");
				return;
			}

			HashMap<String, ProfileVector> profiles1 = ProfileVector.LoadProfiles(inputClass1);
			HashMap<String, ProfileVector> profiles2 = ProfileVector.LoadProfiles(inputClass2);
			
			BufferedReader reader = new BufferedReader(new FileReader(interactions));
			PrintWriter printer = new PrintWriter(output);
			String header = reader.readLine();
			printer.println(header + "\tSparseCorr");			
			
			for(String workingLine = reader.readLine(); workingLine != null; workingLine = reader.readLine())
			{
				String[] cells = workingLine.split("\t");
								
				ProfileVector class1Vector = profiles1.get(cells[0]);
				ProfileVector class2Vector = profiles2.get(cells[1]);
				
				if(class1Vector != null && class2Vector != null)
				{
					double sparseCorrelation = GetProfileSparseCorrelation(class1Vector.values, class2Vector.values);
					printer.println(workingLine + "\t" + sparseCorrelation);
				}
			}
			reader.close();
			printer.close();			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public BipartiteMirrorTree(String inputClass1, String inputClass2, int NumThreads, String output)
	{
		try
		{
			int vectorLength = ProfileVector.GetProfileLength(inputClass1);
			int vectorLength2 = ProfileVector.GetProfileLength(inputClass2);
			if(vectorLength != vectorLength2)
			{
				System.out.println("ERROR: Vector Length Mismatch");
				return;
			}
			
			HashMap<String, ProfileVector> profiles1 = ProfileVector.LoadProfiles(inputClass1);
			HashMap<String, ProfileVector> profiles2 = ProfileVector.LoadProfiles(inputClass2);
			CoevResults results = new CoevResults();
			
			ArrayList<ArrayList<String>> threadWorkingSets = new ArrayList<ArrayList<String>>();
			for(int i = 0; i < NumThreads; i++)
				threadWorkingSets.add(new ArrayList<String>());
			
			int numKeys = 0;
			for(String key : profiles1.keySet())
			{
				threadWorkingSets.get(numKeys % NumThreads).add(key);
				numKeys++;
			}
			
			BMTThread[] threads = new BMTThread[NumThreads];
			
			for(int i = 0; i < NumThreads; i++)
			{
				threads[i] = new BMTThread(threadWorkingSets.get(i), profiles1, profiles2, results);
				threads[i].start();
			}
			for(int i = 0; i < NumThreads; i++)
			{
				threads[i].join();
			}
			
			CoevResults.OutputResultHist(output, results);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
		
	private class BMTThread extends Thread
	{
		
		ArrayList<String> keys;
		HashMap<String, ProfileVector> profiles1;
		HashMap<String, ProfileVector> profiles2;
		CoevResults results;
		
		public BMTThread(ArrayList<String> keys, HashMap<String, ProfileVector> vectors, HashMap<String, ProfileVector> vectors2, CoevResults results)
		{
			this.keys = keys;
			this.profiles1 = vectors;
			this.profiles2 = vectors2;
			this.results = results;
		}

		public void run()
		{	
			CoevResults privateResults = new CoevResults();

			for(String key : keys)
			{
				ProfileVector vector1 = profiles1.get(key);
				if(vector1 != null)
				{
					for(ProfileVector vector2 : profiles2.values())
					{
						privateResults.putValue(GetProfileSparseCorrelation(vector1.values, vector2.values));	
					}
				}
			}
			
			synchronized(results)
			{
				results.meanCorr += privateResults.meanCorr;
				results.corrHits += privateResults.corrHits;
				for(int i = 0; i < privateResults.corrHistogram.length; i++)
				{
					results.corrHistogram[i] += privateResults.corrHistogram[i];
				}
			}
		}
	}
	
	//Linear Correlation Coefficient
	public static double GetProfileCorrelation(int[] a, int[]b)
	{
		double aBar = 0;
		
		for(int x : a)
		{
			aBar += x;
		}
		
		aBar /= a.length;
		
		double bBar = 0;
		
		for(int x : b)
		{
			bBar += x;
		}
		bBar /= b.length;
				
		double covariance = 0;
		for(int i = 0; i < a.length; i++)
		{
			covariance += ((a[i] - aBar)*(b[i] - bBar));
		}
		
		double aSD = 1;
		
		for(int i = 0; i < a.length; i++)
		{
			aSD += ((a[i] - aBar)*(a[i] - aBar));
		}		

		double bSD = 1;
		
		for(int i = 0; i < b.length; i++)
		{
			bSD += ((b[i] - bBar)*(b[i] - bBar));
		}
		
		bSD = Math.sqrt(bSD);
		aSD = Math.sqrt(aSD);
		
		return covariance / (aSD * bSD);
	}

	//Sparse Correlation
	public static double GetProfileSparseCorrelation(int[] a, int[] b)
	{
		double aBar = 0;
		double bBar = 0;
		int overlap = 0;
		
		for(int i = 0; i < a.length; i++)
		{
			if(a[i] > 0 || b[i] > 0)
			{
				aBar += a[i];
				bBar += b[i];
				overlap++;
			}
		}
		
		aBar /= overlap;
		bBar /= overlap;
		
		
		double covariance = 0;
		for(int i = 0; i < a.length; i++)
		{
			if(a[i] > 0 || b[i] > 0)			
				covariance += ((a[i] - aBar)*(b[i] - bBar));
		}
		
		double aSD = 1;
		double bSD = 1;		
		
		for(int i = 0; i < a.length; i++)
		{
			if(a[i] > 0 || b[i] > 0)
			{
				aSD += ((a[i] - aBar)*(a[i] - aBar));
				bSD += ((b[i] - bBar)*(b[i] - bBar));
			
			}
		}	
		
		bSD = Math.sqrt(bSD);
		aSD = Math.sqrt(aSD);
		
		return covariance / (aSD * bSD);
	}
}
