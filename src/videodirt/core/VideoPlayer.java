package videodirt.core;

import org.freedesktop.gstreamer.Format;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.SeekFlags;
import org.freedesktop.gstreamer.SeekType;
import org.freedesktop.gstreamer.elements.PlayBin;
import processing.core.PApplet;
import processing.core.PGraphics;
import java.io.File;

public class VideoPlayer extends PlayBin implements Runnable {

    private PApplet parent;
    private Object[] args;
    private VideoClip clip;
    private VideoPlane plane;


    public VideoPlayer(PApplet parent, Object[] args, VideoPlane plane) {
        super("VideoDirt - " + System.currentTimeMillis());
        this.parent = parent;
        this.args = args;
        this.plane = plane;
    }

    @Override
    public void run() {
        if (!Gst.isInitialized() && !parent.g.isGL()) return;

        File source = VideoLibrary.getFile(args);
        if(source != null) {
            setInputFile(source);
            VideoBridgeSink videosink = new VideoBridgeSink(plane);
            setVideoSink(videosink);
            setAudioSink(null);
            play();
            getState();

            //wait until clip has been loaded
            clip = new VideoClip(this, args);

            //!! forse da rimuovere.... videoclip non piu runnable
            while (!clip.isInitialized()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    //jump frame
                }
            }

            //seek player to the provided selection
            boolean res;
            if (clip.getSpeed() > 0)
                res = seek(clip.getSpeed(), Format.TIME, SeekFlags.FLUSH,
                        SeekType.SET, (long) (clip.getStartPoint() * 1E9), SeekType.SET, (long) (clip.getEndPoint() * 1E9));
            else
                res = seek(clip.getSpeed(), Format.TIME, SeekFlags.FLUSH,
                        SeekType.SET, (long) (clip.getEndPoint() * 1E9), SeekType.SET, (long) (clip.getStartPoint() * 1E9));
            if (!res)
                PGraphics.showWarning("VideoDirt: seek operation failed");

            getState();

            //dispose player
            try {
                Thread.sleep((long) (clip.getDuration() * 1000));
            } catch (InterruptedException e) {
                dispose();
                plane.clear();
            }
            dispose();
            plane.clear();
        }
    }
}