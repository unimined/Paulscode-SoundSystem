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

import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

public class Target {
	private final World world;

	private long distanceMoved = 250;  // how far target has moved (start out in the center)
	private       long crashTime;  // when target last collided with a bullet
	private final long flashTime = 250;  // how long to stay red

	private boolean crashed = false;  // true when collision with a bullet happens

	public Object3D     object3D;
	public SimpleVector direction;  // direction target is traveling
	public float        distance;  // how far to move target each frame

	public Target(World world, Object3D object3D, SimpleVector direction, float distance) {
		this.world = world;
		this.object3D = object3D;
		this.direction = direction;
		this.distance = distance;
	}

	public void destroy() {
		synchronized (BulletTargetCollision.jpctLock) {
			world.removeObject(object3D);
		}
	}

	public void move(long currentTime) {
		// color target blue if it has been red long enough:
		if (crashed && (currentTime - crashTime >= flashTime)) {
			crashed = false;
			synchronized (BulletTargetCollision.jpctLock) {
				object3D.setTexture("Blue");
			}
		}

		// move the target:
		SimpleVector translation = new SimpleVector(direction);
		translation.scalarMul(distance);
		synchronized (BulletTargetCollision.jpctLock) {
			object3D.translate(translation);
		}

		// if we moved far enough, reverse direction:
		distanceMoved += (long) distance;
		if (distanceMoved >= 500) {
			direction.scalarMul(-1);
			distanceMoved = 0;
		}

	}

	// collided with a bullet:
	public void crash() {
		// color the target red:
		synchronized (BulletTargetCollision.jpctLock) {
			object3D.setTexture("Red");
		}
		// remember when the collision occurred:
		crashTime = System.currentTimeMillis();
		crashed = true;
	}
}
