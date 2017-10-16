package videodirt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.freedesktop.gstreamer.Gst;
import oscP5.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class VideoDirt {

    //GStreamer initialization
    static {
        Gst.init();
    }

    //class definition
    private static final int MAX_OVERLAP = 4;
    private static VideoPlane[] videoplanes = new VideoPlane[MAX_OVERLAP];
    private OscP5 osc_receiver;
    private PApplet parent;
    private Method drawFrameMethod;
    public VideoDirt(PApplet parent, OscP5 osc_receiver) {
        //set instance field
        this.parent = parent;
        this.osc_receiver = osc_receiver;

        //create videoplanes
        for (int i = 0; i < MAX_OVERLAP; i++) videoplanes[i] = new VideoPlane();

        //get drawFrame method declared in PApplet
        try {
            drawFrameMethod = parent.getClass().getDeclaredMethod("drawFrame", PImage.class);
            drawFrameMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            PGraphics.showWarning("drawFrame(PImage frame) method missing");
        }
    }

    //singleton initialization
    private static VideoDirt instance;

    public static void start(PApplet parent) {
        //folder ./data by default
        VideoDirt.start(parent, "./data");
    }

    public static void start(PApplet parent, String library_dir) {
        //port 7772 by default
        VideoDirt.start(parent, library_dir, 7772);
    }

    public static void start(PApplet parent, String library_dir, int udpPort) {
        //localhost address by default
        VideoDirt.start(parent, library_dir, "127.0.0.1", udpPort);
    }

    public static void start(PApplet parent, String library_dir, String netAddress, int udpPort) {
        if (instance == null) {
            instance = new VideoDirt(parent, new OscP5(parent, netAddress, udpPort, OscP5.UDP));
            instance.osc_receiver.addListener(new OscEventListener() {
                @Override
                public void oscEvent(OscMessage msg) {
                    if (msg.checkAddrPattern("/playVideo") && VideoLibrary.loaded()) {
                        int index = playerCount.get() % MAX_OVERLAP;
                        playerPool.execute(new VideoPlayer(instance.parent, msg.arguments(), videoplanes[index]));
                    }
                }

                @Override
                public void oscStatus(OscStatus theStatus) {
                    System.out.println(theStatus);
                }
            });
        }

        if (!VideoLibrary.loaded())
            VideoLibrary.load(library_dir);
    }


    //concurrency rules and static buffers definition
    private static final Semaphore playerSemaphore = new Semaphore(MAX_OVERLAP);
    private static final BlockingQueue<Runnable> playerQueue = new ArrayBlockingQueue<>(MAX_OVERLAP);
    private static AtomicInteger playerCount = new AtomicInteger();
    private static final ThreadPoolExecutor playerPool = new ThreadPoolExecutor(1, MAX_OVERLAP, 50, TimeUnit.MILLISECONDS, playerQueue) {
        public void execute(Runnable r) {
            try {
                playerSemaphore.acquire();
                playerCount.incrementAndGet();
                super.execute(r);
            } catch (InterruptedException e) {/*do nothing*/}
        }

        public void afterExecute(Runnable r, Throwable t) {
            playerSemaphore.release();
            super.afterExecute(r, t);
        }
    };

    //static methods
    public static void display() {
        try {

            for (int i = 0; i < MAX_OVERLAP; i++)
                if (videoplanes[i].isActive())
                    instance.drawFrameMethod.invoke(instance.parent, videoplanes[i]);

        } catch (NullPointerException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            //jump VideoDirt operations
        }
    }


}