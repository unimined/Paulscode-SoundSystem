/*
 * JOrbis License:
 *
 * 		   GNU LESSER GENERAL PUBLIC LICENSE
 *                        Version 3, 29 June 2007
 *
 *  Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 *
 *
 *   This version of the GNU Lesser General Public License incorporates
 * the terms and conditions of version 3 of the GNU General Public
 * License, supplemented by the additional permissions listed below.
 *
 *   0. Additional Definitions.
 *
 *   As used herein, "this License" refers to version 3 of the GNU Lesser
 * General Public License, and the "GNU GPL" refers to version 3 of the GNU
 * General Public License.
 *
 *   "The Library" refers to a covered work governed by this License,
 * other than an Application or a Combined Work as defined below.
 *
 *   An "Application" is any work that makes use of an interface provided
 * by the Library, but which is not otherwise based on the Library.
 * Defining a subclass of a class defined by the Library is deemed a mode
 * of using an interface provided by the Library.
 *
 *   A "Combined Work" is a work produced by combining or linking an
 * Application with the Library.  The particular version of the Library
 * with which the Combined Work was made is also called the "Linked
 * Version".
 *
 *   The "Minimal Corresponding Source" for a Combined Work means the
 * Corresponding Source for the Combined Work, excluding any source code
 * for portions of the Combined Work that, considered in isolation, are
 * based on the Application, and not on the Linked Version.
 *
 *   The "Corresponding Application Code" for a Combined Work means the
 * object code and/or source code for the Application, including any data
 * and utility programs needed for reproducing the Combined Work from the
 * Application, but excluding the System Libraries of the Combined Work.
 *
 *   1. Exception to Section 3 of the GNU GPL.
 *
 *   You may convey a covered work under sections 3 and 4 of this License
 * without being bound by section 3 of the GNU GPL.
 *
 *   2. Conveying Modified Versions.
 *
 *   If you modify a copy of the Library, and, in your modifications, a
 * facility refers to a function or data to be supplied by an Application
 * that uses the facility (other than as an argument passed when the
 * facility is invoked), then you may convey a copy of the modified
 * version:
 *
 *    a) under this License, provided that you make a good faith effort to
 *    ensure that, in the event an Application does not supply the
 *    function or data, the facility still operates, and performs
 *    whatever part of its purpose remains meaningful, or
 *
 *    b) under the GNU GPL, with none of the additional permissions of
 *    this License applicable to that copy.
 *
 *   3. Object Code Incorporating Material from Library Header Files.
 *
 *   The object code form of an Application may incorporate material from
 * a header file that is part of the Library.  You may convey such object
 * code under terms of your choice, provided that, if the incorporated
 * material is not limited to numerical parameters, data structure
 * layouts and accessors, or small macros, inline functions and templates
 * (ten or fewer lines in length), you do both of the following:
 *
 *    a) Give prominent notice with each copy of the object code that the
 *    Library is used in it and that the Library and its use are
 *    covered by this License.
 *
 *    b) Accompany the object code with a copy of the GNU GPL and this license
 *    document.
 *
 *   4. Combined Works.
 *
 *   You may convey a Combined Work under terms of your choice that,
 * taken together, effectively do not restrict modification of the
 * portions of the Library contained in the Combined Work and reverse
 * engineering for debugging such modifications, if you also do each of
 * the following:
 *
 *    a) Give prominent notice with each copy of the Combined Work that
 *    the Library is used in it and that the Library and its use are
 *    covered by this License.
 *
 *    b) Accompany the Combined Work with a copy of the GNU GPL and this license
 *    document.
 *
 *    c) For a Combined Work that displays copyright notices during
 *    execution, include the copyright notice for the Library among
 *    these notices, as well as a reference directing the user to the
 *    copies of the GNU GPL and this license document.
 *
 *    d) Do one of the following:
 *
 *        0) Convey the Minimal Corresponding Source under the terms of this
 *        License, and the Corresponding Application Code in a form
 *        suitable for, and under terms that permit, the user to
 *        recombine or relink the Application with a modified version of
 *        the Linked Version to produce a modified Combined Work, in the
 *        manner specified by section 6 of the GNU GPL for conveying
 *        Corresponding Source.
 *
 *        1) Use a suitable shared library mechanism for linking with the
 *        Library.  A suitable mechanism is one that (a) uses at run time
 *        a copy of the Library already present on the user's computer
 *        system, and (b) will operate properly with a modified version
 *        of the Library that is interface-compatible with the Linked
 *        Version.
 *
 *    e) Provide Installation Information, but only if you would otherwise
 *    be required to provide such information under section 6 of the
 *    GNU GPL, and only to the extent that such information is
 *    necessary to install and execute a modified version of the
 *    Combined Work produced by recombining or relinking the
 *    Application with a modified version of the Linked Version. (If
 *    you use option 4d0, the Installation Information must accompany
 *    the Minimal Corresponding Source and Corresponding Application
 *    Code. If you use option 4d1, you must provide the Installation
 *    Information in the manner specified by section 6 of the GNU GPL
 *    for conveying Corresponding Source.)
 *
 *   5. Combined Libraries.
 *
 *   You may place library facilities that are a work based on the
 * Library side by side in a single library together with other library
 * facilities that are not Applications and are not covered by this
 * License, and convey such a combined library under terms of your
 * choice, if you do both of the following:
 *
 *    a) Accompany the combined library with a copy of the same work based
 *    on the Library, uncombined with any other library facilities,
 *    conveyed under the terms of this License.
 *
 *    b) Give prominent notice with the combined library that part of it
 *    is a work based on the Library, and explaining where to find the
 *    accompanying uncombined form of the same work.
 *
 *   6. Revised Versions of the GNU Lesser General Public License.
 *
 *   The Free Software Foundation may publish revised and/or new versions
 * of the GNU Lesser General Public License from time to time. Such new
 * versions will be similar in spirit to the present version, but may
 * differ in detail to address new problems or concerns.
 *
 *   Each version is given a distinguishing version number. If the
 * Library as you received it specifies that a certain numbered version
 * of the GNU Lesser General Public License "or any later version"
 * applies to it, you have the option of following the terms and
 * conditions either of that published version or of any later version
 * published by the Free Software Foundation. If the Library as you
 * received it does not specify a version number of the GNU Lesser
 * General Public License, you may choose any version of the GNU Lesser
 * General Public License ever published by the Free Software Foundation.
 *
 *   If the Library as you received it specifies that a proxy can decide
 * whether future versions of the GNU Lesser General Public License shall
 * apply, that proxy's public statement of acceptance of any version is
 * permanent authorization for you to choose that version for the
 * Library.
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

package paulscode.sound.codecs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.sound.sampled.AudioFormat;

// From the JOrbis library, https://www.jcraft.com/jorbis/
import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.Info;

import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemLogger;

/**
 * The CodecJOrbis class provides an ICodec interface to the external JOrbis library.
 */
