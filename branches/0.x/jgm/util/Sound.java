package jgm.util;

import java.util.logging.*;
import java.net.URI;
import java.net.URL;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

/**
 * This handles playing a sound file via javax.sound.*.
 * It checks to see if the user has provided a custom
 * sound or else uses the bundled resource.
 * 
 * @author Tim
 * @since 0.16
 */
// see http://www.anyexample.com/programming/java/java_play_wav_sound_file.xml
public class Sound extends Thread {
	static Logger log = Logger.getLogger(SoundPlayer.class.getName());
	
	public enum File {
		WHISPER        ("Whisper.wav"),
		GM_WHISPER     ("GMWhisper.wav"),
		BEING_ATTACK   ("PlayerAttack.wav"),
		BEING_FOLLOWED ("PlayerNear.wav"),
		STOP           ("GlideStop.wav");
		
		private URI uri;
		
		/**
		 * Construct a sound file.
		 * @param name The filename of the sound file
		 * @param len The duration of the sound in ms
		 */
		private File(String name) {
			java.io.File f = new java.io.File("sounds/" + name);
			
			try {
				uri = f.exists() ? f.toURI() :
					jgm.JGlideMon.class.getResource("resources/sounds/" + name).toURI();
			} catch (java.net.URISyntaxException e) {				
				log.log(Level.WARNING, "Exception instantiating SoundPlayer.File", e);
				System.exit(-1);
			}
		}
		
		/**
		 * Play the sound.
		 */
		public void play() {
			play(true);
		}
		
		/**
		 * Play the sound
		 * @param wait Whether to wait for the sounnd to finish playing before returning
		 */
		public void play(boolean wait) {
			Thread t = new Sound(uri);
			t.start();
			
			if (wait) {
				try {
					t.join();
				} catch (InterruptedException e) {}
			}
		}
	};
	
	
	
	private URL url;
 
	private Pan curPosition;
 
	private static final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
 
	enum Pan {
		LEFT, RIGHT, NORMAL
	};
 
	public Sound(URI uri) {
		super("Sound:" + uri.toString());
		
		try {
			this.url = uri.toURL();
		} catch (java.net.MalformedURLException e) {
			log.log(Level.WARNING, "Exception instantiating SoundPlayer", e);
			System.exit(-1);
		}
		
		curPosition = Pan.NORMAL;
	}
 
	public void run() {
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(url);
		} catch (Throwable e) {
			log.log(Level.WARNING, "Exception playing sound file", e);
			return;
		}
 
		AudioFormat format = audioInputStream.getFormat();
		SourceDataLine auline = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
 
		try {
			auline = (SourceDataLine) AudioSystem.getLine(info);
			auline.open(format);
		} catch (Throwable e) {
			log.log(Level.WARNING, "Exception playing sound file", e);
			return;
		}
 
		if (curPosition != Pan.NORMAL && auline.isControlSupported(FloatControl.Type.PAN)) {
			FloatControl pan = (FloatControl) auline
					.getControl(FloatControl.Type.PAN);
			if (curPosition == Pan.RIGHT)
				pan.setValue(1.0f);
			else if (curPosition == Pan.LEFT)
				pan.setValue(-1.0f);
		} 
 
		auline.start();
		int nBytesRead = 0;
		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
 
		try {
			while (nBytesRead != -1) {
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
				if (nBytesRead >= 0)
					auline.write(abData, 0, nBytesRead);
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "Exception playing sound file", e);
		} finally {
			auline.drain();
			auline.close();
		}
	}
}
