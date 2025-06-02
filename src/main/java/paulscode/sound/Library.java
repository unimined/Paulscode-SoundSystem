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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.sound.sampled.AudioFormat;

/**
 * The Library class is the class from which all library types are extended.
 * It provides generic methods for interfacing with the audio libraries
 * supported by the SoundSystem.  Specific libraries should extend this class
 * and override the necessary methods.  For consistent naming conventions, each
 * subclass should have the name prefix "Library".
 * <p>
 * This class may also be used as the "No Sound Library" (i.e. silent mode) if
 * no other audio libraries are supported by the host machine, or to mute all
 * sound.
 */
@SuppressWarnings({"unused", "FieldMayBeFinal", "UnusedAssignment", "UnusedReturnValue"})
public class Library {
	/**
	 * Processes status messages, warnings, and error messages.
	 */
	private SoundSystemLogger logger;

	/**
	 * Position and orientation of the listener.
	 */
	protected ListenerData listener;

	/**
	 * Map containing sound file data for easy lookup by filename / identifier.
	 */
	protected HashMap<String, SoundBuffer> bufferMap;

	/**
	 * Map containing all created sources for easy look-up by name.
	 */
	protected HashMap<String, Source> sourceMap;  // (name, source data) pairs

	/**
	 * Interface through which MIDI files can be played.
	 */
	private MidiChannel midiChannel;

	/**
	 * Array containing maximum number of non-streaming audio channels.
	 */
	protected List<Channel> streamingChannels;

	/**
	 * Array containing maximum number of non-streaming audio channels.
	 */
	protected List<Channel> normalChannels;

	/**
	 * Source name last played on each streaming channel.
	 */
	private String[] streamingChannelSourceNames;

	/**
	 * Source name last played on each non-streaming channel.
	 */
	private String[] normalChannelSourceNames;

	/**
	 * Increments through the steaming channel list as new sources are played.
	 */
	private int nextStreamingChannel = 0;

	/**
	 * Increments through the non-steaming channel list as new sources are played.
	 */
	private int nextNormalChannel = 0;

	/**
	 * Handles processing for all streaming sources.
	 */
	protected StreamThread streamThread;

	/**
	 * Whether the library requires reversal of audio data byte order.
	 */
	protected boolean reverseByteOrder = false;

	/**
	 * Constructor: Instantiates the source map and listener information.  NOTES:
	 * The 'super()' method should be at the top of constructors for all extended
	 * classes.  The variable 'libraryType' should be given a new value in the
	 * constructors for all extended classes.
	 */
	public Library() throws SoundSystemException {
		// grab a handle to the message logger:
		logger = SoundSystemConfig.getLogger();

		// instantiate the buffer map:
		bufferMap = new HashMap<>();

		// instantiate the source map:
		sourceMap = new HashMap<>();

		listener = new ListenerData(0.0f, 0.0f, 0.0f,  // position
				0.0f, 0.0f, -1.0f, // look-at direction
				0.0f, 1.0f, 0.0f,  // up direction
				0.0f);            // angle

		streamingChannels = new LinkedList<>();
		normalChannels = new LinkedList<>();
		streamingChannelSourceNames = new String[SoundSystemConfig.getNumberStreamingChannels()];
		normalChannelSourceNames = new String[SoundSystemConfig.getNumberNormalChannels()];

		streamThread = new StreamThread();
		streamThread.start();
	}


	/* ########################################################################## */
	/*                        BEGIN OVERRIDE METHODS                              */
	/*                                                                            */
	/*         The following methods should be overridden as required             */
	/* ########################################################################## */

	/**
	 * Stops all sources, shuts down sound library, and removes references to all
	 * instantiated objects.
	 */
	public void cleanup() {
		streamThread.kill();
		streamThread.interrupt();

		// wait up to 5 seconds for stream thread to end:
		for (int i = 0; i < 50; i++) {
			if (!streamThread.alive()) break;
			try {
				Thread.sleep(100);
			} catch (Exception ignored) {
			}
		}

		if (streamThread.alive()) {
			errorMessage("Stream thread did not die!");
			message("Ignoring errors... continuing clean-up.");
		}

		if (midiChannel != null) {
			midiChannel.cleanup();
			midiChannel = null;
		}

		Channel channel;
		if (streamingChannels != null) {
			while (!streamingChannels.isEmpty()) {
				channel = streamingChannels.remove(0);
				channel.close();
				channel.cleanup();
				channel = null;
			}
			streamingChannels = null;
		}
		if (normalChannels != null) {
			while (!normalChannels.isEmpty()) {
				channel = normalChannels.remove(0);
				channel.close();
				channel.cleanup();
				channel = null;
			}
			normalChannels = null;
		}

		Set<String> keys = sourceMap.keySet();
		Iterator<String> iterator = keys.iterator();
		String sourceName;
		Source source;

		// loop through and cleanup all the sources:
		while (iterator.hasNext()) {
			sourceName = iterator.next();
			source = sourceMap.get(sourceName);
			if (source != null) source.cleanup();
		}
		sourceMap.clear();
		sourceMap = null;

		listener = null;
		streamThread = null;
	}

