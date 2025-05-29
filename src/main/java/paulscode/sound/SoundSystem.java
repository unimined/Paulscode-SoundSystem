/*
 * The SoundSystem License:
 *
 * You are free to use this library for any purpose, commercial or otherwise.
 * You may modify this library or source code, and distribute it any way you
 * like, provided the following conditions are met:
 *
 * 1) You may not falsely claim to be the author of this library or any
 *    unmodified portion of it.
 * 2) You may not copyright this library or a modified version of it and then
 *    sue me for copyright infringement.
 * 3) If you modify the source code, you must clearly document the changes
 *    made before redistributing the modified source code, so other users know
 *    it is not the original code.
 * 4) You are not required to give me credit for this library in any derived
 *    work, but if you do, you must also mention my website:
 *    https://www.paulscode.com
 * 5) I the author will not be responsible for any damages (physical,
 *    financial, or otherwise) caused by the use if this library or any part
 *    of it.
 * 6) I the author do not guarantee, warrant, or make any representations,
 *    either expressed or implied, regarding the use of this library or any
 *    part of it.
 *
 * Author: Paul Lamb
 * https://www.paulscode.com
 */

package paulscode.sound;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import javax.sound.sampled.AudioFormat;

/**
 * The SoundSystem class is the core class for the SoundSystem library.  It is
 * capable of interfacing with external sound library and codec library
 * plugins.  This core class is stripped down to give it a smaller memory
 * footprint and to make it more customizable.  This library was created to
 * provide a simple, common interface to a variety of 3rd-party sound and codec
 * libraries, and to simplify switching between them on the fly.  If no
 * external plugins are loaded, this core class by itself is only capable of
 * playing MIDI files.  Specific implementations (such as SoundSystemJPCT) will
 * extend this core class.  They will automatically link with popular
 * external plugins and provide extra methods for ease of use.
 * There should be only one instance of this class in any program!  The
 * SoundSystem can be constructed by defining which sound library to use, or by
 * allowing SoundSystem to perform its own library compatibility checking.  See
 * {@link SoundSystemConfig SoundSystemConfig} for information
 * about changing default settings and linking with external plugins.
 */
@SuppressWarnings({"unused", "ThrowableNotThrown", "UnusedReturnValue"})
public class SoundSystem {
	/**
	 * Used to return a current value from one of the synchronized
	 * boolean-interface methods.
	 */
	private static final boolean GET = false;

	/**
	 * Used to set the value in one of the synchronized boolean-interface methods.
	 */
	private static final boolean SET = true;

	/**
	 * Used when a parameter for one of the synchronized boolean-interface methods
	 * is not applicable.
	 */
	private static final boolean XXX = false;

	/**
	 * Processes status messages, warnings, and error messages.
	 */
	protected SoundSystemLogger logger;

	/**
	 * Handle to the active sound library.
	 */
	protected Library soundLibrary;

	/**
	 * List of queued commands to perform.
	 */
	protected List<CommandObject> commandQueue;

	/**
	 * Used internally by SoundSystem to keep track of play/pause/stop/rewind
	 * commands.  This prevents source management (culling and activating) from
	 * being adversely affected by the quickPlay, quickStream, and backgroundMusic
	 * methods.
	 */
	private List<CommandObject> sourcePlayList;

	/**
	 * Processes queued commands in the background.
	 */
	protected CommandThread commandThread;

	/**
	 * Generates random numbers.
	 */
	public Random randomNumberGenerator;

	/**
	 * Name of this class.
	 */
	protected String className = "SoundSystem";

	/**
	 * Indicates the currently loaded sound-library, or null if none.
	 */
	private static Class<?> currentLibrary = null;

	/**
	 * Becomes true when the sound library has been initialized.
	 */
	private static boolean initialized = false;

	/**
	 * Indicates the last exception that was thrown.
	 */
	private static SoundSystemException lastException = null;

	/**
	 * Constructor: Create the sound system using the default library.  If the
	 * default library is not compatible, another library type will be loaded
	 * instead, in the order of library preference.
	 * See {@link SoundSystemConfig SoundSystemConfig} for
	 * information about sound library types.
	 */
	public SoundSystem() {
		// create the message logger:
		logger = SoundSystemConfig.getLogger();
		// if the user didn't create one, then do it now:
		if (logger == null) {
			logger = new SoundSystemLogger();
			SoundSystemConfig.setLogger(logger);
		}

		linkDefaultLibrariesAndCodecs();

		LinkedList<Class<?>> libraries = SoundSystemConfig.getLibraries();

		if (libraries != null) {
			ListIterator<Class<?>> i = libraries.listIterator();
			Class<?> c;
			while (i.hasNext()) {
				c = i.next();
				try {
					init(c);
					return;
				} catch (SoundSystemException sse) {
					logger.printExceptionMessage(sse, 1);
				}
			}
		}
		try {
			init(Library.class);
		} catch (SoundSystemException sse) {
			logger.printExceptionMessage(sse, 1);
		}
	}

	/**
	 * Constructor: Create the sound system using the specified library.
	 *
	 * @param libraryClass Library to use.
	 *                     See {@link SoundSystemConfig SoundSystemConfig} for
	 *                     information about choosing a sound library.
	 */
	public SoundSystem(Class<?> libraryClass) throws SoundSystemException {
		// create the message logger:
		logger = SoundSystemConfig.getLogger();
		// if the user didn't create one, then do it now:
		if (logger == null) {
			logger = new SoundSystemLogger();
			SoundSystemConfig.setLogger(logger);
		}
		linkDefaultLibrariesAndCodecs();

		init(libraryClass);
	}

	/**
	 * Links with any default libraries or codecs should be made in this method.
	 * This method is empty in the core SoundSystem class, and should be overridden
	 * by classes which extend SoundSystem.
	 * See {@link SoundSystemConfig SoundSystemConfig} for
	 * information about linking with sound libraries and codecs.
	 */
	protected void linkDefaultLibrariesAndCodecs() {
	}

	/**
	 * Loads the message logger, initializes the specified sound library, and
	 * starts the command thread.  Also instantiates the random number generator
	 * and the command queue.
	 *
	 * @param libraryClass Library to initialize.
	 *                     See {@link SoundSystemConfig SoundSystemConfig} for
	 *                     information about choosing a sound library.
	 */
	protected void init(Class<?> libraryClass) throws SoundSystemException {
		message("", 0);
		message("Starting up " + className + " version " + SoundSystem.class.getPackage().getImplementationVersion() + "...", 0);

		// create the random number generator:
		randomNumberGenerator = new Random();
		// create the command queue:
		commandQueue = new LinkedList<>();
		// create the working source playlist:
		sourcePlayList = new LinkedList<>();

		// Instantiate and start the Command Processor thread:
		commandThread = new CommandThread(this); // Gets a SoundSystem handle
		commandThread.start();

		snooze(200);

		newLibrary(libraryClass);
		message("", 0);
	}

	/**
	 * Ends the command thread, shuts down the sound system, and removes references
	 * to all instantiated objects.
	 */
	public void cleanup() {
		boolean killException = false;
		message("", 0);
		message(className + " shutting down...", 0);

		// End the command thread:
		try {
			commandThread.kill();        // end the command processor loop.
			commandThread.interrupt();   // wake the thread up so it can end.
		} catch (Exception e) {
			killException = true;
		}

		if (!killException) {
			// wait up to 5 seconds for command thread to end:
			for (int i = 0; i < 50; i++) {
				if (!commandThread.alive()) break;
				snooze(100);
			}
		}

		// Let user know if there was a problem ending the command thread
		if (killException || commandThread.alive()) {
			errorMessage("Command thread did not die!", 0);
			message("Ignoring errors... continuing clean-up.", 0);
		}

		initialized(SET, false);
		currentLibrary(SET, null);
		try {
			// Stop all sources and shut down the sound library:
			if (soundLibrary != null) soundLibrary.cleanup();
		} catch (Exception e) {
			errorMessage("Problem during Library.cleanup()!", 0);
			message("Ignoring errors... continuing clean-up.", 0);
		}

		try {
			// remove any queued commands:
			if (commandQueue != null) commandQueue.clear();
		} catch (Exception e) {
			errorMessage("Unable to clear the command queue!", 0);
			message("Ignoring errors... continuing clean-up.", 0);
		}

		try {
			// empty the source management list:
			if (sourcePlayList != null) sourcePlayList.clear();
		} catch (Exception e) {
			errorMessage("Unable to clear the source management list!", 0);
			message("Ignoring errors... continuing clean-up.", 0);
		}

		// Remove references to all instantiated objects:
		randomNumberGenerator = null;
		soundLibrary = null;
		commandQueue = null;
		sourcePlayList = null;
		commandThread = null;

		importantMessage("Author: Paul Lamb, www.paulscode.com", 1);
		message("", 0);
	}

	/**
	 * Wakes up the Command Thread to process commands.  This method should be
	 * used if there is a need to call methods 'ManageSources' and 'CommandQueue'
	 * externally.  In most cases, this method will not be needed, since
	 * SoundSystem automatically wakes the Command Thread every time commands are
	 * placed into either the ManageSources queue or CommandQueue to be processed.
	 */
	public void interruptCommandThread() {
		if (commandThread == null) {
			errorMessage("Command Thread null in method " + "'interruptCommandThread'", 0);
			return;
		}
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}

	/**
	 * Pre-loads a sound into memory.  The file may either be located within the
	 * JAR or at an online location.  If the file is online, filename must begin
	 * with "https://", since that is how SoundSystem recognizes URL's.  If the file
	 * is located within the compiled JAR, the package in which sound files are
	 * located may be set by calling SoundSystemConfig.setSoundFilesPackage().
	 *
	 * @param filename Filename of the sound file to load.
	 */
	public void loadSound(String filename) {
		// Queue a command to load the sound file:
		commandQueue(new CommandObject(CommandObject.LOAD_SOUND, new FilenameURL(filename)));
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}

	/**
	 * Pre-loads a sound specified by the given URL into memory.  The second
	 * parameter 'identifier' should look like a filename, and it must have the
	 * correct extension so SoundSystem knows what codec to use for the file
	 * referenced by the URL instance.
	 *
	 * @param url        URL handle to the sound file to load.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 */
	public void loadSound(URL url, String identifier) {
		// Queue a command to load the sound file from a URL:
		commandQueue(new CommandObject(CommandObject.LOAD_SOUND, new FilenameURL(url, identifier)));
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}

