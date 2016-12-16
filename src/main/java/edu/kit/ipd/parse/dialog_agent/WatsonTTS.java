package edu.kit.ipd.parse.dialog_agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;

import edu.kit.ipd.parse.dialog_agent.tools.ConfigManager;

public class WatsonTTS {

	Properties props;

	// Property constants
	private final String USERNAME_PROP = "USERNAME";
	private final String PASSWORD_PROP = "PASSWORD";

	private final TextToSpeech service;

	public WatsonTTS() {
		this.service = new TextToSpeech();
		props = ConfigManager.getConfiguration(getClass());
		service.setUsernameAndPassword(props.getProperty(USERNAME_PROP), props.getProperty(PASSWORD_PROP));
	}

	public void synthesizeQuestion(String question) {
		AudioFormat audioFormat = new AudioFormat(HttpMediaType.AUDIO_FLAC);
		InputStream inputStream = (InputStream) service.synthesize(question, Voice.EN_MICHAEL, audioFormat).execute();
		
		// Change path to relative path or replace this with audio output instead of saving a file - see voice_recorder
		File audio = new File("/Users/Mario/Dialogmanager/audio/output.flac");

		OutputStream outStream = null;              
		try {
		    outStream = new FileOutputStream(audio);

		    byte[] buffer = new byte[8 * 1024];
		    int bytesRead;
		    while ((bytesRead = inputStream.read(buffer)) != -1) {
		        outStream.write(buffer, 0, bytesRead);
		    }
		} catch (Exception e) {
		    System.out.println(e.getMessage());
		} finally {
		    IOUtils.closeQuietly(inputStream);
		    IOUtils.closeQuietly(outStream);
		    audio = null;
		}
	}
}
