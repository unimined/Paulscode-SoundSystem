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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.LinkedList;
import java.util.Random;
import java.util.stream.IntStream;

// From the jPCT API (https://www.jpct.net):
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.IRenderer;
import com.threed.jpct.Lights;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.KeyMapper;
import com.threed.jpct.util.KeyState;
import com.threed.jpct.util.Light;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.libraries.LibraryJavaSound;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.codecs.CodecWav;

import javax.swing.*;

/**
 * The Helicopter class demonstrates the core SoundSystem used in an actual
 * applet or application.  Comments are included for how to easily switch
 * between applet or application.
 */
@SuppressWarnings("FieldCanBeLocal")
public class Helicopter extends JFrame implements MouseListener, MouseMotionListener, Runnable {
	/*************************** SETTINGS *************************************/

	// Balls:
	//--------------------------------------------------------------------------
	private final int   ballCount              = 20;                   // number of balls
	private final float maxBallScale           = 50.0f;           // largest ball size
	private final float minBallScale           = 5.0f;            // smallest ball size
	private final float maxBounceHeight        = 800.0f;       // highest bounce
	private final float minBounceHeight        = 50.0f;        // shortest bounce
	private final float maxBallDistPerMilli    = 0.3f;     // fastest linear speed
	private final float minBallDistPerMilli    = 0.01f;    // slowest linear speed
	private final float maxBoingPitch          = 1.5f;           // highest pitch boing
	private final float minBoingPitch          = 0.75f;          // lowest pitch boing
	private final float maxBounceAnglePerMilli = 0.01f; // fastest bounce speed
	private final float minBounceAnglePerMilli = 0.007f;// slowest bounce speed
	private final float boingAttenuation       = 0.003f;      // "boing" attenuation
	//--------------------------------------------------------------------------

	// Ground, sky, and sun:
	//--------------------------------------------------------------------------
	// ground y-position
	private final float        groundLevel        = 100f;
	// sky sphere size
	private final float        skyScale           = 100.0f;
	// sky sphere add-color
	private final Color        skyBrightness = new Color(100, 100, 100);
	// overall brightness
	private final Color        ambientLight = new Color(50, 50, 50);
	// sun position in sky
	private final SimpleVector sunSatelliteOffset = new SimpleVector(1000, -1000, -1000);
	// sun brightness
	private final Color        sunIntensity = new Color(255, 255, 255);
	// sunlight attenuation
	private final float        sunAttenuation     = 500.0f;
	// sun's furthest distance
	private final float        sunDiscardDistance = 5000.0f;
	//--------------------------------------------------------------------------

	// Helicopter, camera:
	//--------------------------------------------------------------------------
	private final float        helicopterScale    = 0.075f;
	// rest-position offset
	private final SimpleVector cameraSatelliteOffset = new SimpleVector(10, 10, 50);
	// propeller speed
	private final float        rotationPerMilli   = 0.04f;
	// vertical hover distance
	private final float        hoverHeight = 0.3f * helicopterScale;
	// hover speed
	private final float        hoverPerMilli      = 0.002f;
	// x-axis lift limit
	private final float        maxLiftAngle = (float) Math.PI / 8.0f;
	// x-axis lift speed
	private final float        liftPerMilli       = 0.00157f;
	// y-axis turn limit
	private final float        maxTurnAngle = (float) Math.PI / 4.0f;
	// y-axis turn speed
	private final float        turnPerMilli       = 0.00157f;
	// z-axis tilt limit
	private final float        maxTiltAngle = (float) Math.PI / 4.0f;
	// z-axis tilt speed
	private final float        tiltPerMilli = 0.00157f;
	// linear offset limit
	private final float        maxSatelliteDistance = 10.0f;
	// linear offset speed
	private final float        satelliteDistPerMilli = 0.01f;
	// camera turn speed
	private final float        cameraAnglePerMilli = 0.00157f;
	// camera linear speed
	private final float        cameraDistPerMilli = 0.2f;
	// travel distance limit
	private final float        maxTravelDistance  = 4000.0f;
	// smoke generation speed
	private final long         millisPerSmokePuff = 400;
	// smoke exhaust position
	private final SimpleVector smokeOffset = new SimpleVector(-5.0f * helicopterScale, -90.0f * helicopterScale, -100.0f * helicopterScale);
	//--------------------------------------------------------------------------

	/*************************** END SETTINGS *********************************/


	// Main game-loop thread aliveness:
	private boolean alive = true;

	// Set to true after everything finishes loading:
	private boolean initialized = false;

	// List containing all the balls:
	private final LinkedList<Ball> balls = new LinkedList<>();