	/**
	 * Saves raw PCM audio data in the specified audio format, under the specified
	 * identifier.  This identifier can be later used in place of 'filename'
	 * parameters to reference the sample data.
	 *
	 * @param data       The sample data
	 * @param format     Format the sample data is stored in
	 * @param identifier What to call the sample.
	 */
	public void loadSound(byte[] data, AudioFormat format, String identifier) {
		// Queue a command to load the sound file from a URL:
		commandQueue(new CommandObject(CommandObject.LOAD_DATA, identifier, new SoundBuffer(data, format)));
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}


	/**
	 * Removes a preloaded sound from memory.  This is a good method to use for
	 * freeing up memory after a large sound file is no longer needed.  NOTE: the
	 * source will remain in memory after calling this method as long as the
	 * sound is attached to an existing source.  When calling this method, calls
	 * should also be made to method removeSource( String ) for all sources which
	 * this sound is bound to.
	 *
	 * @param filename Filename/identifier of the sound file to unload.
	 */
	public void unloadSound(String filename) {
		// Queue a command to unload the sound file:
		commandQueue(new CommandObject(CommandObject.UNLOAD_SOUND, filename));
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}

	/**
	 * If the specified source is a streaming source or MIDI source, this method
	 * queues up the next sound to play when the previous playback ends. The file
	 * may either be located within the JAR or at an online location.  If the file
	 * is online, filename must begin with "https://", since that is how SoundSystem
	 * recognizes URL paths.  If the file is located within the compiled JAR, the
	 * package in which sound files are located may be set by calling
	 * SoundSystemConfig.setSoundFilesPackage().  This method has no effect on
	 * non-streaming sources.
	 *
	 * @param sourceName Source identifier.
	 * @param filename   Name of the sound file to play next.
	 */
	public void queueSound(String sourceName, String filename) {
		// Queue a command to queue the sound:
		commandQueue(new CommandObject(CommandObject.QUEUE_SOUND, sourceName, new FilenameURL(filename)));
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}

	/**
	 * If the specified source is a streaming source or MIDI source, this method
	 * queues up the next sound to play when the previous playback ends.  The third
	 * parameter 'identifier' should look like a filename, and it must have the
	 * correct extension so SoundSystem knows what codec to use for the file
	 * referenced by the URL instance.  This method has no effect on non-streaming
	 * sources.
	 *
	 * @param sourceName Source identifier.
	 * @param url        URL handle to the sound file to load.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 */
	public void queueSound(String sourceName, URL url, String identifier) {
		// Queue a command to queue the sound:
		commandQueue(new CommandObject(CommandObject.QUEUE_SOUND, sourceName, new FilenameURL(url, identifier)));
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}

	/**
	 * Removes the first occurrence of the specified filename/identifier from the
	 * specified source's list of sounds to play when previous playback ends.  This
	 * method has no effect on non-streaming sources.
	 *
	 * @param sourceName Source identifier.
	 * @param filename   Filename/identifier of the sound file to play next.
	 */
	public void dequeueSound(String sourceName, String filename) {
		// Queue a command to dequeue the sound:
		commandQueue(new CommandObject(CommandObject.DEQUEUE_SOUND, sourceName, filename));
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}

	/**
	 * Fades out the volume of whatever the specified source is currently playing,
	 * then begins playing the specified file at the source's previously
	 * assigned volume level.  The file may either be located within the JAR or at
	 * an online location.  If the file is online, filename must begin with
	 * "https://", since that is how SoundSystem recognizes URL paths.  If the file
	 * is located within the compiled JAR, the package in which sound files are
	 * located may be set by calling SoundSystemConfig.setSoundFilesPackage().  If
	 * the filename parameter is null or empty, the specified source will simply
	 * fade out and stop.  The milliseconds parameter must be non-negative or zero.
	 * This method will remove anything that is currently in the specified source's
	 * list of queued sounds that would have played next when the current sound
	 * finished playing.  This method may only be used for streaming and MIDI
	 * sources.
	 *
	 * @param sourceName Name of the source to fade out.
	 * @param filename   Name of a sound file to play next, or null for none.
	 * @param millis     Number of milliseconds the fade-out should take.
	 */
	public void fadeOut(String sourceName, String filename, long millis) {
		FilenameURL fu = null;
		if (filename != null) fu = new FilenameURL(filename);
		// Queue a command to fade out:
		commandQueue(new CommandObject(CommandObject.FADE_OUT, sourceName, fu, millis));
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}

	/**
	 * Fades out the volume of whatever the specified source is currently playing,
	 * then begins playing the specified file at the source's previously
	 * assigned volume level.  If the url parameter is null or empty, the
	 * specified source will simply fade out and stop.  The third
	 * parameter 'identifier' should look like a filename, and it must have the
	 * correct extension so SoundSystem knows what codec to use for the file
	 * referenced by the URL instance.  The milliseconds parameter must be
	 * non-negative or zero.  This method will remove anything that is currently in
	 * the specified source's list of queued sounds that would have played next
	 * when the current sound finished playing.  This method may only be used for
	 * streaming and MIDI sources.
	 *
	 * @param sourceName Name of the source to fade out.
	 * @param url        URL handle to the sound file to play next, or null for none.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param millis     Number of milliseconds the fade-out should take.
	 */
	public void fadeOut(String sourceName, URL url, String identifier, long millis) {
		FilenameURL fu = null;
		if (url != null && identifier != null) fu = new FilenameURL(url, identifier);
		// Queue a command to fade out:
		commandQueue(new CommandObject(CommandObject.FADE_OUT, sourceName, fu, millis));
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}

	/**
	 * Fades out the volume of whatever the specified source is currently playing,
	 * then fades the volume back in playing the specified filename.  Final volume
	 * after fade-in completes will be equal to the source's previously assigned
	 * volume level.  The filename parameter may not be null or empty.  The file
	 * may either be located within the JAR or at an online location.  If the file
	 * is online, filename must begin with "https://", since that is how
	 * SoundSystem recognizes URL paths.  If the file is located within the
	 * compiled JAR, the package in which sound files are located may be set by
	 * calling SoundSystemConfig.setSoundFilesPackage().  The milliseconds
	 * parameters must be non-negative or zero.  This method will remove anything
	 * that is currently in the specified source's list of queued sounds that would
	 * have played next when the current sound finished playing. This method may
	 * only be used for streaming and MIDI sources.
	 *
	 * @param sourceName Name of the source to fade out/in.
	 * @param filename   Name of a sound file to play next, or null for none.
	 * @param millisOut  Number of milliseconds the fade-out should take.
	 * @param millisIn   Number of milliseconds the fade-in should take.
	 */
	public void fadeOutIn(String sourceName, String filename, long millisOut, long millisIn) {
		// Queue a command to load the sound file:
		commandQueue(new CommandObject(CommandObject.FADE_OUT_IN, sourceName, new FilenameURL(filename), millisOut, millisIn));
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}

	/**
	 * Fades out the volume of whatever the specified source is currently playing,
	 * then fades the volume back in playing the specified file.  Final volume
	 * after fade-in completes will be equal to the source's previously assigned
	 * volume level.  The url parameter may not be null or empty.  The third
	 * parameter 'identifier' should look like a filename, and it must have the
	 * correct extension so SoundSystem knows what codec to use for the file
	 * referenced by the URL instance.  The milliseconds parameters must be
	 * non-negative or zero.  This method will remove anything that is currently
	 * in the specified source's list of queued sounds that would have played next
	 * when the current sound finished playing.  This method may only be used for
	 * streaming and MIDI sources.
	 *
	 * @param sourceName Name of the source to fade out/in.
	 * @param url        URL handle to the sound file to play next.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param millisOut  Number of milliseconds the fade-out should take.
	 * @param millisIn   Number of milliseconds the fade-in should take.
	 */
	public void fadeOutIn(String sourceName, URL url, String identifier, long millisOut, long millisIn) {
		// Queue a command to load the sound file:
		commandQueue(new CommandObject(CommandObject.FADE_OUT_IN, sourceName, new FilenameURL(url, identifier), millisOut, millisIn));
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}

	/**
	 * Makes sure the current volume levels of streaming sources and MIDI are
	 * correct.  This method is designed to help reduce the "jerky" fading behavior
	 * that happens when using some library and codec plugins (such as
	 * LibraryJavaSound and CodecJOrbis).  This method has no effect on normal
	 * "non-streaming" sources.  It would normally be called somewhere in the main
	 * "game loop".  IMPORTANT: To optimize frame-rates, do not call this method
	 * for every frame.  It is better to just call this method at some acceptable
	 * "granularity" (play around with different granularity to find what sounds
	 * acceptable for a particular situation).
	 */
	public void checkFadeVolumes() {
		// Queue a command to load check fading source volumes:
		commandQueue(new CommandObject(CommandObject.CHECK_FADE_VOLUMES));
		// Wake the command thread to process commands:
		commandThread.interrupt();
	}

	/**
	 * Creates a new permanent, streaming, priority source with zero attenuation.
	 * The file may either be located within the JAR or at an online location.  If
	 * the file is online, filename must begin with "https://", since that is how
	 * SoundSystem recognizes URL paths.  If the file is located within the
	 * compiled JAR, the package in which sound files are located may be set by
	 * calling SoundSystemConfig.setSoundFilesPackage().
	 *
	 * @param sourceName A unique identifier for this source.
	 *                   Two sources may not use the same source name.
	 * @param filename   Filename of the sound file to stream at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 */
	public void backgroundMusic(String sourceName, String filename, boolean toLoop) {
		// Queue a command to quick stream a new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, true, true, toLoop, sourceName, new FilenameURL(filename), 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0, false));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));

		commandThread.interrupt();
	}

	/**
	 * Creates a new permanent, streaming, priority source with zero attenuation.
	 * The third parameter 'identifier' should look like a filename, and it must
	 * have the correct extension so SoundSystem knows what codec to use for the
	 * file referenced by the URL instance.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 */
	public void backgroundMusic(String sourceName, URL url, String identifier, boolean toLoop) {
		// Queue a command to quick stream a new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, true, true, toLoop, sourceName, new FilenameURL(url, identifier), 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0, false));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));

		commandThread.interrupt();
	}

