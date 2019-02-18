import java.io.IOException;
import java.io.PrintWriter;

public class CoevResults
{
	public double meanCorr = 0;
	public double corrHits = 0;
	public long[] corrHistogram = new long[201];
	
	public void putValue(double value)
	{
		int index = 100 + (int)(value*100);
		corrHistogram[index]++;
		
		meanCorr += value;
		corrHits++;
	}
	
	public static void OutputResultHist(String output, CoevResults results) throws IOException
	{
		PrintWriter printer = new PrintWriter(output);
		results.meanCorr /= results.corrHits;
		printer.println("Correlation\tCount\tFraction\tECDF\trevECDF\tmeanCorr\t" + results.meanCorr);
		double totFraction = 0;
		double[] revECDF = new double[results.corrHistogram.length];
		revECDF[revECDF.length - 1] = (((double)results.corrHistogram[revECDF.length -1]) / ((double)results.corrHits));
		for(int i = revECDF.length - 2; i >= 0; i--)
			revECDF[i] = revECDF[i + 1] + (((double)results.corrHistogram[i]) / ((double)results.corrHits));
		
		
		for(int i = 0; i < results.corrHistogram.length; i++)
		{
			double bin = ((double)i-100)*0.01;
			double fraction = (((double)results.corrHistogram[i]) / ((double)results.corrHits));
			totFraction += fraction;
			printer.println(bin + "\t" + results.corrHistogram[i] + "\t" + fraction + "\t" + totFraction + "\t" + revECDF[i]);
		}
		printer.close();
	}
	
}