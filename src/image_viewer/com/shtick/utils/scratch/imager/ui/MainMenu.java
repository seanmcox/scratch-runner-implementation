/**
 * 
 */
package com.shtick.utils.scratch.imager.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.shtick.utils.scratch.imager.Driver;

/**
 * @author scox
 *
 */
public class MainMenu extends JMenuBar {

	/**
	 * 
	 */
	public MainMenu() {
		super();
		
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open Project");
		openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Driver.getDriver().openProject();
			}
		});
		JMenuItem closeItem = new JMenuItem("Close Project");
		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Driver.getDriver().closeProject();
			}
		});
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Driver.getDriver().exit();
			}
		});
		
		fileMenu.add(openItem);
		fileMenu.add(closeItem);
		fileMenu.add(exitItem);
		
		this.add(fileMenu);
	}

}
