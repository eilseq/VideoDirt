package examples;

import processing.core.PApplet;
import processing.core.PImage;
import videodirt.core.VideoDirt;

public class SimpleExample extends PApplet {
	
	public void settings() {
		size(960, 540, P3D);
	}
	
	public void setup() {
		surface.setResizable(true);
		VideoDirt.start(this);
	}

	public void draw() {
		background(0);
		VideoDirt.display();
	}
	
	public void drawFrame(PImage frame) {
		image(frame, 0, 0);
	}
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { SimpleExample.class.getName() });
	}
}