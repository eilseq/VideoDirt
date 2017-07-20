package videodirt;

import processing.core.PApplet;

public class Application extends PApplet {
	
	public void settings() {
		size(640, 360, P3D);
	}
	
	public void setup() {
		VideoDirt.init(this, "/Users/filippoguida/Desktop/VideoDirt");
		frameRate(30);
		textureMode(NORMAL);
	}

	public void draw() {
		background(0);
		VideoDirt.display();
	}
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { videodirt.Application.class.getName() });
	}
}