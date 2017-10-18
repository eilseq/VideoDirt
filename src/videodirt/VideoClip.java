package videodirt;

import org.freedesktop.gstreamer.elements.PlayBin;
import processing.core.PConstants;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

class VideoClip implements PConstants {

    private float cps;
    private String s;
    private int n;
    private float begin;
    private float end = 1;
    private float sustain;
    private float legato;
    private int cut;
    private float speed = 1;            //max 4x (??)
    private String unit = "r";
    private float size = 1;
    private float xpos;
    private float ypos;
    private int blendmode;
    private float opacity = 1;


    VideoClip(Object[] args) {
        //decode OSC
        for (int i = 0; i < args.length; i += 2) {
            try {
                //set clip fields
                Field field = getClass().getDeclaredField(args[i].toString());
                field.set(this, args[i + 1]);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {/*do nothing*/}
        }
    }

    String getDir() {
        return s;
    }

    int getNum() {
        return n;
    }

    float getDuration(PlayBin player) {
        if (sustain <= 0)
            return ((end - begin) * player.queryDuration(TimeUnit.SECONDS)) + legato;
        else
            return sustain + legato;
    }

    float getStartPoint(PlayBin player) {
        return begin * player.queryDuration(TimeUnit.SECONDS);
    }

    float getEndPoint(PlayBin player) {
        return getStartPoint(player) + getDuration(player);
    }

    float getSpeed(PlayBin player) {
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

    float getSize() {
        return size;
    }

    float getOpacity() {
        return opacity;
    }

    float getXPos() {
        return xpos;
    }

    float getYpos() {
        return ypos;
    }

    int getBlendmode() {
        switch(blendmode) {
            case 0: return BLEND;
            case 1: return ADD;
            case 2: return SUBTRACT;
            case 3: return DARKEST;
            case 4: return LIGHTEST;
            case 5: return DIFFERENCE;
            case 6: return MULTIPLY;
            case 7: return SCREEN;
            case 8: return REPLACE;
            case 9: return EXCLUSION;
            case 10:return OVERLAY;
            case 11:return HARD_LIGHT;
            case 12:return SOFT_LIGHT;
            case 13:return DODGE;
            case 14:return BURN;
        }
        return 0;
    }
}