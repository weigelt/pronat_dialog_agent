package edu.kit.ipd.parse.dialog_agent.tts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;

import edu.kit.ipd.parse.dialog_agent.tools.AbsolutePath;
import edu.kit.ipd.parse.dialog_agent.tools.ConfigManager;
import net.sourceforge.javaflacencoder.FLAC_FileEncoder;

public class WatsonTTS {

	private Properties props;

	// Property constants
	private final String USERNAME_PROP = "USERNAME";
	private final String PASSWORD_PROP = "PASSWORD";
	private final String VOICE_PROP = "VOICE";
	private final String PATH_PROP = "PATH";

	private final TextToSpeech service;

	public WatsonTTS() {
		this.service = new TextToSpeech();
		props = ConfigManager.getConfiguration(getClass());
		service.setUsernameAndPassword(props.getProperty(USERNAME_PROP), props.getProperty(PASSWORD_PROP));
	}

	public void synthesizeQuestion(String question) {
		AudioFormat audioFormat = new AudioFormat(HttpMediaType.AUDIO_WAV);
		InputStream inputStream = (InputStream) service.synthesize(question, Voice.getByName(props.getProperty(VOICE_PROP)), audioFormat).execute();
		InputStream inStream = null;
		OutputStream outStream = null;

		AbsolutePath absolutePath = new AbsolutePath(props.getProperty(PATH_PROP));
		String audioFilePath = absolutePath.getAbsolutePathFileWithTimestamp("question", "wav");
		File audioFile = new File(audioFilePath);
		
		try {
			inStream = WaveUtils.reWriteWaveHeader(inputStream);  
		    outStream = new FileOutputStream(audioFile);

		    byte[] buffer = new byte[8 * 1024];
		    int bytesRead;
		    while ((bytesRead = inStream.read(buffer)) != -1) {
		        outStream.write(buffer, 0, bytesRead);
		    }
		    // express inputstream
		    ExpressTTS expressTTS = new ExpressTTS(audioFilePath);
		    
		    // optional: create a flac-file with the question (a wav file is already created)
		    FLAC_FileEncoder flacEncoder = new FLAC_FileEncoder();
		    File inputFile = new File(audioFilePath);
		    AbsolutePath absolutePathFlac = new AbsolutePath(props.getProperty(PATH_PROP));
			String audioFilePathFlac = absolutePathFlac.getAbsolutePathFileWithTimestamp("question", "flac");
			File outputFile = new File(audioFilePathFlac);
			flacEncoder.encode(inputFile, outputFile);   
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    IOUtils.closeQuietly(inStream);
		    IOUtils.closeQuietly(outStream);
		    audioFile = null;
		}	
	}
}