	/**
	 * Creates a new non-streaming source.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   Filename/identifier of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param x          X position for this source.
	 * @param y          Y position for this source.
	 * @param z          Z position for this source.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newSource(boolean priority, String sourceName, String filename, boolean toLoop, float x, float y, float z, int attModel, float distOrRoll) {
		commandQueue(new CommandObject(CommandObject.NEW_SOURCE, priority, false, toLoop, sourceName, new FilenameURL(filename), x, y, z, attModel, distOrRoll));
		commandThread.interrupt();
	}

	/**
	 * Creates a new non-streaming source.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param x          X position for this source.
	 * @param y          Y position for this source.
	 * @param z          Z position for this source.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop, float x, float y, float z, int attModel, float distOrRoll) {
		commandQueue(new CommandObject(CommandObject.NEW_SOURCE, priority, false, toLoop, sourceName, new FilenameURL(url, identifier), x, y, z, attModel, distOrRoll));
		commandThread.interrupt();
	}

	/**
	 * Creates a new streaming source.  The file may either be located within the
	 * JAR or at an online location.  If the file is online, filename must begin
	 * with "https://", since that is how SoundSystem recognizes URL paths.  If the
	 * file is located within the compiled JAR, the package in which sound files
	 * are located may be set by calling SoundSystemConfig.setSoundFilesPackage().
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.
	 *                   Two sources may not use the same source name.
	 * @param filename   The filename of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param x          X position for this source.
	 * @param y          Y position for this source.
	 * @param z          Z position for this source.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newStreamingSource(boolean priority, String sourceName, String filename, boolean toLoop, float x, float y, float z, int attModel, float distOrRoll) {
		commandQueue(new CommandObject(CommandObject.NEW_SOURCE, priority, true, toLoop, sourceName, new FilenameURL(filename), x, y, z, attModel, distOrRoll));
		commandThread.interrupt();
	}

	/**
	 * Creates a new streaming source.  The fourth parameter 'identifier' should
	 * look like a filename, and it must have the correct extension so SoundSystem
	 * knows what codec to use for the file referenced by the URL instance.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same sourceName.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param x          X position for this source.
	 * @param y          Y position for this source.
	 * @param z          Z position for this source.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newStreamingSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop, float x, float y, float z, int attModel, float distOrRoll) {
		commandQueue(new CommandObject(CommandObject.NEW_SOURCE, priority, true, toLoop, sourceName, new FilenameURL(url, identifier), x, y, z, attModel, distOrRoll));
		commandThread.interrupt();
	}

	/**
	 * Opens a direct line for streaming audio data.  This method creates a new
	 * streaming source to play the data at.  The resulting streaming source can be
	 * manipulated the same as any other streaming source.  Raw data can be sent to
	 * the new streaming source using the feedRawAudioData() method.
	 *
	 * @param audioFormat Format that the data will be in.
	 * @param priority    Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName  A unique identifier for this source.
	 *                    Two sources may not use the same source name.
	 * @param x           X position for this source.
	 * @param y           Y position for this source.
	 * @param z           Z position for this source.
	 * @param attModel    Attenuation model to use.
	 * @param distOrRoll  Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void rawDataStream(AudioFormat audioFormat, boolean priority, String sourceName, float x, float y, float z, int attModel, float distOrRoll) {
		commandQueue(new CommandObject(CommandObject.RAW_DATA_STREAM, audioFormat, priority, sourceName, x, y, z, attModel, distOrRoll));
		commandThread.interrupt();
	}

	/**
	 * Creates a temporary source and plays it.  After the source finishes playing,
	 * it is removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for source name.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param filename   Filename/identifier of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param x          X position for this source.
	 * @param y          Y position for this source.
	 * @param z          Z position for this source.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, String filename, boolean toLoop, float x, float y, float z, int attModel, float distOrRoll) {
		//generate a random name for this source:
		String sourceName = "Source_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		// Queue a command to quick play this new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, priority, false, toLoop, sourceName, new FilenameURL(filename), x, y, z, attModel, distOrRoll, true));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		// Wake the command thread to process commands:
		commandThread.interrupt();

		// return the new source name.
		return sourceName;
	}

	/**
	 * Creates a temporary source and plays it.  After the source finishes playing,
	 * it is removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for source name.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param x          X position for this source.
	 * @param y          Y position for this source.
	 * @param z          Z position for this source.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, URL url, String identifier, boolean toLoop, float x, float y, float z, int attModel, float distOrRoll) {
		//generate a random name for this source:
		String sourceName = "Source_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		// Queue a command to quick play this new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, priority, false, toLoop, sourceName, new FilenameURL(url, identifier), x, y, z, attModel, distOrRoll, true));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		// Wake the command thread to process commands:
		commandThread.interrupt();

		// return the new source name.
		return sourceName;
	}

	/**
	 * Creates a temporary source and streams it.  After the source finishes
	 * playing, it is removed.  The file may either be located within the
	 * JAR or at an online location.  If the file is online, filename must begin
	 * with "https://", since that is how SoundSystem recognizes URL paths.  If the
	 * file is located within the compiled JAR, the package in which sound files
	 * are located may be set by calling SoundSystemConfig.setSoundFilesPackage().
	 * Returns a randomly generated name for the new source.  NOTE: to make a
	 * source created by this method permanent, call the setActive() method using
	 * the return value for source name.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param filename   Filename of the sound file to stream at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param x          X position for this source.
	 * @param y          Y position for this source.
	 * @param z          Z position for this source.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, String filename, boolean toLoop, float x, float y, float z, int attModel, float distOrRoll) {
		//generate a random name for this source:
		String sourceName = "Source_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		// Queue a command to quick stream this new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, priority, true, toLoop, sourceName, new FilenameURL(filename), x, y, z, attModel, distOrRoll, true));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		// Wake the command thread to process commands:
		commandThread.interrupt();

		// return the new source name.
		return sourceName;
	}

	/**
	 * Creates a temporary source and streams it.  After the source finishes
	 * playing, it is removed.  The third parameter 'identifier' should
	 * look like a filename, and it must have the correct extension so SoundSystem
	 * knows what codec to use for the file referenced by the URL instance.
	 * Returns a randomly generated name for the new source.  NOTE: to make a
	 * source created by this method permanent, call the setActive() method using
	 * the return value for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param x          X position for this source.
	 * @param y          Y position for this source.
	 * @param z          Z position for this source.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, URL url, String identifier, boolean toLoop, float x, float y, float z, int attModel, float distOrRoll) {
		//generate a random name for this source:
		String sourceName = "Source_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		// Queue a command to quick stream this new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, priority, true, toLoop, sourceName, new FilenameURL(url, identifier), x, y, z, attModel, distOrRoll, true));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		// Wake the command thread to process commands:
		commandThread.interrupt();

		// return the new source name.
		return sourceName;
	}

	/**
	 * Move a source to the specified location.
	 *
	 * @param sourceName Identifier for the source.
	 * @param x          destination X coordinate.
	 * @param y          destination Y coordinate.
	 * @param z          destination Z coordinate.
	 */
	public void setPosition(String sourceName, float x, float y, float z) {
		commandQueue(new CommandObject(CommandObject.SET_POSITION, sourceName, x, y, z));
		commandThread.interrupt();
	}

	/**
	 * Manually sets the specified source's volume.
	 *
	 * @param sourceName Source to move.
	 * @param value      New volume, float value ( 0.0f - 1.0f ).
	 */
	public void setVolume(String sourceName, float value) {
		commandQueue(new CommandObject(CommandObject.SET_VOLUME, sourceName, value));
		commandThread.interrupt();
	}

