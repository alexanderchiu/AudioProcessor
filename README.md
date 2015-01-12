AudioProcessor
==============

Java library for speech enhancement

Usage
==============
java DenoisingExample [wavfile]

Denoiser denoiser = new Denoiser(parameters...);<br/>
float[][] input = ...;<br/>
float[][] output = denoiser.process(input);<br/>

Denoiser denoiser = new Denoiser(parameters...);<br/>
float[] input = ...;<br/>
float[] output = denoiser.process(input);<br/>

References
==============
[1] Forward Backward Decision Directed Approach For Speech Enhancement Richard C. Hendriks, Richard Heusdens and Jesper Jensen<br/>
[2] Ephraim, Y.; Malah, D., "Speech enhancement using a minimum-mean square error short-time spectral amplitude estimator," Acoustics, Speech and Signal Processing, IEEE Transactions on , vol.32, no.6, pp.1109,1121, Dec 1984
doi: 10.1109/TASSP.1984.1164453<br/>
[3] ComplexNumber library by Abdul Fatir https://github.com/abdulfatir/jcomplexnumber <br/>
[4] SpecialMath library by JScience http://jscience.org/ <br/>
[5] WavFile IO class by A.Greensted http://www.labbookpages.co.uk/audio/javaWavFiles.html <br/>
[6] MMSE STSA by Esfandiar Zavarehei http://www.mathworks.com/matlabcentral/fileexchange/10143-mmse-stsa

 

 


