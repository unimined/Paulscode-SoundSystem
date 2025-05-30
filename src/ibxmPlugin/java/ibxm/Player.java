/*
 * IBXM is copyright (c) 2007, Martin Cameron, and is licensed under the BSD License.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of mumart nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ibxm;

import java.io.*;
import javax.sound.sampled.*;

public class Player {
	private       Thread play_thread;
	private final IBXM   ibxm;
	private       Module module;
	private int    song_duration;
	private boolean running, loop;
	private final byte[]         output_buffer;
	private final SourceDataLine output_line;

	/**
	 * Simple command-line test player.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Usage: java ibxm.Player <module file>");
			System.exit(0);
		}
		FileInputStream file_input_stream = new FileInputStream(args[0]);
		Player player = new Player();
		player.set_module(Player.load_module(file_input_stream));
		file_input_stream.close();
		player.play();
	}

	/**
	 * Decode the data in the specified InputStream into a Module instance.
	 *
	 * @param input an InputStream containing the module file to be decoded.
	 * @throws IllegalArgumentException if the data is not recognised as a module file.
	 */
	public static Module load_module(InputStream input) throws IllegalArgumentException, IOException {
		DataInputStream data_input_stream = new DataInputStream(input);
		/* Check if data is in XM format.*/
		byte[] xm_header = new byte[60];
		data_input_stream.readFully(xm_header);
		if (FastTracker2.is_xm(xm_header))
			return FastTracker2.load_xm(xm_header, data_input_stream);
		/* Check if data is in ScreamTracker 3 format.*/
		byte[] s3m_header = new byte[96];
		System.arraycopy(xm_header, 0, s3m_header, 0, 60);
		data_input_stream.readFully(s3m_header, 60, 36);
		if (ScreamTracker3.is_s3m(s3m_header))
			return ScreamTracker3.load_s3m(s3m_header, data_input_stream);
		/* Check if data is in ProTracker format.*/
		byte[] mod_header = new byte[1084];
		System.arraycopy(s3m_header, 0, mod_header, 0, 96);
		data_input_stream.readFully(mod_header, 96, 988);
		return ProTracker.load_mod(mod_header, data_input_stream);
	}

	/**
	 * Instantiate a new Player.
	 */
	public Player() throws LineUnavailableException {
		ibxm = new IBXM(48000);
		set_loop(true);
		output_line = AudioSystem.getSourceDataLine(new AudioFormat(48000, 16, 2, true, true));
		output_buffer = new byte[1024 * 4];
	}

	/**
	 * Set the Module instance to be played.
	 */
	public void set_module(Module m) {
		if (m != null) module = m;
		stop();
		ibxm.set_module(module);
		song_duration = ibxm.calculate_song_duration();
	}

	/**
	 * If loop is true, playback will continue indefinitely,
	 * otherwise the module will play through once and stop.
	 */
	public void set_loop(boolean loop) {
		this.loop = loop;
	}

	/**
	 * Open the audio device and begin playback.
	 * If a module is already playing it will be restarted.
	 */
	public void play() {
		stop();
		play_thread = new Thread(new Driver());
		play_thread.start();
	}

	/**
	 * Stop playback and close the audio device.
	 */
	public void stop() {
		running = false;
		if (play_thread != null) {
			try {
				play_thread.join();
			} catch (InterruptedException ignored) {
			}
		}
	}

	private class Driver implements Runnable {
		public void run() {
			if (running) return;
			try {
				output_line.open();
				output_line.start();
				int play_position = 0;
				running = true;
				while (running) {
					int frames = song_duration - play_position;
					if (frames > 1024) frames = 1024;
					ibxm.get_audio(output_buffer, frames);
					output_line.write(output_buffer, 0, frames * 4);
					play_position += frames;
					if (play_position >= song_duration) {
						play_position = 0;
						if (!loop) running = false;
					}
				}
				output_line.drain();
				output_line.close();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
	}
}
