import java.io.*;
import java.util.Arrays;
public class DenoisingExample {
    public static void main(String[] args) {
        try {
            String filename = args[0];
            int pos = filename.lastIndexOf(".");
            String justName = pos > 0 ? filename.substring(0, pos) : filename;
            // Open the wav file specified as the first argument
            WavFile wavFile = WavFile.openWavFile(new File(filename));
            
            // Display information about the wav file
            wavFile.display();
            int fs = (int)wavFile.getSampleRate();
            int validBits = wavFile.getValidBits();
            // Get the number of audio channels in the wav file
            int numChannels = wavFile.getNumChannels();
            int numFrames = (int)wavFile.getNumFrames();
            int samples = numFrames * numChannels;

            double[] buffer = new double[samples];
            double[][] splitChannel = new double[numChannels][numFrames];

            int framesRead;
            framesRead = wavFile.readFrames(buffer, numFrames);


            // Close the wavFile
            wavFile.close();
            double[] enhancedSingle;
            double[][] enhanced;

            WavFile output = WavFile.newWavFile(new File(justName+"_enhanced.wav"), numChannels, numFrames, validBits, fs);

            Denoiser denoiser = new Denoiser(fs,0.4,9,2,8);
            if (numChannels == 1) {
                enhancedSingle =  denoiser.process(buffer);
                output.writeFrames(enhancedSingle, enhancedSingle.length);
            } else {
                for (int i = 0; i < numFrames; i++) {
                    for (int k = 0; k < numChannels; k++) {
                        splitChannel[k][i] = buffer[i * numChannels + k];
                    }
                }
                enhanced = denoiser.process(splitChannel);

                for (int i = 0; i < enhanced[0].length; i++) {
                    for (int k = 0; k < numChannels; k++) {
                        buffer[i * numChannels + k] = enhanced[k][i];
                    }

                }
                output.writeFrames(buffer, buffer.length);
            }

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