	// List containing all the smoke puffs:
	private final LinkedList<SmokePuff> smokeList = new LinkedList<>();

	// Random number generator:
	private final Random generator = new Random();

	// Objects used for helicopter, sky, and camera assembly:
	private Object3D cameraBase, cameraPivot, cameraSatellite, helicopterXPivot, helicopterYPivot, helicopter, propeller, exhaustPort, sunSatellite, skySphere;
	private Object3D[] helicopterParts, propellerParts;

	// Thread synchronizer for jPCT-related commands:
	private final Object jpctSync = new Object();

	// jPCT Frame buffer:
	private FrameBuffer buffer = null;
	// jPCT World:
	private World       world  = null;
	// jPCT Light source (for the sun):
	private Light       sun    = null;

	// Canvas to draw on (hardware-accelerated rendering mode):
	private Canvas myCanvas;

	// top and left border sizes:
	private final int titleBarHeight;
	private final int leftBorderWidth;

	// Frame dimensions:
	private final int width  = 640;
	private final int height = 480;

	// Keyboard input:
	private KeyMapper keyMapper;

	// Booleans indicating which arrow keys are pressed:
	private boolean leftPressed  = false;
	private boolean rightPressed = false;
	private boolean upPressed    = false;
	private boolean downPressed  = false;

	// Stores the system time for each tick:
	private long lastTick;

	// Position/orientation/animation working variables:
	private       float        currentTurnAngle         = 0.0f;
	private       float        currentTiltAngle         = 0.0f;
	private       float        currentLiftAngle         = 0.0f;
	private       float        currentSatelliteDistance = 0.0f;
	private       float        hoverAngle               = 0.0f;
	private final SimpleVector helicopterOffset         = new SimpleVector();
	private       long         lastSmokePuffMillis;

	// The Sound System:
	private SoundSystem soundSystem;


	public static void main(String[] args) {
		new Helicopter();
	}

