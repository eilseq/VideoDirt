package videodirt.core;

import org.freedesktop.gstreamer.elements.PlayBin;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

class VideoClip {

    private PlayBin player;
    private boolean initialized;

    private float cps;
    private float begin;
    private float end = 1;
    private float sustain;
    private float legato;
    private float speed = 1;            //max 4x (??)
    private String unit = "r";
    private int cut;
    private float size = 1;
    private float xpos;
    private float ypos;
    private int zpos;
    private int blendmode;
    private float opacity = 1;
    private int mirror;


    VideoClip(PlayBin player, Object[] args) {
        this.player = player;

        //decode OSC
        for (int i = 0; i < args.length; i += 2) {
            try {
                Field field = getClass().getDeclaredField(args[i].toString());
                field.set(this, args[i + 1]);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {/*do nothing*/}
        }

        initialized = true;
    }

    boolean isInitialized() {
        return initialized;
    }

    float getDuration() {
        return (sustain <= 0) ? ((end - begin) * player.queryDuration(TimeUnit.SECONDS)) + legato : sustain + legato;
    }

    float getStartPoint() {
        return begin * player.queryDuration(TimeUnit.SECONDS);
    }

    float getEndPoint() {
        return getStartPoint() + getDuration();
    }

    float getSpeed() {
        switch (unit) {
            case "r":
                return speed;
            case "c":
                return ((end - begin) * player.queryDuration(TimeUnit.SECONDS)) / (speed / cps);
            case "s":
                return ((end - begin) * player.queryDuration(TimeUnit.SECONDS)) / speed;
            default:
                return speed;
        }
    }

    /*
    public void stop() {
        super.stop();
        playing = false;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isMirror() {
        return mirror != 0;
    }

    public float getOpacity() {
        return opacity;
    }

    public float getXPos() {
        return xpos;
    }

    public float getYpos() {
        return ypos;
    }

    public float getFrameRate() {
        return getSourceFrameRate();
    }

    public int getBlendmode() {
        switch(blendmode) {
            case 0: return PConstants.BLEND;
            case 1: return PConstants.ADD;
            case 2: return PConstants.SUBTRACT;
            case 3: return PConstants.DARKEST;
            case 4: return PConstants.LIGHTEST;
            case 5: return PConstants.DIFFERENCE;
            case 6: return PConstants.MULTIPLY;
            case 7: return PConstants.SCREEN;
            case 8: return PConstants.REPLACE;
            case 9: return PConstants.EXCLUSION;
            case 10:return PConstants.OVERLAY;
            case 11:return PConstants.HARD_LIGHT;
            case 12:return PConstants.SOFT_LIGHT;
            case 13:return PConstants.DODGE;
            case 14:return PConstants.BURN;
        }
        return 0;
    }

    public void display() {
        //if (available) read();
        //parent.textureMode(PConstants.NORMAL);
        parent.tint(255, opacity*255);
        parent.noStroke();
        parent.beginShape(QUADS);
        parent.texture(this);
        parent.vertex(xpos*parent.width, ypos*parent.height, zpos, 0, 0);
        parent.vertex((xpos+1)*parent.width, ypos*parent.height, zpos, 1, 0);
        parent.vertex((xpos+1)*parent.width, (ypos+1)*parent.height, zpos, 1, 1);
        parent.vertex(xpos*parent.width, (ypos+1)*parent.height,	zpos, 0, 1);
        parent.endShape();
    }*/
}