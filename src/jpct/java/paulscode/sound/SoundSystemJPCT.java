/*
 * jPCT License:
 *
 * Copyright (c) 2002
 * Helge Foerster.  All rights reserved.
 *
 * The use of jPCT is free of charge for personal and commercial use.
 * Redistribution in binary form is permitted provided that the
 * following conditions are met:
 *
 * 1. The class structure and the classes themselves as they are included
 *    in the JAR file that comes with this distribution of jPCT must stay intact.
 *    It is not allowed to decompile the class-files and/or replace the original
 *    files with modified versions until this permission is explicitly granted
 *    by the author.
 * 2. The JAR-file may be decompressed and the class-files may be stored in other
 *    formats/JAR-files as long as 1.) is fulfilled. If jPCT is distributed as part
 *    of another project, it is allowed to obfuscate the class-files together with
 *    the rest of the project-files if you feel that this is required to protect
 *    your intellectual property.
 * 3. It is not required to mention the use of jPCT in your project, albeit it would
 *    be nice doing so. If you do, it is required to mention the jPCT webpage
 *    at http://www.jpct.net
 *
 * THIS SOFTWARE IS PROVIDED 'AS IS' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT, ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
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
import java.util.Set;

// From the jpct library, https://www.jpct.net
import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

// External Plugins:
// NOTE: To view important licensing information about the external plugins
// and their terms of use, please refer to the source code or to their provided
// documentation.
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.codecs.CodecJOgg;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.libraries.LibraryJavaSound;

/**
 * The SoundSystemJPCT class is a user-friendly extension of the core
 * SoundSystem class.
 * It is designed to be easily used with the jpct API
 * (for more information, visit <a href="https://www.jpct.net">jpct.net</a>).
 * In addition to the things which SoundSystem does, SoundSystemJPCT provides methods for
 * binding sources to Object3D's and binding the listener to a Camera.
 * A whole bunch of methods have been added to make source creation much easier than using
 * the core SoundSystem class alone, by automatically using default settings
 * any time a parameter is not specified.
 * All SimpleVector arguments are assumed to be in the jpct coordinate system, and are automatically converted
 * into the SoundSystem coordinate system (y = -y, z = -z), so the user doesn't
 * have to worry about keeping track of two different coordinate systems.
 * Plugins for LibraryLWJGLOpenAL, LibraryJavaSound, CodecWav, and CodecJOgg
 * are automatically linked, so the user doesn't have to worry about plugins either.
 * The SoundSystemJPCT can be constructed by defining which sound library to
 * use or by using the built-in compatibility checking.
 * See {@link SoundSystemConfig} for more information about changing default settings.
 */
@SuppressWarnings("unused")
public class SoundSystemJPCT extends SoundSystem {
	/**
	 * Camera which the listener will follow.
	 */
	private Camera boundCamera;

	/**
	 * Map linking Sources with the Object3D's which they follow.
	 */
	private HashMap<String, Object3D> boundObjects = null;

	/**
	 * Constructor: Create the sound system.  If library priorities have not
	 * been defined, SoundSystemJPCT attempts to load the LWJGL binding of
	 * OpenAL first, and if that fails it tries JavaSound.
	 * If both fail, it switches to silent mode.
	 * See {@link SoundSystemConfig SoundSystemConfig} for
	 * information about linking with sound libraries and codecs.
	 */
	public SoundSystemJPCT() {
		super();
	}

	/**
	 * Constructor: Create the sound system using the specified library.
	 *
	 * @param libraryClass Type of library to use (must extend class 'Library').
	 *                     See {@link SoundSystemConfig SoundSystemConfig} for
	 *                     information about choosing a sound library.
	 */
	public SoundSystemJPCT(Class<?> libraryClass) throws SoundSystemException {
		super(libraryClass);
	}

	/**
	 * Links with the default libraries (LibraryLWJGLOpenAL and LibraryJavaSound)
	 * and default codecs (CodecWav and CodecJOgg).
	 */
	@Override
	protected void linkDefaultLibrariesAndCodecs() {
		try {
			SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
		} catch (SoundSystemException sse) {
			errorMessage("Problem adding library for OpenAL", 0);
			logger.printStackTrace(sse, 1);
		}
		try {
			SoundSystemConfig.addLibrary(LibraryJavaSound.class);
		} catch (SoundSystemException sse) {
			errorMessage("Problem adding library for JavaSound", 0);
			logger.printStackTrace(sse, 1);
		}
		try {
			SoundSystemConfig.setCodec("wav", CodecWav.class);
		} catch (SoundSystemException sse) {
			errorMessage("Problem adding codec for .wav files", 0);
			logger.printStackTrace(sse, 1);
		}
		try {
			SoundSystemConfig.setCodec("ogg", CodecJOgg.class);
		} catch (SoundSystemException sse) {
			errorMessage("Problem adding codec for .ogg files", 0);
			logger.printStackTrace(sse, 1);
		}
	}

	/**
	 * Loads the message logger, initializes the specified sound library, and
	 * starts the command thread.  Also instantiates the random number generator
	 * and the command queue, and sets className to "SoundSystemJPCT".
	 *
	 * @param libraryClass Library to initialize.
	 *                     See {@link SoundSystemConfig} for information about choosing a sound library.
	 */
	@Override
	protected void init(Class<?> libraryClass) throws SoundSystemException {
		className = "SoundSystemJPCT";

		super.init(libraryClass);
	}

	/**
	 * Ends the command thread, shuts down the sound system, and removes references
	 * to all instantiated objects.
	 */
	@Override
	public void cleanup() {
		super.cleanup();

		try {
			if (boundObjects != null) boundObjects.clear();
		} catch (Exception ignored) {
		}

		boundObjects = null;
		boundCamera = null;
	}

	/**
	 * Re-aligns the listener to the camera and keeps sources following the
	 * Object3D's that they are bound to.
	 * This method should be called on the same
	 * thread that manipulates the Camera and Object3D's, to avoid
	 * synchronization-related errors.  A good place to call tick() would be within
	 * the main game loop.
	 */
	public void tick() {
		// If listener is bound to a camera, match its position and orientation:
		if (boundCamera != null) {
			SimpleVector position = convertCoordinates(boundCamera.getPosition());

			SimpleVector direction = convertCoordinates(boundCamera.getDirection());

			SimpleVector up = convertCoordinates(boundCamera.getUpVector());

			ListenerData listener = soundLibrary.getListenerData();

			// Check if the listener has moved:
			if (listener.position.x != position.x || listener.position.y != position.y || listener.position.z != position.z) {
				setListenerPosition(position.x, position.y, position.z);
			}

			// Check if listener's orientation changed:
			if (listener.lookAt.x != direction.x || listener.lookAt.y != direction.y || listener.lookAt.z != direction.z || listener.up.x != up.x || listener.up.y != up.y || listener.up.z != up.z) {
				setListenerOrientation(direction.x, direction.y, direction.z, up.x, up.y, up.z);
			}
		}

		if (boundObjects != null && soundLibrary != null) {
			Set<String> keys = boundObjects.keySet();
			Iterator<String> iterator = keys.iterator();
			String sourceName;
			Source source;
			Object3D object;
			SimpleVector center;

			// loop through and cleanup all the sources:
			while (iterator.hasNext()) {
				sourceName = iterator.next();
				source = soundLibrary.getSource(sourceName);
				if (source == null) {
					iterator.remove();
				} else {
					object = boundObjects.get(sourceName);
					if (object == null) {
						iterator.remove();
					} else {
						center = convertCoordinates(object.getTransformedCenter());
						synchronized (SoundSystemConfig.THREAD_SYNC) {
							if (source.position.x != center.x || source.position.y != center.y || source.position.z != center.z)
								setPosition(sourceName, center.x, center.y, center.z);
						}
					}
				}
			}
		}
	}

