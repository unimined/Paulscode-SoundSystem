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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JApplet;

// From the jPCT API, https://www.jpct.net:
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.IRenderer;
import com.threed.jpct.Lights;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryJavaSound;

public class BulletTargetCollision extends JApplet implements MouseListener, MouseMotionListener, Runnable {
	private Object3D ship;
	private Object3D wing1;
	private Object3D laserDummy1;
	private Object3D targetCone;

	private       FrameBuffer buffer = null;
	private       World       world  = null;
	private       Camera      camera = null;
	private final int         width  = 640;
	private final int         height = 480;
	private       int         prevMouseX, prevMouseY;
	private boolean alive = true;

	private int hits  = 0;
	private int shots = 0;

	private List<Bullet> bullets;

	private Target target;

	private SoundSystem soundSystem;

	private boolean initialized = false;

	public static final Object jpctLock = new Object();

	// Initialize all components of the applet
	@Override
	public void init() {
		try {
			SoundSystemConfig.addLibrary(LibraryJavaSound.class);
			SoundSystemConfig.setCodec("wav", CodecWav.class);
		} catch (SoundSystemException e) {
			System.out.println("Error loading libraries or codecs");
		}
		// initialize the sound system:
		soundSystem = new SoundSystem();

		// create some texture colors:
		TextureManager.getInstance().addTexture("Red", new Texture(2, 2, new Color(255, 0, 0)));
		TextureManager.getInstance().addTexture("Green", new Texture(2, 2, new Color(0, 255, 0)));
		TextureManager.getInstance().addTexture("Blue", new Texture(2, 2, new Color(100, 100, 255)));


		// create the bullet list:
		bullets = new LinkedList<>();

		synchronized (jpctLock) {
			world = new World();  // create a new world
			World.setDefaultThread(Thread.currentThread());

			// Set jPCT up for software mode:
			buffer = new FrameBuffer(width, height, FrameBuffer.SAMPLINGMODE_NORMAL);
			buffer.enableRenderer(IRenderer.RENDERER_SOFTWARE);

			// create the ship object:
			createShip();
			// create the target object:
			createTarget();

			world.buildAllObjects();

			lookAt(ship);  // make sure the camera is facing towards the ship
			letThereBeLight();  // create light sources for the scene
		}

		// receive mouse input from the main applet:
		addMouseListener(this);
		addMouseMotionListener(this);

		initialized = true;
		new Thread(this).start();
	}

	// Draw the scene
	@Override
	public void paint(Graphics g) {
		if (!initialized) return;

		synchronized (jpctLock) {
			buffer.clear();   // erase the previous frame

			// render the world onto the buffer:
			world.renderScene(buffer);
			world.draw(buffer);
			buffer.update();

			Graphics g2 = buffer.getGraphics();
			g2.setColor(Color.WHITE);
			g2.drawString("Shots Fired: " + shots, 4, 16);
			g2.drawString("Hits: " + hits, 4, 32);

			buffer.display(g, 0, 0);  // Paint this frame onto the applet
		}
	}

	@Override
	public void destroy() {
		alive = false;
	}

	@Override
	public void run() {
		while (alive) {
			moveBullets();  // move all bullets
			moveTarget();   // move the target
			this.repaint();
			try {
				Thread.sleep(10);
			} catch (Exception ignored) {
			}
		}
	}

	public synchronized void fireBullet() {
		SimpleVector pos;
		Object3D laser;
		SimpleVector direction;

		synchronized (jpctLock) {
			// starting position for the bullet:
			pos = laserDummy1.getTransformedCenter();
			// create the bullet model:
			laser = Primitives.getBox(1, 4);
			laser.rotateY((float) Math.PI / 4);
			laser.rotateX((float) Math.PI / 2);
			laser.rotateMesh();
			laser.setRotationMatrix(new Matrix());
			laser.build();
			laser.translate(laserDummy1.getTransformedCenter());
			align(laser, wing1);
			laser.setTexture("Red");

			// direction for the bullet to travel:
			direction = targetCone.getTransformedCenter().calcSub(pos).normalize();

			// add the new objects to the world:
			world.addObject(laser);

			// create a new bullet and add it to the list:
			bullets.add(new Bullet(world, laser, direction));
		}

		shots++;
		// play a shooting sound:
		soundSystem.quickPlay(false, "laser.wav", false, pos.x, pos.y, pos.z, SoundSystemConfig.ATTENUATION_ROLLOFF, 0.03f);
	}

	public synchronized void moveBullets() {
		Bullet bullet;
		ListIterator<Bullet> i = bullets.listIterator();

		// get the current time
		long currentTime = System.currentTimeMillis();

		// loop through the bullet list
		while (i.hasNext()) {
			bullet = i.next();

			if (bullet.crashed) {
				hits++;
				// play the collision sound:
				soundSystem.quickPlay(false, "strike.wav", false, bullet.crashPosition.x, bullet.crashPosition.y, bullet.crashPosition.z, SoundSystemConfig.ATTENUATION_ROLLOFF, 0.03f);
				// light up the target:
				target.crash();
				// remove the bullet from the world:
				bullet.remove();
				// remove the bullet from the list:
				i.remove();
			} else if (bullet.expired) {
				// remove the bullet from the world:
				bullet.remove();
				// remove the bullet from the list:
				i.remove();
			} else {
				// move the bullet:
				bullet.move(currentTime);
			}
		}
	}