	/**
	 * Initializes the sound library.
	 */
	public void init() throws SoundSystemException {
		Channel channel;

		// create the streaming channels:
		for (int x = 0; x < SoundSystemConfig.getNumberStreamingChannels(); x++) {
			channel = createChannel(SoundSystemConfig.TYPE_STREAMING);
			if (channel == null) break;
			streamingChannels.add(channel);
		}
		// create the non-streaming channels:
		for (int x = 0; x < SoundSystemConfig.getNumberNormalChannels(); x++) {
			channel = createChannel(SoundSystemConfig.TYPE_NORMAL);
			if (channel == null) break;
			normalChannels.add(channel);
		}
	}

	/**
	 * Checks if the no-sound library type is compatible.
	 *
	 * @return True or false.
	 */
	public static boolean libraryCompatible() {
		return true;  // the no-sound library is always compatible.
	}

	/**
	 * Creates a new channel of the specified type (normal or streaming).  Possible
	 * values for channel type can be found in the
	 * {@link paulscode.sound.SoundSystemConfig SoundSystemConfig} class.
	 *
	 * @param type Type of channel.
	 * @return The new channel.
	 */
	protected Channel createChannel(int type) {
		return new Channel(type);
	}

	/**
	 * Pre-loads a sound into memory.
	 *
	 * @param filenameURL Filename/URL of the sound file to load.
	 * @return True if the sound loaded properly.
	 */
	public boolean loadSound(FilenameURL filenameURL) {
		return true;
	}

	/**
	 * Saves the specified sample data, under the specified identifier.  This
	 * identifier can be later used in place of 'filename' parameters to reference
	 * the sample data.
	 *
	 * @param buffer     the sample data and audio format to save.
	 * @param identifier What to call the sample.
	 * @return True if there weren't any problems.
	 */
	public boolean loadSound(SoundBuffer buffer, String identifier) {
		return true;
	}

	/**
	 * Returns the filenames of all previously loaded sounds.
	 *
	 * @return LinkedList of String filenames.
	 */
	public LinkedList<String> getAllLoadedFilenames() {
		// update the volume of all sources:
		return new LinkedList<>(bufferMap.keySet());
	}

	/**
	 * @deprecated use {@link #getAllSourceNames()} instead
	 */
	@Deprecated
	@SuppressWarnings("SpellCheckingInspection")
	public LinkedList<String> getAllSourcenames() {
		return getAllSourceNames();
	}

	/**
	 * Returns the source names of all sources.
	 *
	 * @return LinkedList of String source names.
	 */
	public LinkedList<String> getAllSourceNames() {
		LinkedList<String> sourceNames = new LinkedList<>();
		Set<String> keys = sourceMap.keySet();
		Iterator<String> iterator = keys.iterator();

		if (midiChannel != null) sourceNames.add(midiChannel.getSourceName());

		// loop through and update the volume of all sources:
		while (iterator.hasNext()) {
			sourceNames.add(iterator.next());
		}

		return sourceNames;
	}

	/**
	 * Removes a preloaded sound from memory.  This is a good method to use for
	 * freeing up memory after a large sound file is no longer needed.  NOTE: the
	 * source will remain in memory after this method has been called, for as long
	 * as the sound is attached to an existing source.
	 *
	 * @param filename Filename/identifier of the sound file to unload.
	 */
	public void unloadSound(String filename) {
		bufferMap.remove(filename);
	}

