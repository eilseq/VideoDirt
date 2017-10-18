package examples;

import processing.core.*;
import videodirt.VideoDirt;

public class SimpleExample extends PApplet {

    VideoDirt videodirt  = new VideoDirt(this, "/Users/filippoguida/Desktop/Shadows/videoclips");
	
	public void settings() {
	    size(960, 540, P3D);
	}

	public void draw() {
        background(0);
        videodirt.display();
	}

    public void drawFrame(PImage frame, int x, int y) {
        image(frame, x, y);
    }
	
	public static void main(String _args[]) {
	    PApplet.main(new String[] { SimpleExample.class.getName() });
	}
}