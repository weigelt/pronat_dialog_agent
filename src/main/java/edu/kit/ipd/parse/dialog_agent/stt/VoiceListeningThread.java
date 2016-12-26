package edu.kit.ipd.parse.dialog_agent.stt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads data from the input channel and writes to the output stream
 */
public class VoiceListeningThread implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(VoiceRecorder.class);
	
	String absoluteFilePath;
	
	int resolution;
	
	final int bufSize = 16384;
	
	AudioInputStream audioInputStream;
	
	String errStr;

	double duration, seconds;

	File file;
	
	TargetDataLine line;

	Thread thread;
	
	public VoiceListeningThread(String absoluteFilePath, int resolution) {
		this.absoluteFilePath = absoluteFilePath;
		this.resolution = resolution;
	}
	
	public void start() {
		errStr = null;
		thread = new Thread(this);
		thread.setName("Capture");
		thread.start();
	}

	public void stop() {
		thread = null;
	}

	private void shutDown(String message) {
		if ((errStr = message) != null && thread != null) {
			thread = null;
			System.err.println(errStr);
		}
	}

	public void run() {

		duration = 0;
		audioInputStream = null;

		// define the required attributes for our line,
		// and make sure a compatible line is supported.
		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		float rate = 44100.0f;
		int channels = 2;
		int sampleSize = resolution; // 16 means 16 Bit resolution
		boolean bigEndian = false;

		AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate,
				bigEndian);

		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

		if (!AudioSystem.isLineSupported(info)) {
			shutDown("Line matching " + info + " not supported.");
			return;
		}

		// get and open the target data line for capture.
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format, line.getBufferSize());
		} catch (LineUnavailableException ex) {
			shutDown("Unable to open the line: " + ex);
			return;
		} catch (SecurityException ex) {
			shutDown(ex.toString());
			// JavaSound.showInfoDialog();
			return;
		} catch (Exception ex) {
			shutDown(ex.toString());
			return;
		}

		// play back the captured audio data
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int frameSizeInBytes = format.getFrameSize();
		int bufferLengthInFrames = line.getBufferSize() / 8;
		int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
		byte[] data = new byte[bufferLengthInBytes];
		int numBytesRead;

		line.start();

		while (thread != null) {
			if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
				break;
			}
			out.write(data, 0, numBytesRead);
		}

		// we reached the end of the stream.
		// stop and close the line.
		line.stop();
		line.close();
		line = null;

		// stop and close the output stream
		try {
			out.flush();
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// load bytes into the audio input stream for playback
		byte audioBytes[] = out.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
		audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);

		AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(format, audioInputStream);

		try {
			File outputFile = new File(absoluteFilePath);
			FlacFileEncoder ffe = new FlacFileEncoder();
			ffe.encode(playbackInputStream, outputFile);
			logger.info("Answer is saved under: " + absoluteFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}

		long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / format.getFrameRate());
		duration = milliseconds / 1000.0;

		try {
			audioInputStream.reset();
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}
}