	/**
	 * Opens a direct line for streaming audio data.
	 *
	 * @param audioFormat Format that the data will be in.
	 * @param sourceName  A unique identifier for this source.
	 *                    Two sources may not use the same source name.
	 * @param posX        X position for this source.
	 * @param posY        Y position for this source.
	 * @param posZ        Z position for this source.
	 * @param attModel    Attenuation model to use.
	 * @param distOrRoll  Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void rawDataStream(AudioFormat audioFormat, boolean priority, String sourceName, float posX, float posY, float posZ, int attModel, float distOrRoll) {
		sourceMap.put(sourceName, new Source(audioFormat, priority, sourceName, posX, posY, posZ, attModel, distOrRoll));
	}

	/**
	 * Creates a new source using the specified information.
	 *
	 * @param priority    Setting this to true will prevent other sounds from overriding this one.
	 * @param toStream    Setting this to true will load the sound in pieces rather than all at once.
	 * @param toLoop      Should this source loop, or play only once.
	 * @param sourceName  A unique identifier for this source.
	 *                    Two sources may not use the same source name.
	 * @param filenameURL Filename/URL of the sound file to play at this source.
	 * @param posX        X position for this source.
	 * @param posY        Y position for this source.
	 * @param posZ        Z position for this source.
	 * @param attModel    Attenuation model to use.
	 * @param distOrRoll  Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newSource(boolean priority, boolean toStream, boolean toLoop, String sourceName, FilenameURL filenameURL, float posX, float posY, float posZ, int attModel, float distOrRoll) {
		sourceMap.put(sourceName, new Source(priority, toStream, toLoop, sourceName, filenameURL, null, posX, posY, posZ, attModel, distOrRoll, false));
	}

	/**
	 * Creates and immediately plays a new source that will be removed when it
	 * finishes playing.
	 *
	 * @param priority    Setting this to true will prevent other sounds from overriding this one.
	 * @param toStream    Setting this to true will load the sound in pieces rather than all at once.
	 * @param toLoop      Should this source loop, or play only once.
	 * @param sourceName  A unique identifier for this source.
	 *                    Two sources may not use the same source name.
	 * @param filenameURL The filename/URL of the sound file to play at this source.
	 * @param posX        X position for this source.
	 * @param posY        Y position for this source.
	 * @param posZ        Z position for this source.
	 * @param attModel    Attenuation model to use.
	 * @param distOrRoll  Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void quickPlay(boolean priority, boolean toStream, boolean toLoop, String sourceName, FilenameURL filenameURL, float posX, float posY, float posZ, int attModel, float distOrRoll, boolean tmp) {
		sourceMap.put(sourceName, new Source(priority, toStream, toLoop, sourceName, filenameURL, null, posX, posY, posZ, attModel, distOrRoll, tmp));
	}

	/**
	 * Defines whether the source should be removed after it finishes playing.
	 *
	 * @param sourceName The source's name.
	 * @param temporary  True or False.
	 */
	public void setTemporary(String sourceName, boolean temporary) {
		Source mySource = sourceMap.get(sourceName);
		if (mySource != null) mySource.setTemporary(temporary);
	}

	/**
	 * Changes the specified source's position.
	 *
	 * @param sourceName The source's name.
	 * @param x          Destination X coordinate.
	 * @param y          Destination Y coordinate.
	 * @param z          Destination Z coordinate.
	 */
	public void setPosition(String sourceName, float x, float y, float z) {
		Source mySource = sourceMap.get(sourceName);
		if (mySource != null) mySource.setPosition(x, y, z);
	}

	/**
	 * Sets the specified source's priority factor.
	 * A priority source will not be overridden if there are too many sources playing at once.
	 *
	 * @param sourceName The source's name.
	 * @param pri        True or False.
	 */
	public void setPriority(String sourceName, boolean pri) {
		Source mySource = sourceMap.get(sourceName);
		if (mySource != null) mySource.setPriority(pri);
	}

	/**
	 * Sets the specified source's looping parameter.
	 *
	 * @param sourceName The source's name.
	 * @param loop       if false, the source will play once and stop
	 */
	public void setLooping(String sourceName, boolean loop) {
		Source mySource = sourceMap.get(sourceName);
		if (mySource != null) mySource.setLooping(loop);
	}

	/**
	 * Sets the specified source's attenuation model.
	 *
	 * @param sourceName The source's name.
	 * @param model      Attenuation model to use.
	 */
	public void setAttenuation(String sourceName, int model) {
		Source mySource = sourceMap.get(sourceName);
		if (mySource != null) mySource.setAttenuation(model);
	}

	/**
	 * Sets the specified source's fade distance or roll-off factor.
	 *
	 * @param sourceName The source's name.
	 * @param dr         Fade distance or roll-off factor.
	 */
	public void setDistOrRoll(String sourceName, float dr) {
		Source mySource = sourceMap.get(sourceName);
		if (mySource != null) mySource.setDistOrRoll(dr);
	}

	/**
	 * Sets the specified source's velocity, for use in Doppler effect.
	 *
	 * @param sourceName The source's name.
	 * @param x          Velocity along world x-axis.
	 * @param y          Velocity along world y-axis.
	 * @param z          Velocity along world z-axis.
	 */
	public void setVelocity(String sourceName, float x, float y, float z) {
		Source mySource = sourceMap.get(sourceName);
		if (mySource != null) mySource.setVelocity(x, y, z);
	}

	/**
	 * Sets the listener's velocity, for use in Doppler effect.
	 *
	 * @param x Velocity along world x-axis.
	 * @param y Velocity along world y-axis.
	 * @param z Velocity along world z-axis.
	 */
	public void setListenerVelocity(float x, float y, float z) {
		listener.setVelocity(x, y, z);
	}

	/**
	 * Notifies the underlying library that the Doppler parameters have changed.
	 */
	public void dopplerChanged() {
	}

	/**
	 * Returns the number of milliseconds since the specified source began playing.
	 *
	 * @return milliseconds, or -1 if not playing or unable to calculate
	 */
	public float millisecondsPlayed(String sourceName) {
		if (sourceName == null || sourceName.isEmpty()) {
			errorMessage("Source name not specified in method 'millisecondsPlayed'");
			return -1;
		}

		if (midiSourceName(sourceName)) {
			errorMessage("Unable to calculate milliseconds for MIDI source.");
			return -1;
		} else {
			Source source = sourceMap.get(sourceName);
			if (source == null) {
				errorMessage("Source '" + sourceName + "' not found in method 'millisecondsPlayed'");
				return -1;
			}
			return source.millisecondsPlayed();
		}
	}

