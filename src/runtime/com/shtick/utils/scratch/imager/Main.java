/**
 * 
 */
package com.shtick.utils.scratch.imager;

import java.io.File;
import java.io.IOException;

import com.shtick.utils.scratch.ScratchFile;
import com.shtick.utils.scratch.imager.ui.MainFrame;

/**
 * @author scox
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length==0)
			System.exit(1);
		MainFrame mainFrame=null;
		try {
			mainFrame = new MainFrame(new ScratchFile(new File(args[0])));
		}
		catch(IOException t) {
			t.printStackTrace();
			System.exit(1);
		}
		
		mainFrame.setVisible(true);

	}

}
