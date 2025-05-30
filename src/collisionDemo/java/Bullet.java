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

public class Bullet {
	private final World        world;
	private final Object3D     object3D;
	private final SimpleVector direction;  // direction the bullet is traveling

	private final long creationTime;  // when the bullet was created
	private final long lifeSpan = 20000;  // how many milliseconds the bullet can last

	private       long  lastMoveTime;  // time the bullet last moved
	private final float distancePerMilli = 0.5f;  // how far the bullet should move each millisecond

	public boolean      expired       = false;  // true when the bullet lifeSpan has run out
	public boolean      crashed       = false;  // true when the bullet collides with the target
	public SimpleVector crashPosition = null;  // position where the collision occurred

	public Bullet(World world, Object3D object3D, SimpleVector direction) {
		creationTime = System.currentTimeMillis();
		lastMoveTime = creationTime;

		this.world = world;
		this.object3D = object3D;
		this.direction = direction;
	}

	public void remove() {
		synchronized (BulletTargetCollision.jpctLock) {
			world.removeObject(object3D);
		}
	}

	public void move(long currentTime) {
		// if bullet expired or crashed, do nothing
		if (expired || crashed) return;

		// check if bullet should expire
		if (currentTime - creationTime >= lifeSpan) {
			expired = true;
			return;
		}

		// calculate how far to move the bullet
		float distance = (currentTime - lastMoveTime) * distancePerMilli;

		// save the current time
		lastMoveTime = currentTime;

		// check if the bullet should expire
		if (lastMoveTime - creationTime >= lifeSpan) {
			expired = true;
			return;
		}

		SimpleVector position;
		float targetDistance;

		synchronized (BulletTargetCollision.jpctLock) {
			// check if the movement will result in a collision:
			position = object3D.getTransformedCenter();
			targetDistance = world.calcMinDistance(position, direction, 100000);
		}

		// See if the bullet is going to collide with something:
		if ((targetDistance != Object3D.COLLISION_NONE) && (targetDistance <= distance)) {
			// collision occurred, move bullet to the collision point:
			SimpleVector smallTranslation = new SimpleVector(direction);
			smallTranslation.scalarMul(targetDistance);
			synchronized (BulletTargetCollision.jpctLock) {
				object3D.translate(smallTranslation);
			}

			crash();

			return;
		}

		// move bullet the full distance:
		SimpleVector translation = new SimpleVector(direction);
		translation.scalarMul(distance);
		synchronized (BulletTargetCollision.jpctLock) {
			object3D.translate(translation);
		}
	}

	// check if a target movement would result in a bullet collision
	public boolean targetMove(SimpleVector targetDirection, float sDistance) {
		SimpleVector position;
		SimpleVector inverseDirection;
		float targetDistance;

		synchronized (BulletTargetCollision.jpctLock) {
			position = object3D.getTransformedCenter();
			inverseDirection = new SimpleVector(targetDirection);
			inverseDirection.scalarMul(-1.0f);
			targetDistance = world.calcMinDistance(position, inverseDirection, 100000);
		}

		// See if the bullet is going to collide with something:
		if ((targetDistance != Object3D.COLLISION_NONE) && (targetDistance <= sDistance)) {
			crash(); // collide the bullet
			return true; // true, collision will occur
		}

		return false; // false, no collision will happen
	}

	// bullet collided with a target:
	public void crash() {
		synchronized (BulletTargetCollision.jpctLock) {
			crashPosition = object3D.getTransformedCenter();
		}
		crashed = true;
	}
}
