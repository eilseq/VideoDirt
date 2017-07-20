package videodirt;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import processing.core.PApplet;
import processing.video.Movie;

public class VideoClip extends Movie {

	private	static	List<VideoClip>	running_clips =	new CopyOnWriteArrayList<VideoClip>();

	private float	cps;
	
	private float	begin;
	private float	end		= 1;
	private float	sustain;
	private float	legato	= 1;
	private int		cut;
	
	private float	speed	= 1;
	private char 	unit		= 'r';
	
	private float	xsize	= 1;
	private float	ysize	= 1;
	private float	xpos	;
	private float	ypos	;
	private float	zpos;

	//private float	exposure	= 1;
	private float 	opacity	= 1;	

	
	VideoClip(PApplet parent, Object[] args) {
		super(parent, VideoLibrary.getFilename(args));
		decodeOSC(args);
	}
		
	private void decodeOSC(Object[] args) {  //decode OSC arguments and set relative parameters
		for (int i=0; i < args.length; i++) {
			try {
				Field field = getClass().getDeclaredField(args[i].toString());
			    field.set(this, args[i+1]);
			}
			catch (Exception e) { /*do nothing on unrecognized field*/ }
		}
	}	
	
	public void play() {
		super.play();
		
		if (sustain <= 0) sustain = super.duration();
		begin = begin*sustain*legato;
		end = end*sustain*legato;
		jump(begin);
		
		switch(unit) {
			case 'r': speed(speed);
			break;
			case 'c': speed((speed/cps)/super.duration());
			break;
			case 's': speed(speed/super.duration());
			break;
		}

		running_clips.add(this);
		if (cut != 0) {
			running_clips.removeIf(clip_to_check -> {
				if (clip_to_check != this && clip_to_check.cut == cut) clip_to_check.dispose();
				return (clip_to_check != this && clip_to_check.cut == cut);
			});
		}
	}
	
	public void stop() {
		super.stop();
		running_clips.remove(this);
		this.dispose();
	}
	
	public float duration() {
		return end-begin;
	}
		
	public void display() {
		if (available) read();
		parent.tint(255, opacity*255);
		parent.noStroke();	   
		parent.beginShape(QUADS);
		parent.texture(this);
		parent.vertex(xpos*parent.width,			ypos*parent.height, 			zpos, 0, 0);
		parent.vertex((xpos+xsize)*parent.width,	ypos*parent.height, 			zpos, 1, 0);
		parent.vertex((xpos+xsize)*parent.width,	(ypos+ysize)*parent.height,	zpos, 1, 1);
		parent.vertex(xpos*parent.width, 		(ypos+ysize)*parent.height,	zpos, 0, 1);
		parent.endShape();
	}
	
	public static void displayAll() {
		running_clips.forEach(video -> video.display());
	}
	
	public static boolean isRunning(VideoClip clip) {
		return running_clips.contains(clip);
	}
}