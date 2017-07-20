package videodirt;

import oscP5.*;
import processing.core.PApplet; 

public class VideoDirt {
	
	private static VideoDirt instance;

	OscP5	osc_receiver	 = new OscP5(this, 57120);
	PApplet	parent;
	
	public VideoDirt() {}
	
	public void oscEvent( OscMessage msg ) {
		if (msg.checkAddrPattern("/play2") && VideoLibrary.loaded()) 
			(new Thread(new VideoPlayer(parent, msg.arguments()))).start();
	}

    public static VideoDirt init(PApplet parent, String library_dir){
        if (instance == null){ //if there is no instance available... create new one
        		instance = new VideoDirt();
        		instance.parent = parent;
        		VideoLibrary.load(library_dir);
        }

        return instance;
    }

	public static void display() {
		VideoClip.displayAll();
	}
}