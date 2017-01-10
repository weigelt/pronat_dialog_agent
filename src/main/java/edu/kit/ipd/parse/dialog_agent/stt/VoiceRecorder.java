package edu.kit.ipd.parse.dialog_agent.stt;
/*
*
* Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
*
* Sun grants you ("Licensee") a non-exclusive, royalty free,
* license to use, modify and redistribute this software in 
* source and binary code form, provided that i) this copyright
* notice and license appear on all copies of the software; and 
* ii) Licensee does not utilize the software in a manner
* which is disparaging to Sun.
*
* This software is provided "AS IS," without a warranty
* of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS
* AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE 
* HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR 
* ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
* OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT
* WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
* OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
* INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS
* OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY
* TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY
* OF SUCH DAMAGES.

This software is not designed or intended for use in on-line
control of aircraft, air traffic, aircraft navigation or
aircraft communications; or in the design, construction,
operation or maintenance of any nuclear facility. Licensee 
represents and warrants that it will not use or redistribute 
the Software for such purposes.
*/

/*  The above copyright statement is included because this 
* program uses several methods from the JavaSoundDemo
* distributed by SUN. In some cases, the sound processing methods
* unmodified or only slightly modified.
* All other methods copyright Steve Potts, 2002
*/

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import edu.kit.ipd.parse.dialog_agent.tools.AbsolutePath;
import edu.kit.ipd.parse.dialog_agent.tools.ConfigManager;

/**
 * SimpleSoundCapture Example. This is a simple program to record sounds and
 * play them back. It uses some methods from the CapturePlayback program in the
 * JavaSoundDemo. For licensizing reasons the disclaimer above is included.
 * 
 * @author Steve Potts
 */
public class VoiceRecorder extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1109123200624785351L;

	Properties props = ConfigManager.getConfiguration(getClass());
	
	int resolution = Integer.parseInt(props.getProperty("RESOLUTION"));

	AbsolutePath absolutePath = new AbsolutePath(props.getProperty("PATH"));
	
	String absoluteFilePath = absolutePath.getAbsolutePathFileWithTimestamp("answer", "flac");
	
	VoiceListeningThread voiceListeningThread = new VoiceListeningThread(absoluteFilePath, resolution);
	
	JButton captB;

	JTextField textField;

	public VoiceRecorder() {
		setLayout(new BorderLayout());
		SoftBevelBorder sbb = new SoftBevelBorder(SoftBevelBorder.LOWERED);
		setBorder(new EmptyBorder(5, 5, 5, 5));

		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

		JPanel p2 = new JPanel();
		p2.setBorder(sbb);
		p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder(new EmptyBorder(10, 0, 5, 0));
		captB = addButton("Record", buttonsPanel, true);
		p2.add(buttonsPanel);

		p1.add(p2);
		add(p1);
	}

	private JButton addButton(String name, JPanel p, boolean state) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		b.setEnabled(state);
		p.add(b);
		return b;
	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj.equals(captB)) {
			if (captB.getText().startsWith("Record")) {
				voiceListeningThread.start();
				captB.setText("Stop");
			} else {
				voiceListeningThread.stop();
			}
		}
	}

	public Path getAnswer() {
		VoiceRecorder vc = new VoiceRecorder();
		JFrame f = new JFrame("Capture/Playback");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add("Center", vc);
		f.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = 360;
		int h = 170;
		f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
		f.setSize(w, h);
		f.show();
		File audioFile = new File(absoluteFilePath);
		while(!audioFile.exists()) {
			// wait till an audio file containing the answer is created
		}
		f.dispose();
		Path path = Paths.get(absoluteFilePath);
		return path;
	}
}