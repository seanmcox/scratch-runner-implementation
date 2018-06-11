/**
 * 
 */
package com.shtick.utils.scratch.runner;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.shtick.utils.scratch.runner.Info;
import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;
import com.shtick.utils.scratch.runner.impl.elements.StageImplementation;
import com.shtick.utils.scratch.runner.impl.ui.StagePanel;

/**
 * @author sean.cox
 *
 */
public class Main implements com.shtick.utils.scratch.runner.core.Main{
	private static ScratchRuntimeImplementation RUNTIME;
	private static String RESOURCE_PROJECT_FILE = "project.json";
	private StageImplementation stage;
	private Stack<JPanel> frameStack = new Stack<>();

	private JFrame mainWindow;
	private int stageWidth;
	private int stageHeight;
	
	/**
	 * 
	 */
	public Main() {
		super();
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public void main(String[] args) throws IOException{
		if(args.length==0) {
			System.err.println("No arguments provided.");
			help();
			System.exit(1);
			return;
		}
		
		System.out.println(Info.NAME+" "+Info.VERSION);
		if(args[0].equals("-h")) {
			help();
			System.exit(0);
			return;
		}
		if(args[0].equals("-v")) {
			System.exit(0);
			return;
		}
		String file=null;
		int stageWidth=480;
		int stageHeight=360;
		int frameWidth=-1;
		int frameHeight=-1;
		for(int i=0;i<args.length;i++) {
			switch(args[i]) {
				case "-f":{
					i++;
					if(i>=args.length) {
						System.out.println("No file provided after -f");
						help();
						System.exit(1);
					}
					file = args[i];
					break;
				}
				case "-ws":{
					i++;
					if(i>=args.length) {
						System.err.println("No value provided after -ws");
						help();
						System.exit(1);
					}
					try {
						stageWidth = Integer.parseInt(args[i]);
					}
					catch(NumberFormatException t) {
						System.err.println("Invalid number for -ws");
						help();
						System.exit(1);
					}
					if(stageWidth<0) {
						System.err.println("Only positive value accepted for -ws");
						help();
						System.exit(1);
					}
					break;
				}
				case "-hs":{
					i++;
					if(i>=args.length) {
						System.err.println("No value provided after -hs");
						help();
						System.exit(1);
					}
					try {
						stageHeight = Integer.parseInt(args[i]);
					}
					catch(NumberFormatException t) {
						System.err.println("Invalid number for -hs");
						help();
						System.exit(1);
					}
					if(stageHeight<0) {
						System.err.println("Only positive value accepted for -hs");
						help();
						System.exit(1);
					}
					break;
				}
				case "-wf":{
					i++;
					if(i>=args.length) {
						System.err.println("No value provided after -wf");
						help();
						System.exit(1);
					}
					try {
						frameWidth = Integer.parseInt(args[i]);
					}
					catch(NumberFormatException t) {
						System.err.println("Invalid number for -wf");
						help();
						System.exit(1);
					}
					if(frameWidth<0) {
						System.err.println("Only positive value accepted for -wf");
						help();
						System.exit(1);
					}
					break;
				}
				case "-hf":{
					i++;
					if(i>=args.length) {
						System.err.println("No value provided after -hf");
						help();
						System.exit(1);
					}
					try {
						frameHeight = Integer.parseInt(args[i]);
					}
					catch(NumberFormatException t) {
						System.err.println("Invalid number for -hf");
						help();
						System.exit(1);
					}
					if(frameHeight<0) {
						System.err.println("Only positive value accepted for -hf");
						help();
						System.exit(1);
					}
					break;
				}
				default:
					System.err.println("Unrecognized parameter: "+args[i]);
					help();
					System.exit(1);
			}
		}
		
		if(file==null) {
			System.out.println("Project file not specified.");
			help();
			System.exit(1);
		}
		
		// TODO Implement the fullscreen option. (It's a little more complicated to ensure that the program can exit properly.)
		// TODO Implement an option that shows and uses the green flag and stop sign.
		start(new File(file), stageWidth, stageHeight, (frameWidth<0)?stageWidth:frameWidth, (frameHeight<0)?stageHeight:frameHeight, false);
		synchronized(RUNTIME){
			while(!RUNTIME.isStopped()){
				try{
					RUNTIME.wait();
				}
				catch(InterruptedException t){
					t.printStackTrace();
				}
			}
		}
	}
	
	private static void help() {
		System.out.println("java -jar scratchrunner.jar -f <file> [-wf <pixels>] [-hf <pixels>] [-ws <pixels>] [-hs <pixels>]");
		System.out.println("\tRun a scratch program.");
		System.out.println("java -jar scratchrunner.jar -h");
		System.out.println("\tPrint out this help information.");
		System.out.println("java -jar scratchrunner.jar -v");
		System.out.println("\tPrint out the version information only.");
		System.out.println("");
		System.out.println("Parameters");
		System.out.println("\t-f:  Scratch project file to run. (Required)");
		System.out.println("\t-wf: Frame/window width. (default = stage width)");
		System.out.println("\t-hf: Frame/window height. (default = stage height)");
		System.out.println("\t-ws: Stage width. (default = 480)");
		System.out.println("\t-hs: Stage height. (default = 360)");
		System.out.println("\t     The stage will always fill up the frame.");
		System.out.println("\t     These parameters only affect the internal reckoning of the coordinates.");
	}
	
	/**
	 * @param projectFile 
	 * @param stageWidth The width of the stage in stage units. The standard value is 480. In combination with the frame width, this value determines how stage positions translate to pixel positions on the horizontal axis.
	 * @param stageHeight The height of the stage in stage units. The standard value is 360. In combination with the frame height, this value determines how stage positions translate to pixel positions on the vertical axis.
	 * @param frameWidth The width of the stage in pixels.
	 * @param frameHeight The height of the stage in pixels.
	 * @param fullscreen 
	 * @throws IOException 
	 * 
	 */
	public void start(File projectFile, int stageWidth, int stageHeight, int frameWidth, int frameHeight, boolean fullscreen) throws IOException{
		this.stageWidth = stageWidth;
		this.stageHeight = stageHeight;
		
		mainWindow=new JFrame(Info.NAME+" "+Info.VERSION);

		if(RUNTIME!=null)
			throw new RuntimeException("Only one ScratchRuntime can be instantiated.");
		RUNTIME = new ScratchRuntimeImplementation(projectFile, stageWidth, stageHeight);

		mainWindow.setUndecorated(true);
		if(fullscreen) {
			GraphicsDevice graphicsDevice=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			if(!graphicsDevice.isFullScreenSupported()){
				System.err.println("GUI: Full screen is not supported.");
			}
			else {
				graphicsDevice.setFullScreenWindow(mainWindow);
			}
		}
		mainWindow.setSize(frameWidth, frameHeight);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setVisible(true);
		
		openView(RUNTIME.getStagePanel());
		RUNTIME.start();
	}
	
	/**
	 * 
	 * @return The instance of ScratchRuntime.
	 */
	public static ScratchRuntimeImplementation getScratchRuntime() {
		return RUNTIME;
	}	

	/**
	 * 
	 * @param app
	 */
	private void closeView(JPanel app){
		Runnable runnable=new Runnable() {
			@Override
			public void run() {
				synchronized(frameStack){
					boolean showApp=false;
					if(frameStack.size()>0){
						if(frameStack.peek()==app){
							mainWindow.getContentPane().remove(app);
							showApp=true;
							frameStack.pop();
						}
						else{
							frameStack.remove(app);
						}
					}
					if(showApp){
						if(frameStack.size()==0)
							;// Show the main menu
						mainWindow.getContentPane().add(frameStack.peek(),BorderLayout.CENTER);
						mainWindow.invalidate();
						mainWindow.validate();
						mainWindow.repaint();
					}
				}
			}
		};
		SwingUtilities.invokeLater(runnable);
	}
	
	/**
	 * 
	 * @param app
	 */
	private void openView(JPanel app){
		Runnable runnable=new Runnable() {
			@Override
			public void run() {
				synchronized(frameStack){
					if((frameStack.size()==0)||(frameStack.peek()!=app)){
						if((frameStack.size()>0)&&(frameStack.peek()!=null))
							mainWindow.getContentPane().remove(frameStack.peek());
						frameStack.remove(app);
						frameStack.push(app);
						mainWindow.getContentPane().add(frameStack.peek(),BorderLayout.CENTER);
						mainWindow.invalidate();
						mainWindow.validate();
						mainWindow.repaint();
					}
				}
			}
		};
		if(SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		}
		else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			}
			catch(InvocationTargetException|InterruptedException t) {
				throw new RuntimeException(t);
			}
		}
	}
	
	/**
	 * Exits the application.
	 */
	public void exit(){
		synchronized(this){
			if(RUNTIME.isRunning()) {
				RUNTIME.stop();;
			}
			this.notify();
		}
		System.exit(0);
	}
}
