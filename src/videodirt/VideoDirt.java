package videodirt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.*;

import org.freedesktop.gstreamer.Gst;
import oscP5.*;
import processing.core.*;

public class VideoDirt {

    private static final String DEFAULT_IP_ADDRESS = "127.0.0.1";
    private static final int DEFAULT_OSC_PORT = 7772;
    private static final String DEFAULT_LIB_DIR = "./data";
    private static final int MAX_OVERLAP = 4;

    private static final ThreadPoolExecutor playerPool;
    static {
        //initialize gstreamer
        Gst.init();

        //set concurrency rules (reject tasks after max player overlap)
        playerPool = new ThreadPoolExecutor(1, MAX_OVERLAP,60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());

        //define rejected task behaviour
        playerPool.setRejectedExecutionHandler((r, executor) ->
                PGraphics.showWarning("VideoDirt: task rejected, event frequency too high "));
    }

    //singleton
    private static VideoDirt instance;
    private PApplet parent;
    private String lib_dir;
    private OscP5 osc_receiver;
    private ArrayList<VideoPlane> videoplanes;
    private int plane_token;
    private Method drawFrameMethod;

    public VideoDirt(PApplet parent) {
        this(parent, DEFAULT_LIB_DIR);
    }

    public VideoDirt(PApplet parent, String lib_dir) {
        this(parent, lib_dir, new OscP5(parent, DEFAULT_IP_ADDRESS, DEFAULT_OSC_PORT, OscP5.UDP));
    }

    public VideoDirt(PApplet parent, String lib_dir, OscP5 osc_receiver) {
        if(instance == null) {
            this.parent = parent;
            this.lib_dir = lib_dir;
            this.osc_receiver = osc_receiver;

            //initialize unique instance
            init();
            instance = this;
        } else {
            PGraphics.showWarning("VideoDirt: unique instance already defined");
        }
    }

    private void init() {
        //create empty video-planes
        videoplanes = new ArrayList<>();
        for(int i = 0; i < MAX_OVERLAP; i++) videoplanes.add(new VideoPlane());

        //load library
        if (!VideoLibrary.loaded())
            VideoLibrary.load(lib_dir);

        //get drawFrame method declared in PApplet
        try {
            drawFrameMethod = parent.getClass().getDeclaredMethod("drawFrame", PImage.class, int.class, int.class);
            drawFrameMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            //report issue and exit
            PGraphics.showMethodWarning("VideoDirt: method 'drawFrame(PImage frame, int x, int y)' must be defined");
            parent.exit();
        }

        //define stop() method
        parent.registerMethod("stop", this);

        //set osc listener
        osc_receiver.addListener(new OscEventListener() {
            //every osc event on root /playVideo launch a new video player instance
            @Override
            public void oscEvent(OscMessage msg) {
                if (msg.checkAddrPattern("/playVideo")) {
                    VideoClip clip = new VideoClip(msg.arguments());
                    VideoPlane plane = videoplanes.get(plane_token % MAX_OVERLAP);
                    playerPool.execute(new VideoPlayer(clip, plane));
                    plane_token++;
                }
            }
            @Override
            public void oscStatus(OscStatus theStatus) {
                System.out.println(theStatus);
            }
        });
    }

    public void display() {
        //add graphics
        videoplanes.forEach((plane) -> {
            if (plane.isActive()) {
                //evaluate incoming graphics changes
                VideoClip clip = plane.getConnectedClip();

                //resize
                int width = (int)(parent.displayWidth*clip.getSize());
                int height = (int)(parent.displayHeight*clip.getSize());
                plane.resize(width, height);

                //set blendmode & opacity
                parent.blendMode(clip.getBlendmode());
                parent.tint(255, clip.getOpacity()*255);

                //launch drawFrame in parent context
                try {
                    drawFrameMethod.invoke(parent,
                            plane,
                            (int)(parent.displayWidth*clip.getXPos()),
                            (int)(parent.displayHeight*clip.getYpos())
                    );
                } catch (NullPointerException
                        | IllegalAccessException
                        | IllegalArgumentException
                        | InvocationTargetException e) {
                    //jump operations
                }
            }
        });

        //restore blendmode & opacity
        parent.blendMode(PConstants.NORMAL);
    }

    public void stop() throws InterruptedException {
        Gst.quit();
        playerPool.shutdown();
        playerPool.awaitTermination(1L, TimeUnit.SECONDS);
    }
}