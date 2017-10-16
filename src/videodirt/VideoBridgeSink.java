package videodirt;

import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.lowlevel.GstBinAPI;
import org.freedesktop.gstreamer.lowlevel.GstNative;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

class VideoBridgeSink extends Bin {

    //define pixel format in order to improve performances
    private final static String DEFAULT_CAPS;
    static {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            DEFAULT_CAPS = "video/x-raw, format=BGRx";
        } else {
            DEFAULT_CAPS = "video/x-raw, format=xRGB";
        }
    }

    //create pipeline
    private VideoPlane plane;
    private AppSink sink;
    private AppSinkListener listener = new AppSinkListener();

    VideoBridgeSink(VideoPlane plane) {
        super();
        this.plane = plane;

        //enable new_sample signal and connect a listener to it
        sink = (AppSink) ElementFactory.make("appsink", "sink");
        sink.set("emit-signals", true);
        sink.set("sync", true);
        sink.connect(listener);

        //convert frame input into 32bit RGB
        Element conv = ElementFactory.make("videoconvert", "Video Converter");
        Element capsfilter = ElementFactory.make("capsfilter", "Caps Filter");
        capsfilter.setCaps(new Caps(DEFAULT_CAPS));
        addMany(conv, capsfilter, sink);
        Element.linkMany(conv, capsfilter, sink);

        //link the ghost pads on the bin to the sink pad on the converter
        addPad(new GhostPad("sink", conv.getStaticPad("sink")));
    }

    //create sample listenere in order to provide a bridge between gstreamer and processing
    private class AppSinkListener implements AppSink.NEW_SAMPLE {
        @Override
        public FlowReturn newSample(AppSink appSink) {
            Sample gst_sample = appSink.pullSample();
            Structure capsStruct = gst_sample.getCaps().getStructure(0);
            int width = capsStruct.getInteger("width");
            int height = capsStruct.getInteger("height");

            Buffer gst_buffer = gst_sample.getBuffer();
            ByteBuffer byte_buffer = gst_buffer.map(false);
            IntBuffer int_buffer = byte_buffer.asIntBuffer();

            plane.init(int_buffer, width, height);

            gst_buffer.unmap();
            gst_sample.dispose();
            return FlowReturn.OK;
        }
    }
}