@SuppressWarnings({"unused", "FieldMayBeFinal", "FieldCanBeLocal"})
public class CodecJOrbis implements ICodec {
	/**
	 * URL to the audio file to stream from.
	 */
	private URL url;

	/**
	 * Used for connecting to the URL.
	 */
	private URLConnection urlConnection = null;

	/**
	 * InputStream context for reading data from the file.
	 */
	private InputStream inputStream;

	/**
	 * Format in which the converted audio data is stored.
	 */
	private AudioFormat audioFormat;

	/**
	 * True if there is no more data to read in.
	 */
	private boolean endOfStream = false;

	/**
	 * True if the stream has finished initializing.
	 */
	private boolean initialized = false;

	/**
	 * Used to hold the data as it is read in.
	 */
	private byte[] buffer = null;

	/**
	 * Amount of data to read in at one time.
	 */
	private int bufferSize;

	/**
	 * Number of bytes read.
	 */
	private int count = 0;

	/**
	 * Location within the data.
	 */
	private int index = 0;

	/**
	 * Size of the data after it has been converted into pcm.
	 */
	private int convertedBufferSize;

	/**
	 * Linear buffer to hold the data after it has been converted into pcm.
	 */
	private byte[] convertedBuffer = null;

	/**
	 * Nonlinear pcm data.
	 */
	private float[][][] pcmInfo;