	/**
	 * Feeds raw data through the specified source.
	 * The source must be a streaming source,
	 * and it can not be already associated with a file or URL to stream from.
	 *
	 * @param sourceName Name of the streaming source to play from.
	 * @param buffer     Byte buffer containing raw audio data to stream.
	 * @return Number of prior buffers that have been processed, or -1 if unable to queue the buffer (if the source was culled, for example).
	 */
	public int feedRawAudioData(String sourceName, byte[] buffer) {
		if (sourceName == null || sourceName.isEmpty()) {
			errorMessage("Source name not specified in method 'feedRawAudioData'");
			return -1;
		}

		if (midiSourceName(sourceName)) {
			errorMessage("Raw audio data can not be fed to the MIDI channel.");
			return -1;
		} else {
			Source source = sourceMap.get(sourceName);
			if (source == null) {
				errorMessage("Source '" + sourceName + "' not found in method 'feedRawAudioData'");
			}
			return feedRawAudioData(source, buffer);
		}
	}

	/**
	 * Feeds raw data through the specified source.
	 * The source must be a streaming source,
	 * and it can not be already associated with a file or URL to stream from.
	 *
	 * @param source Streaming source to play from.
	 * @param buffer Byte buffer containing raw audio data to stream.
	 * @return Number of prior buffers that have been processed, or -1 if unable to queue the buffer (if the source was culled, for example).
	 */
	public int feedRawAudioData(Source source, byte[] buffer) {
		if (source == null) {
			errorMessage("Source parameter null in method 'feedRawAudioData'");
			return -1;
		}
		if (!source.toStream) {
			errorMessage("Only a streaming source may be specified in method 'feedRawAudioData'");
			return -1;
		}
		if (!source.rawDataStream) {
			errorMessage("Streaming source already associated with a file or URL in method 'feedRawAudioData'");
			return -1;
		}

		if (!source.playing() || source.channel == null) {
			Channel channel;
			if (source.channel != null && (source.channel.attachedSource == source)) channel = source.channel;
			else channel = getNextChannel(source);

			int processed = source.feedRawAudioData(channel, buffer);
			channel.attachedSource = source;
			streamThread.watch(source);
			streamThread.interrupt();
			return processed;
		}

		return (source.feedRawAudioData(source.channel, buffer));
	}

	/**
	 * Looks up the specified source and plays it.
	 *
	 * @param sourceName Name of the source to play.
	 */
	public void play(String sourceName) {
		if (sourceName == null || sourceName.isEmpty()) {
			errorMessage("Source name not specified in method 'play'");
			return;
		}

		if (midiSourceName(sourceName)) {
			midiChannel.play();
		} else {
			Source source = sourceMap.get(sourceName);
			if (source == null) {
				errorMessage("Source '" + sourceName + "' not found in method 'play'");
			}
			play(source);
		}
	}

	/**
	 * Plays the specified source.
	 *
	 * @param source The source to play.
	 */
	public void play(Source source) {
		if (source == null) return;

		// raw data streams will automatically play when data is sent to them,
		// so no need to do anything here.
		if (source.rawDataStream) return;

		if (!source.active()) return;

		if (!source.playing()) {
			Channel channel = getNextChannel(source);

			if (channel != null) {
				if (source.channel != null && source.channel.attachedSource != source) source.channel = null;
				channel.attachedSource = source;
				source.play(channel);
				if (source.toStream) {
					streamThread.watch(source);
					streamThread.interrupt();
				}
			}
		}
	}

	/**
	 * Stops the specified source.
	 *
	 * @param sourceName The source's name.
	 */
	public void stop(String sourceName) {
		if (sourceName == null || sourceName.isEmpty()) {
			errorMessage("Source name not specified in method 'stop'");
			return;
		}
		if (midiSourceName(sourceName)) {
			midiChannel.stop();
		} else {
			Source mySource = sourceMap.get(sourceName);
			if (mySource != null) mySource.stop();
		}
	}

	/**
	 * Pauses the specified source.
	 *
	 * @param sourceName The source's name.
	 */
	public void pause(String sourceName) {
		if (sourceName == null || sourceName.isEmpty()) {
			errorMessage("Source name not specified in method 'stop'");
			return;
		}
		if (midiSourceName(sourceName)) {
			midiChannel.pause();
		} else {
			Source mySource = sourceMap.get(sourceName);
			if (mySource != null) mySource.pause();
		}
	}

	/**
	 * Rewinds the specified source.
	 *
	 * @param sourceName The source's name.
	 */
	public void rewind(String sourceName) {
		if (midiSourceName(sourceName)) {
			midiChannel.rewind();
		} else {
			Source mySource = sourceMap.get(sourceName);
			if (mySource != null) mySource.rewind();
		}
	}

	/**
	 * Clears all previously queued data from a stream.
	 *
	 * @param sourceName The source's name.
	 */
	public void flush(String sourceName) {
		if (midiSourceName(sourceName)) errorMessage("You can not flush the MIDI channel");
		else {
			Source mySource = sourceMap.get(sourceName);
			if (mySource != null) mySource.flush();
		}
	}

