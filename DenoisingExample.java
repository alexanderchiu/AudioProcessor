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




            Denoiser denoiser = new Denoiser(fs, 0.4, 9, 2, 8);
            if (numChannels == 1) {
                enhancedSingle =  denoiser.process(buffer);
                WavFile output = WavFile.newWavFile(new File(justName + "_enhanced.wav"), numChannels,  enhancedSingle.length, validBits, fs);
                output.writeFrames(enhancedSingle, enhancedSingle.length);
                double[] agcsignal;

                AGC agc = new AGC(fs, 0.4, 9, 2, 8);

                agcsignal = agc.process(enhancedSingle);

                WavFile output2 = WavFile.newWavFile(new File(justName + "_agc.wav"), numChannels, agcsignal.length, validBits, fs);


                output2.writeFrames(agcsignal, agcsignal.length);
            } else {
                double[][] enhanced;


                for (int i = 0; i < numFrames; i++) {
                    for (int k = 0; k < numChannels; k++) {
                        splitChannel[k][i] = buffer[i * numChannels + k];
                    }
                }
                enhanced = denoiser.process(splitChannel);

                double[] bufferMultiChannel = new double[enhanced[0].length * numChannels];

                for (int i = 0; i < enhanced[0].length; i++) {
                    for (int k = 0; k < numChannels; k++) {

                        bufferMultiChannel[i * numChannels + k] = enhanced[k][i];
                    }

                }


                WavFile output = WavFile.newWavFile(new File(justName + "_enhanced.wav"), numChannels,  enhanced[0].length, validBits, fs);
                output.writeFrames(bufferMultiChannel, bufferMultiChannel.length);
                double[][] agcsignal;

                AGC agc = new AGC(fs, 0.2, 9, 2, 8);

                for (int i = 0; i < enhanced[0].length; i++) {
                    for (int k = 0; k < numChannels; k++) {
                        splitChannel[k][i] = bufferMultiChannel[i * numChannels + k];
                    }
                }
                agcsignal = agc.process(splitChannel);

                for (int i = 0; i < enhanced[0].length; i++) {
                    for (int k = 0; k < numChannels; k++) {
                        bufferMultiChannel[i * numChannels + k] = agcsignal[k][i];
                    }

                }
                WavFile output2 = WavFile.newWavFile(new File(justName + "_agc.wav"), numChannels, enhanced[0].length, validBits, fs);
                output2.writeFrames(bufferMultiChannel, bufferMultiChannel.length);
            }

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
