/**
 * 
 */
package com.shtick.utils.scratch.imager;

/**
 * @author scox
 *
 */
public abstract class Driver {
	private static Driver DRIVER;

	/**
	 * 
	 */
	public Driver() {
		super();
		if(DRIVER!=null)
			throw new RuntimeException("Driver already created.");
		DRIVER = this;
	}
	
	/**
	 * 
	 * @return The instance of Driver for this application.
	 */
	public static Driver getDriver() {
		return DRIVER;
	}

	/**
	 * 
	 */
	public abstract void exit();

	/**
	 * 
	 */
	public abstract void closeProject();

	/**
	 * 
	 */
	public abstract void openProject();
}