	/**
	 * Culls the specified source.  A culled source will not play until it has been
	 * activated again.
	 *
	 * @param sourceName The source's name.
	 */
	public void cull(String sourceName) {
		Source mySource = sourceMap.get(sourceName);
		if (mySource != null) mySource.cull();
	}

	/**
	 * Activates a previously culled source, so it can be played again.
	 *
	 * @param sourceName The source's name.
	 */
	public void activate(String sourceName) {
		Source mySource = sourceMap.get(sourceName);
		if (mySource != null) {
			mySource.activate();
			if (mySource.toPlay) play(mySource);
		}
	}

	/**
	 * Sets the overall volume to the specified value, affecting all sources.
	 *
	 * @param value New volume, float value ( 0.0f - 1.0f ).
	 */
	public void setMasterVolume(float value) {
		SoundSystemConfig.setMasterGain(value);
		if (midiChannel != null) midiChannel.resetGain();
	}

	/**
	 * Manually sets the specified source's volume.
	 *
	 * @param sourceName The source's name.
	 * @param value      A float value ( 0.0f - 1.0f ).
	 */
	public void setVolume(String sourceName, float value) {
		if (midiSourceName(sourceName)) {
			midiChannel.setVolume(value);
		} else {
			Source mySource = sourceMap.get(sourceName);
			if (mySource != null) {
				float newVolume = value;
				if (newVolume < 0.0f) newVolume = 0.0f;
				else if (newVolume > 1.0f) newVolume = 1.0f;

				mySource.sourceVolume = newVolume;
				mySource.positionChanged();
			}
		}
	}