	/**
	 * Location within the data.
	 */
	private int[] pcmIndex;

	/**
	 * Data packet.
	 */
	private Packet      joggPacket      = new Packet();
	/**
	 * Data Page.
	 */
	private Page        joggPage        = new Page();
	/**
	 * Stream state.
	 */
	private StreamState joggStreamState = new StreamState();
	/**
	 * Packet streaming layer.
	 */
	private SyncState   joggSyncState   = new SyncState();
	/**
	 * Internal data storage.
	 */
	private DspState    jorbisDspState  = new DspState();
	/**
	 * Block of stored JOrbis data.
	 */
	private Block       jorbisBlock     = new Block(jorbisDspState);
	/**
	 * Comment fields.
	 */
	private Comment     jorbisComment   = new Comment();
	/**
	 * Info about the data.
	 */
	private Info        jorbisInfo      = new Info();

	/**
	 * Processes status messages, warnings, and error messages.
	 */
	private SoundSystemLogger logger;

	/**
	 * Constructor:  Grabs a handle to the logger.
	 */
	public CodecJOrbis() {
		logger = SoundSystemConfig.getLogger();
	}

	/**
	 * This method is ignored by CodecJOrbis, because it produces "nice" data.
	 *
	 * @param b True if the calling audio library requires byte-reversal from certain codecs
	 */
	public void reverseByteOrder(boolean b) {
	}

