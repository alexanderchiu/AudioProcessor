import java.io.*;
import java.util.Arrays;
public class DenoisingExample {
    public static void main(String[] args) {
        try {
            // Open the wav file specified as the first argument
            WavFile wavFile = WavFile.openWavFile(new File("hynek.wav"));


            // Display information about the wav file
            wavFile.display();

            // Get the number of audio channels in the wav file
            int numChannels = wavFile.getNumChannels();
            int numFrames = (int)wavFile.getNumFrames();
            int samples = numFrames * numChannels;

            // Create a buffer of 100 frames
            double[] buffer = new double[samples];

            int framesRead;
            framesRead = wavFile.readFrames(buffer, numFrames);

            // Close the wavFile
            wavFile.close();

           Denoiser denoiser = new Denoiser();

           double[] enhanced = denoiser.process(buffer);


            // Output the minimum and maximum value


        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
