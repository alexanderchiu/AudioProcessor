import java.io.*;
import java.util.Arrays;
public class DenoisingExample
{
	public static void main(String[] args)
	{
		try
		{
			// Open the wav file specified as the first argument
			WavFile wavFile = WavFile.openWavFile(new File(args[0]));


			// Display information about the wav file
			wavFile.display();

			// Get the number of audio channels in the wav file
			int numChannels = wavFile.getNumChannels();
                             int numFrames = (int)wavFile.getNumFrames();
                             int samples = numFrames*numChannels;


                             System.out.println(numChannels);
                             System.out.println(numFrames);
			// Create a buffer of 100 frames
			double[] buffer = new double[samples];

                             int framesRead;
			framesRead = wavFile.readFrames(buffer, numFrames);


			// Close the wavFile
			wavFile.close();

			// Output the minimum and maximum value


                           // File f=new File("wav.txt");


                           //  PrintWriter out=new PrintWriter(f);
                           //  for(int i =0;i<numFrames;i++){
                           //  out.printf("%f ",buffer[i]);
                           //  }
                           //  System.out.println("Done ..........");
                           //  out.close();

		}
		catch (Exception e)
		{
			System.err.println(e);
		}
	}
}