	/**
	 * Prepares an input stream to read from.  If another stream is already opened,
	 * it will be closed and a new input stream opened in its place.
	 *
	 * @param url URL to an ogg file to stream from.
	 * @return False if an error occurred or if end of stream was reached.
	 */
	public boolean initialize(URL url) {
		initialized(SET, false);

		if (joggStreamState != null) joggStreamState.clear();
		if (jorbisBlock != null) jorbisBlock.clear();
		if (jorbisDspState != null) jorbisDspState.clear();
		if (jorbisInfo != null) jorbisInfo.clear();
		if (joggSyncState != null) joggSyncState.clear();

		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException ignored) {
			}
		}

		this.url = url;
		//this.bufferSize = SoundSystemConfig.getStreamingBufferSize() / 2;
		this.bufferSize = 4096 * 2;

		buffer = null;
		count = 0;
		index = 0;

		joggStreamState = new StreamState();
		jorbisBlock = new Block(jorbisDspState);
		jorbisDspState = new DspState();
		jorbisInfo = new Info();
		joggSyncState = new SyncState();

		try {
			urlConnection = url.openConnection();
		} catch (IOException e) {
			errorMessage("Unable to create a UrlConnection in method " + "'initialize'.");
			printStackTrace(e);
			cleanup();
			return false;
		}
		if (urlConnection != null) {
			try {
				inputStream = urlConnection.getInputStream();
			} catch (IOException ioe) {
				errorMessage("Unable to acquire input stream in method " + "'initialize'.");
				printStackTrace(ioe);
				cleanup();
				return false;
			}
		}

		endOfStream(SET, false);

		joggSyncState.init();
		joggSyncState.buffer(bufferSize);
		buffer = joggSyncState.data;

		try {
			if (!readHeader()) {
				errorMessage("Error reading the header");
				return false;
			}
		} catch (IOException ioe) {
			errorMessage("Error reading the header");
			return false;
		}

		convertedBufferSize = bufferSize * 2;

		jorbisDspState.synthesis_init(jorbisInfo);
		jorbisBlock.init(jorbisDspState);

		int channels = jorbisInfo.channels;
		int rate = jorbisInfo.rate;

		audioFormat = new AudioFormat((float) rate, 16, channels, true, false);
		pcmInfo = new float[1][][];
		pcmIndex = new int[jorbisInfo.channels];

		initialized(SET, true);

		return true;
	}

	/**
	 * Returns false if the stream is busy initializing.
	 *
	 * @return True if steam is initialized.
	 */
	public boolean initialized() {
		return initialized(GET, XXX);
	}

	/**
	 * Reads in one stream buffer worth of audio data.  See
	 * {@link SoundSystemConfig SoundSystemConfig} for more
	 * information about accessing and changing default settings.
	 *
	 * @return The audio data wrapped into a SoundBuffer context.
	 */
	public SoundBuffer read() {
		byte[] returnBuffer = null;

		while (!endOfStream(GET, XXX) && (returnBuffer == null || returnBuffer.length < SoundSystemConfig.getStreamingBufferSize())) {
			if (returnBuffer == null) returnBuffer = readBytes();
			else returnBuffer = appendByteArrays(returnBuffer, readBytes());
		}

		if (returnBuffer == null) return null;

		return new SoundBuffer(returnBuffer, audioFormat);
	}

	/**
	 * Reads in all the audio data from the stream (up to the default "maximum file size").
	 * See {@link SoundSystemConfig} for more information about accessing and changing default settings.
	 *
	 * @return the audio data wrapped into a SoundBuffer context.
	 */
	public SoundBuffer readAll() {
		byte[] returnBuffer = null;

		while (!endOfStream(GET, XXX)) {
			if (returnBuffer == null) returnBuffer = readBytes();
			else returnBuffer = appendByteArrays(returnBuffer, readBytes());
		}

		if (returnBuffer == null) return null;

		return new SoundBuffer(returnBuffer, audioFormat);
	}

	/**
	 * Returns false if there is still more data available to be read in.
	 *
	 * @return True if end of stream was reached.
	 */
	public boolean endOfStream() {
		return endOfStream(GET, XXX);
	}

	/**
	 * Closes the input stream and remove references to all instantiated objects.
	 */
	public void cleanup() {
		joggStreamState.clear();
		jorbisBlock.clear();
		jorbisDspState.clear();
		jorbisInfo.clear();
		joggSyncState.clear();

		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException ignored) {
			}
		}

		joggStreamState = null;
		jorbisBlock = null;
		jorbisDspState = null;
		jorbisInfo = null;
		joggSyncState = null;
		inputStream = null;
	}

	/**
	 * Returns the audio format of the data being returned by the read() and
	 * readAll() methods.
	 *
	 * @return Information wrapped into an AudioFormat context.
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	/**
	 * Reads in the header information for the ogg file, which is contained in the
	 * first three packets of data.
	 *
	 * @return True if successful.
	 */
	private boolean readHeader() throws IOException {
		// Update up JOrbis internal buffer:
		index = joggSyncState.buffer(bufferSize);
		// Read in a buffer of data:
		int bytes = inputStream.read(joggSyncState.data, index, bufferSize);
		if (bytes < 0) bytes = 0;
		// Let JOrbis know how many bytes we got:
		joggSyncState.wrote(bytes);

		if (joggSyncState.pageout(joggPage) != 1) {
			// Finished reading the entire file:
			if (bytes < bufferSize) return true;

			errorMessage("Ogg header not recognized in method 'readHeader'.");
			return false;
		}

		// Initialize JOrbis:
		joggStreamState.init(joggPage.serialno());

		jorbisInfo.init();
		jorbisComment.init();
		if (joggStreamState.pagein(joggPage) < 0) {
			errorMessage("Problem with first Ogg header page in method " + "'readHeader'.");
			return false;
		}

		if (joggStreamState.packetout(joggPacket) != 1) {
			errorMessage("Problem with first Ogg header packet in method " + "'readHeader'.");
			return false;
		}

		if (jorbisInfo.synthesis_headerin(jorbisComment, joggPacket) < 0) {
			errorMessage("File does not contain Vorbis header in method " + "'readHeader'.");
			return false;
		}

		int i = 0;
		while (i < 2) {
			while (i < 2) {
				int result = joggSyncState.pageout(joggPage);
				if (result == 0) break;
				if (result == 1) {
					joggStreamState.pagein(joggPage);
					while (i < 2) {
						result = joggStreamState.packetout(joggPacket);
						if (result == 0) break;

						if (result == -1) {
							errorMessage("Secondary Ogg header corrupt in " + "method 'readHeader'.");
							return false;
						}

						jorbisInfo.synthesis_headerin(jorbisComment, joggPacket);
						i++;
					}
				}
			}
			index = joggSyncState.buffer(bufferSize);
			bytes = inputStream.read(joggSyncState.data, index, bufferSize);
			if (bytes < 0) bytes = 0;
			if (bytes == 0 && i < 2) {
				errorMessage("End of file reached before finished reading" + "Ogg header in method 'readHeader'");
				return false;
			}

			joggSyncState.wrote(bytes);
		}

		index = joggSyncState.buffer(bufferSize);
		buffer = joggSyncState.data;

		return true;
	}

	/**
	 * Reads and decodes a chunk of data of length "convertedBufferSize".
	 *
	 * @return Array containing the converted audio data.
	 */
	private byte[] readBytes() {
		if (!initialized(GET, XXX)) return null;

		if (endOfStream(GET, XXX)) return null;

		if (convertedBuffer == null) convertedBuffer = new byte[convertedBufferSize];
		byte[] returnBuffer = null;

		float[][] pcmf;
		int samples, bout, ptr, mono, val, i, j;

		switch (joggSyncState.pageout(joggPage)) {
			case (0):
			case (-1):
				break;
			default: {
				joggStreamState.pagein(joggPage);
				if (joggPage.granulepos() == 0) {
					endOfStream(SET, true);
					return null;
				}

				processPackets:
				while (true) {
					switch (joggStreamState.packetout(joggPacket)) {
						case (0):
							break processPackets;
						case (-1):
							break;
						default: {
							if (jorbisBlock.synthesis(joggPacket) == 0) jorbisDspState.synthesis_blockin(jorbisBlock);

							while ((samples = jorbisDspState.synthesis_pcmout(pcmInfo, pcmIndex)) > 0) {
								pcmf = pcmInfo[0];
								bout = (Math.min(samples, convertedBufferSize));
								for (i = 0; i < jorbisInfo.channels; i++) {
									ptr = i * 2;
									mono = pcmIndex[i];
									for (j = 0; j < bout; j++) {
										val = (int) (pcmf[i][mono + j] * 32767);
										if (val > 32767) val = 32767;
										if (val < -32768) val = -32768;
										if (val < 0) val = val | 0x8000;
										convertedBuffer[ptr] = (byte) (val);
										convertedBuffer[ptr + 1] = (byte) (val >>> 8);
										ptr += 2 * (jorbisInfo.channels);
									}
								}
								jorbisDspState.synthesis_read(bout);

								returnBuffer = appendByteArrays(returnBuffer, convertedBuffer, 2 * jorbisInfo.channels * bout);
							}
						}
					}
				}

				if (joggPage.eos() != 0) endOfStream(SET, true);
			}
		}

		if (!endOfStream(GET, XXX)) {
			index = joggSyncState.buffer(bufferSize);
			buffer = joggSyncState.data;
			try {
				count = inputStream.read(buffer, index, bufferSize);
			} catch (Exception e) {
				printStackTrace(e);
				return null;
			}
			if (count == -1) return returnBuffer;

			joggSyncState.wrote(count);
			if (count == 0) endOfStream(SET, true);
		}

		return returnBuffer;
	}

	/**
	 * Internal method for synchronizing access to the boolean 'initialized'.
	 *
	 * @param action GET or SET.
	 * @param value  New value if action == SET, or XXX if action == GET.
	 * @return True if steam is initialized.
	 */
	private synchronized boolean initialized(boolean action, boolean value) {
		if (action == SET) initialized = value;
		return initialized;
	}

	/**
	 * Internal method for synchronizing access to the boolean 'endOfStream'.
	 *
	 * @param action GET or SET.
	 * @param value  New value if action == SET, or XXX if action == GET.
	 * @return True if end of stream was reached.
	 */
	private synchronized boolean endOfStream(boolean action, boolean value) {
		if (action == SET) endOfStream = value;
		return endOfStream;
	}

	/**
	 * Trims down the size of the array if it is larger than the specified
	 * maximum length.
	 *
	 * @param array     Array containing audio data.
	 * @param maxLength Maximum size this array may be.
	 * @return New array.
	 */
	private static byte[] trimArray(byte[] array, int maxLength) {
		byte[] trimmedArray = null;
		if (array != null && array.length > maxLength) {
			trimmedArray = new byte[maxLength];
			System.arraycopy(array, 0, trimmedArray, 0, maxLength);
		}
		return trimmedArray;
	}

	/**
	 * Creates a new array with the second array appended to the end of the first
	 * array.
	 *
	 * @param arrayOne      The first array.
	 * @param arrayTwo      The second array.
	 * @param arrayTwoBytes The number of bytes to append from the second array.
	 * @return Byte array containing information from both arrays.
	 */
	private static byte[] appendByteArrays(byte[] arrayOne, byte[] arrayTwo, int arrayTwoBytes) {
		byte[] newArray;
		int bytes = arrayTwoBytes;

		// Make sure we aren't trying to append more than is there:
		if (arrayTwo == null || arrayTwo.length == 0) bytes = 0;
		else if (arrayTwo.length < arrayTwoBytes) bytes = arrayTwo.length;

		if (arrayOne == null && (arrayTwo == null || bytes <= 0)) {
			// no data, just return
			return null;
		} else if (arrayOne == null) {
			// create the new array, same length as arrayTwo:
			newArray = new byte[bytes];
			// fill the new array with the contents of arrayTwo:
			System.arraycopy(arrayTwo, 0, newArray, 0, bytes);
		} else if (arrayTwo == null || bytes <= 0) {
			// create the new array, same length as arrayOne:
			newArray = new byte[arrayOne.length];
			// fill the new array with the contents of arrayOne:
			System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
		} else {
			// create the new array large enough to hold both arrays:
			newArray = new byte[arrayOne.length + bytes];
			System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
			// fill the new array with the contents of both arrays:
			System.arraycopy(arrayTwo, 0, newArray, arrayOne.length, bytes);
		}

		return newArray;
	}

	/**
	 * Creates a new array with the second array appended to the end of the first
	 * array.
	 *
	 * @param arrayOne The first array.
	 * @param arrayTwo The second array.
	 * @return Byte array containing information from both arrays.
	 */
	private static byte[] appendByteArrays(byte[] arrayOne, byte[] arrayTwo) {
		byte[] newArray;
		if (arrayOne == null && arrayTwo == null) {
			// no data, just return
			return null;
		} else if (arrayOne == null) {
			// create the new array, same length as arrayTwo:
			newArray = new byte[arrayTwo.length];
			// fill the new array with the contents of arrayTwo:
			System.arraycopy(arrayTwo, 0, newArray, 0, arrayTwo.length);
		} else if (arrayTwo == null) {
			// create the new array, same length as arrayOne:
			newArray = new byte[arrayOne.length];
			// fill the new array with the contents of arrayOne:
			System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
		} else {
			// create the new array large enough to hold both arrays:
			newArray = new byte[arrayOne.length + arrayTwo.length];
			System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
			// fill the new array with the contents of both arrays:
			System.arraycopy(arrayTwo, 0, newArray, arrayOne.length, arrayTwo.length);
		}

		return newArray;
	}

	/**
	 * Prints an error message.
	 *
	 * @param message Message to print.
	 */
	private void errorMessage(String message) {
		logger.errorMessage("CodecJOrbis", message, 0);
	}

	/**
	 * Prints an exception's error message followed by the stack trace.
	 *
	 * @param e Exception containing the information to print.
	 */
	private void printStackTrace(Exception e) {
		logger.printStackTrace(e, 1);
	}
}
