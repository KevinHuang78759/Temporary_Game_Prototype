/*
 * CollisionController.java
 *
 * This controller implements basic collision detection as described in
 * the instructions.  All objects in this game are treated as circles,
 * and a collision happens when circles intersect.
 *
 * This controller is EXTREMELY ineffecient.  To improve its performance,
 * you will need to use collision cells, as described in the instructions.
 * You should not need to modify any method other than the constructor
 * and processCollisions.  However, you will need to add your own methods.
 *
 * This is the only file that you need to modify as the first part of
 * the lab. 
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.optimize;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.math.*;
import edu.cornell.gdiac.optimize.entity.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Controller implementing simple game physics.
 *
 * This is a very inefficient physics engine.  Part of this lab is determining
 * how to make it more efficient.
 */
public class CollisionController {
	// Geometry of the background image
	/** (Scaled) distance of the floor ledge from bottom */
	protected static final float BOTTOM_OFFSET    = 0.075f;
	
	// These cannot be modified after the controller is constructed.
	// If these change, make a new constructor.
	/** Width of the collision geometry */
	private float width;
	/** Height of the collision geometry */
	private float height;

	/// ACCESSORS
	/**
	 * Returns width of the game window (necessary to detect out of bounds)
	 *
	 * @return width of the game window
	 */
	public float getWidth() {
		return width;
	}
	
	/**
	 * Returns height of the game window (necessary to detect out of bounds)
	 *
	 * @return height of the game window
	 */
	public float getHeight() {
		return height;
	}
	
	/**
	 * Returns the height of the floor ledge.
	 *
	 * The floor ledge supports the player ship, and is what all of the shells
	 * bounce off of.  It is raised slightly higher than the bottom of the screen.
	 *
	 * @return the height of the floor ledge.
	 */
	public float getFloorLedge() {
		return BOTTOM_OFFSET*height;
	}

	/**
	 * Creates a CollisionController for the given screen dimensions.
	 *
	 * @param width   Width of the screen 
	 * @param height  Height of the screen
	 */
	public CollisionController(float width, float height) {
		this.width = width;
		this.height = height;
	}

}