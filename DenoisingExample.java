import java.io.*;
import java.util.Arrays;
public class DenoisingExample {
    public static void main(String[] args) {
        try {
            // Open the wav file specified as the first argument
            WavFile wavFile = WavFile.openWavFile(new File("noisy.wav"));

            // Display information about the wav file
            wavFile.display();
            int fs = (int)wavFile.getSampleRate();
            int validBits = wavFile.getValidBits();
            // Get the number of audio channels in the wav file
            int numChannels = wavFile.getNumChannels();
            int numFrames = (int)wavFile.getNumFrames();
            int samples = numFrames * numChannels;

            double[] buffer = new double[samples];

            int framesRead;
            framesRead = wavFile.readFrames(buffer, numFrames);

            wavFile.close();

            Denoiser denoiser = new Denoiser(fs);
            double[] enhanced = denoiser.process(buffer);

            WavFile output = WavFile.newWavFile(new File("enhanced.wav"), numChannels, numFrames, validBits, fs);
            output.writeFrames(enhanced, enhanced.length);
            output.display();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
