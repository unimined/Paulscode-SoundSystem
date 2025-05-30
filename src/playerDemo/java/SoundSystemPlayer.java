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

import com.threed.jpct.util.KeyMapper;
import com.threed.jpct.util.KeyState;

import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.codecs.CodecJOgg;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.libraries.LibraryJavaSound;

/**
 * The SoundSystemPlayerApplet class demonstrates playing MIDI, streaming .ogg,
 * and using the quickPlay() method from the SoundSystem core library.
 */
public class SoundSystemPlayer extends JFrame implements Runnable {
	boolean     running = true;
	SoundSystem mySoundSystem;

	private final int width  = 640;
	private final int height = 480;

	KeyMapper keyMapper;

	boolean musicPlayStop  = false;
	boolean streamPlayStop = false;
	boolean JavaOpenAL     = false;

	BufferedImage frameBuffer = null;

	// top and left insets (both zero for an applet)
	private final int titleBarHeight;
	private final int leftBorderWidth;

	public static void main(String[] args) {
		new SoundSystemPlayer();
	}

	public SoundSystemPlayer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("SoundSystem Player Demo");
		pack();
		Insets insets = getInsets();
		titleBarHeight = insets.top - 1;
		leftBorderWidth = insets.left - 1;
		setSize(width + leftBorderWidth + insets.right - 1, height + titleBarHeight + insets.bottom - 1);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);

		init();
	}

	public void init() {
		// get keystrokes picked up by the canvas:
		keyMapper = new KeyMapper(this);

		frameBuffer = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

		drawFrame();

		this.requestFocus();
		this.requestFocusInWindow();

		// start the main loop
		new Thread(this).start();
	}

	// Demonstrate OpenAL 3D panning with source at various distances:
	@Override
	public void run() {
		// Load some library and codec plugins:
		try {
			SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
			SoundSystemConfig.addLibrary(LibraryJavaSound.class);
			SoundSystemConfig.setCodec("wav", CodecWav.class);
			SoundSystemConfig.setCodec("ogg", CodecJOgg.class);
		} catch (SoundSystemException e) {
			System.out.println("error linking with the plugins");
		}

		// Instantiate the SoundSystem:
		try {
			mySoundSystem = new SoundSystem(LibraryJavaSound.class);
			mySoundSystem = new SoundSystem(LibraryLWJGLOpenAL.class);
		} catch (SoundSystemException e) {
			System.out.println("JavaSound library is not compatible on this computer");
			e.printStackTrace();
			return;
		}
		mySoundSystem.newStreamingSource(true, "OGG Music", "beats.ogg", true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
		mySoundSystem.newStreamingSource(true, "MIDI Music", "beethoven.mid", true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
		while (running) {
			pollKeyboard();  // check for keyboard input

			this.repaint();
			try {
				Thread.sleep(20);
			} catch (Exception ignored) {
			}
		}
		mySoundSystem.cleanup();
	}

	@Override
	public void paint(Graphics g) {
		if (frameBuffer == null) return;

		g.drawImage(frameBuffer, leftBorderWidth, titleBarHeight, null);
	}

	public void drawFrame() {
		Graphics gB = frameBuffer.getGraphics();

		gB.setColor(Color.white);
		gB.fillRect(0, 0, width, height);
		gB.setColor(Color.black);

		if (JavaOpenAL) gB.drawString("<S> Switches to JavaSound", 5, 25);
		else gB.drawString("<S> Switches to OpenAL", 5, 25);

		if (musicPlayStop) gB.drawString("<Enter> Stops beethoven.mid", 5, 125);
		else gB.drawString("<Enter> Plays beethoven.mid", 5, 125);
		if (streamPlayStop) gB.drawString("<Spacebar> Stops beats.ogg stream", 5, 175);
		else gB.drawString("<Spacebar> Streams beats.ogg", 5, 175);

		gB.drawString("<1> Quick-Plays boing.wav", 5, 275);
		gB.drawString("<2> Quick-Plays click.wav", 5, 325);
		gB.drawString("<3> Quick-Plays comeon.wav", 5, 375);
		gB.drawString("<4> Quick-Plays explosion.wav", 5, 425);
		gB.drawString("<5> Quick-Plays bell.wav", 5, 475);

		gB.drawString("<ESC> Shuts down", 5, 525);
	}

	// Use the KeyMapper to poll the keyboard
	private void pollKeyboard() {
		KeyState state;
		do {
			state = keyMapper.poll();
			if (state != KeyState.NONE) {
				keyAffected(state);
			}
		} while (state != KeyState.NONE);
	}

	private void keyAffected(KeyState state) {
		int code = state.getKeyCode();
		boolean event = state.getState();

		switch (code) {
			case (KeyEvent.VK_S): {
				if (event) {
					try {
						if (JavaOpenAL) mySoundSystem.switchLibrary(LibraryJavaSound.class);
						else mySoundSystem.switchLibrary(LibraryLWJGLOpenAL.class);
						musicPlayStop = false;
						streamPlayStop = false;
						JavaOpenAL = !JavaOpenAL;
					} catch (SoundSystemException e) {
						if (JavaOpenAL) System.out.println("JavaSound library not " + "compatible on this computer");
						else System.out.println("OpenAL library not " + "compatible on this computer");
						e.printStackTrace();
					}
					drawFrame();
				}
				break;
			}
			case (KeyEvent.VK_ESCAPE): {
				running = event;
				break;
			}
			case (KeyEvent.VK_ENTER): {
				if (event) {
					if (musicPlayStop) mySoundSystem.stop("MIDI Music");
					else mySoundSystem.play("MIDI Music");

					mySoundSystem.setVolume("MIDI Music", 0.5f);
					musicPlayStop = !musicPlayStop;
				}
				break;
			}
			case (KeyEvent.VK_SPACE): {
				if (event) {
					if (streamPlayStop) mySoundSystem.stop("OGG Music");
					else mySoundSystem.play("OGG Music");

					streamPlayStop = !streamPlayStop;
				}
				break;
			}
			case (KeyEvent.VK_1): {
				if (event) {
					mySoundSystem.quickPlay(false, "0", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
				}
				break;
			}
			case (KeyEvent.VK_2): {
				if (event) {
					mySoundSystem.quickPlay(false, "click.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
				}
				break;
			}
			case (KeyEvent.VK_3): {
				if (event) {
					mySoundSystem.quickPlay(false, "comeon.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
				}
				break;
			}
			case (KeyEvent.VK_4): {
				if (event) {
					mySoundSystem.quickPlay(false, "explosion.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
				}
				break;
			}
			case (KeyEvent.VK_5): {
				if (event) {
					mySoundSystem.quickPlay(false, "bell.wav", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
				}
				break;
			}
		}
	}
}