	/**
	 * Returns the current volume of the specified source, or zero if the specified
	 * source was not found.
	 *
	 * @param sourceName Source to read volume from.
	 * @return Float value representing the source volume (0.0f - 1.0f).
	 */
	public float getVolume(String sourceName) {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			if (soundLibrary != null) return soundLibrary.getVolume(sourceName);
			else return 0.0f;
		}
	}

	/**
	 * Manually sets the specified source's pitch.
	 *
	 * @param sourceName The source's name.
	 * @param value      A float value ( 0.5f - 2.0f ).
	 */
	public void setPitch(String sourceName, float value) {
		commandQueue(new CommandObject(CommandObject.SET_PITCH, sourceName, value));
		commandThread.interrupt();
	}

	/**
	 * Returns the pitch of the specified source.
	 *
	 * @param sourceName The source's name.
	 * @return Float value representing the source pitch (0.5f - 2.0f).
	 */
	public float getPitch(String sourceName) {
		if (soundLibrary != null) return soundLibrary.getPitch(sourceName);
		else return 1.0f;
	}

	/**
	 * Set a source's priority factor.  A priority source will not be overridden when
	 * too many sources are playing at once.
	 *
	 * @param sourceName Identifier for the source.
	 * @param pri        Setting this to true makes this source a priority source.
	 */
	public void setPriority(String sourceName, boolean pri) {
		commandQueue(new CommandObject(CommandObject.SET_PRIORITY, sourceName, pri));
		commandThread.interrupt();
	}

	/**
	 * Changes a source to looping or non-looping.
	 *
	 * @param sourceName Identifier for the source.
	 * @param lp         This source should loop.
	 */
	public void setLooping(String sourceName, boolean lp) {
		commandQueue(new CommandObject(CommandObject.SET_LOOPING, sourceName, lp));
		commandThread.interrupt();
	}

	/**
	 * Changes a source's attenuation model.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation.
	 *
	 * @param sourceName Identifier for the source.
	 * @param model      Attenuation model to use.
	 */
	public void setAttenuation(String sourceName, int model) {
		commandQueue(new CommandObject(CommandObject.SET_ATTENUATION, sourceName, model));
		commandThread.interrupt();
	}

	/**
	 * Changes a source's fade distance or roll-off factor.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about fade distance and roll-off.
	 *
	 * @param sourceName Identifier for the source.
	 * @param dr         Either the fading distance or roll-off factor, depending on the attenuation model used.
	 */
	public void setDistOrRoll(String sourceName, float dr) {
		commandQueue(new CommandObject(CommandObject.SET_DIST_OR_ROLL, sourceName, dr));
		commandThread.interrupt();
	}

	/**
	 * Changes the Doppler factor, for determining Doppler effect scale.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Doppler effect.
	 *
	 * @param dopplerFactor New value for Doppler factor.
	 */
	public void changeDopplerFactor(float dopplerFactor) {
		commandQueue(new CommandObject(CommandObject.CHANGE_DOPPLER_FACTOR, dopplerFactor));
		commandThread.interrupt();
	}

	/**
	 * Changes the Doppler velocity, for use in Doppler effect.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Doppler effect.
	 *
	 * @param dopplerVelocity New value for Doppler velocity.
	 */
	public void changeDopplerVelocity(float dopplerVelocity) {
		commandQueue(new CommandObject(CommandObject.CHANGE_DOPPLER_VELOCITY, dopplerVelocity));
		commandThread.interrupt();
	}

	/**
	 * Sets the specified source's velocity, for use in Doppler effect.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Doppler effect.
	 *
	 * @param sourceName The source's name.
	 * @param x          Velocity along world x-axis.
	 * @param y          Velocity along world y-axis.
	 * @param z          Velocity along world z-axis.
	 */
	public void setVelocity(String sourceName, float x, float y, float z) {
		commandQueue(new CommandObject(CommandObject.SET_VELOCITY, sourceName, x, y, z));
		commandThread.interrupt();
	}

	/**
	 * Sets the listener's velocity, for use in Doppler effect.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Doppler effect.
	 *
	 * @param x Velocity along world x-axis.
	 * @param y Velocity along world y-axis.
	 * @param z Velocity along world z-axis.
	 */
	public void setListenerVelocity(float x, float y, float z) {
		commandQueue(new CommandObject(CommandObject.SET_LISTENER_VELOCITY, x, y, z));
		commandThread.interrupt();
	}

	/**
	 * Returns the number of milliseconds since the specified source began playing.
	 *
	 * @return milliseconds, or -1 if not playing or unable to calculate
	 */
	public float millisecondsPlayed(String sourceName) {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			return soundLibrary.millisecondsPlayed(sourceName);
		}
	}

	/**
	 * Feeds raw data through the specified source.  The source must be a
	 * streaming source, and it can not be already associated with a file or URL to
	 * stream from.  Only use this for streaming sources created with the
	 * rawDataStream() method.  NOTE:  Be careful how much data you send to a
	 * source to stream.  The data will be processed at playback speed, so if you
	 * queue up 1 hour worth of data, it will take 1 hour to play (not to mention
	 * hogging a ton of memory).  To clear out all queued data from the source, use
	 * the flush() method.  Also note: if there is a break in the data stream,
	 * you will hear clicks and stutters, so ensure that the data flow is steady.
	 *
	 * @param sourceName Name of the streaming source to play from.
	 * @param buffer     Byte buffer containing raw audio data to stream.
	 */
	public void feedRawAudioData(String sourceName, byte[] buffer) {
		commandQueue(new CommandObject(CommandObject.FEED_RAW_AUDIO_DATA, sourceName, buffer));
		commandThread.interrupt();
	}

	/**
	 * Plays the specified source.
	 *
	 * @param sourceName Identifier for the source.
	 */
	public void play(String sourceName) {
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		commandThread.interrupt();
	}

	/**
	 * Pauses the specified source.
	 *
	 * @param sourceName Identifier for the source.
	 */
	public void pause(String sourceName) {
		commandQueue(new CommandObject(CommandObject.PAUSE, sourceName));
		commandThread.interrupt();
	}

	/**
	 * Stops the specified source.
	 *
	 * @param sourceName Identifier for the source.
	 */
	public void stop(String sourceName) {
		commandQueue(new CommandObject(CommandObject.STOP, sourceName));
		commandThread.interrupt();
	}

	/**
	 * Rewinds the specified source.
	 *
	 * @param sourceName Identifier for the source.
	 */
	public void rewind(String sourceName) {
		commandQueue(new CommandObject(CommandObject.REWIND, sourceName));
		commandThread.interrupt();
	}

	/**
	 * Flushes all previously queued audio data from a streaming source.
	 *
	 * @param sourceName Identifier for the source.
	 */
	public void flush(String sourceName) {
		commandQueue(new CommandObject(CommandObject.FLUSH, sourceName));
		commandThread.interrupt();
	}

	/**
	 * Culls the specified source.  A culled source can not be played until it has
	 * been activated again.
	 *
	 * @param sourceName Identifier for the source.
	 */
	public void cull(String sourceName) {
		commandQueue(new CommandObject(CommandObject.CULL, sourceName));
		commandThread.interrupt();
	}

	/**
	 * Activates the specified source after it was culled, so it can be played
	 * again.
	 *
	 * @param sourceName Identifier for the source.
	 */
	public void activate(String sourceName) {
		commandQueue(new CommandObject(CommandObject.ACTIVATE, sourceName));
		commandThread.interrupt();
	}

	/**
	 * Sets a flag for a source indicating whether it should be used or if it
	 * should be removed after it finishes playing.  One possible use for this
	 * method is to make temporary sources that were created with quickPlay()
	 * permanent.  Another use could be to have a source automatically removed
	 * after it finishes playing.  NOTE: Setting a source to temporary does not
	 * stop it, and setting a source to permanent does not play it.  It is also
	 * important to note that a looping temporary source will not be removed as
	 * long as it keeps playing.
	 *
	 * @param sourceName Identifier for the source.
	 * @param temporary  True = temporary, False = permanent.
	 */
	public void setTemporary(String sourceName, boolean temporary) {
		commandQueue(new CommandObject(CommandObject.SET_TEMPORARY, sourceName, temporary));
		commandThread.interrupt();
	}

	/**
	 * Removes the specified source and clears up any memory it used.
	 *
	 * @param sourceName Identifier for the source.
	 */
	public void removeSource(String sourceName) {
		commandQueue(new CommandObject(CommandObject.REMOVE_SOURCE, sourceName));
		commandThread.interrupt();
	}

	/**
	 * Moves the listener relative to the current location.
	 *
	 * @param x X offset.
	 * @param y Y offset.
	 * @param z Z offset.
	 */
	public void moveListener(float x, float y, float z) {
		commandQueue(new CommandObject(CommandObject.MOVE_LISTENER, x, y, z));
		commandThread.interrupt();
	}

	/**
	 * Moves the listener to the specified location.
	 *
	 * @param x Destination X coordinate.
	 * @param y Destination Y coordinate.
	 * @param z Destination Z coordinate.
	 */
	public void setListenerPosition(float x, float y, float z) {
		commandQueue(new CommandObject(CommandObject.SET_LISTENER_POSITION, x, y, z));
		commandThread.interrupt();
	}

	/**
	 * Turns the listener counterclockwise by "angle" radians around the y-axis,
	 * relative to the current angle.
	 *
	 * @param angle radian offset.
	 */
	public void turnListener(float angle) {
		commandQueue(new CommandObject(CommandObject.TURN_LISTENER, angle));
		commandThread.interrupt();
	}

	/**
	 * Sets the listener's angle in radians around the y-axis.
	 *
	 * @param angle radians.
	 */
	public void setListenerAngle(float angle) {
		commandQueue(new CommandObject(CommandObject.SET_LISTENER_ANGLE, angle));
		commandThread.interrupt();
	}

	/**
	 * Sets the listener's orientation.
	 *
	 * @param lookX X coordinate of the (normalized) look-at vector.
	 * @param lookY Y coordinate of the (normalized) look-at vector.
	 * @param lookZ Z coordinate of the (normalized) look-at vector.
	 * @param upX   X coordinate of the (normalized) up-direction vector.
	 * @param upY   Y coordinate of the (normalized) up-direction vector.
	 * @param upZ   Z coordinate of the (normalized) up-direction vector.
	 */
	public void setListenerOrientation(float lookX, float lookY, float lookZ, float upX, float upY, float upZ) {
		commandQueue(new CommandObject(CommandObject.SET_LISTENER_ORIENTATION, lookX, lookY, lookZ, upX, upY, upZ));
		commandThread.interrupt();
	}

	/**
	 * Sets the overall volume, affecting all sources.
	 *
	 * @param value New volume, float value ( 0.0f - 1.0f ).
	 */
	public void setMasterVolume(float value) {
		commandQueue(new CommandObject(CommandObject.SET_MASTER_VOLUME, value));
		commandThread.interrupt();
	}

	/**
	 * Returns the overall volume, affecting all sources.
	 *
	 * @return Float value representing the master volume (0.0f - 1.0f).
	 */
	public float getMasterVolume() {
		return SoundSystemConfig.getMasterGain();
	}

	/**
	 * Method for obtaining information about the listener's position and
	 * orientation.
	 *
	 * @return a {@link ListenerData ListenerData} object.
	 */
	public ListenerData getListenerData() {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			return soundLibrary.getListenerData();
		}
	}

	/**
	 * Switches to the specified library, and preserves all sources.
	 *
	 * @param libraryClass Library to use.
	 * @return True if switch was successful.
	 * See {@link SoundSystemConfig SoundSystemConfig} for
	 * information about choosing a sound library.
	 */
	public boolean switchLibrary(Class<?> libraryClass) throws SoundSystemException {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			initialized(SET, false);

			HashMap<String, Source> sourceMap = null;
			ListenerData listenerData = null;

			boolean wasMidiChannel = false;
			MidiChannel midiChannel = null;
			FilenameURL midiFilenameURL = null;
			String midiSourceName = "";
			boolean midiToLoop = true;

			if (soundLibrary != null) {
				currentLibrary(SET, null);
				sourceMap = copySources(soundLibrary.getSources());
				listenerData = soundLibrary.getListenerData();
				midiChannel = soundLibrary.getMidiChannel();
				if (midiChannel != null) {
					wasMidiChannel = true;
					midiToLoop = midiChannel.getLooping();
					midiSourceName = midiChannel.getSourceName();
					midiFilenameURL = midiChannel.getFilenameURL();
				}

				soundLibrary.cleanup();
				soundLibrary = null;
			}
			message("", 0);
			message("Switching to " + SoundSystemConfig.getLibraryTitle(libraryClass), 0);
			message("(" + SoundSystemConfig.getLibraryDescription(libraryClass) + ")", 1);

			try {
				soundLibrary = (Library) libraryClass.newInstance();
			} catch (Exception e) {
				errorMessage("The specified library did not load properly", 1);
			}

			if (errorCheck(soundLibrary == null, "Library null after " + "initialization in method 'switchLibrary'", 1)) {
				SoundSystemException sse = new SoundSystemException(className + " did not load properly.  " + "Library was null after initialization.", SoundSystemException.LIBRARY_NULL);
				lastException(SET, sse);
				initialized(SET, true);
				throw sse;
			}

			try {
				soundLibrary.init();
			} catch (SoundSystemException sse) {
				lastException(SET, sse);
				initialized(SET, true);
				throw sse;
			}

			soundLibrary.setListenerData(listenerData);
			if (wasMidiChannel) {
				if (midiChannel != null) midiChannel.cleanup();
				midiChannel = new MidiChannel(midiToLoop, midiSourceName, midiFilenameURL);
				soundLibrary.setMidiChannel(midiChannel);
			}
			soundLibrary.copySources(sourceMap);

			message("", 0);

			lastException(SET, null);
			initialized(SET, true);

			return true;
		}
	}

	/**
	 * Switches to the specified library, loosing all sources.
	 *
	 * @param libraryClass Library to use.
	 *                     See {@link SoundSystemConfig SoundSystemConfig} for
	 *                     information about choosing a sound library.
	 */
	public boolean newLibrary(Class<?> libraryClass) throws SoundSystemException {
		initialized(SET, false);

		commandQueue(new CommandObject(CommandObject.NEW_LIBRARY, libraryClass));
		commandThread.interrupt();

		for (int x = 0; (!initialized(GET, XXX)) && (x < 100); x++) {
			snooze(400);
			commandThread.interrupt();
		}

		if (!initialized(GET, XXX)) {
			SoundSystemException sse = new SoundSystemException(className + " did not load after 30 seconds.", SoundSystemException.LIBRARY_NULL);
			lastException(SET, sse);
			throw sse;
		} else {
			SoundSystemException sse = lastException(GET, null);
			if (sse != null) throw sse;
		}
		return true;
	}

	/**
	 * Switches to the specified library, loosing all sources.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the newLibrary() method instead.
	 *
	 * @param libraryClass Library to use.
	 *                     See {@link SoundSystemConfig SoundSystemConfig} for
	 *                     information about choosing a sound library.
	 */
	private void CommandNewLibrary(Class<?> libraryClass) {
		initialized(SET, false);

		String headerMessage = "Initializing ";
		if (soundLibrary != null) {
			currentLibrary(SET, null);
			// we are switching libraries
			headerMessage = "Switching to ";
			soundLibrary.cleanup();
			soundLibrary = null;
		}
		message(headerMessage + SoundSystemConfig.getLibraryTitle(libraryClass), 0);
		message("(" + SoundSystemConfig.getLibraryDescription(libraryClass) + ")", 1);

		try {
			soundLibrary = (Library) libraryClass.newInstance();
		} catch (Exception e) {
			errorMessage("The specified library did not load properly", 1);
		}

		if (errorCheck(soundLibrary == null, "Library null after " + "initialization in method 'newLibrary'", 1)) {
			lastException(SET, new SoundSystemException(className + " did not load properly.  " + "Library was null after initialization.", SoundSystemException.LIBRARY_NULL));
			importantMessage("Switching to silent mode", 1);

			try {
				soundLibrary = new Library();
			} catch (SoundSystemException sse) {
				lastException(SET, new SoundSystemException("Silent mode did not load properly.  " + "Library was null after initialization.", SoundSystemException.LIBRARY_NULL));
				initialized(SET, true);
				return;
			}
		}

		try {
			soundLibrary.init();
		} catch (SoundSystemException sse) {
			lastException(SET, sse);
			initialized(SET, true);
			return;
		}

		lastException(SET, null);
		initialized(SET, true);
	}

	/**
	 * Calls the library's initialize() method.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly.
	 */
	private void CommandInitialize() {
		try {
			if (errorCheck(soundLibrary == null, "Library null after " + "initialization in method 'CommandInitialize'", 1)) {
				SoundSystemException sse = new SoundSystemException(className + " did not load properly.  " + "Library was null after initialization.", SoundSystemException.LIBRARY_NULL);
				lastException(SET, sse);
				throw sse;
			}
			soundLibrary.init();
		} catch (SoundSystemException sse) {
			lastException(SET, sse);
			initialized(SET, true);
		}
	}

	/**
	 * Loads sample data from a sound file or URL into memory.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the loadSound() method instead.
	 *
	 * @param filenameURL Filename/URL of the sound file to load.
	 */
	private void CommandLoadSound(FilenameURL filenameURL) {
		if (soundLibrary != null) soundLibrary.loadSound(filenameURL);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandLoadSound'", 0);
	}

	/**
	 * Saves the specified sample data, under the specified identifier.  This
	 * identifier can be later used in place of 'filename' parameters to reference
	 * the sample data.  This method is used internally by SoundSystem for thread
	 * synchronization, and it can not be called directly - please use the
	 * loadSound() method instead.
	 *
	 * @param buffer     the sample data and audio format to save.
	 * @param identifier What to call the sample.
	 */
	private void CommandLoadSound(SoundBuffer buffer, String identifier) {
		if (soundLibrary != null) soundLibrary.loadSound(buffer, identifier);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandLoadSound'", 0);
	}

	/**
	 * Removes previously loaded sampled data from memory.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the unloadSound() method instead.
	 *
	 * @param filename Filename or string identifier of sound to unload.
	 */
	private void CommandUnloadSound(String filename) {
		if (soundLibrary != null) soundLibrary.unloadSound(filename);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandLoadSound'", 0);
	}

	/**
	 * If the specified source is a streaming source or MIDI source, this method
	 * queues up the next sound to play when the previous playback ends.  This
	 * method has no effect on non-streaming sources.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the queueSound() method instead.
	 *
	 * @param sourceName  Source identifier.
	 * @param filenameURL Filename/URL of the sound file to play next.
	 */
	private void CommandQueueSound(String sourceName, FilenameURL filenameURL) {
		if (soundLibrary != null) soundLibrary.queueSound(sourceName, filenameURL);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandQueueSound'", 0);
	}

	/**
	 * Removes the first occurrence of the specified filename/identifier from the
	 * specified source's list of sounds to play when previous playback ends.  This
	 * method has no effect on non-streaming sources.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the dequeueSound() method instead.
	 *
	 * @param sourceName Source identifier.
	 * @param filename   Filename/identifier of the sound file to remove from the queue.
	 */
	private void CommandDequeueSound(String sourceName, String filename) {
		if (soundLibrary != null) soundLibrary.dequeueSound(sourceName, filename);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandDequeueSound'", 0);
	}

	/**
	 * Fades out the volume of whatever the specified source is currently playing,
	 * then begins playing the specified file at the source's previously
	 * assigned volume level.  If the filenameURL parameter is null or empty, the
	 * specified source will simply fade out and stop.  The milliseconds parameter
	 * must be non-negative or zero.  This method will remove anything that is
	 * currently in the specified source's list of queued sounds that would have
	 * played next when the current sound finished playing.  This method may only
	 * be used for streaming and MIDI sources.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the fadeOut() method instead.
	 *
	 * @param sourceName  Name of the source to fade out.
	 * @param filenameURL Filename/URL of a sound file to play next, or null for none.
	 * @param millis      Number of milliseconds the fade-out should take.
	 */
	private void CommandFadeOut(String sourceName, FilenameURL filenameURL, long millis) {
		if (soundLibrary != null) soundLibrary.fadeOut(sourceName, filenameURL, millis);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandFadeOut'", 0);
	}

	/**
	 * Fades out the volume of whatever the specified source is currently playing,
	 * then fades the volume back in playing the specified file.  Final volume
	 * after fade-in completes will be equal to the source's previously assigned
	 * volume level.  The filenameURL parameter may not be null or empty.  The
	 * milliseconds parameters must be non-negative or zero.  This method will
	 * remove anything that is currently in the specified source's list of queued
	 * sounds that would have played next when the current sound finished playing.
	 * This method may only be used for streaming and MIDI sources.  This method is
	 * used internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the fadeOutIn() method instead.
	 *
	 * @param sourceName  Name of the source to fade out/in.
	 * @param filenameURL Filename/URL of a sound file to play next, or null for none.
	 * @param millisOut   Number of milliseconds the fade-out should take.
	 * @param millisIn    Number of milliseconds the fade-in should take.
	 */
	private void CommandFadeOutIn(String sourceName, FilenameURL filenameURL, long millisOut, long millisIn) {
		if (soundLibrary != null) soundLibrary.fadeOutIn(sourceName, filenameURL, millisOut, millisIn);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandFadeOutIn'", 0);
	}

	/**
	 * Makes sure the current volume levels of streaming sources and MIDI are
	 * correct.  This method is designed to help reduce the "jerky" fading behavior
	 * that happens when using some library and codec plugins (such as
	 * LibraryJavaSound and CodecJOrbis).  This method has no effect on normal
	 * "non-streaming" sources.  It would normally be called somewhere in the main
	 * "game loop".  IMPORTANT: To optimize frame-rates, do not call this method
	 * for every frame.  It is better to just call this method at some acceptable
	 * "granularity" (play around with different granularity to find what sounds
	 * acceptable for a particular situation).  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the checkFadeVolumes() method instead.
	 */
	private void CommandCheckFadeVolumes() {
		if (soundLibrary != null) soundLibrary.checkFadeVolumes();
		else errorMessage("Variable 'soundLibrary' null in method " + "'CommandCheckFadeVolumes'", 0);
	}

	/**
	 * Loads a sound file into memory.  This method is used internally by
	 * SoundSystem for thread synchronization, and it can not be called directly -
	 * please use the {@link #newSource} method instead.
	 *
	 * @param priority    Setting this to true will prevent other sounds from overriding this one.
	 * @param toStream    Whether to stream the source.
	 * @param toLoop      Whether to loop the source.
	 * @param sourceName  A unique identifier for the source.
	 * @param filenameURL Filename/URL of the sound file to play at this source.
	 * @param x           X position for this source.
	 * @param y           Y position for this source.
	 * @param z           Z position for this source.
	 * @param attModel    Attenuation model to use.
	 * @param distOrRoll  Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	private void CommandNewSource(boolean priority, boolean toStream, boolean toLoop, String sourceName, FilenameURL filenameURL, float x, float y, float z, int attModel, float distOrRoll) {
		if (soundLibrary != null) {
			if (filenameURL.getFilename().matches(SoundSystemConfig.EXTENSION_MIDI) && !SoundSystemConfig.midiCodec()) {
				soundLibrary.loadMidi(toLoop, sourceName, filenameURL);
			} else {
				soundLibrary.newSource(priority, toStream, toLoop, sourceName, filenameURL, x, y, z, attModel, distOrRoll);
			}
		} else errorMessage("Variable 'soundLibrary' null in method 'CommandNewSource'", 0);
	}

	/**
	 * Opens a direct line for streaming audio data.  This method is used
	 * internally by SoundSystem, and it can not be called directly - please use
	 * the rawDataStream() method instead.
	 *
	 * @param audioFormat Format that the data will be in.
	 * @param priority    Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName  A unique identifier for this source.  Two sources may not use the same sourceName.
	 * @param x           X position for this source.
	 * @param y           Y position for this source.
	 * @param z           Z position for this source.
	 * @param attModel    Attenuation model to use.
	 * @param distOrRoll  Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	private void CommandRawDataStream(AudioFormat audioFormat, boolean priority, String sourceName, float x, float y, float z, int attModel, float distOrRoll) {
		if (soundLibrary != null)
			soundLibrary.rawDataStream(audioFormat, priority, sourceName, x, y, z, attModel, distOrRoll);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandRawDataStream'", 0);
	}

	/**
	 * Creates a temporary source and either plays or streams it.  After the source
	 * finishes playing, it is removed.  This method is used internally by
	 * SoundSystem for thread synchronization, and it can not be called directly -
	 * please use the {@link #quickPlay(boolean, String, boolean, float, float, float, int, float)} method instead.
	 *
	 * @param priority    Setting this to true will prevent other sounds from overriding this one.
	 * @param toStream    Whether to stream the source.
	 * @param toLoop      Whether to loop the source.
	 * @param sourceName  A unique identifier for the source.
	 * @param filenameURL Filename/URL of the sound file to play at this source.
	 * @param x           X position for this source.
	 * @param y           Y position for this source.
	 * @param z           Z position for this source.
	 * @param attModel    Attenuation model to use.
	 * @param distOrRoll  Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @param temporary   Whether the source should be removed after it finishes playing.
	 */
	private void CommandQuickPlay(boolean priority, boolean toStream, boolean toLoop, String sourceName, FilenameURL filenameURL, float x, float y, float z, int attModel, float distOrRoll, boolean temporary) {
		if (soundLibrary != null) {
			if (filenameURL.getFilename().matches(SoundSystemConfig.EXTENSION_MIDI) && !SoundSystemConfig.midiCodec()) {
				soundLibrary.loadMidi(toLoop, sourceName, filenameURL);
			} else {
				soundLibrary.quickPlay(priority, toStream, toLoop, sourceName, filenameURL, x, y, z, attModel, distOrRoll, temporary);
			}
		} else errorMessage("Variable 'soundLibrary' null in method 'CommandQuickPlay'", 0);
	}

	/**
	 * Moves a source to the specified coordinates.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setPosition() method instead.
	 *
	 * @param sourceName Source to move.
	 * @param x          Destination X coordinate.
	 * @param y          Destination Y coordinate.
	 * @param z          Destination Z coordinate.
	 */
	private void CommandSetPosition(String sourceName, float x, float y, float z) {
		if (soundLibrary != null) soundLibrary.setPosition(sourceName, x, y, z);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandMoveSource'", 0);
	}

	/**
	 * Manually sets the specified source's volume.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setVolume() method instead.
	 *
	 * @param sourceName Source to change the volume of.
	 * @param value      New volume, float value ( 0.0f - 1.0f ).
	 */
	private void CommandSetVolume(String sourceName, float value) {
		if (soundLibrary != null) soundLibrary.setVolume(sourceName, value);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandSetVolume'", 0);
	}

	/**
	 * Manually sets the specified source's pitch.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setPitch() method instead.
	 *
	 * @param sourceName Source to change the pitch of.
	 * @param value      New pitch, float value ( 0.5f - 2.0f ).
	 */
	private void CommandSetPitch(String sourceName, float value) {
		if (soundLibrary != null) soundLibrary.setPitch(sourceName, value);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandSetPitch'", 0);
	}

	/**
	 * Set a source's priority factor.  A priority source will not be overridden when
	 * too many sources are playing at once.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setPriority() method instead.
	 *
	 * @param sourceName Identifier for the source.
	 * @param pri        Setting this to true makes this source a priority source.
	 */
	private void CommandSetPriority(String sourceName, boolean pri) {
		if (soundLibrary != null) soundLibrary.setPriority(sourceName, pri);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandSetPriority'", 0);
	}

	/**
	 * Changes a source to looping or non-looping.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setLooping() method instead.
	 *
	 * @param sourceName Identifier for the source.
	 * @param lp         This source should loop.
	 */
	private void CommandSetLooping(String sourceName, boolean lp) {
		if (soundLibrary != null) soundLibrary.setLooping(sourceName, lp);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandSetLooping'", 0);
	}

	/**
	 * Changes a source's attenuation model.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setAttenuation() method instead.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation.
	 *
	 * @param sourceName Identifier for the source.
	 * @param model      Attenuation model to use.
	 */
	private void CommandSetAttenuation(String sourceName, int model) {
		if (soundLibrary != null) soundLibrary.setAttenuation(sourceName, model);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandSetAttenuation'", 0);
	}

	/**
	 * Changes a source's fade distance or roll-off factor.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about fade distance and roll-off.
	 *
	 * @param sourceName Identifier for the source.
	 * @param dr         Either the fading distance or roll-off factor, depending on the attenuation model used.
	 */
	private void CommandSetDistOrRoll(String sourceName, float dr) {
		if (soundLibrary != null) soundLibrary.setDistOrRoll(sourceName, dr);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandSetDistOrRoll'", 0);
	}

	/**
	 * Changes the Doppler factor.  This method is used internally by SoundSystem
	 * for thread synchronization, and it can not be called directly - please use
	 * the setDopplerFactor() method instead.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Doppler effect.
	 *
	 * @param dopplerFactor New Doppler factor, for determining Doppler effect scale.
	 */
	private void CommandChangeDopplerFactor(float dopplerFactor) {
		if (soundLibrary != null) {
			SoundSystemConfig.setDopplerFactor(dopplerFactor);
			soundLibrary.dopplerChanged();
		} else errorMessage("Variable 'soundLibrary' null in method 'CommandSetDopplerFactor'", 0);
	}

	/**
	 * Changes the Doppler velocity.  This method is used internally by SoundSystem
	 * for thread synchronization, and it can not be called directly - please use
	 * the setDopplerVelocity() method instead.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Doppler effect.
	 *
	 * @param dopplerVelocity New Doppler velocity, for use in Doppler effect.
	 */
	private void CommandChangeDopplerVelocity(float dopplerVelocity) {
		if (soundLibrary != null) {
			SoundSystemConfig.setDopplerVelocity(dopplerVelocity);
			soundLibrary.dopplerChanged();
		} else errorMessage("Variable 'soundLibrary' null in method 'CommandSetDopplerFactor'", 0);
	}

	/**
	 * Changes a source's velocity, for use in Doppler effect.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setVelocity() method instead.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Doppler effect.
	 *
	 * @param sourceName Identifier for the source.
	 * @param x          Source's velocity along the world x-axis.
	 * @param y          Source's velocity along the world y-axis.
	 * @param z          Source's velocity along the world z-axis.
	 */
	private void CommandSetVelocity(String sourceName, float x, float y, float z) {
		if (soundLibrary != null) soundLibrary.setVelocity(sourceName, x, y, z);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandVelocity'", 0);
	}

	/**
	 * Changes the listener's velocity, for use in Doppler effect.  This method is
	 * used internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setListenerVelocity() method instead.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Doppler effect.
	 *
	 * @param x Velocity along the world x-axis.
	 * @param y Velocity along the world y-axis.
	 * @param z Velocity along the world z-axis.
	 */
	private void CommandSetListenerVelocity(float x, float y, float z) {
		if (soundLibrary != null) soundLibrary.setListenerVelocity(x, y, z);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandSetListenerVelocity'", 0);
	}

	/**
	 * Plays the specified source.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the play() method instead.
	 *
	 * @param sourceName Identifier for the source.
	 */
	private void CommandPlay(String sourceName) {
		if (soundLibrary != null) soundLibrary.play(sourceName);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandPlay'", 0);
	}

	/**
	 * Feeds raw data through the specified source.  The source must be a
	 * streaming source, and it can not be already associated with a file or URL to
	 * stream from.  This method is used internally by SoundSystem for thread
	 * synchronization, and it can not be called directly - please use the
	 * feedRawAudioData() method instead.
	 *
	 * @param sourceName Name of the streaming source to play from.
	 * @param buffer     Byte buffer containing raw audio data to stream.
	 */
	private void CommandFeedRawAudioData(String sourceName, byte[] buffer) {
		if (soundLibrary != null) soundLibrary.feedRawAudioData(sourceName, buffer);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandFeedRawAudioData'", 0);
	}

	/**
	 * Pauses the specified source.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the pause() method instead.
	 *
	 * @param sourceName Identifier for the source.
	 */
	private void CommandPause(String sourceName) {
		if (soundLibrary != null) soundLibrary.pause(sourceName);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandPause'", 0);
	}

	/**
	 * Stops the specified source.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the stop() method instead.
	 *
	 * @param sourceName Identifier for the source.
	 */
	private void CommandStop(String sourceName) {
		if (soundLibrary != null) soundLibrary.stop(sourceName);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandStop'", 0);
	}

	/**
	 * Rewinds the specified source.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the rewind() method instead.
	 *
	 * @param sourceName Identifier for the source.
	 */
	private void CommandRewind(String sourceName) {
		if (soundLibrary != null) soundLibrary.rewind(sourceName);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandRewind'", 0);
	}

	/**
	 * Flushes all previously queued audio data from a streaming source.  This
	 * method is used internally by SoundSystem for thread synchronization, and it
	 * can not be called directly - please use the flush() method instead.
	 *
	 * @param sourceName Identifier for the source.
	 */
	private void CommandFlush(String sourceName) {
		if (soundLibrary != null) soundLibrary.flush(sourceName);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandFlush'", 0);
	}

	/**
	 * Sets a flag for a source indicating whether it should be used or if it
	 * should be removed after it finishes playing.  One possible use for this
	 * method is to make temporary sources that were created with quickPlay()
	 * permanent.  Another use could be to have a source automatically removed
	 * after it finishes playing.  NOTE: Setting a source to inactive does not stop
	 * it, and setting a source to active does not play it.  It is also important
	 * to note that a looping inactive source will not be removed as long as
	 * it keeps playing.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setTemporary() method instead.
	 *
	 * @param sourceName Identifier for the source.
	 * @param temporary  True or False.
	 */
	private void CommandSetTemporary(String sourceName, boolean temporary) {
		if (soundLibrary != null) soundLibrary.setTemporary(sourceName, temporary);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandSetActive'", 0);
	}

	/**
	 * Removes the specified source and clears up any memory it used.  This method
	 * is used internally by SoundSystem for thread synchronization, and it can not
	 * be called directly - please use the removeSource() method instead.
	 *
	 * @param sourceName Identifier for the source.
	 */
	private void CommandRemoveSource(String sourceName) {
		if (soundLibrary != null) soundLibrary.removeSource(sourceName);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandRemoveSource'", 0);
	}

	/**
	 * Moves the listener relative to the current location.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the moveListener() method instead.
	 *
	 * @param x X offset.
	 * @param y Y offset.
	 * @param z Z offset.
	 */
	private void CommandMoveListener(float x, float y, float z) {
		if (soundLibrary != null) soundLibrary.moveListener(x, y, z);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandMoveListener'", 0);
	}

	/**
	 * Moves the listener to the specified location.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setListenerPosition() method instead.
	 *
	 * @param x Destination X coordinate.
	 * @param y Destination Y coordinate.
	 * @param z Destination Z coordinate.
	 */
	private void CommandSetListenerPosition(float x, float y, float z) {
		if (soundLibrary != null) soundLibrary.setListenerPosition(x, y, z);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandSetListenerPosition'", 0);
	}

	/**
	 * Turns the listener counterclockwise by "angle" radians around the y-axis,
	 * relative to the current angle.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the turnListener() method instead.
	 *
	 * @param angle radian offset.
	 */
	private void CommandTurnListener(float angle) {
		if (soundLibrary != null) soundLibrary.turnListener(angle);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandTurnListener'", 0);
	}

	/**
	 * Sets the listener's angle in radians around the y-axis.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setListenerAngle() method instead.
	 *
	 * @param angle radians.
	 */
	private void CommandSetListenerAngle(float angle) {
		if (soundLibrary != null) soundLibrary.setListenerAngle(angle);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandSetListenerAngle'", 0);
	}

	/**
	 * Sets the listener's orientation.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setListenerOrientation() method instead.
	 *
	 * @param lookX X coordinate of the (normalized) look-at vector.
	 * @param lookY Y coordinate of the (normalized) look-at vector.
	 * @param lookZ Z coordinate of the (normalized) look-at vector.
	 * @param upX   X coordinate of the (normalized) look-at vector.
	 * @param upY   Y coordinate of the (normalized) look-at vector.
	 * @param upZ   Z coordinate of the (normalized) look-at vector.
	 */
	private void CommandSetListenerOrientation(float lookX, float lookY, float lookZ, float upX, float upY, float upZ) {
		if (soundLibrary != null) soundLibrary.setListenerOrientation(lookX, lookY, lookZ, upX, upY, upZ);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandSetListenerOrientation'", 0);
	}

	/**
	 * Culls the specified source.  A culled source can not be played until it has
	 * been activated again.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the cull() method instead.
	 *
	 * @param sourceName Identifier for the source.
	 */
	private void CommandCull(String sourceName) {
		if (soundLibrary != null) soundLibrary.cull(sourceName);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandCull'", 0);
	}

	/**
	 * Activates a previously culled source, so it can be played again.    This
	 * method is used internally by SoundSystem for thread synchronization, and it
	 * can not be called directly - please use the activate() method instead.
	 *
	 * @param sourceName Identifier for the source.
	 */
	private void CommandActivate(String sourceName) {
		if (soundLibrary != null) soundLibrary.activate(sourceName);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandActivate'", 0);
	}

	/**
	 * Sets the overall volume, affecting all sources.  This method is used
	 * internally by SoundSystem for thread synchronization, and it can not be
	 * called directly - please use the setMasterVolume() method instead.
	 *
	 * @param value New volume, float value ( 0.0f - 1.0f ).
	 */
	private void CommandSetMasterVolume(float value) {
		if (soundLibrary != null) soundLibrary.setMasterVolume(value);
		else errorMessage("Variable 'soundLibrary' null in method 'CommandSetMasterVolume'", 0);
	}

	/**
	 * This method can be overridden by extended classes to be used for source
	 * management (culling and activating sources based on established rules).  One
	 * possible use for this method is sorting sources by distance, culling the
	 * furthest, and activating the closest.
	 * This method is automatically called on the CommandThread before processing
	 * queued commands, so you do not need to call it anywhere else.  Note: use
	 * methods cull() and activate() here, NOT CommandCull() and CommandActivate()
	 * (thread safety issue).
	 * IMPORTANT: Always use synchronized( SoundSystemConfig.THREAD_SYNC ) when
	 * manually manipulating sources from outside the Command Thread!
	 */
	protected void ManageSources() {
		// OVERRIDDEN METHODS MUST USE THIS:

//		 synchronized( SoundSystemConfig.THREAD_SYNC ) {
//		 // TODO: Sort the sources, cull and activate, etc.
//		 }
	}

	@Deprecated
	public boolean CommandQueue(CommandObject newCommand) {
		return commandQueue(newCommand);
	}

	/**
	 * Queues a command.
	 * If newCommand is null, all commands are dequeued and executed.
	 * This is automatically used by the sound system, so it is not
	 * likely that a user would ever need to use this method.
	 * See {@link CommandObject CommandObject} for more information
	 * about commands.
	 *
	 * @param newCommand Command to queue, or null to execute commands.
	 * @return True if more commands exist, false if queue is empty.
	 */
	public boolean commandQueue(CommandObject newCommand) {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			if (newCommand == null) {
				// New command is null - that means execute all queued commands.
				boolean activations = false;
				CommandObject commandObject;

				// Loop through the command queue:
				while (commandQueue != null && !commandQueue.isEmpty()) {
					// Grab the oldest command in the queue:
					commandObject = commandQueue.remove(0);
					// See what it is, and execute the proper Command method:
					if (commandObject != null) {
						switch (commandObject.Command) {
							case CommandObject.INITIALIZE:
								CommandInitialize();
								break;
							case CommandObject.LOAD_SOUND:
								CommandLoadSound((FilenameURL) commandObject.objectArgs[0]);
								break;
							case CommandObject.LOAD_DATA:
								CommandLoadSound((SoundBuffer) commandObject.objectArgs[0], commandObject.stringArgs[0]);
								break;
							case CommandObject.UNLOAD_SOUND:
								CommandUnloadSound(commandObject.stringArgs[0]);
								break;
							case CommandObject.QUEUE_SOUND:
								CommandQueueSound(commandObject.stringArgs[0], (FilenameURL) commandObject.objectArgs[0]);
								break;
							case CommandObject.DEQUEUE_SOUND:
								CommandDequeueSound(commandObject.stringArgs[0], commandObject.stringArgs[1]);
								break;
							case CommandObject.FADE_OUT:
								CommandFadeOut(commandObject.stringArgs[0], (FilenameURL) commandObject.objectArgs[0], commandObject.longArgs[0]);
								break;
							case CommandObject.FADE_OUT_IN:
								CommandFadeOutIn(commandObject.stringArgs[0], (FilenameURL) commandObject.objectArgs[0], commandObject.longArgs[0], commandObject.longArgs[1]);
								break;
							case CommandObject.CHECK_FADE_VOLUMES:
								CommandCheckFadeVolumes();
								break;
							case CommandObject.NEW_SOURCE:
								CommandNewSource(commandObject.boolArgs[0], commandObject.boolArgs[1], commandObject.boolArgs[2], commandObject.stringArgs[0], (FilenameURL) commandObject.objectArgs[0], commandObject.floatArgs[0], commandObject.floatArgs[1], commandObject.floatArgs[2], commandObject.intArgs[0], commandObject.floatArgs[3]);
								break;
							case CommandObject.RAW_DATA_STREAM:
								CommandRawDataStream((AudioFormat) commandObject.objectArgs[0], commandObject.boolArgs[0], commandObject.stringArgs[0], commandObject.floatArgs[0], commandObject.floatArgs[1], commandObject.floatArgs[2], commandObject.intArgs[0], commandObject.floatArgs[3]);
								break;
							case CommandObject.QUICK_PLAY:
								CommandQuickPlay(commandObject.boolArgs[0], commandObject.boolArgs[1], commandObject.boolArgs[2], commandObject.stringArgs[0], (FilenameURL) commandObject.objectArgs[0], commandObject.floatArgs[0], commandObject.floatArgs[1], commandObject.floatArgs[2], commandObject.intArgs[0], commandObject.floatArgs[3], commandObject.boolArgs[3]);
								break;
							case CommandObject.SET_POSITION:
								CommandSetPosition(commandObject.stringArgs[0], commandObject.floatArgs[0], commandObject.floatArgs[1], commandObject.floatArgs[2]);
								break;
							case CommandObject.SET_VOLUME:
								CommandSetVolume(commandObject.stringArgs[0], commandObject.floatArgs[0]);
								break;
							case CommandObject.SET_PITCH:
								CommandSetPitch(commandObject.stringArgs[0], commandObject.floatArgs[0]);
								break;
							case CommandObject.SET_PRIORITY:
								CommandSetPriority(commandObject.stringArgs[0], commandObject.boolArgs[0]);
								break;
							case CommandObject.SET_LOOPING:
								CommandSetLooping(commandObject.stringArgs[0], commandObject.boolArgs[0]);
								break;
							case CommandObject.SET_ATTENUATION:
								CommandSetAttenuation(commandObject.stringArgs[0], commandObject.intArgs[0]);
								break;
							case CommandObject.SET_DIST_OR_ROLL:
								CommandSetDistOrRoll(commandObject.stringArgs[0], commandObject.floatArgs[0]);
								break;
							case CommandObject.CHANGE_DOPPLER_FACTOR:
								CommandChangeDopplerFactor(commandObject.floatArgs[0]);
								break;
							case CommandObject.CHANGE_DOPPLER_VELOCITY:
								CommandChangeDopplerVelocity(commandObject.floatArgs[0]);
								break;
							case CommandObject.SET_VELOCITY:
								CommandSetVelocity(commandObject.stringArgs[0], commandObject.floatArgs[0], commandObject.floatArgs[1], commandObject.floatArgs[2]);
								break;
							case CommandObject.SET_LISTENER_VELOCITY:
								CommandSetListenerVelocity(commandObject.floatArgs[0], commandObject.floatArgs[1], commandObject.floatArgs[2]);
								break;
							// Methods related to playing sources must be processed
							// after cull/activate commands in order for source
							// management to work properly, so save them for
							// later:
							//------------------------------------------------------
							case CommandObject.PLAY:
								sourcePlayList.add(commandObject);
								break;
							case CommandObject.FEED_RAW_AUDIO_DATA:
								sourcePlayList.add(commandObject);
								break;
							//------------------------------------------------------
							case CommandObject.PAUSE:
								CommandPause(commandObject.stringArgs[0]);
								break;
							case CommandObject.STOP:
								CommandStop(commandObject.stringArgs[0]);
								break;
							case CommandObject.REWIND:
								CommandRewind(commandObject.stringArgs[0]);
								break;
							case CommandObject.FLUSH:
								CommandFlush(commandObject.stringArgs[0]);
								break;
							case CommandObject.CULL:
								CommandCull(commandObject.stringArgs[0]);
								break;
							case CommandObject.ACTIVATE:
								activations = true;
								CommandActivate(commandObject.stringArgs[0]);
								break;
							case CommandObject.SET_TEMPORARY:
								CommandSetTemporary(commandObject.stringArgs[0], commandObject.boolArgs[0]);
								break;
							case CommandObject.REMOVE_SOURCE:
								CommandRemoveSource(commandObject.stringArgs[0]);
								break;
							case CommandObject.MOVE_LISTENER:
								CommandMoveListener(commandObject.floatArgs[0], commandObject.floatArgs[1], commandObject.floatArgs[2]);
								break;
							case CommandObject.SET_LISTENER_POSITION:
								CommandSetListenerPosition(commandObject.floatArgs[0], commandObject.floatArgs[1], commandObject.floatArgs[2]);
								break;
							case CommandObject.TURN_LISTENER:
								CommandTurnListener(commandObject.floatArgs[0]);
								break;
							case CommandObject.SET_LISTENER_ANGLE:
								CommandSetListenerAngle(commandObject.floatArgs[0]);
								break;
							case CommandObject.SET_LISTENER_ORIENTATION:
								CommandSetListenerOrientation(commandObject.floatArgs[0], commandObject.floatArgs[1], commandObject.floatArgs[2], commandObject.floatArgs[3], commandObject.floatArgs[4], commandObject.floatArgs[5]);
								break;
							case CommandObject.SET_MASTER_VOLUME:
								CommandSetMasterVolume(commandObject.floatArgs[0]);
								break;
							case CommandObject.NEW_LIBRARY:
								CommandNewLibrary(commandObject.classArgs[0]);
								break;
							// If we don't recognize the command, just skip it:
							default:
								break;
						}
					}
				}

				// If any sources were reactivated, check if they need to be
				// replayed:
				if (activations) soundLibrary.replaySources();

				// Now that we have the correct sources culled and activated, we
				// can start playing sources.  Loop through the playlist and
				// execute the commands:
				while (sourcePlayList != null && !sourcePlayList.isEmpty()) {
					// Grab the oldest command in the queue:
					commandObject = sourcePlayList.remove(0);
					if (commandObject != null) {
						// See what it is, and execute the proper Command method:
						switch (commandObject.Command) {
							case CommandObject.PLAY:
								CommandPlay(commandObject.stringArgs[0]);
								break;
							case CommandObject.FEED_RAW_AUDIO_DATA:
								CommandFeedRawAudioData(commandObject.stringArgs[0], commandObject.buffer);
								break;
						}
					}
				}

				return (commandQueue != null && !commandQueue.isEmpty());
			} else {
				// make sure the commandQueue exists:
				if (commandQueue == null) return false;
				// queue a new command
				commandQueue.add(newCommand);
				// Of course there is something in the list now, since we just
				// added it:
				return true;
			}
		}
	}

	/**
	 * Searches for and removes any temporary sources that have finished
	 * playing.  This method is used internally by SoundSystem, and it is
	 * unlikely that the user will ever need to use it.
	 */
	public void removeTemporarySources() {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			if (soundLibrary != null) soundLibrary.removeTemporarySources();
		}
	}

	/**
	 * Returns true if the specified source is playing.
	 *
	 * @param sourceName Unique identifier of the source to check.
	 * @return True or false.
	 */
	public boolean playing(String sourceName) {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			if (soundLibrary == null) return false;

			Source src = soundLibrary.getSources().get(sourceName);

			if (src == null) return false;

			return src.playing();
		}
	}

	/**
	 * @return true if anything is currently playing.
	 */
	public boolean playing() {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			if (soundLibrary == null) return false;

			HashMap<String, Source> sourceMap = soundLibrary.getSources();
			if (sourceMap == null) return false;

			Set<String> keys = sourceMap.keySet();
			Iterator<String> iterator = keys.iterator();
			String sourceName;
			Source source;

			while (iterator.hasNext()) {
				sourceName = iterator.next();
				source = sourceMap.get(sourceName);
				if (source != null) if (source.playing()) return true;
			}

			return false;
		}
	}

	/**
	 * Copies and returns the peripheral information from a map of sources.  This
	 * method is used internally by SoundSystem, and it is unlikely that the user
	 * will ever need to use it.
	 *
	 * @param sourceMap Sources to copy.
	 * @return New map of sources.
	 */
	private HashMap<String, Source> copySources(HashMap<String, Source> sourceMap) {
		Set<String> keys = sourceMap.keySet();
		Iterator<String> iterator = keys.iterator();
		String sourceName;
		Source source;

		// New map of generic source data:
		HashMap<String, Source> returnMap = new HashMap<>();


		// loop through and store information from all the sources:
		while (iterator.hasNext()) {
			sourceName = iterator.next();
			source = sourceMap.get(sourceName);
			if (source != null) returnMap.put(sourceName, new Source(source, null));
		}
		return returnMap;
	}

	/**
	 * Checks if the specified library type is compatible.
	 *
	 * @param libraryClass Library type to check.
	 * @return True or false.
	 */
	public static boolean libraryCompatible(Class<?> libraryClass) {
		// create the message logger:
		SoundSystemLogger logger = SoundSystemConfig.getLogger();
		// if the user didn't create one, then do it now:
		if (logger == null) {
			logger = new SoundSystemLogger();
			SoundSystemConfig.setLogger(logger);
		}
		logger.message("", 0);
		logger.message("Checking if " + SoundSystemConfig.getLibraryTitle(libraryClass) + " is compatible...", 0);

		boolean comp = SoundSystemConfig.libraryCompatible(libraryClass);

		if (comp) logger.message("...yes", 1);
		else logger.message("...no", 1);

		return comp;
	}

	/**
	 * Returns the currently loaded library, or -1 if none.
	 *
	 * @return Global library identifier
	 */
	public static Class<?> currentLibrary() {
		return (currentLibrary(GET, null));
	}

	/**
	 * Returns false if a sound library is busy initializing.
	 *
	 * @return True or false.
	 */
	public static boolean initialized() {
		return (initialized(GET, XXX));
	}

	/**
	 * Returns the last SoundSystemException thrown, or null if none.
	 *
	 * @return The last exception.
	 */
	public static SoundSystemException getLastException() {
		return (lastException(GET, null));
	}

	/**
	 * Stores a SoundSystemException which can be retrieved later with the
	 * 'getLastException' method.
	 *
	 * @param e Exception to store.
	 */
	public static void setException(SoundSystemException e) {
		lastException(SET, e);
	}

	/**
	 * Sets or returns the value of boolean 'initialized'.
	 *
	 * @param action Action to perform (GET or SET).
	 * @param value  New value if action is SET, otherwise XXX.
	 * @return value of boolean 'initialized'.
	 */
	private static boolean initialized(boolean action, boolean value) {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			if (action == SET) initialized = value;
			return initialized;
		}
	}

	/**
	 * Sets or returns the value of boolean 'initialized'.
	 *
	 * @param action Action to perform (GET or SET).
	 * @param value  New value if action is SET, otherwise XXX.
	 * @return value of boolean 'initialized'.
	 */
	private static Class<?> currentLibrary(boolean action, Class<?> value) {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			if (action == SET) currentLibrary = value;
			return currentLibrary;
		}
	}

	/**
	 * Sets or returns the error code for the last error that occurred.  If no
	 * errors have occurred, returns SoundSystem.ERROR_NONE
	 *
	 * @param action Action to perform (GET or SET).
	 * @param e      New exception if action is SET, otherwise XXX.
	 * @return Last SoundSystemException thrown.
	 */
	private static SoundSystemException lastException(boolean action, SoundSystemException e) {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			if (action == SET) lastException = e;
			return lastException;
		}
	}

	/**
	 * Sleeps for the specified number of milliseconds.
	 */
	protected static void snooze(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException ignored) {
		}
	}

	/**
	 * Prints a message.
	 *
	 * @param message Message to print.
	 * @param indent  Number of tabs to indent the message.
	 */
	protected void message(String message, int indent) {
		logger.message(message, indent);
	}

	/**
	 * Prints an important message.
	 *
	 * @param message Message to print.
	 * @param indent  Number of tabs to indent the message.
	 */
	protected void importantMessage(String message, int indent) {
		logger.importantMessage(message, indent);
	}

	/**
	 * Prints the specified message if error is true.
	 *
	 * @param error   True or False.
	 * @param message Message to print if error is true.
	 * @param indent  Number of tabs to indent the message.
	 * @return True if error is true.
	 */
	protected boolean errorCheck(boolean error, String message, int indent) {
		return logger.errorCheck(error, className, message, indent);
	}

	/**
	 * Prints an error message.
	 *
	 * @param message Message to print.
	 * @param indent  Number of tabs to indent the message.
	 */
	protected void errorMessage(String message, int indent) {
		logger.errorMessage(className, message, indent);
	}
}
