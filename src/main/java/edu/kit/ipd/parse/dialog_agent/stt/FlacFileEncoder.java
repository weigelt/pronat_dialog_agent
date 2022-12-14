package edu.kit.ipd.parse.dialog_agent.stt;

/*
 * Copyright (C) 2010  Preston Lacey http://javaflacencoder.sourceforge.net/
 * All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import net.sourceforge.javaflacencoder.EncodingConfiguration;
import net.sourceforge.javaflacencoder.FLACEncoder;
import net.sourceforge.javaflacencoder.FLACFileOutputStream;
import net.sourceforge.javaflacencoder.StreamConfiguration;

/**
 * FLAC_FileEncoder is a class to encode an input wav File to an output Flac file. It allows the
 * EncodingConfiguration to be set only once, prior to encoding the entire File.
 * @author Preston Lacey
 */
public class FlacFileEncoder {
	/** Maximum number of bytes to read from file at once */
	private static final int MAX_READ = 16384;

	/** Status enum for encode result */
	public enum Status {
		/** Unknown State. */
		UNKNOWN,
		/** Everything went well */
		FULL_ENCODE,

		/** Something unspecified went wrong... */
		GENERAL_ERROR,

		/**
		 * internal error is something that went haywire that was discovered due to internal sanity
		 * checks. A problem in API.
		 */
		INTERNAL_ERROR,

		/** File given was not able to be read */
		UNSUPPORTED_FILE,

		/** Generic file IO Error */
		FILE_IO_ERROR,

		/** Sample size unsupported */
		UNSUPPORTED_SAMPLE_SIZE,

		/** Error with output file */
		OUTPUT_FILE_ERROR,
		/** No errors found. */
		OK
	}

	FLACEncoder flac = null;
	StreamConfiguration sc = null;
	EncodingConfiguration ec = null;
	File outFile = null;
	int lastTotalSamples = 0;
	boolean useThreads;

	/**
	 * Constructor creates a FLAC_FileEncoder object with default StreamConfiguration configuration
	 * and default EncodingConfiguration. Thread use defaults to true.
	 */
	public FlacFileEncoder() {
		flac = new FLACEncoder();
		sc = new StreamConfiguration();
		ec = new EncodingConfiguration();
		useThreads = true;
	}

	private void adjustConfigurations(AudioFormat format) {
		int sampleRate = (int) format.getSampleRate();
		int sampleSize = format.getSampleSizeInBits();
		int channels = format.getChannels();
		// int blockSize = sc.getMaxBlockSize();
		/*
		 * sc = new StreamConfiguration(channels, blockSize, blockSize, sampleRate, sampleSize); */
		sc.setSampleRate(sampleRate);
		sc.setBitsPerSample(sampleSize);
		sc.setChannelCount(channels);
	}

	private Status openStream() {
		Status status = Status.OK;
		boolean result = flac.setStreamConfiguration(sc);
		result = result & flac.setEncodingConfiguration(ec);
		if (!result)
			status = Status.INTERNAL_ERROR;
		else {
			FLACFileOutputStream fout = null;
			try {
				fout = new FLACFileOutputStream(outFile.getPath());
			} catch (Exception e) {
				status = Status.OUTPUT_FILE_ERROR;
				e.printStackTrace();
			}
			if (status == Status.OK) {
				flac.setOutputStream(fout);
				try {
					flac.openFLACStream();
				} catch (IOException e) {
					status = Status.INTERNAL_ERROR;
				}
			} else
				status = Status.OUTPUT_FILE_ERROR;
		}
		return status;
	}

	/**
	 * Encode the given input wav file to an output file.
	 * @param inputFile
	 *            Input wav file to encode.
	 * @param outputFile
	 *            Output file to write FLAC stream to. If file exists, it will be overwritten
	 *            without prompting.
	 * @return Status flag for encode
	 */
	public Status encode(AudioInputStream sin, File outputFile) {
		Status status = Status.FULL_ENCODE;
		outFile = outputFile;
		AudioFormat format = null;
		try {
			format = sin.getFormat();
			// sanitize and optimize configurations
			adjustConfigurations(format);
			// open stream
			openStream();
			int frameSize = format.getFrameSize();
			int sampleSize = format.getSampleSizeInBits();
			int bytesPerSample = sampleSize / 8;
			if (sampleSize % 8 != 0) {
				// end processing now
				Exception newEx = new Exception(Status.UNSUPPORTED_SAMPLE_SIZE.name());
				throw newEx;

			}
			int channels = format.getChannels();
			boolean bigEndian = format.isBigEndian();
			byte[] samplesIn = new byte[MAX_READ];
			int samplesRead;
			int framesRead;
			int[] sampleData = new int[MAX_READ * channels / frameSize];
			int unencodedSamples = 0;
			int totalSamples = 0;
			while ((samplesRead = sin.read(samplesIn, 0, MAX_READ)) != -1) {
				framesRead = samplesRead / frameSize;
				if (bigEndian) {
					for (int i = 0; i < framesRead * channels; i++) {
						int lower8Mask = 255;
						int temp = 0;
						int totalTemp = 0;
						for (int x = bytesPerSample - 1; x >= 0; x++) {
							int upShift = 8 * x;
							if (x == 0)// don't mask...we want sign
								temp = samplesIn[bytesPerSample * i + x] << upShift;
							else
								temp = (samplesIn[bytesPerSample * i + x] & lower8Mask) << upShift;
							totalTemp = totalTemp | temp;
						}
						sampleData[i] = totalTemp;
					}
				} else {
					for (int i = 0; i < framesRead * channels; i++) {
						int lower8Mask = 255;
						int temp = 0;
						int totalTemp = 0;
						for (int x = 0; x < bytesPerSample; x++) {
							int upShift = 8 * x;
							if (x == bytesPerSample - 1)// don't mask...we want sign
								temp = samplesIn[bytesPerSample * i + x] << upShift;
							else
								temp = (samplesIn[bytesPerSample * i + x] & lower8Mask) << upShift;
							totalTemp = totalTemp | temp;
						}
						sampleData[i] = totalTemp;
					}
				}
				if (framesRead > 0) {
					flac.addSamples(sampleData, framesRead);
					unencodedSamples += framesRead;
				}
				// if(unencodedSamples > blockSize*100) {
				if (useThreads) {// Thread.yield();// 
					unencodedSamples -= flac.t_encodeSamples(unencodedSamples, false, 5); // 5 describes the number of threads which are processed parallel
				}
				else
					unencodedSamples -= flac.encodeSamples(unencodedSamples, false);
				totalSamples += unencodedSamples;
			}
			totalSamples += unencodedSamples;
			if (useThreads) {
				unencodedSamples -= flac.t_encodeSamples(unencodedSamples, true, 5); // 5 describes the number of threads which are processed parallel
			}	
			else
				unencodedSamples -= flac.encodeSamples(unencodedSamples, true);
			// unencodedSamples = 0;
			lastTotalSamples = totalSamples;
		} catch (IOException e) {
			status = Status.FILE_IO_ERROR;
		} catch (Exception e) {
			status = Status.GENERAL_ERROR;
			String message = e.getMessage();
			if (message == null) {
				e.printStackTrace();
			} else if (message.equals(Status.UNSUPPORTED_SAMPLE_SIZE.name()))
				status = Status.UNSUPPORTED_SAMPLE_SIZE;
		}

		return status;
	}
}