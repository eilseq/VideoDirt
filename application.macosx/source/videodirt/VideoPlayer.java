package videodirt;

import processing.core.PApplet;

public class VideoPlayer implements Runnable {
	
	private	VideoClip	clip;
	private PApplet		parent;
	private	Object[]		args;

	public VideoPlayer(PApplet parent, Object[] args) {
		this.parent	= parent;
		this.args	= args;
	}
	
	public void run() {
		clip = new VideoClip(parent, args);	
		clip.play();
		
		try {
			Thread.sleep((long) ((long) 1000*clip.duration()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		clip.stop();	
	}
}