package videodirt;

import org.freedesktop.gstreamer.Format;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.SeekFlags;
import org.freedesktop.gstreamer.SeekType;
import org.freedesktop.gstreamer.elements.PlayBin;
import processing.core.PGraphics;
import java.io.File;

public class VideoPlayer extends PlayBin implements Runnable {

    private VideoClip clip;
    private VideoPlane plane;
    private File source;

    public VideoPlayer(VideoClip clip, VideoPlane plane) {
        super("VideoDirt - " + System.currentTimeMillis());
        this.clip = clip;
        this.plane = plane;
        plane.connectClip(clip);
    }

    @Override
    public void run() {
        if (Gst.isInitialized()) {
            source = VideoLibrary.getFile(clip);
            if (source != null) {
                //load file
                setInputFile(source);

                //setup bridge between gstreamer and processing
                VideoBridgeSink videosink;
                videosink = new VideoBridgeSink(plane);
                setVideoSink(videosink);
                setAudioSink(null); //mute

                //play video and retrieve infos
                play();
                getState();

                //seek on provided selection
                boolean res;
                if (clip.getSpeed(this) > 0)
                    res = seek(clip.getSpeed(this), Format.TIME, SeekFlags.FLUSH,
                            SeekType.SET, (long) (clip.getStartPoint(this) * 1E9),
                            SeekType.SET, (long) (clip.getEndPoint(this) * 1E9));
                else
                    res = seek(clip.getSpeed(this), Format.TIME, SeekFlags.FLUSH,
                            SeekType.SET, (long) (clip.getEndPoint(this) * 1E9),
                            SeekType.SET, (long) (clip.getStartPoint(this) * 1E9));
                if (!res)
                    PGraphics.showWarning("VideoDirt: seek operation failed");

                getState();

                //dispose player after playback
                try {
                    Thread.sleep((long) (clip.getDuration(this) * 1000));
                } catch (InterruptedException e) {
                    dispose();
                    plane.clear();
                }
                dispose();
                plane.clear();
            }
        }
    }
}