	public synchronized void moveTarget() {
		// loop through the bullets to see if they will collide with the target:
		bullets.forEach(bullet -> bullet.targetMove(target.direction, target.distance));

		// move the target:
		target.move(System.currentTimeMillis());
	}

	// create a "ship" of sorts
	private void createShip() {
		ship = Primitives.getBox(10, 1);
		ship.rotateY((float) Math.PI / 4);
		ship.rotateMesh();
		ship.setRotationMatrix(new Matrix());
		ship.setOrigin(new SimpleVector(0, 0, 0));
		ship.setTexture("Blue");
		wing1 = Primitives.getBox(2, 4);
		wing1.rotateY((float) Math.PI / 4);
		wing1.rotateX((float) Math.PI / 2);
		wing1.rotateMesh();
		wing1.setRotationMatrix(new Matrix());
		wing1.setOrigin(new SimpleVector(-12, 0, 10));
		wing1.setTexture("Green");

		// create visible dummyLaser:
		laserDummy1 = Primitives.getBox(1, 4);
		laserDummy1.rotateY((float) Math.PI / 4);
		laserDummy1.rotateX((float) Math.PI / 2);
		laserDummy1.rotateMesh();
		laserDummy1.setRotationMatrix(new Matrix());
		laserDummy1.setOrigin(new SimpleVector(-12, 0, 25));
		laserDummy1.setTexture("Green");

		// create targetCone:
		targetCone = Object3D.createDummyObj();
		targetCone.setOrigin(new SimpleVector(0, 0, 50));

		// build all visible objects:
		ship.build();
		wing1.build();
		laserDummy1.build();
		targetCone.build();

		// Set up parent/child relationships for the ship:
		ship.addChild(wing1);
		ship.addChild(targetCone);
		wing1.addChild(laserDummy1);

		// add all visible objects to the world:
		world.addObject(ship);
		world.addObject(wing1);
		world.addObject(laserDummy1);
		world.addObject(targetCone);

		// rotate the gun so it looks like it is pointing at targetCone:
		wing1.rotateY((float) Math.PI / 8);
	}

	// create the target object3D
	public void createTarget() {
		Object3D obj = Primitives.getBox(20, 1);
		obj.setOrigin(new SimpleVector(0, 0, 200));
		obj.build();
		obj.setTexture("Blue");

		world.addObject(obj);

		obj.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);

		target = new Target(world, obj, new SimpleVector(1, 0, 0), 2);
	}

	// point the camera toward the given object
	private void lookAt(Object3D obj) {
		camera = world.getCamera();  // grab a handle to the camera
		camera.setPosition(-100, -50, -200);  // set the camera position
		camera.lookAt(obj.getTransformedCenter());  // look toward the object
	}

	// create light sources for the scene
	private void letThereBeLight() {
		world.getLights().setOverbrightLighting(Lights.OVERBRIGHT_LIGHTING_DISABLED);
		world.getLights().setRGBScale(Lights.RGB_SCALE_2X);

		// Set the overall brightness of the world:
		world.setAmbientLight(50, 50, 50);

		// Create a main light-source:
		world.addLight(new SimpleVector(50, -50, -300), 20, 20, 20);
	}

	// align objects, taking parents into account
	private void align(Object3D from, Object3D to) {
		float scale = from.getScale();
		from.setScale(1);
		Matrix matrix = to.getWorldTransformation();
		float[] dm = matrix.getDump();
		for (int i = 12; i < 15; i++) {
			dm[i] = 0;
		}
		dm[15] = 1;
		matrix.setDump(dm);
		from.setRotationMatrix(matrix);
		from.setScale(scale);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	// Dragging the mouse should rotate the ship
	public void mouseDragged(MouseEvent e) {
		// get the mouse's coordinates:
		int x = e.getX();
		int y = e.getY();
		Dimension size = e.getComponent().getSize();

		// if the left mouse button is down, rotate the ship:
		if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
			// Calculate the angles to rotate the ship:
			float thetaY = (float) Math.PI * ((float) (x - prevMouseX) / (float) size.width);
			float thetaX = (float) Math.PI * ((float) (prevMouseY - y) / (float) size.height);
			// Apply the rotations to the ship:
			ship.rotateAxis(ship.getXAxis(), thetaX);
			ship.rotateAxis(ship.getYAxis(), thetaY);
		}

		// Keep track of the mouse location:
		prevMouseX = x;
		prevMouseY = y;
	}

	public void mousePressed(MouseEvent e) {
		// Start keeping track of the mouse location now.
		// This prevents the ship from jerking to the new angle
		// whenever mouseDragged first gets called:
		prevMouseX = e.getX();
		prevMouseY = e.getY();

		// if the right mouse button was clicked, place a laser and laserTarget:
		if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK) {
			fireBullet();
		}
	}

}