	/**
	 * Binds the listener to the specified camera, so that when the camera's
	 * position or orientation changes, the listener automatically follows it.
	 * NOTE: When using this method, it is necessary to call the tick() method
	 * within a loop running on the same thread which changes the specified Camera.
	 *
	 * @param c Camera to follow.
	 */
	public void bindListener(Camera c) {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			boundCamera = c;

			if (boundCamera != null) {
				SimpleVector position = convertCoordinates(boundCamera.getPosition());

				SimpleVector direction = convertCoordinates(boundCamera.getDirection());

				SimpleVector up = convertCoordinates(boundCamera.getUpVector());

				ListenerData listener = soundLibrary.getListenerData();

				// Check if the listener has moved:
				if (listener.position.x != position.x || listener.position.y != position.y || listener.position.z != position.z) {
					setListenerPosition(position.x, position.y, position.z);
				}

				// Check if listener's orientation changed:
				if (listener.lookAt.x != direction.x || listener.lookAt.y != direction.y || listener.lookAt.z != direction.z || listener.up.x != up.x || listener.up.y != up.y || listener.up.z != up.z) {
					setListenerOrientation(direction.x, direction.y, direction.z, up.x, up.y, up.z);
				}
			}
		}
	}

	/**
	 * Binds a source to an Object3D so that when the Object3D's position changes,
	 * the source automatically follows it.  NOTE: When using this method, it is
	 * necessary to call the tick() method within a loop running on the same
	 * thread which changes the specified Object3D.
	 *
	 * @param sourceName The source's identifier.
	 * @param obj        The object to follow.
	 */
	public void bindSource(String sourceName, Object3D obj) {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			if (boundObjects == null) boundObjects = new HashMap<>();
			if (sourceName != null && obj != null) boundObjects.put(sourceName, obj);
		}
	}

	/**
	 * Releases the specified source from its bound Object3D.
	 *
	 * @param sourceName The source's identifier.
	 */
	public void releaseSource(String sourceName) {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			boundObjects.remove(sourceName);
		}
	}

	/**
	 * Releases all sources bound to the specified Object3D.  This method should be
	 * called before deleting an Object3D with sources attached to it.
	 *
	 * @param obj Object3D to remove sources from.
	 */
	public void releaseAllSources(Object3D obj) {
		synchronized (SoundSystemConfig.THREAD_SYNC) {
			Set<String> keys = boundObjects.keySet();
			Iterator<String> iterator = keys.iterator();
			String sourceName;
			Object3D object;
			while (iterator.hasNext()) {
				sourceName = iterator.next();
				object = boundObjects.get(sourceName);
				if (object == null || object == obj) iterator.remove();
			}
		}
	}

	/**
	 * Creates a new permanent, streaming, looping priority source with zero
	 * attenuation.
	 *
	 * @param filename The name of the sound file to play at this source.
	 * @return The new source's name.
	 */
	public String backgroundMusic(String filename) {
		// return a name for the looping background music:
		return backgroundMusic(filename, true);
	}

	/**
	 * Creates a new permanent, streaming, priority source with zero attenuation.
	 *
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should the music loop, or play only once.
	 * @return The new source's name.
	 */
	public String backgroundMusic(String filename, boolean toLoop) {
		//generate a random name for this source:
		String sourceName = "Background_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		backgroundMusic(sourceName, filename, toLoop);
		// return a name for the background music:
		return sourceName;
	}

	/**
	 * Creates a new permanent, streaming, looping priority source with zero
	 * attenuation.
	 *
	 * @param sourceName A unique identifier for this source.
	 *                   Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 */
	public void backgroundMusic(String sourceName, String filename) {
		backgroundMusic(sourceName, filename, true);
	}

	/**
	 * Creates a new permanent, streaming, looping priority source with zero
	 * attenuation.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @return The new source's name.
	 */
	public String backgroundMusic(URL url, String identifier) {
		// return a name for the looping background music:
		return backgroundMusic(url, identifier, true);
	}

	/**
	 * Creates a new permanent, streaming, priority source with zero attenuation.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should the music loop, or play only once.
	 * @return The new source's name.
	 */
	public String backgroundMusic(URL url, String identifier, boolean toLoop) {
		//generate a random name for this source:
		String sourceName = "Background_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		backgroundMusic(sourceName, url, identifier, toLoop);
		// return a name for the background music:
		return sourceName;
	}

	/**
	 * Creates a new permanent, streaming, looping priority source with zero
	 * attenuation.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 */
	public void backgroundMusic(String sourceName, URL url, String identifier) {
		backgroundMusic(sourceName, url, identifier, true);
	}

	/**
	 * Creates a new non-streaming, non-priority source at the origin.  Default
	 * values are used for attenuation.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 */
	public void newSource(String sourceName, String filename, boolean toLoop) {
		newSource(sourceName, filename, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new non-streaming source at the origin.  Default values are used
	 * for attenuation.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 */
	public void newSource(boolean priority, String sourceName, String filename, boolean toLoop) {
		newSource(priority, sourceName, filename, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new non-streaming, non-priority source at the specified position.
	 * Default values are used for attenuation.
	 *
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 */
	public void newSource(String sourceName, String filename, boolean toLoop, SimpleVector jpctPosition) {
		newSource(sourceName, filename, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new non-streaming source at the specified position.  Default
	 * values are used for attenuation.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 */
	public void newSource(boolean priority, String sourceName, String filename, boolean toLoop, SimpleVector jpctPosition) {
		newSource(priority, sourceName, filename, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new non-streaming, non-priority source at the origin.  Default
	 * value is used for either fade-distance or roll-off factor, depending on the
	 * value of parameter 'attModel'.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 */
	public void newSource(String sourceName, String filename, boolean toLoop, int attModel) {
		newSource(sourceName, filename, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a new non-streaming source at the origin.  Default value is used for
	 * either fade-distance or roll-off factor, depending on the value of parameter
	 * 'attModel'.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 */
	public void newSource(boolean priority, String sourceName, String filename, boolean toLoop, int attModel) {
		newSource(priority, sourceName, filename, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a new non-streaming, non-priority source at the origin.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newSource(String sourceName, String filename, boolean toLoop, int attModel, float distOrRoll) {
		newSource(sourceName, filename, toLoop, new SimpleVector(0, 0, 0), attModel, distOrRoll);
	}

	/**
	 * Creates a new non-streaming source at the origin.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newSource(boolean priority, String sourceName, String filename, boolean toLoop, int attModel, float distOrRoll) {
		newSource(priority, sourceName, filename, toLoop, 0, 0, 0, attModel, distOrRoll);
	}

	/**
	 * Creates a new non-streaming, non-priority source at the specified position.
	 * Default value is used for either fade-distance or roll-off factor, depending
	 * on the value of parameter 'attModel'.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 */
	public void newSource(String sourceName, String filename, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		newSource(false, sourceName, filename, toLoop, jpctPosition, attModel);
	}

	/**
	 * Creates a new non-streaming source at the specified position.  Default value
	 * is used for either fade-distance or roll-off factor, depending on the value
	 * of parameter 'attModel'.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 */
	public void newSource(boolean priority, String sourceName, String filename, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		switch (attModel) {
			case SoundSystemConfig.ATTENUATION_ROLLOFF:
				newSource(priority, sourceName, filename, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultRolloff());
				break;
			case SoundSystemConfig.ATTENUATION_LINEAR:
				newSource(priority, sourceName, filename, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultFadeDistance());
				break;
			default:
				newSource(priority, sourceName, filename, toLoop, jpctPosition, attModel, 0);
				break;
		}
	}

	/**
	 * Creates a new non-streaming, non-priority source at the specified position.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newSource(String sourceName, String filename, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		newSource(false, sourceName, filename, toLoop, jpctPosition, attModel, distOrRoll);
	}

	/**
	 * Creates a new non-streaming source at the specified position.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName   A unique identifier for this source.
	 *                     Two sources may not use the same source name.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newSource(boolean priority, String sourceName, String filename, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		SimpleVector position = convertCoordinates(jpctPosition);

		commandQueue(new CommandObject(CommandObject.NEW_SOURCE, priority, false, toLoop, sourceName, new FilenameURL(filename), position.x, position.y, position.z, attModel, distOrRoll));
		commandThread.interrupt();
	}

	/**
	 * Creates a new non-streaming, non-priority source at the origin.  Default
	 * values are used for attenuation.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 */
	public void newSource(String sourceName, URL url, String identifier, boolean toLoop) {
		newSource(sourceName, url, identifier, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new non-streaming source at the origin.  Default values are used
	 * for attenuation.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 */
	public void newSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop) {
		newSource(priority, sourceName, url, identifier, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new non-streaming, non-priority source at the specified position.
	 * Default values are used for attenuation.
	 *
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 */
	public void newSource(String sourceName, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition) {
		newSource(sourceName, url, identifier, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new non-streaming source at the specified position.  Default
	 * values are used for attenuation.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 */
	public void newSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition) {
		newSource(priority, sourceName, url, identifier, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new non-streaming, non-priority source at the origin.  Default
	 * value is used for either fade-distance or roll-off factor, depending on the
	 * value of parameter 'attModel'.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 */
	public void newSource(String sourceName, URL url, String identifier, boolean toLoop, int attModel) {
		newSource(sourceName, url, identifier, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a new non-streaming source at the origin.  Default value is used for
	 * either fade-distance or roll-off factor, depending on the value of parameter
	 * 'attModel'.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 */
	public void newSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop, int attModel) {
		newSource(priority, sourceName, url, identifier, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a new non-streaming, non-priority source at the origin.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newSource(String sourceName, URL url, String identifier, boolean toLoop, int attModel, float distOrRoll) {
		newSource(sourceName, url, identifier, toLoop, new SimpleVector(0, 0, 0), attModel, distOrRoll);
	}

	/**
	 * Creates a new non-streaming source at the origin.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop, int attModel, float distOrRoll) {
		newSource(priority, sourceName, url, identifier, toLoop, 0, 0, 0, attModel, distOrRoll);
	}

	/**
	 * Creates a new non-streaming, non-priority source at the specified position.
	 * Default value is used for either fade-distance or roll-off factor, depending
	 * on the value of parameter 'attModel'.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 */
	public void newSource(String sourceName, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		newSource(false, sourceName, url, identifier, toLoop, jpctPosition, attModel);
	}

	/**
	 * Creates a new non-streaming source at the specified position.  Default value
	 * is used for either fade-distance or roll-off factor, depending on the value
	 * of parameter 'attModel'.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 */
	public void newSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		switch (attModel) {
			case SoundSystemConfig.ATTENUATION_ROLLOFF:
				newSource(priority, sourceName, url, identifier, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultRolloff());
				break;
			case SoundSystemConfig.ATTENUATION_LINEAR:
				newSource(priority, sourceName, url, identifier, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultFadeDistance());
				break;
			default:
				newSource(priority, sourceName, url, identifier, toLoop, jpctPosition, attModel, 0);
				break;
		}
	}

	/**
	 * Creates a new non-streaming, non-priority source at the specified position.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newSource(String sourceName, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		newSource(false, sourceName, url, identifier, toLoop, jpctPosition, attModel, distOrRoll);
	}

	/**
	 * Creates a new non-streaming source at the specified position.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Attenuation, fade distance, and roll-off factor.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		SimpleVector position = convertCoordinates(jpctPosition);

		commandQueue(new CommandObject(CommandObject.NEW_SOURCE, priority, false, toLoop, sourceName, new FilenameURL(url, identifier), position.x, position.y, position.z, attModel, distOrRoll));
		commandThread.interrupt();
	}

	/**
	 * Creates a new streaming non-priority source at the origin.  Default values
	 * are used for attenuation.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 */
	public void newStreamingSource(String sourceName, String filename, boolean toLoop) {
		newStreamingSource(sourceName, filename, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new streaming source.  Default values are used for attenuation.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 */
	public void newStreamingSource(boolean priority, String sourceName, String filename, boolean toLoop) {
		newStreamingSource(priority, sourceName, filename, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new streaming non-priority source at the specified position.
	 * Default values are used for attenuation.
	 *
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 */
	public void newStreamingSource(String sourceName, String filename, boolean toLoop, SimpleVector jpctPosition) {
		newStreamingSource(sourceName, filename, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new streaming source at the specified position.  Default values
	 * are used for attenuation.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 */
	public void newStreamingSource(boolean priority, String sourceName, String filename, boolean toLoop, SimpleVector jpctPosition) {
		newStreamingSource(priority, sourceName, filename, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new streaming non-priority source at the origin.  Default value is
	 * used for either fade-distance or roll-off factor, depending on the value of
	 * parameter 'attModel'.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 */
	public void newStreamingSource(String sourceName, String filename, boolean toLoop, int attModel) {
		newStreamingSource(sourceName, filename, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a new streaming source at the origin.  Default value is used for
	 * either fade-distance or roll-off factor, depending on the value of parameter
	 * 'attModel'.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 */
	public void newStreamingSource(boolean priority, String sourceName, String filename, boolean toLoop, int attModel) {
		newStreamingSource(priority, sourceName, filename, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a new streaming non-priority source at the origin.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newStreamingSource(String sourceName, String filename, boolean toLoop, int attModel, float distOrRoll) {
		newStreamingSource(sourceName, filename, toLoop, new SimpleVector(0, 0, 0), attModel, distOrRoll);
	}

	/**
	 * Creates a new streaming source at the origin.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newStreamingSource(boolean priority, String sourceName, String filename, boolean toLoop, int attModel, float distOrRoll) {
		newStreamingSource(priority, sourceName, filename, toLoop, 0, 0, 0, attModel, distOrRoll);
	}

	/**
	 * Creates a new streaming non-priority source at the specified position.
	 * Default value is used for either fade-distance or roll-off factor, depending
	 * on the value of parameter 'attModel'.
	 *
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 */
	public void newStreamingSource(String sourceName, String filename, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		newStreamingSource(false, sourceName, filename, toLoop, jpctPosition, attModel);
	}

	/**
	 * Creates a new streaming source at the specified position.  Default value is
	 * used for either fade-distance or roll-off factor, depending on the value of
	 * parameter 'attModel'.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 */
	public void newStreamingSource(boolean priority, String sourceName, String filename, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		switch (attModel) {
			case SoundSystemConfig.ATTENUATION_ROLLOFF:
				newStreamingSource(priority, sourceName, filename, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultRolloff());
				break;
			case SoundSystemConfig.ATTENUATION_LINEAR:
				newStreamingSource(priority, sourceName, filename, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultFadeDistance());
				break;
			default:
				newStreamingSource(priority, sourceName, filename, toLoop, jpctPosition, attModel, 0);
				break;
		}
	}

	/**
	 * Creates a new streaming non-priority source at the specified position.
	 *
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newStreamingSource(String sourceName, String filename, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		newStreamingSource(false, sourceName, filename, toLoop, jpctPosition, attModel, distOrRoll);
	}

	/**
	 * Creates a new streaming source at the specified position.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newStreamingSource(boolean priority, String sourceName, String filename, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		SimpleVector position = convertCoordinates(jpctPosition);

		commandQueue(new CommandObject(CommandObject.NEW_SOURCE, priority, true, toLoop, sourceName, new FilenameURL(filename), position.x, position.y, position.z, attModel, distOrRoll));
		commandThread.interrupt();
	}

	/**
	 * Creates a new streaming non-priority source at the origin.  Default values
	 * are used for attenuation.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 */
	public void newStreamingSource(String sourceName, URL url, String identifier, boolean toLoop) {
		newStreamingSource(sourceName, url, identifier, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new streaming source.  Default values are used for attenuation.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 */
	public void newStreamingSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop) {
		newStreamingSource(priority, sourceName, url, identifier, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new streaming non-priority source at the specified position.
	 * Default values are used for attenuation.
	 *
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 */
	public void newStreamingSource(String sourceName, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition) {
		newStreamingSource(sourceName, url, identifier, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new streaming source at the specified position.  Default values
	 * are used for attenuation.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 */
	public void newStreamingSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition) {
		newStreamingSource(priority, sourceName, url, identifier, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a new streaming non-priority source at the origin.  Default value is
	 * used for either fade-distance or roll-off factor, depending on the value of
	 * parameter 'attModel'.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 */
	public void newStreamingSource(String sourceName, URL url, String identifier, boolean toLoop, int attModel) {
		newStreamingSource(sourceName, url, identifier, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a new streaming source at the origin.  Default value is used for
	 * either fade-distance or roll-off factor, depending on the value of parameter
	 * 'attModel'.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 */
	public void newStreamingSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop, int attModel) {
		newStreamingSource(priority, sourceName, url, identifier, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a new streaming non-priority source at the origin.
	 *
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newStreamingSource(String sourceName, URL url, String identifier, boolean toLoop, int attModel, float distOrRoll) {
		newStreamingSource(sourceName, url, identifier, toLoop, new SimpleVector(0, 0, 0), attModel, distOrRoll);
	}

	/**
	 * Creates a new streaming source at the origin.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newStreamingSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop, int attModel, float distOrRoll) {
		newStreamingSource(priority, sourceName, url, identifier, toLoop, 0, 0, 0, attModel, distOrRoll);
	}

	/**
	 * Creates a new streaming non-priority source at the specified position.
	 * Default value is used for either fade-distance or roll-off factor, depending
	 * on the value of parameter 'attModel'.
	 *
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 */
	public void newStreamingSource(String sourceName, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		newStreamingSource(false, sourceName, url, identifier, toLoop, jpctPosition, attModel);
	}

	/**
	 * Creates a new streaming source at the specified position.  Default value is
	 * used for either fade-distance or roll-off factor, depending on the value of
	 * parameter 'attModel'.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 */
	public void newStreamingSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		switch (attModel) {
			case SoundSystemConfig.ATTENUATION_ROLLOFF:
				newStreamingSource(priority, sourceName, url, identifier, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultRolloff());
				break;
			case SoundSystemConfig.ATTENUATION_LINEAR:
				newStreamingSource(priority, sourceName, url, identifier, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultFadeDistance());
				break;
			default:
				newStreamingSource(priority, sourceName, url, identifier, toLoop, jpctPosition, attModel, 0);
				break;
		}
	}

	/**
	 * Creates a new streaming non-priority source at the specified position.
	 *
	 * @param sourceName   A unique identifier for this source.  Two sources may not use the same source name.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newStreamingSource(String sourceName, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		newStreamingSource(false, sourceName, url, identifier, toLoop, jpctPosition, attModel, distOrRoll);
	}

	/**
	 * Creates a new streaming source at the specified position.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param sourceName   A unique identifier for this source.
	 *                     Two sources may not use the same source name.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 */
	public void newStreamingSource(boolean priority, String sourceName, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		SimpleVector position = convertCoordinates(jpctPosition);

		commandQueue(new CommandObject(CommandObject.NEW_SOURCE, priority, true, toLoop, sourceName, new FilenameURL(url, identifier), position.x, position.y, position.z, attModel, distOrRoll));
		commandThread.interrupt();
	}

	/**
	 * Creates a temporary, non-priority source at the origin and plays it.
	 * Default values are used for attenuation.  After the source finishes playing,
	 * it is removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for sourceName.
	 *
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @return The new source's name.
	 */
	public String quickPlay(String filename, boolean toLoop) {
		return quickPlay(filename, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary source at the origin and plays it.  Default values are
	 * used for attenuation.  After the source finishes playing, it is removed.
	 * Returns a randomly generated name for the new source.  NOTE: to make a
	 * source created by this method permanent, call the setActive() method using
	 * the return value for sourceName.
	 *
	 * @param priority Setting this to true will prevent other sounds from overriding this one.
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, String filename, boolean toLoop) {
		return quickPlay(priority, filename, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary non-priority source at the specified position and plays
	 * it.  Default values are used for attenuation.  After the source finishes
	 * playing, it is removed.  Returns a randomly generated name for the new
	 * source.  NOTE: to make a source created by this method permanent, call the
	 * setActive() method using the return value for sourceName.
	 *
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @return The new source's name.
	 */
	public String quickPlay(String filename, boolean toLoop, SimpleVector jpctPosition) {
		return quickPlay(filename, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary source and plays it.  Default values are used for
	 * attenuation.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return
	 * value for sourceName.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, String filename, boolean toLoop, SimpleVector jpctPosition) {
		return quickPlay(priority, filename, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary non-priority source at the origin and plays it.  Default
	 * value is used for either fade-distance or roll-off factor, depending on the
	 * value of parameter 'attModel'.  After the source finishes playing, it is
	 * removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for sourceName.
	 *
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @param attModel Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickPlay(String filename, boolean toLoop, int attModel) {
		return quickPlay(filename, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a temporary source and plays it.  Default value is used for either
	 * fade-distance or roll-off factor, depending on the value of parameter
	 * 'attModel'.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return value
	 * for sourceName.
	 *
	 * @param priority Setting this to true will prevent other sounds from overriding this one.
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @param attModel Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, String filename, boolean toLoop, int attModel) {
		return quickPlay(priority, filename, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a temporary non-priority source at the origin and plays it.  After
	 * the source finishes playing, it is removed.  Returns a randomly generated
	 * name for the new source.  NOTE: to make a source created by this method
	 * permanent, call the setActive() method using the return value for
	 * sourceName.
	 *
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(String filename, boolean toLoop, int attModel, float distOrRoll) {
		return quickPlay(filename, toLoop, new SimpleVector(0, 0, 0), attModel, distOrRoll);
	}

	/**
	 * Creates a temporary source at the origin and plays it.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, String filename, boolean toLoop, int attModel, float distOrRoll) {
		return quickPlay(priority, filename, toLoop, 0, 0, 0, attModel, distOrRoll);
	}

	/**
	 * Creates a temporary non-priority source and plays it.  Default value is used
	 * for either fade-distance or roll-off factor, depending on the value of
	 * parameter 'attModel'.  After the source finishes playing, it is removed.
	 * Returns a randomly generated name for the new source.  NOTE: to make a
	 * source created by this method permanent, call the setActive() method using
	 * the return value for sourceName.
	 *
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickPlay(String filename, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		return quickPlay(false, filename, toLoop, jpctPosition, attModel);
	}

	/**
	 * Creates a temporary source and plays it.  Default value is used for either
	 * fade-distance or roll-off factor, depending on the value of parameter
	 * 'attModel'.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return value
	 * for sourceName.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, String filename, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		switch (attModel) {
			case SoundSystemConfig.ATTENUATION_ROLLOFF:
				return quickPlay(priority, filename, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultRolloff());
			case SoundSystemConfig.ATTENUATION_LINEAR:
				return quickPlay(priority, filename, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultFadeDistance());
			default:
				return quickPlay(priority, filename, toLoop, jpctPosition, attModel, 0);
		}
	}

	/**
	 * Creates a temporary non-priority source and plays it.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(String filename, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		return quickPlay(false, filename, toLoop, jpctPosition, attModel, distOrRoll);
	}

	/**
	 * Creates a temporary source and plays it.  After the source finishes playing,
	 * it is removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for sourceName.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, String filename, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		//generate a random name for this source:
		String sourceName = "Source_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		SimpleVector position = convertCoordinates(jpctPosition);

		// Queue a command to quick play this new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, priority, false, toLoop, sourceName, new FilenameURL(filename), position.x, position.y, position.z, attModel, distOrRoll, true));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		// Wake the command thread to process commands:
		commandThread.interrupt();

		// return the new source name.
		return sourceName;
	}

	/**
	 * Creates a temporary non-priority source, binds it to the specified Object3D,
	 * and plays.  Default values are used for attenuation.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for
	 * the new source.  NOTE: to make a source created by this method permanent,
	 * call the setActive() method using the return value for sourceName.
	 *
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @param object   Object3D for this source to follow.
	 * @return The new source's name.
	 */
	public String quickPlay(String filename, boolean toLoop, Object3D object) {
		return quickPlay(filename, toLoop, object, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary source, binds it to the specified Object3D, and plays.
	 * Default values are used for attenuation.  After the source finishes
	 * playing, it is removed.  Returns a randomly generated name for the new
	 * source.  NOTE: to make a source created by this method permanent, call the
	 * setActive() method using the return value for sourceName.
	 *
	 * @param priority Setting this to true will prevent other sounds from overriding this one.
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @param object   Object3D for this source to follow.
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, String filename, boolean toLoop, Object3D object) {
		return quickPlay(priority, filename, toLoop, object, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary non-priority source, binds it to the specified Object3D,
	 * and plays.  Default value is used for either fade-distance or roll-off
	 * factor, depending on the value of parameter 'attModel'.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @param object   Object3D for this source to follow.
	 * @param attModel Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickPlay(String filename, boolean toLoop, Object3D object, int attModel) {
		return quickPlay(false, filename, toLoop, object, attModel);
	}

	/**
	 * Creates a temporary source, binds it to the specified Object3D, and plays.
	 * Default value is used for either fade-distance or roll-off factor, depending
	 * on the value of parameter 'attModel'.  After the source finishes playing,
	 * it is removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for sourceName.
	 *
	 * @param priority Setting this to true will prevent other sounds from overriding this one.
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @param object   Object3D for this source to follow.
	 * @param attModel Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, String filename, boolean toLoop, Object3D object, int attModel) {
		switch (attModel) {
			case SoundSystemConfig.ATTENUATION_ROLLOFF:
				return quickPlay(priority, filename, toLoop, object, attModel, SoundSystemConfig.getDefaultRolloff());
			case SoundSystemConfig.ATTENUATION_LINEAR:
				return quickPlay(priority, filename, toLoop, object, attModel, SoundSystemConfig.getDefaultFadeDistance());
			default:
				return quickPlay(priority, filename, toLoop, object, attModel, 0);
		}
	}

	/**
	 * Creates a temporary non-priority source, binds it to the specified Object3D,
	 * and plays.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return value
	 * for sourceName.
	 *
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(String filename, boolean toLoop, Object3D object, int attModel, float distOrRoll) {
		return quickPlay(false, filename, toLoop, object, attModel, distOrRoll);
	}

	/**
	 * Creates a temporary source, binds it to the specified Object3D, and plays
	 * it.  After the source finishes playing, it is removed.  Returns a randomly
	 * generated name for the new source.  NOTE: to make a source created by this
	 * method permanent, call the setActive() method using the return value for
	 * sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, String filename, boolean toLoop, Object3D object, int attModel, float distOrRoll) {
		//generate a random name for this source:
		String sourceName = "Source_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		SimpleVector jpctPosition;
		if (object != null) jpctPosition = object.getTransformedCenter();
		else jpctPosition = new SimpleVector(0, 0, 0);

		SimpleVector position = convertCoordinates(jpctPosition);

		// Queue a command to quick play this new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, priority, false, toLoop, sourceName, new FilenameURL(filename), position.x, position.y, position.z, attModel, distOrRoll, true));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		// Wake the command thread to process commands:
		commandThread.interrupt();

		// Create the boundObjects map if it doesn't exist:
		if (boundObjects == null) boundObjects = new HashMap<>();

		// Add the source name and Object3D to the map:
		if (object != null) boundObjects.put(sourceName, object);

		// return the new source name.
		return sourceName;
	}

	/**
	 * Creates a temporary, non-priority source at the origin and plays it.
	 * Default values are used for attenuation.  After the source finishes playing,
	 * it is removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for sourceName.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @return The new source's name.
	 */
	public String quickPlay(URL url, String identifier, boolean toLoop) {
		return quickPlay(url, identifier, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary source at the origin and plays it.  Default values are
	 * used for attenuation.  After the source finishes playing, it is removed.
	 * Returns a randomly generated name for the new source.  NOTE: to make a
	 * source created by this method permanent, call the setActive() method using
	 * the return value for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, URL url, String identifier, boolean toLoop) {
		return quickPlay(priority, url, identifier, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary non-priority source at the specified position and plays
	 * it.  Default values are used for attenuation.  After the source finishes
	 * playing, it is removed.  Returns a randomly generated name for the new
	 * source.  NOTE: to make a source created by this method permanent, call the
	 * setActive() method using the return value for sourceName.
	 *
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @return The new source's name.
	 */
	public String quickPlay(URL url, String identifier, boolean toLoop, SimpleVector jpctPosition) {
		return quickPlay(url, identifier, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary source and plays it.  Default values are used for
	 * attenuation.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return
	 * value for sourceName.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition) {
		return quickPlay(priority, url, identifier, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary non-priority source at the origin and plays it.  Default
	 * value is used for either fade-distance or roll-off factor, depending on the
	 * value of parameter 'attModel'.  After the source finishes playing, it is
	 * removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for sourceName.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickPlay(URL url, String identifier, boolean toLoop, int attModel) {
		return quickPlay(url, identifier, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a temporary source and plays it.  Default value is used for either
	 * fade-distance or roll-off factor, depending on the value of parameter
	 * 'attModel'.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return value
	 * for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, URL url, String identifier, boolean toLoop, int attModel) {
		return quickPlay(priority, url, identifier, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a temporary non-priority source at the origin and plays it.  After
	 * the source finishes playing, it is removed.  Returns a randomly generated
	 * name for the new source.  NOTE: to make a source created by this method
	 * permanent, call the setActive() method using the return value for
	 * sourceName.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(URL url, String identifier, boolean toLoop, int attModel, float distOrRoll) {
		return quickPlay(url, identifier, toLoop, new SimpleVector(0, 0, 0), attModel, distOrRoll);
	}

	/**
	 * Creates a temporary source at the origin and plays it.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, URL url, String identifier, boolean toLoop, int attModel, float distOrRoll) {
		return quickPlay(priority, url, identifier, toLoop, 0, 0, 0, attModel, distOrRoll);
	}

	/**
	 * Creates a temporary non-priority source and plays it.  Default value is used
	 * for either fade-distance or roll-off factor, depending on the value of
	 * parameter 'attModel'.  After the source finishes playing, it is removed.
	 * Returns a randomly generated name for the new source.  NOTE: to make a
	 * source created by this method permanent, call the setActive() method using
	 * the return value for sourceName.
	 *
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickPlay(URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		return quickPlay(false, url, identifier, toLoop, jpctPosition, attModel);
	}

	/**
	 * Creates a temporary source and plays it.  Default value is used for either
	 * fade-distance or roll-off factor, depending on the value of parameter
	 * 'attModel'.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return value
	 * for sourceName.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		switch (attModel) {
			case SoundSystemConfig.ATTENUATION_ROLLOFF:
				return quickPlay(priority, url, identifier, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultRolloff());
			case SoundSystemConfig.ATTENUATION_LINEAR:
				return quickPlay(priority, url, identifier, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultFadeDistance());
			default:
				return quickPlay(priority, url, identifier, toLoop, jpctPosition, attModel, 0);
		}
	}

	/**
	 * Creates a temporary non-priority source and plays it.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		return quickPlay(false, url, identifier, toLoop, jpctPosition, attModel, distOrRoll);
	}

	/**
	 * Creates a temporary source and plays it.  After the source finishes playing,
	 * it is removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for sourceName.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		//generate a random name for this source:
		String sourceName = "Source_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		SimpleVector position = convertCoordinates(jpctPosition);

		// Queue a command to quick play this new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, priority, false, toLoop, sourceName, new FilenameURL(url, identifier), position.x, position.y, position.z, attModel, distOrRoll, true));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		// Wake the command thread to process commands:
		commandThread.interrupt();

		// return the new source name.
		return sourceName;
	}

	/**
	 * Creates a temporary non-priority source, binds it to the specified Object3D,
	 * and plays.  Default values are used for attenuation.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for
	 * the new source.  NOTE: to make a source created by this method permanent,
	 * call the setActive() method using the return value for sourceName.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @return The new source's name.
	 */
	public String quickPlay(URL url, String identifier, boolean toLoop, Object3D object) {
		return quickPlay(url, identifier, toLoop, object, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary source, binds it to the specified Object3D, and plays.
	 * Default values are used for attenuation.  After the source finishes
	 * playing, it is removed.  Returns a randomly generated name for the new
	 * source.  NOTE: to make a source created by this method permanent, call the
	 * setActive() method using the return value for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, URL url, String identifier, boolean toLoop, Object3D object) {
		return quickPlay(priority, url, identifier, toLoop, object, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary non-priority source, binds it to the specified Object3D,
	 * and plays.  Default value is used for either fade-distance or roll-off
	 * factor, depending on the value of parameter 'attModel'.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @param attModel   Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickPlay(URL url, String identifier, boolean toLoop, Object3D object, int attModel) {
		return quickPlay(false, url, identifier, toLoop, object, attModel);
	}

	/**
	 * Creates a temporary source, binds it to the specified Object3D, and plays.
	 * Default value is used for either fade-distance or roll-off factor, depending
	 * on the value of parameter 'attModel'.  After the source finishes playing,
	 * it is removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @param attModel   Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, URL url, String identifier, boolean toLoop, Object3D object, int attModel) {
		switch (attModel) {
			case SoundSystemConfig.ATTENUATION_ROLLOFF:
				return quickPlay(priority, url, identifier, toLoop, object, attModel, SoundSystemConfig.getDefaultRolloff());
			case SoundSystemConfig.ATTENUATION_LINEAR:
				return quickPlay(priority, url, identifier, toLoop, object, attModel, SoundSystemConfig.getDefaultFadeDistance());
			default:
				return quickPlay(priority, url, identifier, toLoop, object, attModel, 0);
		}
	}

	/**
	 * Creates a temporary non-priority source, binds it to the specified Object3D,
	 * and plays.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return value
	 * for sourceName.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(URL url, String identifier, boolean toLoop, Object3D object, int attModel, float distOrRoll) {
		return quickPlay(false, url, identifier, toLoop, object, attModel, distOrRoll);
	}

	/**
	 * Creates a temporary source, binds it to the specified Object3D, and plays
	 * it.  After the source finishes playing, it is removed.  Returns a randomly
	 * generated name for the new source.  NOTE: to make a source created by this
	 * method permanent, call the setActive() method using the return value for
	 * sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickPlay(boolean priority, URL url, String identifier, boolean toLoop, Object3D object, int attModel, float distOrRoll) {
		//generate a random name for this source:
		String sourceName = "Source_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		SimpleVector jpctPosition;
		if (object != null) jpctPosition = object.getTransformedCenter();
		else jpctPosition = new SimpleVector(0, 0, 0);

		SimpleVector position = convertCoordinates(jpctPosition);

		// Queue a command to quick play this new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, priority, false, toLoop, sourceName, new FilenameURL(url, identifier), position.x, position.y, position.z, attModel, distOrRoll, true));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		// Wake the command thread to process commands:
		commandThread.interrupt();

		// Create the boundObjects map if it doesn't exist:
		if (boundObjects == null) boundObjects = new HashMap<>();

		// Add the source name and Object3D to the map:
		if (object != null) boundObjects.put(sourceName, object);

		// return the new source name.
		return sourceName;
	}

	/**
	 * Creates a temporary, non-priority source at the origin and streams it.
	 * Default values are used for attenuation.  After the source finishes playing,
	 * it is removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for sourceName.
	 *
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @return The new source's name.
	 */
	public String quickStream(String filename, boolean toLoop) {
		return quickStream(filename, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary source at the origin and streams it.  Default values are
	 * used for attenuation.  After the source finishes playing, it is removed.
	 * Returns a randomly generated name for the new source.  NOTE: to make a
	 * source created by this method permanent, call the setActive() method using
	 * the return value for sourceName.
	 *
	 * @param priority Setting this to true will prevent other sounds from overriding this one.
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, String filename, boolean toLoop) {
		return quickStream(priority, filename, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary non-priority source and streams it.  Default values are
	 * used for attenuation.  After the source finishes playing, it is removed.
	 * Returns a randomly generated name for the new source.  NOTE: to make a
	 * source created by this method permanent, call the setActive() method using
	 * the return value for sourceName.
	 *
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @return The new source's name.
	 */
	public String quickStream(String filename, boolean toLoop, SimpleVector jpctPosition) {
		return quickStream(filename, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary source and streams it.  Default values are used for
	 * attenuation.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return
	 * value for sourceName.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, String filename, boolean toLoop, SimpleVector jpctPosition) {
		return quickStream(priority, filename, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary non-priority source at the origin and streams it.
	 * Default value is used for either fade-distance or roll-off factor, depending
	 * on the value of parameter 'attModel'.  After the source finishes playing, it
	 * is removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for sourceName.
	 *
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @param attModel Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickStream(String filename, boolean toLoop, int attModel) {
		return quickStream(filename, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a temporary source and streams it.  Default value is used for either
	 * fade-distance or roll-off factor, depending on the value of parameter
	 * 'attModel'.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return value
	 * for sourceName.
	 *
	 * @param priority Setting this to true will prevent other sounds from overriding this one.
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @param attModel Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, String filename, boolean toLoop, int attModel) {
		return quickStream(priority, filename, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a temporary non-priority source at the origin and streams it.  After
	 * the source finishes playing, it is removed.  Returns a randomly generated
	 * name for the new source.  NOTE: to make a source created by this method
	 * permanent, call the setActive() method using the return value for
	 * sourceName.
	 *
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(String filename, boolean toLoop, int attModel, float distOrRoll) {
		return quickStream(filename, toLoop, new SimpleVector(0, 0, 0), attModel, distOrRoll);
	}

	/**
	 * Creates a temporary source at the origin and streams it.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, String filename, boolean toLoop, int attModel, float distOrRoll) {
		return quickStream(priority, filename, toLoop, 0, 0, 0, attModel, distOrRoll);
	}

	/**
	 * Creates a temporary non-priority source and streams it.  Default value is
	 * used for either fade-distance or roll-off factor, depending on the value of
	 * parameter 'attModel'.  After the source finishes playing, it is removed.
	 * Returns a randomly generated name for the new source.  NOTE: to make a
	 * source created by this method permanent, call the setActive() method using
	 * the return value for sourceName.
	 *
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickStream(String filename, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		return quickStream(false, filename, toLoop, jpctPosition, attModel);
	}

	/**
	 * Creates a temporary source and streams it.  Default value is used for either
	 * fade-distance or roll-off factor, depending on the value of parameter
	 * 'attModel'.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return value
	 * for sourceName.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, String filename, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		switch (attModel) {
			case SoundSystemConfig.ATTENUATION_ROLLOFF:
				return quickStream(priority, filename, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultRolloff());
			case SoundSystemConfig.ATTENUATION_LINEAR:
				return quickStream(priority, filename, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultFadeDistance());
			default:
				return quickStream(priority, filename, toLoop, jpctPosition, attModel, 0);
		}
	}

	/**
	 * Creates a temporary non-priority source and streams it.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(String filename, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		return quickStream(false, filename, toLoop, jpctPosition, attModel, distOrRoll);
	}

	/**
	 * Creates a temporary source and streams it.  After the source finishes
	 * playing, it is removed.  Returns a randomly generated name for the new
	 * source.  NOTE: to make a source created by this method permanent, call the
	 * setActive() method using the return value for sourceName.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param filename     The name of the sound file to play at this source.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, String filename, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		//generate a random name for this source:
		String sourceName = "Source_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		SimpleVector position = convertCoordinates(jpctPosition);

		// Queue a command to quick stream this new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, priority, true, toLoop, sourceName, new FilenameURL(filename), position.x, position.y, position.z, attModel, distOrRoll, true));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		// Wake the command thread to process commands:
		commandThread.interrupt();

		// return the new source name.
		return sourceName;
	}

	/**
	 * Creates a temporary non-priority source, binds it to the specified Object3D,
	 * and streams it.  Default values are used for attenuation.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @param object   Object3D for this source to follow.
	 * @return The new source's name.
	 */
	public String quickStream(String filename, boolean toLoop, Object3D object) {
		return quickStream(filename, toLoop, object, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary source, binds it to the specified Object3D, and streams
	 * it.  Default values are used for attenuation.  After the source finishes
	 * playing, it is removed.  Returns a randomly generated name for the new
	 * source.  NOTE: to make a source created by this method permanent, call the
	 * setActive() method using the return value for sourceName.
	 *
	 * @param priority Setting this to true will prevent other sounds from overriding this one.
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @param object   Object3D for this source to follow.
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, String filename, boolean toLoop, Object3D object) {
		return quickStream(priority, filename, toLoop, object, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary non-priority source, binds it to the specified Object3D,
	 * and streams it.  Default value is used for either fade-distance or roll-off
	 * factor, depending on the value of parameter 'attModel'.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @param object   Object3D for this source to follow.
	 * @param attModel Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickStream(String filename, boolean toLoop, Object3D object, int attModel) {
		return quickStream(false, filename, toLoop, object, attModel);
	}

	/**
	 * Creates a temporary source, binds it to the specified Object3D, and streams
	 * it.  Default value is used for either fade-distance or roll-off factor,
	 * depending on the value of parameter 'attModel'.  After the source finishes
	 * playing, it is removed.  Returns a randomly generated name for the new
	 * source.  NOTE: to make a source created by this method permanent, call the
	 * setActive() method using the return value for sourceName.
	 *
	 * @param priority Setting this to true will prevent other sounds from overriding this one.
	 * @param filename The name of the sound file to play at this source.
	 * @param toLoop   Should this source loop, or play only once.
	 * @param object   Object3D for this source to follow.
	 * @param attModel Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, String filename, boolean toLoop, Object3D object, int attModel) {
		switch (attModel) {
			case SoundSystemConfig.ATTENUATION_ROLLOFF:
				return quickStream(priority, filename, toLoop, object, attModel, SoundSystemConfig.getDefaultRolloff());
			case SoundSystemConfig.ATTENUATION_LINEAR:
				return quickStream(priority, filename, toLoop, object, attModel, SoundSystemConfig.getDefaultFadeDistance());
			default:
				return quickStream(priority, filename, toLoop, object, attModel, 0);
		}
	}

	/**
	 * Creates a temporary non-priority source, binds it to the specified Object3D,
	 * and streams it.  After the source finishes playing, it is removed.  Returns
	 * a randomly generated name for the new source.  NOTE: to make a source
	 * created by this method permanent, call the setActive() method using the
	 * return value for sourceName.
	 *
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(String filename, boolean toLoop, Object3D object, int attModel, float distOrRoll) {
		return quickStream(false, filename, toLoop, object, attModel, distOrRoll);
	}

	/**
	 * Creates a temporary source, binds it to the specified Object3D, and streams
	 * it.  After the source finishes playing, it is removed.  Returns a randomly
	 * generated name for the new source.  NOTE: to make a source created by this
	 * method permanent, call the setActive() method using the return value for
	 * sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param filename   The name of the sound file to play at this source.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, String filename, boolean toLoop, Object3D object, int attModel, float distOrRoll) {
		//generate a random name for this source:
		String sourceName = "Source_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		SimpleVector jpctPosition;
		if (object != null) jpctPosition = object.getTransformedCenter();
		else jpctPosition = new SimpleVector(0, 0, 0);

		SimpleVector position = convertCoordinates(jpctPosition);

		// Queue a command to quick stream this new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, priority, true, toLoop, sourceName, new FilenameURL(filename), position.x, position.y, position.z, attModel, distOrRoll, true));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		// Wake the command thread to process commands:
		commandThread.interrupt();

		// Create the boundObjects map if it doesn't exist:
		if (boundObjects == null) boundObjects = new HashMap<>();

		// Add the source name and Object3D to the map:
		if (object != null) boundObjects.put(sourceName, object);

		// return the new source name.
		return sourceName;
	}

	/**
	 * Creates a temporary, non-priority source at the origin and streams it.
	 * Default values are used for attenuation.  After the source finishes playing,
	 * it is removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for sourceName.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @return The new source's name.
	 */
	public String quickStream(URL url, String identifier, boolean toLoop) {
		return quickStream(url, identifier, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary source at the origin and streams it.  Default values are
	 * used for attenuation.  After the source finishes playing, it is removed.
	 * Returns a randomly generated name for the new source.  NOTE: to make a
	 * source created by this method permanent, call the setActive() method using
	 * the return value for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, URL url, String identifier, boolean toLoop) {
		return quickStream(priority, url, identifier, toLoop, new SimpleVector(0, 0, 0), SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary non-priority source and streams it.  Default values are
	 * used for attenuation.  After the source finishes playing, it is removed.
	 * Returns a randomly generated name for the new source.  NOTE: to make a
	 * source created by this method permanent, call the setActive() method using
	 * the return value for sourceName.
	 *
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @return The new source's name.
	 */
	public String quickStream(URL url, String identifier, boolean toLoop, SimpleVector jpctPosition) {
		return quickStream(url, identifier, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary source and streams it.  Default values are used for
	 * attenuation.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return
	 * value for sourceName.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition) {
		return quickStream(priority, url, identifier, toLoop, jpctPosition, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary non-priority source at the origin and streams it.
	 * Default value is used for either fade-distance or roll-off factor, depending
	 * on the value of parameter 'attModel'.  After the source finishes playing, it
	 * is removed.  Returns a randomly generated name for the new source.  NOTE:
	 * to make a source created by this method permanent, call the setActive()
	 * method using the return value for sourceName.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickStream(URL url, String identifier, boolean toLoop, int attModel) {
		return quickStream(url, identifier, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a temporary source and streams it.  Default value is used for either
	 * fade-distance or roll-off factor, depending on the value of parameter
	 * 'attModel'.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return value
	 * for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, URL url, String identifier, boolean toLoop, int attModel) {
		return quickStream(priority, url, identifier, toLoop, new SimpleVector(0, 0, 0), attModel);
	}

	/**
	 * Creates a temporary non-priority source at the origin and streams it.  After
	 * the source finishes playing, it is removed.  Returns a randomly generated
	 * name for the new source.  NOTE: to make a source created by this method
	 * permanent, call the setActive() method using the return value for
	 * sourceName.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(URL url, String identifier, boolean toLoop, int attModel, float distOrRoll) {
		return quickStream(url, identifier, toLoop, new SimpleVector(0, 0, 0), attModel, distOrRoll);
	}

	/**
	 * Creates a temporary source at the origin and streams it.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, URL url, String identifier, boolean toLoop, int attModel, float distOrRoll) {
		return quickStream(priority, url, identifier, toLoop, 0, 0, 0, attModel, distOrRoll);
	}

	/**
	 * Creates a temporary non-priority source and streams it.  Default value is
	 * used for either fade-distance or roll-off factor, depending on the value of
	 * parameter 'attModel'.  After the source finishes playing, it is removed.
	 * Returns a randomly generated name for the new source.  NOTE: to make a
	 * source created by this method permanent, call the setActive() method using
	 * the return value for sourceName.
	 *
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickStream(URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		return quickStream(false, url, identifier, toLoop, jpctPosition, attModel);
	}

	/**
	 * Creates a temporary source and streams it.  Default value is used for either
	 * fade-distance or roll-off factor, depending on the value of parameter
	 * 'attModel'.  After the source finishes playing, it is removed.  Returns a
	 * randomly generated name for the new source.  NOTE: to make a source created
	 * by this method permanent, call the setActive() method using the return value
	 * for sourceName.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel) {
		switch (attModel) {
			case SoundSystemConfig.ATTENUATION_ROLLOFF:
				return quickStream(priority, url, identifier, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultRolloff());
			case SoundSystemConfig.ATTENUATION_LINEAR:
				return quickStream(priority, url, identifier, toLoop, jpctPosition, attModel, SoundSystemConfig.getDefaultFadeDistance());
			default:
				return quickStream(priority, url, identifier, toLoop, jpctPosition, attModel, 0);
		}
	}

	/**
	 * Creates a temporary non-priority source and streams it.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		return quickStream(false, url, identifier, toLoop, jpctPosition, attModel, distOrRoll);
	}

	/**
	 * Creates a temporary source and streams it.  After the source finishes
	 * playing, it is removed.  Returns a randomly generated name for the new
	 * source.  NOTE: to make a source created by this method permanent, call the
	 * setActive() method using the return value for sourceName.
	 *
	 * @param priority     Setting this to true will prevent other sounds from overriding this one.
	 * @param url          URL handle to the sound file to stream at this source.
	 * @param identifier   Filename/identifier of the file referenced by the URL.
	 * @param toLoop       Should this source loop, or play only once.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 * @param attModel     Attenuation model to use.
	 * @param distOrRoll   Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, URL url, String identifier, boolean toLoop, SimpleVector jpctPosition, int attModel, float distOrRoll) {
		//generate a random name for this source:
		String sourceName = "Source_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		SimpleVector position = convertCoordinates(jpctPosition);

		// Queue a command to quick stream this new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, priority, true, toLoop, sourceName, new FilenameURL(url, identifier), position.x, position.y, position.z, attModel, distOrRoll, true));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		// Wake the command thread to process commands:
		commandThread.interrupt();

		// return the new source name.
		return sourceName;
	}

	/**
	 * Creates a temporary non-priority source, binds it to the specified Object3D,
	 * and streams it.  Default values are used for attenuation.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for source name.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @return The new source's name.
	 */
	public String quickStream(URL url, String identifier, boolean toLoop, Object3D object) {
		return quickStream(url, identifier, toLoop, object, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary source, binds it to the specified Object3D, and streams
	 * it.  Default values are used for attenuation.  After the source finishes
	 * playing, it is removed.  Returns a randomly generated name for the new
	 * source.  NOTE: to make a source created by this method permanent, call the
	 * setActive() method using the return value for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, URL url, String identifier, boolean toLoop, Object3D object) {
		return quickStream(priority, url, identifier, toLoop, object, SoundSystemConfig.getDefaultAttenuation());
	}

	/**
	 * Creates a temporary non-priority source, binds it to the specified Object3D,
	 * and streams it.  Default value is used for either fade-distance or roll-off
	 * factor, depending on the value of parameter 'attModel'.  After the source
	 * finishes playing, it is removed.  Returns a randomly generated name for the
	 * new source.  NOTE: to make a source created by this method permanent, call
	 * the setActive() method using the return value for sourceName.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @param attModel   Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickStream(URL url, String identifier, boolean toLoop, Object3D object, int attModel) {
		return quickStream(false, url, identifier, toLoop, object, attModel);
	}

	/**
	 * Creates a temporary source, binds it to the specified Object3D, and streams
	 * it.  Default value is used for either fade-distance or roll-off factor,
	 * depending on the value of parameter 'attModel'.  After the source finishes
	 * playing, it is removed.  Returns a randomly generated name for the new
	 * source.  NOTE: to make a source created by this method permanent, call the
	 * setActive() method using the return value for sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @param attModel   Attenuation model to use.
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, URL url, String identifier, boolean toLoop, Object3D object, int attModel) {
		switch (attModel) {
			case SoundSystemConfig.ATTENUATION_ROLLOFF:
				return quickStream(priority, url, identifier, toLoop, object, attModel, SoundSystemConfig.getDefaultRolloff());
			case SoundSystemConfig.ATTENUATION_LINEAR:
				return quickStream(priority, url, identifier, toLoop, object, attModel, SoundSystemConfig.getDefaultFadeDistance());
			default:
				return quickStream(priority, url, identifier, toLoop, object, attModel, 0);
		}
	}

	/**
	 * Creates a temporary non-priority source, binds it to the specified Object3D,
	 * and streams it.  After the source finishes playing, it is removed.  Returns
	 * a randomly generated name for the new source.  NOTE: to make a source
	 * created by this method permanent, call the setActive() method using the
	 * return value for sourceName.
	 *
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(URL url, String identifier, boolean toLoop, Object3D object, int attModel, float distOrRoll) {
		return quickStream(false, url, identifier, toLoop, object, attModel, distOrRoll);
	}

	/**
	 * Creates a temporary source, binds it to the specified Object3D, and streams
	 * it.  After the source finishes playing, it is removed.  Returns a randomly
	 * generated name for the new source.  NOTE: to make a source created by this
	 * method permanent, call the setActive() method using the return value for
	 * sourceName.
	 *
	 * @param priority   Setting this to true will prevent other sounds from overriding this one.
	 * @param url        URL handle to the sound file to stream at this source.
	 * @param identifier Filename/identifier of the file referenced by the URL.
	 * @param toLoop     Should this source loop, or play only once.
	 * @param object     Object3D for this source to follow.
	 * @param attModel   Attenuation model to use.
	 * @param distOrRoll Either the fading distance or roll-off factor, depending on the value of "attModel".
	 * @return The new source's name.
	 */
	public String quickStream(boolean priority, URL url, String identifier, boolean toLoop, Object3D object, int attModel, float distOrRoll) {
		//generate a random name for this source:
		String sourceName = "Source_" + randomNumberGenerator.nextInt() + "_" + randomNumberGenerator.nextInt();

		SimpleVector jpctPosition;
		if (object != null) jpctPosition = object.getTransformedCenter();
		else jpctPosition = new SimpleVector(0, 0, 0);

		SimpleVector position = convertCoordinates(jpctPosition);

		// Queue a command to quick stream this new source:
		commandQueue(new CommandObject(CommandObject.QUICK_PLAY, priority, true, toLoop, sourceName, new FilenameURL(url, identifier), position.x, position.y, position.z, attModel, distOrRoll, true));
		commandQueue(new CommandObject(CommandObject.PLAY, sourceName));
		// Wake the command thread to process commands:
		commandThread.interrupt();

		// Create the boundObjects map if it doesn't exist:
		if (boundObjects == null) boundObjects = new HashMap<>();

		// Add the source name and Object3D to the map:
		if (object != null) boundObjects.put(sourceName, object);

		// return the new source name.
		return sourceName;
	}

	/**
	 * Move a source to the specified location.
	 *
	 * @param sourceName   Identifier for the source.
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 */
	public void setPosition(String sourceName, SimpleVector jpctPosition) {
		SimpleVector position = convertCoordinates(jpctPosition);

		commandQueue(new CommandObject(CommandObject.SET_POSITION, sourceName, position.x, position.y, position.z));
		commandThread.interrupt();
	}

	/**
	 * Moves the listener relative to the current location.
	 *
	 * @param jpctRelative SimpleVector containing jpct coordinates.
	 */
	public void moveListener(SimpleVector jpctRelative) {
		SimpleVector relative = convertCoordinates(jpctRelative);

		commandQueue(new CommandObject(CommandObject.MOVE_LISTENER, relative.x, relative.y, relative.z));
		commandThread.interrupt();
	}

	/**
	 * Moves the listener to the specified location.
	 *
	 * @param jpctPosition SimpleVector containing jpct coordinates.
	 */
	public void setListenerPosition(SimpleVector jpctPosition) {
		SimpleVector position = convertCoordinates(jpctPosition);

		commandQueue(new CommandObject(CommandObject.SET_LISTENER_POSITION, position.x, position.y, position.z));
		commandThread.interrupt();
	}

	/**
	 * Sets the listener's orientation.
	 *
	 * @param jpctLook normalized SimpleVector representing the "look" direction (in jpct coordinates).
	 * @param jpctUp   normalized SimpleVector representing the "up" direction (in jpct coordinates).
	 */
	public void setListenerOrientation(SimpleVector jpctLook, SimpleVector jpctUp) {
		SimpleVector look = convertCoordinates(jpctLook);
		SimpleVector up = convertCoordinates(jpctUp);

		commandQueue(new CommandObject(CommandObject.SET_LISTENER_ORIENTATION, look.x, look.y, look.z, up.x, up.y, up.z));
		commandThread.interrupt();
	}

	/**
	 * Sets the listener's orientation to match the specified camera.
	 *
	 * @param camera Camera to match.
	 */
	public void setListenerOrientation(Camera camera) {
		SimpleVector look = convertCoordinates(camera.getDirection());
		SimpleVector up = convertCoordinates(camera.getUpVector());

		commandQueue(new CommandObject(CommandObject.SET_LISTENER_ORIENTATION, look.x, look.y, look.z, up.x, up.y, up.z));
		commandThread.interrupt();
	}

	/**
	 * Sets the specified source's velocity, for use in Doppler effect.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Doppler effect.
	 *
	 * @param sourceName The source's name.
	 * @param velocity   Velocity vector in jpct world-space.
	 */
	public void setVelocity(String sourceName, SimpleVector velocity) {
		SimpleVector vel = convertCoordinates(velocity);
		commandQueue(new CommandObject(CommandObject.SET_VELOCITY, sourceName, vel.x, vel.y, vel.z));
		commandThread.interrupt();
	}

	/**
	 * Sets the listener's velocity, for use in Doppler effect.
	 * See {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about Doppler effect.
	 *
	 * @param velocity Velocity vector in jpct world-space.
	 */
	public void setListenerVelocity(SimpleVector velocity) {
		SimpleVector vel = convertCoordinates(velocity);
		commandQueue(new CommandObject(CommandObject.SET_LISTENER_VELOCITY, vel.x, vel.y, vel.z));
		commandThread.interrupt();
	}

	/**
	 * Converts a SimpleVector from jpct coordinates into SoundSystem coordinates,
	 * and vice versa.  (y = -y and z = -z)
	 */
	public SimpleVector convertCoordinates(SimpleVector v) {
		return new SimpleVector(v.x, -v.y, -v.z);
	}
}
