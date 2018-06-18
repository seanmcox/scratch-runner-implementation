/**
 * 
 */
package com.shtick.utils.scratch.imager.structure;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.shtick.utils.scratch.ScratchFile;
import com.shtick.utils.scratch.imager.Driver;
import com.shtick.utils.scratch.imager.ui.MainFrame;

/**
 * @author scox
 *
 */
public class MainDriver extends Driver{
	private MainFrame mainFrame=null;

	/**
	 * @param file 
	 * 
	 */
	public MainDriver() {
		this(null);
	}
	
	/**
	 * @param file 
	 * 
	 */
	public MainDriver(String file) {
		if(file!=null) {
			try {
				mainFrame = new MainFrame(new ScratchFile(new File(file)));
			}
			catch(IOException t) {
				t.printStackTrace();
				System.exit(1);
			}
		}
		else {
			mainFrame = new MainFrame();
		}
		
		mainFrame.setVisible(true);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.imager.Driver#exit()
	 */
	@Override
	public void exit() {
		System.exit(0);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.imager.Driver#closeProject()
	 */
	@Override
	public void closeProject() {
		mainFrame.openFile(null);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.imager.Driver#openProject()
	 */
	@Override
	public void openProject() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "ZIP and SB2 files";
			}
			
			@Override
			public boolean accept(File f) {
				if(f.isDirectory())
					return true;
				return f.getName().toLowerCase().endsWith(".zip")||f.getName().toLowerCase().endsWith(".sb2");
			}
		});
		int result = fileChooser.showOpenDialog(mainFrame);
		if(result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			try {
				mainFrame.openFile(new ScratchFile(selectedFile));
			}
			catch(IOException t) {
				t.printStackTrace();
				JOptionPane.showMessageDialog(mainFrame, t.getMessage(), "Error Opening File", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