	/**
	 * Returns the current volume of the specified source, or zero if the specified
	 * source was not found.
	 *
	 * @param sourceName Source to read volume from.
	 * @return Float value representing the source volume (0.0f - 1.0f).
	 */
	public float getVolume(String sourceName) {
		if (midiSourceName(sourceName)) {
			return midiChannel.getVolume();
		} else {
			Source mySource = sourceMap.get(sourceName);
			if (mySource != null) return mySource.sourceVolume;
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
		if (!midiSourceName(sourceName)) {
			Source mySource = sourceMap.get(sourceName);
			if (mySource != null) {
				float newPitch = value;
				if (newPitch < 0.5f) newPitch = 0.5f;
				else if (newPitch > 2.0f) newPitch = 2.0f;

				mySource.setPitch(newPitch);
				mySource.positionChanged();
			}
		}
	}

	/**
	 * Returns the pitch of the specified source.
	 *
	 * @param sourceName The source's name.
	 * @return Float value representing the source pitch (0.5f - 2.0f).
	 */
	public float getPitch(String sourceName) {
		if (!midiSourceName(sourceName)) {
			Source mySource = sourceMap.get(sourceName);
			if (mySource != null) return mySource.getPitch();
		}
		return 1.0f;
	}

	/**
	 * Moves the listener relative to the current position.
	 *
	 * @param x X offset.
	 * @param y Y offset.
	 * @param z Z offset.
	 */
	public void moveListener(float x, float y, float z) {
		setListenerPosition(listener.position.x + x, listener.position.y + y, listener.position.z + z);
	}

	/**
	 * Changes the listener's position.
	 *
	 * @param x Destination X coordinate.
	 * @param y Destination Y coordinate.
	 * @param z Destination Z coordinate.
	 */
	public void setListenerPosition(float x, float y, float z) {
		// update listener's position
		listener.setPosition(x, y, z);

		Set<String> keys = sourceMap.keySet();
		Iterator<String> iterator = keys.iterator();
		String sourceName;
		Source source;

		// loop through and update the volume of all sources:
		while (iterator.hasNext()) {
			sourceName = iterator.next();
			source = sourceMap.get(sourceName);
			if (source != null) source.positionChanged();
		}
	}

	/**
	 * Turn the listener 'angle' radians counterclockwise around the y-Axis,
	 * relative to the current angle.
	 *
	 * @param angle Angle in radians.
	 */
	public void turnListener(float angle) {
		setListenerAngle(listener.angle + angle);

		Set<String> keys = sourceMap.keySet();
		Iterator<String> iterator = keys.iterator();
		String sourceName;
		Source source;

		// loop through and update the volume of all sources:
		while (iterator.hasNext()) {
			sourceName = iterator.next();
			source = sourceMap.get(sourceName);
			if (source != null) source.positionChanged();
		}
	}

	/**
	 * Changes the listeners orientation to the specified 'angle' radians
	 * counterclockwise around the y-Axis.
	 *
	 * @param angle Angle in radians.
	 */
	public void setListenerAngle(float angle) {
		listener.setAngle(angle);

		Set<String> keys = sourceMap.keySet();
		Iterator<String> iterator = keys.iterator();
		String sourceName;
		Source source;

		// loop through and update the volume of all sources:
		while (iterator.hasNext()) {
			sourceName = iterator.next();
			source = sourceMap.get(sourceName);
			if (source != null) source.positionChanged();
		}
	}

	/**
	 * Changes the listeners orientation using the specified coordinates.
	 *
	 * @param lookX X element of the look-at direction.
	 * @param lookY Y element of the look-at direction.
	 * @param lookZ Z element of the look-at direction.
	 * @param upX   X element of the up direction.
	 * @param upY   Y element of the up direction.
	 * @param upZ   Z element of the up direction.
	 */
	public void setListenerOrientation(float lookX, float lookY, float lookZ, float upX, float upY, float upZ) {
		listener.setOrientation(lookX, lookY, lookZ, upX, upY, upZ);

		Set<String> keys = sourceMap.keySet();
		Iterator<String> iterator = keys.iterator();
		String sourceName;
		Source source;

		// loop through and update the volume of all sources:
		while (iterator.hasNext()) {
			sourceName = iterator.next();
			source = sourceMap.get(sourceName);
			if (source != null) source.positionChanged();
		}
	}

	/**
	 * Changes the listeners position and orientation using the specified listener
	 * data.
	 *
	 * @param l Listener data to use.
	 */
	public void setListenerData(ListenerData l) {
		listener.setData(l);
	}

	/**
	 * Creates sources based on the source map provided.
	 *
	 * @param srcMap Sources to copy.
	 */
	public void copySources(HashMap<String, Source> srcMap) {
		if (srcMap == null) return;
		Set<String> keys = srcMap.keySet();
		Iterator<String> iterator = keys.iterator();
		String sourceName;
		Source srcData;

		// remove any existing sources before starting:
		sourceMap.clear();

		// loop through and copy all the sources:
		while (iterator.hasNext()) {
			sourceName = iterator.next();
			srcData = srcMap.get(sourceName);
			if (srcData != null) {
				loadSound(srcData.filenameURL);
				sourceMap.put(sourceName, new Source(srcData, null));
			}
		}
	}

	/**
	 * Stops and deletes the specified source.
	 *
	 * @param sourceName The source's name.
	 */
	public void removeSource(String sourceName) {
		Source mySource = sourceMap.get(sourceName);
		if (mySource != null) {
			// if this is a streaming source just mark it removed - https://github.com/MinecraftForge/MinecraftForge/pull/4765
			if (mySource.toStream) mySource.removed = true;
			else mySource.cleanup(); // end the source, free memory
		}
		sourceMap.remove(sourceName);
	}

	/**
	 * Searches for and removes all temporary sources that have finished playing.
	 */
	public void removeTemporarySources() {
		Set<String> keys = sourceMap.keySet();
		Iterator<String> iterator = keys.iterator();
		String sourceName;
		Source srcData;

		// loop through and cleanup all the sources:
		while (iterator.hasNext()) {
			sourceName = iterator.next();
			srcData = sourceMap.get(sourceName);
			if ((srcData != null) && (srcData.temporary) && (!srcData.playing())) {
				srcData.cleanup(); // end the source, free memory
				iterator.remove();
			}
		}
	}

	/* ########################################################################## */
	/*                         END OVERRIDE METHODS                               */
	/* ########################################################################## */

	/**
	 * Returns a handle to the next available channel.  If the specified
	 * source is a normal source, a normal channel is returned, and if it is a
	 * streaming source, then a streaming channel is returned.  If all channels of
	 * the required type are currently playing, then the next channel playing a
	 * non-priority source is returned.  If no channels are available (i.e. they
	 * are all playing priority sources) then getNextChannel returns null.
	 *
	 * @param source Source to find a channel for.
	 * @return The next available channel, or null.
	 */
	private Channel getNextChannel(Source source) {
		if (source == null) return null;

		String sourceName = source.sourcename;
		if (sourceName == null) return null;

		int x;
		int channels;
		int nextChannel;
		List<Channel> channelList;
		String[] sourceNames;
		String name;

		if (source.toStream) {
			nextChannel = nextStreamingChannel;
			channelList = streamingChannels;
			sourceNames = streamingChannelSourceNames;
		} else {
			nextChannel = nextNormalChannel;
			channelList = normalChannels;
			sourceNames = normalChannelSourceNames;
		}

		channels = channelList.size();

		// Check if this source is already on a channel:
		for (x = 0; x < channels; x++) {
			if (sourceName.equals(sourceNames[x])) return channelList.get(x);
		}

		int n = nextChannel;
		Source src;
		// Play on the next new or non-playing channel:
		for (x = 0; x < channels; x++) {
			name = sourceNames[n];
			if (name == null) src = null;
			else src = sourceMap.get(name);

			if (src == null || !src.playing()) {
				if (source.toStream) {
					nextStreamingChannel = n + 1;
					if (nextStreamingChannel >= channels) nextStreamingChannel = 0;
				} else {
					nextNormalChannel = n + 1;
					if (nextNormalChannel >= channels) nextNormalChannel = 0;
				}
				sourceNames[n] = sourceName;
				return channelList.get(n);
			}
			n++;
			if (n >= channels) n = 0;
		}

		n = nextChannel;
		// Play on the next non-priority channel:
		for (x = 0; x < channels; x++) {
			name = sourceNames[n];
			if (name == null) src = null;
			else src = sourceMap.get(name);

			if (src == null || !src.playing() || !src.priority) {
				if (source.toStream) {
					nextStreamingChannel = n + 1;
					if (nextStreamingChannel >= channels) nextStreamingChannel = 0;
				} else {
					nextNormalChannel = n + 1;
					if (nextNormalChannel >= channels) nextNormalChannel = 0;
				}
				sourceNames[n] = sourceName;
				return channelList.get(n);
			}
			n++;
			if (n >= channels) n = 0;
		}

		return null;
	}

	/**
	 * Plays all sources whose 'toPlay' variable is true but are not currently
	 * playing (such as sources which were culled while looping and then
	 * reactivated).
	 */
	public void replaySources() {
		Set<String> keys = sourceMap.keySet();
		Iterator<String> iterator = keys.iterator();
		String sourceName;
		Source source;

		// loop through and cleanup all the sources:
		while (iterator.hasNext()) {
			sourceName = iterator.next();
			source = sourceMap.get(sourceName);
			if (source != null) {
				if (source.toPlay && !source.playing()) {
					play(sourceName);
					source.toPlay = false;
				}
			}
		}
	}

	/**
	 * If the specified source is a streaming source or MIDI source, this method
	 * queues up the next sound to play when the previous playback ends.  This
	 * method has no effect on non-streaming sources.
	 *
	 * @param sourceName  Source identifier.
	 * @param filenameURL Filename/URL of the sound file to play next.
	 */
	public void queueSound(String sourceName, FilenameURL filenameURL) {
		if (midiSourceName(sourceName)) {
			midiChannel.queueSound(filenameURL);
		} else {
			Source mySource = sourceMap.get(sourceName);
			if (mySource != null) mySource.queueSound(filenameURL);
		}
	}

	/**
	 * Removes the first occurrence of the specified filename from the specified
	 * source's list of sounds to play when previous playback ends.  This method
	 * has no effect on non-streaming sources.
	 *
	 * @param sourceName Source identifier.
	 * @param filename   Filename/identifier of the sound file to remove from the queue.
	 */
	public void dequeueSound(String sourceName, String filename) {
		if (midiSourceName(sourceName)) {
			midiChannel.dequeueSound(filename);
		} else {
			Source mySource = sourceMap.get(sourceName);
			if (mySource != null) mySource.dequeueSound(filename);
		}
	}

	/**
	 * Fades out the volume of whatever the specified source is currently playing,
	 * then begins playing the specified file at the source's previously
	 * assigned volume level.  If the filenameURL parameter is null or empty, the
	 * specified source will simply fade out and stop.  The milliseconds parameter
	 * must be non-negative or zero.  This method will remove anything that is
	 * currently in the specified source's list of queued sounds that would have
	 * played next when the current sound finished playing.  This method may only
	 * be used for streaming and MIDI sources.
	 *
	 * @param sourceName  Name of the source to fade out.
	 * @param filenameURL Filename/URL of the sound file to play next, or null for none.
	 * @param millis      Number of milliseconds the fadeout should take.
	 */
	public void fadeOut(String sourceName, FilenameURL filenameURL, long millis) {
		if (midiSourceName(sourceName)) {
			midiChannel.fadeOut(filenameURL, millis);
		} else {
			Source mySource = sourceMap.get(sourceName);
			if (mySource != null) mySource.fadeOut(filenameURL, millis);
		}
	}

	/**
	 * Fades out the volume of whatever the specified source is currently playing,
	 * then fades the volume back in playing the specified file.  Final volume
	 * after fade-in completes will be equal to the source's previously assigned
	 * volume level.  The filenameURL parameter may not be null or empty.  The
	 * milliseconds parameters must be non-negative or zero.  This method will
	 * remove anything that is currently in the specified source's list of queued
	 * sounds that would have played next when the current sound finished playing.
	 * This method may only be used for streaming and MIDI sources.
	 *
	 * @param sourceName  Name of the source to fade out/in.
	 * @param filenameURL Filename/URL of the sound file to play next, or null for none.
	 * @param millisOut   Number of milliseconds the fade-out should take.
	 * @param millisIn    Number of milliseconds the fade-in should take.
	 */
	public void fadeOutIn(String sourceName, FilenameURL filenameURL, long millisOut, long millisIn) {
		if (midiSourceName(sourceName)) {
			midiChannel.fadeOutIn(filenameURL, millisOut, millisIn);
		} else {
			Source mySource = sourceMap.get(sourceName);
			if (mySource != null) mySource.fadeOutIn(filenameURL, millisOut, millisIn);
		}
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
		if (midiChannel != null) midiChannel.resetGain();
		Channel c;
		Source s;
		for (Channel streamingChannel : streamingChannels) {
			c = streamingChannel;
			if (c != null) {
				s = c.attachedSource;
				if (s != null) s.checkFadeOut();
			}
		}
		c = null;
		s = null;
	}

	/**
	 * Loads the specified MIDI file, and saves the source information about it.
	 *
	 * @param toLoop      Midi file should loop or play once.
	 * @param sourceName  Source identifier.
	 * @param filenameURL Filename/URL of the MIDI file to load.
	 */
	public void loadMidi(boolean toLoop, String sourceName, FilenameURL filenameURL) {
		if (filenameURL == null) {
			errorMessage("Filename/URL not specified in method 'loadMidi'.");
			return;
		}

		if (!filenameURL.getFilename().matches(SoundSystemConfig.EXTENSION_MIDI)) {
			errorMessage("Filename/identifier doesn't end in '.mid' or'.midi' in method loadMidi.");
			return;
		}

		if (midiChannel == null) {
			midiChannel = new MidiChannel(toLoop, sourceName, filenameURL);
		} else {
			midiChannel.switchSource(toLoop, sourceName, filenameURL);
		}
	}

	/**
	 * Unloads the current Midi file.
	 */
	public void unloadMidi() {
		if (midiChannel != null) midiChannel.cleanup();
		midiChannel = null;
	}

	/**
	 * @deprecated use {@link #midiSourceName(String)} instead
	 */
	@Deprecated
	@SuppressWarnings("SpellCheckingInspection")
	public boolean midiSourcename(String sourceName) {
		return midiSourceName(sourceName);
	}

	/**
	 * Checks if the source name matches the midi source.
	 *
	 * @param sourceName Source identifier.
	 * @return True if source name and midi source name match.
	 */
	public boolean midiSourceName(String sourceName) {
		if (midiChannel == null || sourceName == null) return false;

		if (midiChannel.getSourceName() == null || sourceName.isEmpty()) return false;

		return sourceName.equals(midiChannel.getSourceName());
	}

	/**
	 * Returns the Source object identified by the specified name.
	 *
	 * @param sourceName The source's name.
	 * @return The source, or null if not found.
	 */
	public Source getSource(String sourceName) {
		return sourceMap.get(sourceName);
	}

	/**
	 * Returns a handle to the MIDI channel, or null if one does not exist.
	 *
	 * @return The MIDI channel.
	 */
	public MidiChannel getMidiChannel() {
		return midiChannel;
	}

	/**
	 * Specifies the MIDI channel to use.
	 *
	 * @param c New MIDI channel.
	 */
	public void setMidiChannel(MidiChannel c) {
		if (midiChannel != null && midiChannel != c) midiChannel.cleanup();

		midiChannel = c;
	}

	/**
	 * Tells all the sources that the listener has moved.
	 */
	public void listenerMoved() {
		Set<String> keys = sourceMap.keySet();
		Iterator<String> iterator = keys.iterator();
		String sourceName;
		Source srcData;

		// loop through and copy all the sources:
		while (iterator.hasNext()) {
			sourceName = iterator.next();
			srcData = sourceMap.get(sourceName);
			if (srcData != null) {
				srcData.listenerMoved();
			}
		}
	}

	/**
	 * Returns the sources map.
	 *
	 * @return Map of all sources.
	 */
	public HashMap<String, Source> getSources() {
		return sourceMap;
	}

	/**
	 * Returns information about the listener.
	 *
	 * @return A ListenerData object.
	 */
	public ListenerData getListenerData() {
		return listener;
	}

	/**
	 * Indicates whether this library requires some codecs to reverse-order
	 * the audio data they generate.
	 *
	 * @return True if audio data should be reverse-ordered.
	 */
	public boolean reverseByteOrder() {
		return reverseByteOrder;
	}

	/**
	 * Returns the short title of this library type.
	 *
	 * @return A short title.
	 */
	public static String getTitle() {
		return "No Sound";
	}

	/**
	 * Returns a longer description of this library type.
	 *
	 * @return A longer description.
	 */
	public static String getDescription() {
		return "Silent Mode";
	}

	/**
	 * Returns the name of the class.
	 *
	 * @return "Library" + library title.
	 */
	public String getClassName() {
		return "Library";
	}

	/**
	 * Prints a message.
	 *
	 * @param message Message to print.
	 */
	protected void message(String message) {
		logger.message(message, 0);
	}

	/**
	 * Prints an important message.
	 *
	 * @param message Message to print.
	 */
	protected void importantMessage(String message) {
		logger.importantMessage(message, 0);
	}

	/**
	 * Prints the specified message if error is true.
	 *
	 * @param error   True or False.
	 * @param message Message to print if error is true.
	 * @return True if error is true.
	 */
	protected boolean errorCheck(boolean error, String message) {
		return logger.errorCheck(error, getClassName(), message, 0);
	}

	/**
	 * Prints an error message.
	 *
	 * @param message Message to print.
	 */
	protected void errorMessage(String message) {
		logger.errorMessage(getClassName(), message, 0);
	}

	/**
	 * Prints an exception's error message followed by the stack trace.
	 *
	 * @param e Exception containing the information to print.
	 */
	protected void printStackTrace(Exception e) {
		logger.printStackTrace(e, 1);
	}
}