	public Helicopter() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Holy Bouncing Helicopter Balls!");
		pack();
		Insets insets = getInsets();
		titleBarHeight = insets.top - 1;
		leftBorderWidth = insets.left - 1;
		setSize(width + leftBorderWidth + insets.right - 1,
				height + titleBarHeight + insets.bottom - 1);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);

		init();
	}

	/**
	 * Shuts down the main game-loop thread, and cleans up.
	 */
	public void destroy() {
		alive = false;
	}

	/**
	 * Initializes all components of the program.
	 */
	public void init() {
		// Make sure everything will be visible:
		Config.maxPolysVisible = 24000;
		Config.farPlane = 5000;

		world = new World();  // create a new world
		World.setDefaultThread(Thread.currentThread());

		// create a new buffer and canvas to draw on:
		buffer = new FrameBuffer(width, height, FrameBuffer.SAMPLINGMODE_HARDWARE_ONLY);
		buffer.disableRenderer(IRenderer.RENDERER_SOFTWARE);
		myCanvas = buffer.enableGLCanvasRenderer();
		add(myCanvas, BorderLayout.CENTER);
		myCanvas.setVisible(true);

		// Load the helicopter model:
		loadHelicopter(helicopterScale);

		// Load the sky sphere:
		TextureManager.getInstance().addTexture("SKYTILE.JPG", new Texture(getClass().getClassLoader().getResourceAsStream("Textures/skyTile.jpg"), true));
		skySphere = Loader.load3DS(getClass().getClassLoader().getResourceAsStream("Models/skySphere.3ds"), skyScale)[0];
		skySphere.setLighting(Object3D.LIGHTING_NO_LIGHTS);
		skySphere.setAdditionalColor(skyBrightness);
		skySphere.rotateX((float) Math.PI / 2);
		skySphere.rotateZ((float) Math.PI);
		skySphere.rotateMesh();
		skySphere.setRotationMatrix(new Matrix());
		skySphere.rotateY((float) Math.PI + 0.5f);
		world.addObject(skySphere);

		// Create the ground:
		TextureManager.getInstance().addTexture("Ground", new Texture(getClass().getClassLoader().getResourceAsStream("Textures/grassTile.jpg"), true));
		Object3D ground;
		int x, y;
		for (y = -5; y < 5; y++) {
			for (x = -5; x < 5; x++) {
				ground = Primitives.getPlane(1, 1000);
				// Make sure we can see if from above:
				ground.rotateX((float) Math.PI / 2);
				// Move it to the correct position:
				ground.translate(new SimpleVector(x * 1000, groundLevel, y * 1000));
				ground.setTexture("Ground");
				world.addObject(ground);
			}
		}

		// Set up the camera/helicopter/sky/sun assembly:
		cameraBase = Object3D.createDummyObj();
		cameraPivot = Object3D.createDummyObj();
		cameraSatellite = Object3D.createDummyObj();
		sunSatellite = Object3D.createDummyObj();
		cameraBase.addChild(cameraPivot);
		cameraBase.addChild(sunSatellite);
		cameraBase.addChild(skySphere);
		cameraPivot.addChild(cameraSatellite);
		cameraSatellite.addChild(helicopterXPivot);
		cameraSatellite.translate(cameraSatelliteOffset);
		sunSatellite.translate(sunSatelliteOffset);

		// Create the balls:
		for (x = 0; x < ballCount; x++)
			balls.add(new Ball());

		// Make sure everything is built:
		world.buildAllObjects();

		// create light sources for the scene:
		letThereBeLight();

		// receive mouse input from the frame and canvas:
		addMouseListener(this);
		addMouseMotionListener(this);
		myCanvas.addMouseListener(this);
		myCanvas.addMouseMotionListener(this);

		// receive keyboard input from the frame and canvas:
		keyMapper = new KeyMapper(myCanvas);
		this.addKeyListener(keyMapper);

		/* SoundSystem Stuff */
		// Set up the Sound System
		try {
			// add some plug-ins:
			SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
			SoundSystemConfig.addLibrary(LibraryJavaSound.class);
			SoundSystemConfig.setCodec("wav", CodecWav.class);
		} catch (SoundSystemException ignored) {
		}
		soundSystem = new SoundSystem();
		// start the helicopter looping-source:
		soundSystem.quickPlay(true, "helicopter.wav", true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
		// play some background music:
		soundSystem.backgroundMusic("music", "cottoneyejoe.mid", true);
		// MIDI tends to be very loud, so turn it way down:
		soundSystem.setVolume("music", 0.05f);
		/* End */

		// Start the timers:
		lastTick = System.currentTimeMillis();
		lastSmokePuffMillis = lastTick;

		// Everything is loaded:
		initialized = true;

		// Start the main game-loop:
		new Thread(this).start();
	}

	/**
	 * Draws the scene.
	 * @param g Graphics instance to draw on (software mode), or ignored (hardware).
	 */
	@Override
	public void paint(Graphics g) {
		if (!initialized) return;

		synchronized (jpctSync) {
			buffer.clear();   // erase the previous frame

			// render the world onto the buffer:
			world.renderScene(buffer);
			world.draw(buffer);
			buffer.update();

			if (buffer.usesRenderer(IRenderer.RENDERER_SOFTWARE)) {
				// Paint this frame onto the applet/frame (software mode)
				buffer.display(g, leftBorderWidth, titleBarHeight);
			} else {
				// Repaint the canvas (hardware mode)
				buffer.displayGLOnly();
				myCanvas.repaint();
			}
		}
	}

	/**
	 * Main game-loop.  Updates all components.
	 */
	@Override
	public void run() {
		// How often to check the MIDI volume: (TODO: Why is this needed?)
		int volumeCheckMillis = 500;
		long lastVolumeCheck = lastTick;

		// Loop until we're told to shut down:
		while (alive) {
			// check for keyboard input:
			pollKeyboard();

			// Thread synchronization (for jPCT components):
			synchronized (jpctSync) {
				// Check the current time and how much has passed:
				long currentTimeMillis = System.currentTimeMillis();
				long millisPast = currentTimeMillis - lastTick;
				lastTick = currentTimeMillis;

				/* SoundSystem Stuff */
				// Check the MIDI volume: (TODO: Why is this needed?)
				if (currentTimeMillis - lastVolumeCheck >= volumeCheckMillis) {
					soundSystem.checkFadeVolumes();
					lastVolumeCheck = currentTimeMillis;
				}
				/* End */

				// Turn the propeller:
				propeller.rotateAxis(propeller.getYAxis(), rotationPerMilli * millisPast);

				// Hover, rotate, spin, etc:
				updateHelicopterOrientation(millisPast);

				// Produce the smoke effects:
				updateSmokePuffs(currentTimeMillis);

				// Make sure the sun is at the proper location in the sky:
				sun.setPosition(sunSatellite.getTransformedCenter());

				// Move the camera based on keyboard input:
				if (leftPressed) cameraPivot.rotateAxis(cameraPivot.getYAxis(), millisPast * cameraAnglePerMilli);
				if (rightPressed) cameraPivot.rotateAxis(cameraPivot.getYAxis(), -millisPast * cameraAnglePerMilli);
				float moveDistance = cameraDistPerMilli * millisPast;
				SimpleVector moveDirection = cameraPivot.getZAxis().normalize();
				if (upPressed) {
					moveDirection.scalarMul(moveDistance);
					cameraBase.translate(moveDirection);
				}
				if (downPressed) {
					moveDirection.scalarMul(-moveDistance);
					cameraBase.translate(moveDirection);
				}

				// If we reach the edge of the world, step back:
				if (cameraBase.getTransformedCenter().length() > maxTravelDistance) {
					SimpleVector limit = cameraBase.getTransformedCenter().normalize();
					limit.scalarMul(maxTravelDistance);
					cameraBase.translate(limit.calcSub(cameraBase.getTransformedCenter()));
				}

				// Update the camera and listener position and orientation:
				updateCameraListener();

				// Loop through all the balls and update them:
				balls.forEach(ball -> ball.update(millisPast));
			}

			// Remind the applet to repaint itself:
			this.repaint();

			// Limit the framerate to < 100 FPS:
			try {
				Thread.sleep(10);
			} catch (InterruptedException ignored) {
			}
		}
		/* SoundSystem Stuff */
		// Clean up:
		soundSystem.cleanup();
		/* End */
	}

	/**
	 * Updates the camera and listener position and orientation.
	 */
	private void updateCameraListener() {
		world.getCamera().setPosition(cameraBase.getTransformedCenter());
		world.getCamera().setOrientation(cameraPivot.getZAxis(), cameraPivot.getYAxis());
		/* SoundSystem Stuff */
		soundSystem.setListenerPosition(world.getCamera().getPosition().x, -world.getCamera().getPosition().y, -world.getCamera().getPosition().z);
		soundSystem.setListenerOrientation(world.getCamera().getDirection().x, -world.getCamera().getDirection().y, -world.getCamera().getDirection().z, world.getCamera().getUpVector().x, -world.getCamera().getUpVector().y, -world.getCamera().getUpVector().z);
		/* End */
	}

	/**
	 * Generates the smoke puff effects based on how much time has elapsed.
	 * @param currentTimeMillis Current system time.
	 */
	public void updateSmokePuffs(long currentTimeMillis) {
		// Check if it is time to generate another smoke puff:
		if (currentTimeMillis - lastSmokePuffMillis > millisPerSmokePuff) {
			lastSmokePuffMillis = currentTimeMillis;
			SimpleVector smokePosition = exhaustPort.getTransformedCenter();
			SimpleVector direction = helicopter.getYAxis();
			direction.scalarMul(-1.0f);
			smokeList.add(new SmokePuff(smokePosition, direction));
		}

		// Loop through all the existing smoke puffs and update them:
		smokeList.removeIf(smokePuff -> !smokePuff.update(currentTimeMillis));
	}

	/**
	 * Updates the helicopter's position and orientation based on how much time has
	 * passed and which keys the user is pressing.
	 * @param millisPast Number of milliseconds since last update.
	 */
	public void updateHelicopterOrientation(long millisPast) {
		// Hover up and down:
		hoverAngle += hoverPerMilli * millisPast;
		if (hoverAngle > (2.0d * Math.PI)) hoverAngle -= (float) (2.0d * Math.PI);
		SimpleVector hoverOffset = new SimpleVector(0, Math.sin(hoverAngle) * hoverHeight, 0);
		helicopterOffset.scalarMul(-1.0f);
		helicopterOffset.add(hoverOffset);
		helicopter.translate(helicopterOffset);

		// Turn and tilt left or right depending on arrow keys:
		float turnAngle = currentTurnAngle;
		float tiltAngle = currentTiltAngle;
		if (leftPressed) {
			turnAngle += millisPast * turnPerMilli;
			tiltAngle += millisPast * tiltPerMilli;
		}
		if (rightPressed) {
			turnAngle -= millisPast * turnPerMilli;
			tiltAngle -= millisPast * tiltPerMilli;
		}
		if (leftPressed == rightPressed) {
			if (turnAngle < 0) {
				turnAngle += millisPast * turnPerMilli;
				if (turnAngle > 0) turnAngle = 0;
			} else if (turnAngle > 0) {
				turnAngle -= millisPast * turnPerMilli;
				if (turnAngle < 0) turnAngle = 0;
			}
			if (tiltAngle < 0) {
				tiltAngle += millisPast * tiltPerMilli;
				if (tiltAngle > 0) tiltAngle = 0;
			} else if (tiltAngle > 0) {
				tiltAngle -= millisPast * tiltPerMilli;
				if (tiltAngle < 0) tiltAngle = 0;
			}
		}
		if (turnAngle > maxTurnAngle) turnAngle = maxTurnAngle;
		if (turnAngle < -maxTurnAngle) turnAngle = -maxTurnAngle;
		if (tiltAngle > maxTiltAngle) tiltAngle = maxTiltAngle;
		if (tiltAngle < -maxTiltAngle) tiltAngle = -maxTiltAngle;
		helicopterYPivot.rotateAxis(helicopterYPivot.getYAxis(), turnAngle - currentTurnAngle);
		currentTurnAngle = turnAngle;
		helicopter.rotateAxis(helicopter.getZAxis(), tiltAngle - currentTiltAngle);
		currentTiltAngle = tiltAngle;

		// Move/tilt forward or back depending on arrow keys:
		float satelliteDistance = currentSatelliteDistance;
		float liftAngle = currentLiftAngle;
		if (upPressed) {
			satelliteDistance += millisPast * satelliteDistPerMilli;
			liftAngle += millisPast * liftPerMilli;
		}
		if (downPressed) {
			satelliteDistance -= millisPast * satelliteDistPerMilli;
			liftAngle -= millisPast * liftPerMilli;
		}
		if (upPressed == downPressed) {
			if (satelliteDistance < 0) {
				satelliteDistance += millisPast * satelliteDistPerMilli;
				if (satelliteDistance > 0) satelliteDistance = 0;
			} else if (satelliteDistance > 0) {
				satelliteDistance -= millisPast * satelliteDistPerMilli;
				if (satelliteDistance < 0) satelliteDistance = 0;
			}
			if (liftAngle < 0) {
				liftAngle += millisPast * liftPerMilli;
				if (liftAngle > 0) liftAngle = 0;
			} else if (liftAngle > 0) {
				liftAngle -= millisPast * liftPerMilli;
				if (liftAngle < 0) liftAngle = 0;
			}
		}
		if (satelliteDistance > maxSatelliteDistance) satelliteDistance = maxSatelliteDistance;
		if (satelliteDistance < -maxSatelliteDistance) satelliteDistance = -maxSatelliteDistance;
		if (liftAngle > maxLiftAngle) liftAngle = maxLiftAngle;
		if (liftAngle < -maxLiftAngle) liftAngle = -maxLiftAngle;
		helicopterXPivot.rotateAxis(helicopterXPivot.getXAxis(), liftAngle - currentLiftAngle);
		currentLiftAngle = liftAngle;

		float distChange = satelliteDistance - currentSatelliteDistance;
		SimpleVector trans = cameraSatellite.getZAxis().normalize();
		trans.scalarMul(distChange);
		cameraSatellite.translate(trans);
		currentSatelliteDistance = satelliteDistance;
	}

	/**
	 * Loads the helicopter model and textures, sets transparencies, and
	 * establishes parent/child relationships.
	 * @param scale Scale to use for the model.
	 */
	public void loadHelicopter(float scale) {
		// Create some textures to use:
		TextureManager.getInstance().addTexture("Red", new Texture(2, 2, new Color(255, 0, 0)));
		TextureManager.getInstance().addTexture("Green", new Texture(2, 2, new Color(0, 255, 0)));
		TextureManager.getInstance().addTexture("Blue", new Texture(2, 2, new Color(0, 0, 255)));
		TextureManager.getInstance().addTexture("Yellow", new Texture(2, 2, new Color(255, 255, 0)));
		TextureManager.getInstance().addTexture("Purple", new Texture(2, 2, new Color(255, 0, 255)));
		TextureManager.getInstance().addTexture("smoke.png", new Texture(getClass().getClassLoader().getResourceAsStream("Textures/smoke.png"), true));
		TextureManager.getInstance().addTexture("smokeFade.png", new Texture(getClass().getClassLoader().getResourceAsStream("Textures/smokeFade.png"), true));
		TextureManager.getInstance().addTexture("camo.jpg", new Texture(getClass().getClassLoader().getResourceAsStream("Textures/camo.jpg"), true));
		TextureManager.getInstance().addTexture("carbon.jpg", new Texture(getClass().getClassLoader().getResourceAsStream("Textures/carbon.jpg"), true));

		// Set up parent/child relationships::
		helicopterXPivot = Object3D.createDummyObj();
		helicopterYPivot = Object3D.createDummyObj();
		helicopter = Object3D.createDummyObj();
		helicopterYPivot.addChild(helicopter);
		helicopterXPivot.addChild(helicopterYPivot);
		helicopterParts = Loader.loadOBJ(getClass().getClassLoader().getResourceAsStream("Models/helicopter.obj"), getClass().getClassLoader().getResourceAsStream("Models/helicopter.mtl"), scale);
		IntStream.range(0, helicopterParts.length).forEach(x -> {
			// turn to the correct initial orientation:
			helicopterParts[x].rotateZ((float) Math.PI);
			helicopterParts[x].rotateMesh();
			helicopterParts[x].setRotationMatrix(new Matrix());
			helicopterParts[x].build();
			world.addObject(helicopterParts[x]);
			helicopter.addChild(helicopterParts[x]);
		});
		helicopterParts[0].setTransparencyMode(Object3D.TRANSPARENCY_MODE_ADD);
		helicopterParts[0].setTransparency(0);
		helicopterParts[1].setTransparencyMode(Object3D.TRANSPARENCY_MODE_ADD);
		helicopterParts[1].setTransparency(0);
		propeller = Object3D.createDummyObj();
		propellerParts = Loader.loadOBJ(getClass().getClassLoader().getResourceAsStream("Models/propeller.obj"), getClass().getClassLoader().getResourceAsStream("Models/propeller.mtl"), scale);
		IntStream.range(0, propellerParts.length).forEach(x -> {
			// turn to the correct initial orientation:
			propellerParts[x].rotateZ((float) Math.PI);
			propellerParts[x].rotateMesh();
			propellerParts[x].setRotationMatrix(new Matrix());
			propellerParts[x].build();
			world.addObject(propellerParts[x]);
			propeller.addChild(propellerParts[x]);
		});
		propellerParts[1].setTransparencyMode(Object3D.TRANSPARENCY_MODE_ADD);
		propellerParts[1].setTransparency(0);
		helicopter.addChild(propeller);
		propeller.translate(new SimpleVector(0, -95.0f * scale, 0));
		exhaustPort = Object3D.createDummyObj();
		exhaustPort.translate(smokeOffset);
		helicopter.addChild(exhaustPort);
	}

	/**
	 * Creates a billboard quad with a z-offset (handles like a cube rather than
	 * a plane).
	 * @param width Quad width.
	 * @param zOffset Z-offset (toward the camera).
	 * @return Handle to the billboard quad.
	 */
	public static Object3D createQuad(float width, float zOffset, String texture) {
		float offset = width / 2.0f;
		Object3D obj = new Object3D(2);

		obj.addTriangle(new SimpleVector(-offset, -offset, 0), 0, 0, new SimpleVector(-offset, offset, 0), 0, 1, new SimpleVector(offset, offset, 0), 1, 1, TextureManager.getInstance().getTextureID(texture));
		obj.addTriangle(new SimpleVector(offset, offset, 0), 1, 1, new SimpleVector(offset, -offset, 0), 1, 0, new SimpleVector(-offset, -offset, 0), 0, 0, TextureManager.getInstance().getTextureID(texture));
		// Make it billboard:
		obj.setBillboarding(Object3D.BILLBOARDING_ENABLED);
		// Set up the transparency:
		obj.setTransparency(0);
		obj.setTransparencyMode(Object3D.TRANSPARENCY_MODE_ADD);
		obj.setLighting(Object3D.LIGHTING_NO_LIGHTS);
		obj.build();

		SimpleVector o = new SimpleVector(0, 0, -zOffset);
		Matrix m = obj.getTranslationMatrix();
		obj.setTranslationMatrix(new Matrix());
		obj.translate(o);
		obj.translateMesh();
		obj.setTranslationMatrix(m);

		return obj;
	}

	/**
	 * Generates a new shade of the specified color based on the intensity parameter.
	 * @param color Original color.
	 * @param intensity Shade.
	 * @return New color.
	 */
	private Color intensityColor(Color color, float intensity) {
		float r = color.getRed();
		float g = color.getGreen();
		float b = color.getBlue();

		r *= intensity;
		g *= intensity;
		b *= intensity;

		if (r > 255) r = 255;
		if (r < 0) r = 0;
		if (g > 255) g = 255;
		if (g < 0) g = 0;
		if (b > 255) b = 255;
		if (b < 0) b = 0;

		return new Color((int) r, (int) g, (int) b);
	}

	/**
	 * Sets up the scenes lighting.
	 */
	private void letThereBeLight() {
		world.getLights().setOverbrightLighting(Lights.OVERBRIGHT_LIGHTING_DISABLED);
		world.getLights().setRGBScale(Lights.RGB_SCALE_2X);

		// Set the overall brightness of the world:
		world.setAmbientLight(ambientLight.getRed(), ambientLight.getGreen(), ambientLight.getBlue());

		// Create the main light-source:
		sun = new Light(world);
		sun.setIntensity(sunIntensity.getRed(), sunIntensity.getGreen(), sunIntensity.getBlue());
		sun.setDiscardDistance(sunDiscardDistance);
		sun.setPosition(sunSatelliteOffset);
		sun.setAttenuation(sunAttenuation);
	}

	/**
	 * Uses the KeyMapper to poll the keyboard.
	 */
	private void pollKeyboard() {
		KeyState state;
		do {
			state = keyMapper.poll();
			if (state != KeyState.NONE) {
				keyAffected(state);
			}
		} while (state != KeyState.NONE);
	}

	/**
	 * Called when a key was pressed or released.
	 * @param state Information about which key was affected
	 */
	private void keyAffected(KeyState state) {
		int code = state.getKeyCode();
		boolean event = state.getState();

		switch (code) {
			case (KeyEvent.VK_ESCAPE): {
				alive = event;
				break;
			}
			case (KeyEvent.VK_UP): {
				upPressed = event;
				break;
			}
			case (KeyEvent.VK_DOWN): {
				downPressed = event;
				break;
			}
			case (KeyEvent.VK_LEFT): {
				leftPressed = event;
				break;
			}
			case (KeyEvent.VK_RIGHT): {
				rightPressed = event;
				break;
			}
			case (KeyEvent.VK_A): {
				if (!event) break;
			}
			case (KeyEvent.VK_C): {
				if (!event) break;
			}
		}
	}

	/**
	 * Takes a float and scales it to the specified range.
	 * @param f Float value 0.0f - 1.0f.
	 * @param minVal Minimum possible value.
	 * @param maxVal Maximum possible value.
	 * @return Scaled value.
	 */
	public float clamp(float f, float minVal, float maxVal) {
		return minVal + (f * (maxVal - minVal));
	}

	/**
	 * Generates a random float in the specified range.
	 * @param minVal Minimum possible value.
	 * @param maxVal Maximum possible value.
	 * @return Random float.
	 */
	public float getFloat(float minVal, float maxVal) {
		return clamp(generator.nextFloat(), minVal, maxVal);
	}

	/**
	 * Generates a random integer between 0 and the specified limit.
	 * @param maxVal Maximum possible value.
	 * @return Random int.
	 */
	public int getInt(int maxVal) {
		return (int) (generator.nextFloat() * (((float) maxVal) + 0.999f));
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}


	/**
	 * The SmokePuff class is a cool smoking special effect.
	 */
	private class SmokePuff {
		public Object3D     quad;
		public float        energy = 1.0f;
		public SimpleVector direction;

		// Smoke settings:
		private final Color smokeColor       = Color.WHITE;
		private final float energyPerMilli   = 0.0025f;
		private final float scalePerMilli    = 0.0125f;
		private final float distancePerMilli = 0.125f * helicopterScale;
		private       long  lastUpdateMillis;
		private       float scale            = 1.0f;
		private       int   frame            = 0;

		/**
		 * Constructor:  Produces a smoke puff.
		 * @param position Starting position.
		 * @param direction Direction of travel.
		 */
		public SmokePuff(SimpleVector position, SimpleVector direction) {
			this.direction = direction;
			quad = createQuad(25.0f * helicopterScale, 12.5f * helicopterScale, "smoke.png");
			quad.translate(position);
			world.addObject(quad);
			lastUpdateMillis = System.currentTimeMillis();
		}

		/**
		 * Updates the smoke-puff animation based on time.
		 * @param currentTimeMillis Current system time.
		 * @return False when the smoke puff runs out of energy.
		 */
		public boolean update(long currentTimeMillis) {
			// Check if there's any energy left:
			if (energy == 0) {
				// No energy, remove it from the world:
				quad.setVisibility(false);
				world.removeObject(quad);
				return false;
			}

			// Update energy, scale, and position based on elapsed time:
			float millisPast = (float) (currentTimeMillis - lastUpdateMillis);
			lastUpdateMillis = currentTimeMillis;
			float energyDrain = millisPast * energyPerMilli;
			float distance = millisPast * distancePerMilli;
			float scaleOffset = millisPast * scalePerMilli;
			energy -= energyDrain;
			if (energy <= 0) {
				frame++;
				if (frame == 1) {
					energy = 1;
					quad.setTexture("smokeFade.png");
				} else {
					energy = 0;
				}
			}
			quad.setAdditionalColor(intensityColor(smokeColor, energy));
			SimpleVector trans = new SimpleVector(direction).normalize();
			trans.scalarMul(distance);
			quad.translate(trans);
			scale += scaleOffset;
			quad.setScale(scale);

			return true;
		}
	}

	/**
	 * The Ball class is for the balls, of course.
	 */
	private class Ball {
		private final Object3D     ball;
		private final float        scale;
		private final float        bounceScale;
		private final float        pitch;
		private final float        maxHeight;
		private final float        distancePerMilli;
		private final SimpleVector direction;
		private       float        bounceAngle;
		private final float        bounceAnglePerMilli;
		private final float        groundOffset;

		/**
		 * Constructor:  Produces a random ball based on current settings.
		 */
		public Ball() {
			// Get a random size for the ball:
			scale = generator.nextFloat();

			// Translate that into an actual scale value:
			float modelScale = clamp(scale, minBallScale, maxBallScale);
			// Offset from the ground depends on the ball's scale:
			groundOffset = modelScale;
			// Smaller balls have a higher-pitched boing sound:
			pitch = clamp(1.0f - scale, minBoingPitch, maxBoingPitch);

			ball = Primitives.getSphere(5, modelScale);
			ball.build();
			world.addObject(ball);

			// Get a random bounce scale:
			bounceScale = generator.nextFloat();
			// Translate that into a bounce height
			maxHeight = clamp(bounceScale, minBounceHeight, maxBounceHeight);
			// Bounce speed depends on bounce height:
			bounceAnglePerMilli = clamp(-bounceScale, minBounceAnglePerMilli, maxBounceAnglePerMilli);
			// Determine a linear speed for the ball to travel:
			distancePerMilli = getFloat(minBallDistPerMilli, maxBallDistPerMilli);
			// Choose a color for the ball:
			int colorChoice = getInt(4);
			switch (colorChoice) {
				case 0:
					ball.setTexture("Red");
					break;
				case 1:
					ball.setTexture("Green");
					break;
				case 2:
					ball.setTexture("Blue");
					break;
				case 3:
					ball.setTexture("Yellow");
					break;
				default:
					ball.setTexture("Purple");
					break;
			}

			// Get a random horizontal starting position for the ball:
			SimpleVector position = new SimpleVector(getFloat(-500f, 500f), 0, getFloat(-500f, 500f));
			// Get a random horizontal direction to travel:
			direction = new SimpleVector(getFloat(-500f, 500f), 0, getFloat(-500f, 500f)).normalize();

			// Get a random starting angle:
			bounceAngle = getFloat(0.0f, (float) Math.PI * 2.0f);
			// Translate that into a height:
			float cos = (float) Math.cos(bounceAngle);
			float cosAbs = Math.abs(cos);

			// Move the ball to its starting position:
			ball.translate(new SimpleVector(position.x, (-cosAbs * maxHeight) + groundLevel - groundOffset, position.z));
		}

		/**
		 * Animates the ball based on how much time has passed.
		 * @param millisPast Milliseconds elapsed since last update.
		 */
		public void update(long millisPast) {
			// Update the bounce angle:
			float oldAngle = bounceAngle;
			bounceAngle += bounceAnglePerMilli * millisPast;
			float distance = distancePerMilli * millisPast;
			// Play the boing sound if the ball hit the ground:
			if (((oldAngle < (float) Math.PI / 2.0f) && (bounceAngle >= (float) Math.PI / 2.0f)) || ((oldAngle < (float) Math.PI + (float) Math.PI / 2.0f) && (bounceAngle >= (float) Math.PI + (float) Math.PI / 2.0f)))
				boing();
			// Keep angle in the range 0 - 2PI:
			while (bounceAngle >= (float) Math.PI * 2.0f) bounceAngle -= (float) Math.PI * 2.0f;
			// Get cosine of the bounce angle:
			float cos = (float) Math.cos(bounceAngle);
			float cosAbs = Math.abs(cos);

			// Update the horizontal position:
			SimpleVector trans = direction.normalize();
			trans.scalarMul(distance);
			trans.add(ball.getTransformedCenter());
			// Don't go past the edge of the world:
			if (trans.x < -5000) {
				trans.x = -5000;
				direction.x = Math.abs(direction.x);
				// Boing off the "wall":
				boing();
			} else if (trans.x > 5000) {
				trans.x = 5000;
				direction.x = -Math.abs(direction.x);
				// Boing off the "wall":
				boing();
			}
			if (trans.z < -5000) {
				trans.z = -5000;
				direction.z = Math.abs(direction.z);
				// Boing off the "wall":
				boing();
			} else if (trans.z > 5000) {
				trans.z = 5000;
				direction.z = -Math.abs(direction.z);
				// Boing off the "wall":
				boing();
			}

			// Combine horizontal position and bounce angle:
			trans.y = (-cosAbs * maxHeight) + groundLevel - groundOffset;

			// Update the ball's position:
			ball.translate(trans.calcSub(ball.getTransformedCenter()));
		}

		/**
		 * Play the "boing" sound effect.
		 */
		private void boing() {
			/* SoundSystem Stuff */
			SimpleVector position = ball.getTransformedCenter();
			soundSystem.setPitch(soundSystem.quickPlay(false, "boing.wav", false, position.x, -position.y, -position.z, SoundSystemConfig.ATTENUATION_ROLLOFF, boingAttenuation), pitch);
			/* End */
		}

	}
}
