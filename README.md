# VideoDirt

## Experimental Video Playback

##### Filippo Guida, 2016 | [filippoguida.cc](filippoguida.cc)

https://www.youtube.com/watch?v=QE-IodiN2-A

## Overview

VideoDirt is a software application enabling users to the create and manipulate video contents in realtime through pattern-oriented live coding languages. Written in Java it is based on the Processing library, that can be also used as external library in Processing sketches. It is capable of receiving OSC messages coming from Tidal language and use them to mix and manipulate video contents from a media-library. The video decoder is based on GStreamer, a cross-platform native library that allow the application to decode any kind of video codec and work at best performances.

## Functionalities

VideoDirt functionalities are made in order to limit language changes to a minimum. So it offer basically the
same parameters to each event, without the ones related to audio effects:

- **s**: name of the pointed folder
- **n**: index of the pointed sample
- **sustain**: the duration of the sound in seconds. If don't set, will be used the natural duration of sample.
- **begin**: skips the beginning of each sample, shortening them.
- **end**: cuts the end of samples, shortening them.
- **legato**: inter-onset time between events, in relation to sustain. If don't set, will be played the whole sample.
- **cut**: set the cut-group. Every sample the same group will be forced stopped.
- **speed**: speed of sample player
- **unit**: controls how the speed parameter is interpreted. (see Tidal reference)

The presence of this basic parameters allow VideoDirt to address messages coming from the sample transformer functions
provided by Tidal, applying the same effect to video clips:

- **loopAt**: makes sample fit the given number of cycles.
- **chop**: turn a pattern of samples into a pattern of sample parts.
- **gap**: similar to chop, but only the sample in place is played.
- **striate**: kind of granulator, cutting samples into bits and interlacing it together.
- **striateL**: just like striate, but also loops each sample chunk a number of times specified.
- **stut**: applies a type of delay to a pattern.

In addition to the parameters mentioned above, more have been added to manage basic video manipulations in order to
manage the presence of multiple clips simultaneously. All the videos are printed as texture of plane shapes inside a
3D environment, so each clip has 2 parameter to manage the size:

- **xsize**: horizontal size of clip, normalized between 0 and 1 in relation to the window size.
- **ysize**: vertical size of clip, normalized between 0 and 1 in relation to the window size.

and 3 parameters to manage its position:

- **xpos**: horizontal position of clip inside the window, normalized between -1 and 1 from the center.
- **ypos**: vertical position of clip inside the window, normalized between -1 and 1 from the center.
- **zpos**: Z-axis position, not normalized. Useful to manage video layers and create zoom in/out effects.

There are also some features that provide color, light and compositing effects needs to mix together all
the clips in place. This list is still growing:

- **opacity**: describes the transparency-level, where 1 is not transparent at all and 0 is completely transparent.
- **exposure**: set the amount of light per unit area, normalized between 0 and 1.
- **cutcolor**: cut a selected color from the video. Useful to cut subject from a green screen.
- ...

## Classes

The whole software is made three main classes, that represents the Processing library:

### VideoLibrary:

A static class that collect all the videoclips file patches and provide it to the OSC interpreter:

```{.java}
public class VideoLibrary {
  ...
  public static void load (String library_dir) {
    ...
	}

	public static String getFilename (Object[] osc_args) {
    ...
	}
  ...
}
```

### VideoClip:

This class offer all the base video functionalities, inherited from the standard Movie (processing.video),
and decode the incoming OSC-message in order to set the next clip in place as described.
The application create a VideoClip instance for each incoming message, that will be disposed once playback has finished.

```{.java}
public class VideoClip extends Movie {
...
	VideoClip(PApplet parent, Object[] osc_args) {
		super(parent, VideoLibrary.getFilename(osc_args));
		decodeOSC(args);
	}
  ...
	public void display() {
		...
    //The display function performed in PApplet context
    ...
	}
}
```

### VideoPlayer:

This class provide multi-threading functionalities to manage the high frequency of VideoClip creation.
Each VideoPlayer instance create and manage a single VideoClip instance in a different thread where are performed all
the operation directly connected to messages interpretation and clip setup, in order to save resources and increase performances:

```{.java}
public class VideoPlayer implements Runnable {
...
	public void run() {
		clip = new VideoClip(parent, args);
		clip.play();
    ...
    Thread.sleep(clip.duration());
    ...
		clip.stop();
	}
}
```

### VideoDirt:

A singleton class collecting all the functionalities. This one can be imported into a Processing sketch as external library
and used to print all the video clips alongside other graphic materials defined inside the sketch itself:

```{.java}
import videodirt.VideoDirt;

public void settings() {
	size(640, 360, P3D);      //be sure to set P3D renderer
}

public void setup() {
	VideoDirt.init(this, "./dirt-samples");
	textureMode(NORMAL);      //be sure to set normal texture mode
}

public void draw() {
	background(0);
	VideoDirt.display();

	/* perform other graphic operations here */
}
```

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Version

1.0.0

## Authors

- **Filippo Guida** - _Initial work_ - [filippoguida.cc](filippoguida.cc)

See also the list of [contributors](https://github.com/filippoguida/VideoDirt/contributors) who participated in this project.

## License

This project is licensed under the GNU General Public License v 3.0 - see the [LICENSE.md](LICENSE.md) file for details
