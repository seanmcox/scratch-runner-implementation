/**
 * 
 */
package com.shtick.utils.scratch.runner.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.Time;
import javax.media.protocol.DataSource;
import javax.media.protocol.InputSourceStream;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import com.shtick.util.tokenizers.json.NumberToken;
import com.shtick.utils.data.json.JSONDecoder;
import com.shtick.utils.data.json.JSONNumberDecoder;
import com.shtick.utils.scratch.runner.core.Opcode;
import com.shtick.utils.scratch.runner.core.OpcodeHat;
import com.shtick.utils.scratch.runner.core.OpcodeUtils;
import com.shtick.utils.scratch.runner.core.ScratchRuntime;
import com.shtick.utils.scratch.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch.runner.core.Opcode.DataType;
import com.shtick.utils.scratch.runner.core.elements.BlockTuple;
import com.shtick.utils.scratch.runner.core.elements.RenderableChild;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.ScriptTuple;
import com.shtick.utils.scratch.runner.core.elements.Sprite;
import com.shtick.utils.scratch.runner.core.elements.Stage;
import com.shtick.utils.scratch.runner.core.elements.StageMonitor;
import com.shtick.utils.scratch.runner.core.elements.Tuple;
import com.shtick.utils.scratch.runner.impl.bundle.Activator;
import com.shtick.utils.scratch.runner.impl.elements.BlockTupleImplementation;
import com.shtick.utils.scratch.runner.impl.elements.CostumeImplementation;
import com.shtick.utils.scratch.runner.impl.elements.ListImplementation;
import com.shtick.utils.scratch.runner.impl.elements.ScriptTupleImplementation;
import com.shtick.utils.scratch.runner.impl.elements.SoundImplementation;
import com.shtick.utils.scratch.runner.impl.elements.SpriteImplementation;
import com.shtick.utils.scratch.runner.impl.elements.StageImplementation;
import com.shtick.utils.scratch.runner.impl.elements.StageMonitorImplementation;
import com.shtick.utils.scratch.runner.impl.elements.TupleImplementation;
import com.shtick.utils.scratch.runner.impl.elements.VariableImplementation;
import com.shtick.utils.scratch.runner.impl.ui.StagePanel;

/**
 * @author sean.cox
 *
 */
public class ScratchRuntimeImplementation implements ScratchRuntime {
	private static ScratchRuntimeImplementation RUNTIME;
	private static final ThreadTaskQueue TASK_QUEUE = new ThreadTaskQueue();
	private static String RESOURCE_PROJECT_FILE = "project.json";
	private StageImplementation stage;
	/**
	 * Map of named (non-clone) sprites.
	 */
	private TreeMap<String, Sprite> spritesByName;
	private LinkedList<StageMonitorImplementation> stageMonitors = new LinkedList<>();
	private int instructionDelayMillis;

	private JFrame mainWindow;
	private int stageWidth;
	private int stageHeight;
	
	// Mouse properties
	private final Object MOUSE_LOCK = new Object();
	private double mouseStageX;
	private double mouseStageY;
	private LinkedList<MouseListener> stageMouseListeners = new LinkedList<>();
	private LinkedList<MouseMotionListener> stageMouseMotionListeners = new LinkedList<>();
	
	private static boolean EXIT = false;
	private Stack<JPanel> frameStack = new Stack<>();
	private BubbleImage bubbleImage;
	private ScratchFile scratchFile;
	private HashSet<Clip> activeClips = new HashSet<>(10);
	private HashSet<Integer> pressedKeys = new HashSet<>(10);
	private boolean mouseDown = false;
	
	/**
	 * 
	 */
	public ScratchRuntimeImplementation() {
		super();
		if(RUNTIME!=null)
			throw new RuntimeException("Only one ScratchRuntime can be instantiated.");
		RUNTIME = this;
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
		
		if(args[0].equals("-h")) {
			help();
			System.exit(0);
			return;
		}
		if(args[0].equals("-v")) {
			System.exit(0);
			return;
		}

		start(5, new File(args[0]), 480, 360, 480, 360, false);
//		start(10, new File("/Users/sean.cox/Documents/Personal Workspace Oxygen/Scratch Runner/data/project.sb2"), 480, 360, 480, 360, false);
		synchronized(this){
			while(!EXIT){
				try{
					this.wait();
				}
				catch(InterruptedException t){
					t.printStackTrace();
				}
			}
		}
	}
	
	private static void help() {
		System.out.println("java -jar scratchrunner.jar <scratch file>");
		System.out.println("\tRun a scratch program.");
		System.out.println("java -jar scratchrunner.jar -h");
		System.out.println("\tPrint out this help information.");
		System.out.println("java -jar scratchrunner.jar -v");
		System.out.println("\tPrint out the version information only.");
	}
	
	/**
	 * @param instructionDelayMillis 
	 * @param projectFile 
	 * @param stageWidth 
	 * @param stageHeight 
	 * @param frameWidth 
	 * @param frameHeight 
	 * @param fullscreen 
	 * @throws IOException 
	 * 
	 */
	public void start(int instructionDelayMillis, File projectFile, int stageWidth, int stageHeight, int frameWidth, int frameHeight, boolean fullscreen) throws IOException{
		this.instructionDelayMillis = instructionDelayMillis;
		loadProject(projectFile);
		
		this.stageWidth = stageWidth;
		this.stageHeight = stageHeight;
		
		mainWindow=new JFrame(Info.NAME+" "+Info.VERSION);
		mainWindow.setUndecorated(true);
		if(fullscreen) {
			GraphicsDevice graphicsDevice=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			if(!graphicsDevice.isFullScreenSupported()){
				java.lang.System.out.println("GUI: Full screen is not supported.");
			}
			else {
				graphicsDevice.setFullScreenWindow(mainWindow);
			}
		}
		mainWindow.setSize(frameWidth, frameHeight);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setVisible(true);
		
		mainWindow.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public synchronized void mouseMoved(MouseEvent e) {
				final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
				SwingUtilities.invokeLater(()->{
					synchronized(stageMouseMotionListeners) {
						for(MouseMotionListener listener:stageMouseMotionListeners)
							listener.mouseMoved(stageEvent);
					}
				});
			}
			
			@Override
			public synchronized void mouseDragged(MouseEvent e) {
				final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
				SwingUtilities.invokeLater(()->{
					synchronized(stageMouseMotionListeners) {
						for(MouseMotionListener listener:stageMouseMotionListeners)
							listener.mouseDragged(stageEvent);
					}
				});
			}
			
			private MouseEvent getStageMouseEventAndProcess(MouseEvent e) {
				synchronized(MOUSE_LOCK) {
					mouseStageX = e.getX()*stageWidth/mainWindow.getWidth()-stageWidth/2;
					mouseStageY = -e.getY()*stageHeight/mainWindow.getHeight()+stageHeight/2;
	
					return new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), (int)mouseStageX, (int)mouseStageY, e.getClickCount(), e.isPopupTrigger());
				}
			}
		});
		
		mainWindow.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
				if(e.getButton()==MouseEvent.BUTTON1)
					mouseDown = false;
				synchronized(stageMouseListeners) {
					for(MouseListener listener:stageMouseListeners)
						listener.mouseReleased(stageEvent);
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
				if(e.getButton()==MouseEvent.BUTTON1)
					mouseDown = true;
				synchronized(stageMouseListeners) {
					for(MouseListener listener:stageMouseListeners)
						listener.mousePressed(stageEvent);
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
				synchronized(stageMouseListeners) {
					for(MouseListener listener:stageMouseListeners)
						listener.mouseExited(stageEvent);
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
				synchronized(stageMouseListeners) {
					for(MouseListener listener:stageMouseListeners)
						listener.mouseEntered(stageEvent);
				}
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
				if(e.getButton()!=MouseEvent.BUTTON1)
					return;
				
				synchronized(stageMouseListeners) {
					for(MouseListener listener:stageMouseListeners)
						listener.mouseClicked(stageEvent);
				}
			}
			
			private MouseEvent getStageMouseEventAndProcess(MouseEvent e) {
				synchronized(MOUSE_LOCK) {
					mouseStageX = e.getX()*stageWidth/mainWindow.getWidth()-stageWidth/2;
					mouseStageY = -e.getY()*stageHeight/mainWindow.getHeight()+stageHeight/2;
	
					return new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), (int)mouseStageX, (int)mouseStageY, e.getClickCount(), e.isPopupTrigger());
				}
			}
		});
		
		mainWindow.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				synchronized(pressedKeys) {
					pressedKeys.remove(getKey(e));
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				synchronized(pressedKeys) {
					pressedKeys.add(getKey(e));
				}
			}
			
			private int getKey(KeyEvent e) {
				if(((e.getKeyChar()>='a')&&(e.getKeyChar()<='z'))||((e.getKeyChar()>='A')&&(e.getKeyChar()<='Z'))||((e.getKeyChar()>='0')&&(e.getKeyChar()<='9')))
					return e.getKeyChar();
				return e.getKeyCode();
			}
		});

		openView(new StagePanel());
		
		greenFlagClicked();
	}
	
	/**
	 * 
	 * @return The instance of ScratchRuntime.
	 */
	public static ScratchRuntimeImplementation getScratchRuntime() {
		return RUNTIME;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#startScript(com.shtick.utils.scratch.runner.core.elements.ScriptTuple)
	 */
	@Override
	public ScriptTupleRunner startScript(ScriptTuple script, boolean isAtomic) {
		final ScriptTupleRunner[] retval = new ScriptTupleRunner[1];
		Runnable runnable = ()->{
			ScriptTupleRunnerThread thread = stage.createRunner((ScriptTupleImplementation)script, isAtomic);
			if(thread!=null)
				thread.start();
			retval[0] = thread.getScriptTupleRunner();
		};
		if(TASK_QUEUE.isQueueThread())
			runnable.run();
		else
			TASK_QUEUE.invokeAndWait(runnable);
		return retval[0];
	}

	private void greenFlagClicked() {
		Set<Opcode> opcodes = Activator.OPCODE_TRACKER.getOpcodes();
		for(Opcode opcode:opcodes) {
			if(!(opcode instanceof OpcodeHat))
				continue;
			OpcodeHat hat = (OpcodeHat)opcode;
			hat.applicationStarted(this);
		}
	}

	/**
	 * 
	 * @return A ThreadTaskQueue being executed by a Thread that has authority to manage the Scratch script Threads.
	 */
	public ThreadTaskQueue getThreadManagementTaskQueue() {
		return TASK_QUEUE;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeInterface#getCurrentStage()
	 */
	@Override
	public Stage getCurrentStage() {
		return stage;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeInterface#getScriptContextByName(java.lang.String)
	 */
	@Override
	public ScriptContext getScriptContextByName(String name) {
		Sprite sprite = getSpriteByName(name);
		if(sprite!=null)
			return sprite;
		if(name.equals(stage.getObjName()))
			return stage;
		return null;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeInterface#getSpriteByName(java.lang.String)
	 */
	@Override
	public Sprite getSpriteByName(String name) {
		return spritesByName.get(name);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeInterface#getAllRenderableChildren()
	 */
	@Override
	public RenderableChild[] getAllRenderableChildren() {
		return stage.getAllRenderableChildren();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#bringToFront(com.shtick.utils.scratch.runner.core.elements.Sprite)
	 */
	@Override
	public void bringToFront(Sprite sprite) {
		stage.bringToFront(sprite);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#sendToBack(com.shtick.utils.scratch.runner.core.elements.Sprite)
	 */
	@Override
	public void sendToBack(Sprite sprite) {
		stage.sendToBack(sprite);	
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#sendBackNLayers(com.shtick.utils.scratch.runner.core.elements.Sprite, int)
	 */
	@Override
	public void sendBackNLayers(Sprite sprite, int n) {
		stage.sendBackNLayers(sprite, n);
	}

	/**
	 * 
	 * @return An array of all the stage monitors.
	 */
	public StageMonitor[] getStageMonitors() {
		return stageMonitors.toArray(new StageMonitor[stageMonitors.size()]);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#setSpriteBubbleImage(com.shtick.utils.scratch.runner.core.elements.Sprite, java.awt.Image)
	 */
	@Override
	public void setSpriteBubbleImage(Sprite sprite, Image image) {
		if((sprite==null)||(image==null))
			bubbleImage=null;
		else
		bubbleImage = new BubbleImage(sprite, image);
	}
	
	/**
	 * 
	 * @return The current BubbleImage defined.
	 */
	public BubbleImage getBubbleImage() {
		return bubbleImage;
	}

	/**
	 * 
	 */
	@Override
	public void repaintStage() {
		Thread thread = Thread.currentThread();
		boolean repaint = true;
		if(thread instanceof ScriptTupleRunnerThread) {
			repaint = !((ScriptTupleRunnerThread)thread).isAtomic(); 
		}
		if(repaint)
			mainWindow.repaint();
		// TODO If atomic, then track whether repaint was requested and whether a repaint was attempted and aborted here.
		//      When the atomic script ends, then execute a repaint request.
	}

	/**
	 * @return the instructionDelayMillis
	 */
	public int getInstructionDelayMillis() {
		return instructionDelayMillis;
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
			EXIT=true;
			this.notify();
		}
		System.exit(0);
	}

	@Override
	public int getStageWidth() {
		return stageWidth;
	}

	@Override
	public int getStageHeight() {
		return stageHeight;
	}
	
	@Override
	public Point2D.Double getMouseStagePosition(){
		return new Point2D.Double(mouseStageX, mouseStageY);
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#isMouseDown()
	 */
	@Override
	public boolean isMouseDown() {
		return mouseDown;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void addKeyListener(KeyListener listener) {
		mainWindow.addKeyListener(listener);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void removeKeyListener(KeyListener listener) {
		mainWindow.removeKeyListener(listener);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addStageMouseListener(com.shtick.utils.scratch.runner.core.StageMouseListener)
	 */
	@Override
	public void addStageMouseListener(MouseListener listener) {
		synchronized(stageMouseListeners) {
			stageMouseListeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeStageMouseListener(com.shtick.utils.scratch.runner.core.StageMouseListener)
	 */
	@Override
	public void removeStageMouseListener(MouseListener listener) {
		synchronized(stageMouseListeners) {
			stageMouseListeners.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addStageMouseMotionListener(java.awt.event.MouseMotionListener)
	 */
	@Override
	public void addStageMouseMotionListener(MouseMotionListener listener) {
		synchronized(stageMouseMotionListeners) {
			stageMouseMotionListeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeStageMouseMotionListener(java.awt.event.MouseMotionListener)
	 */
	@Override
	public void removeStageMouseMotionListener(MouseMotionListener listener) {
		synchronized(stageMouseMotionListeners) {
			stageMouseMotionListeners.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#isKeyPressed(java.lang.String)
	 */
	@Override
	public boolean isKeyPressed(String keyID) {
		synchronized(pressedKeys) {
			int keyCode;
			switch(keyID) {
			case "any":
				return pressedKeys.size()>0;
			case "up arrow":
				keyCode = KeyEvent.VK_UP;
				break;
			case "down arrow":
				keyCode = KeyEvent.VK_DOWN;
				break;
			case "left arrow":
				keyCode = KeyEvent.VK_LEFT;
				break;
			case "right arrow":
				keyCode = KeyEvent.VK_RIGHT;
				break;
			case "space":
				keyCode = KeyEvent.VK_SPACE;
				break;
			case "enter":
				keyCode = KeyEvent.VK_ENTER;
				break;
			default:
				if(keyID.length()!=1)
					throw new IllegalArgumentException("Invalid key code: "+keyID);
				keyCode = keyID.charAt(0);
				break;
			}
			return pressedKeys.contains(keyCode);
		}
	}

	/**
	 * 
	 * @param resourceName
	 * @param volume 
	 * @return The Clip
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 * @throws LineUnavailableException
	 */
	public Clip playSound(String resourceName, double volume) throws IOException, UnsupportedAudioFileException, LineUnavailableException{
		    AudioInputStream stream;
		    AudioFormat format;
		    DataLine.Info info;
		    Clip clip;

		    InputStream in = new BufferedInputStream(scratchFile.getResource(resourceName));
		    try {
		    		stream = AudioSystem.getAudioInputStream(in);
		    }
		    catch(UnsupportedAudioFileException t) {
		    		in.close();
//		    		InputSourceStream iss = new InputSourceStream(new BufferedInputStream(scratchFile.getResource(resourceName)), null);
//		    		DataSource ds=new PullDataSource() {
//						
//						@Override
//						public void stop() throws IOException {
//							// TODO Auto-generated method stub
//							
//						}
//						
//						@Override
//						public void start() throws IOException {
//							// TODO Auto-generated method stub
//							
//						}
//						
//						@Override
//						public Time getDuration() {
//							// TODO Auto-generated method stub
//							return null;
//						}
//						
//						@Override
//						public Object[] getControls() {
//							// TODO Auto-generated method stub
//							return null;
//						}
//						
//						@Override
//						public Object getControl(String arg0) {
//							// TODO Auto-generated method stub
//							return null;
//						}
//						
//						@Override
//						public String getContentType() {
//							// TODO Auto-generated method stub
//							return null;
//						}
//						
//						@Override
//						public void disconnect() {
//							iss.close();
//						}
//						
//						@Override
//						public void connect() throws IOException {}
//						
//						@Override
//						public PullSourceStream[] getStreams() {
//							return new PullSourceStream[] {iss};
//						}
//					};
//		    		Player m_Player = Manager.createPlayer(ds);
//		    		m_Player.start();
//		    		m_Player.getControl(arg0)
		    		System.err.println(resourceName);
		    		t.printStackTrace();
		    		return null;
		    }
		    
		    format = stream.getFormat();
		    info = new DataLine.Info(Clip.class, format);
		    synchronized(activeClips) {
			    clip = (Clip) AudioSystem.getLine(info);
			    clip.addLineListener(new LineListener() {
					@Override
					public void update(LineEvent event) {
						if((event.getType()==LineEvent.Type.STOP)||(event.getType()==LineEvent.Type.CLOSE)) {
							synchronized(activeClips) {
								activeClips.remove(clip);
							}
						}
					}
				});
			    clip.open(stream);
			    if(clip.isControlSupported(FloatControl.Type.VOLUME)) {
				    FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.VOLUME);
				    if(control!=null)
				    		control.setValue(control.getMinimum()+(float)volume*(control.getMaximum()-control.getMinimum())/100);
			    }
			    else if(clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
				    FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
				    if(control!=null)
			    		control.setValue(Math.min(control.getMinimum()-(float)volume*control.getMinimum()/100,control.getMaximum()));
			    }
			    else {
			    		System.err.println("Neither VOLUME nor MASTER_GAIN controls supported for clip; "+resourceName);
			    }
				TASK_QUEUE.invokeLater(()->{
				    clip.start();
				});
			    activeClips.add(clip);
		    }
		    return clip;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#stopAllSounds()
	 */
	@Override
	public void stopAllSounds() {
	    synchronized(activeClips) {
	    		for(Clip clip:activeClips) {
	    			clip.stop();
	    			clip.close();
	    		}
	    		activeClips.clear();
	    }
	}

	/**
	 * 
	 * @return A Graphics2D object that will let you paint on the background using a coordinate system similar to scratch's, but with the y-axis reversed. (ie. centered on the middle, with x increasing to the right and y increasing to the bottom. The width and the height are the stage width and height.)
	 */
	public Graphics2D getPenLayerGraphics() {
		JPanel panel = frameStack.peek();
		if(panel instanceof StagePanel)
			return ((StagePanel)panel).getPenLayerGraphics();
		return null;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#clearPenLayer()
	 */
	@Override
	public void clearPenLayer() {
		JPanel panel = frameStack.peek();
		if(panel instanceof StagePanel) {
			((StagePanel)panel).clearPenLayer();
			repaintStage();
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addComponent(java.awt.Component, int, int, int, int)
	 */
	@Override
	public void addComponent(Component component, int x, int y, int width, int height) {
		JPanel panel = frameStack.peek();
		if(panel instanceof StagePanel)
			((StagePanel)panel).addComponent(component, x, y, width, height);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeComponent(java.awt.Component)
	 */
	@Override
	public void removeComponent(Component component) {
		JPanel panel = frameStack.peek();
		if(panel instanceof StagePanel)
			((StagePanel)panel).removeComponent(component);
	}
	
	/**
	 * 
	 * @param sprite
	 */
	public void addClone(Sprite sprite) {
		if(!sprite.isClone())
			throw new IllegalStateException();
		stage.addChild(sprite);
	}
	
	/**
	 * 
	 * @param sprite Removes the sprite from the list of sprites, but only if it is a clone.
	 */
	public void deleteClone(Sprite sprite) {
		if(!sprite.isClone())
			throw new IllegalStateException();
		stage.removeChild(sprite);
	}

	private void loadProject(File file) throws IOException{
		spritesByName = new TreeMap<>();
		
		scratchFile = new ScratchFile(file);
		Map<String,Object> jsonMap;
		try(InputStream in = scratchFile.getResource(RESOURCE_PROJECT_FILE);){
			jsonMap = JSONDecoder.decode(in, new JSONNumberDecoder() {
			
				@Override
				public Object decodeNumber(NumberToken token) {
					if((token.getFractionalPart()==null)&&(token.getExponentialPart()==null))
						return new Long((token.isNegative()?"-":"")+token.getWholePart().getText());
					return Double.valueOf(token.toString());
				}
			});
		}
		stage = parseStage(jsonMap, scratchFile);
	}
	
	private StageImplementation parseStage(Map<String,Object> jsonMap, ScratchFile scratchFile) throws IOException{
		VariableImplementation[] variables = null;
		if(jsonMap.get("variables")!=null) {
			Object variablesJson = jsonMap.get("variables");
			if(!(variablesJson instanceof java.util.List<?>))
				throw new IOException("variables not encoded in list");
			java.util.List<Object> variablesList = (java.util.List<Object>)variablesJson;
			variables = new VariableImplementation[variablesList.size()];
			int i=0;
			for(Object variableObject:variablesList) {
				if(!(variableObject instanceof Map<?,?>))
					throw new IOException("variable not encoded as map");
				String name = (String)((Map<String,Object>)variableObject).get("name");
				String value = (String)((Map<String,Object>)variableObject).get("name");
				variables[i] = new VariableImplementation(name, value);
				i++;
			}
		}
		
		HashMap<String,ListImplementation> listsByName = null;
		if(jsonMap.get("lists")!=null) {
			Object listsJson = jsonMap.get("lists");
			if(!(listsJson instanceof java.util.List<?>))
				throw new IOException("lists not encoded in list");
			java.util.List<Object> listsList = (java.util.List<Object>)listsJson;
			listsByName = new HashMap<>(listsList.size());
			for(Object object:listsList)
				parseList(object,listsByName);
		}
		else {
			listsByName = new HashMap<>();
		}
		
		SoundImplementation[] sounds = null;
		if(jsonMap.get("sounds")!=null) {
			Object json = jsonMap.get("sounds");
			if(!(json instanceof java.util.List<?>))
				throw new IOException("sounds not encoded in list");
			java.util.List<Object> list = (java.util.List<Object>)json;
			sounds = new SoundImplementation[list.size()];
			int i=0;
			for(Object object:list) {
				sounds[i] = parseSound(object);
				i++;
			}
		}
		
		CostumeImplementation[] costumes = null;
		if(jsonMap.get("sounds")!=null) {
			Object json = jsonMap.get("costumes");
			if(!(json instanceof java.util.List<?>))
				throw new IOException("costumes not encoded in list");
			java.util.List<Object> list = (java.util.List<Object>)json;
			costumes = new CostumeImplementation[list.size()];
			int i=0;
			for(Object object:list) {
				costumes[i] = parseCostume(object, scratchFile);
				i++;
			}
		}
		
		Double videoAlpha = (jsonMap.get("videoAlpha") instanceof Double)?(Double)jsonMap.get("videoAlpha"):new Double(((Integer)jsonMap.get("videoAlpha")).doubleValue());

		StageImplementation retval =  new StageImplementation(
				(String)jsonMap.get("objName"),
				variables,
				listsByName,
				sounds,
				costumes,
				((Long)jsonMap.get("currentCostumeIndex")).intValue(),
				(Long)jsonMap.get("penLayerID"),
				(String)jsonMap.get("penLayerMD5"),
				(Long)jsonMap.get("tempoBPM"),
				videoAlpha,
				(Map<String,Object>)jsonMap.get("info"));
		
		if(jsonMap.get("scripts")!=null) {
			Object scriptsJson = jsonMap.get("scripts");
			if(!(scriptsJson instanceof java.util.List<?>))
				throw new IOException("scripts not encoded in list");
			java.util.List<Object> scriptsList = (java.util.List<Object>)scriptsJson;
			for(Object scriptObject:scriptsList)
				retval.addScript(parseScriptTuple(retval, scriptObject));
		}
		
		if(jsonMap.get("children")!=null) {
			Object json = jsonMap.get("children");
			if(!(json instanceof java.util.List<?>))
				throw new IOException("children not encoded in list");
			java.util.List<Object> list = (java.util.List<Object>)json;
			for(Object object:list)
				retval.addChild(parseStageChild(retval, object, scratchFile, listsByName));
		}
		
		return retval;
	}
	
	private ScriptTupleImplementation parseScriptTuple(ScriptContext context, Object scriptTupleObject) throws IOException{
		if(!(scriptTupleObject instanceof java.util.List<?>))
			throw new IOException("scriptTuple not encoded as list.");
		java.util.List<Object> scriptTupleList = (java.util.List<Object>)scriptTupleObject;
		if(scriptTupleList.size()!=3)
			throw new IOException("scriptTuple array not of length 3.");
		if(!(scriptTupleList.get(2) instanceof java.util.List<?>))
			throw new IOException("scriptTuple list of block tuples not found.");
		java.util.List<Object> blockTuplesList = (java.util.List<Object>)scriptTupleList.get(2);
		BlockTuple[] blockTuples = new BlockTuple[blockTuplesList.size()];
		int i = 0;
		for(Object blockTupleObject:blockTuplesList) {
			Tuple tuple = parseTuple(blockTupleObject); 
			if(!(tuple instanceof BlockTuple))
				throw new IOException("BlockTuple required but not found as ScriptTuple element.");
			blockTuples[i] = (BlockTuple)tuple;
			i++;
		}
		ScriptTupleImplementation retval = new ScriptTupleImplementation(context, blockTuples);
		if(blockTuples.length>0) {
			BlockTuple maybeHat = blockTuples[0];
			String opcode = maybeHat.getOpcode();
			java.util.List<Object> arguments = maybeHat.getArguments();
			Opcode opcodeImplementation = Activator.OPCODE_TRACKER.getOpcode(opcode);
			Object[] executableArguments = new Object[arguments.size()];
			if((opcodeImplementation != null)&&(opcodeImplementation instanceof OpcodeHat)) {
				DataType[] types = opcodeImplementation.getArgumentTypes();
				if(types.length!= arguments.size())
					throw new IOException("Invalid arguments found for opcode, "+opcode);
				for(i=0;i<arguments.size();i++) {
					switch(types[i]) {
					case BOOLEAN:
						executableArguments[i] = arguments.get(i);
						if(!(executableArguments[i] instanceof Boolean))
							throw new IOException("Non-tuple provided where tuple expected.");
						break;
					case NUMBER:
						executableArguments[i] = OpcodeUtils.getNumericValue(arguments.get(i));
						break;
					case OBJECT:
						executableArguments[i] = arguments.get(i);
						if(!((executableArguments[i] instanceof Boolean)||(executableArguments[i] instanceof Number)||(executableArguments[i] instanceof String)))
							throw new IOException("Non-object provided where object expected.");
						break;
					case STRING:
						executableArguments[i] = OpcodeUtils.getStringValue(arguments.get(i));
						break;
					case TUPLE:
						if(!(arguments.get(i) instanceof Tuple))
							throw new IOException("Non-tuple provided where tuple expected.");
						executableArguments[i] = ((Tuple)arguments.get(i)).toArray();
						break;
					default:
						throw new RuntimeException("Unhandled DataType, "+types[i].name()+", in method signature for opcode, "+opcode);
					}
				}
				((OpcodeHat)opcodeImplementation).registerListeningScript(retval, executableArguments);
			}
			else {
				// TODO Why bother remembering the script?
			}
		}
		return retval;
	}
	
	private static Tuple parseTuple(Object blockTupleObject) throws IOException{
		if(!(blockTupleObject instanceof java.util.List<?>))
			throw new IOException("block tuple not encoded as list.");
		java.util.List<Object> blockTupleList = (java.util.List<Object>)blockTupleObject;
		if(blockTupleList.size()==0)
			return new TupleImplementation(new Object[0]);
		String opcode = null;
		int offset=0;
		Object[] args;
		if(blockTupleList.get(0)instanceof String) {
			if(!(blockTupleList.get(0) instanceof String))
				throw new IOException("Block tuple opcode not defined.");
			opcode = (String)blockTupleList.get(0);
			args = new Object[blockTupleList.size()-1];
			offset++;
		}
		else {
			args = new Object[blockTupleList.size()];
		}
		for(int i=offset;i<blockTupleList.size();i++) {
			args[i-offset] = blockTupleList.get(i);
			if(args[i-offset] instanceof java.util.List<?>) {
				// Figure out if this is a sub-script or another block tuple.
				java.util.List<Object> arg = (java.util.List<Object>)args[i-1];
				if((arg.size()>0)&&(arg.get(0) instanceof java.util.List<?>)) {
					ArrayList<BlockTuple> script = new ArrayList<>(arg.size());
					for(int j=0;j<arg.size();j++) {
						Tuple tuple = parseTuple(arg.get(j)); 
						if(!(tuple instanceof BlockTuple))
							throw new IOException("BlockTuple required but not found as ScriptTuple element.");
						script.add((BlockTuple)tuple);
					}
					args[i-offset] = script;
				}
				else {
					args[i-offset] = parseTuple(arg);
				}
			}
		}
		
		if(opcode!=null) {
			ArrayList<Object> listArgs = new ArrayList<>(args.length);
			for(Object arg:args)
				listArgs.add(arg);
			return new BlockTupleImplementation(opcode, listArgs);
		}
		return new TupleImplementation(args);
	}
	
	private static SoundImplementation parseSound(Object object) throws IOException{
		if(!(object instanceof java.util.Map<?,?>))
			throw new IOException("sound not encoded as map.");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;
		return new SoundImplementation((String)map.get("soundName"), (Long)map.get("soundID"), (String)map.get("md5"), (Long)map.get("sampleCount"), (Long)map.get("rate"), (String)map.get("format"));
	}
	
	private static CostumeImplementation parseCostume(Object object, ScratchFile scratchFile) throws IOException{
		if(!(object instanceof java.util.Map<?,?>))
			throw new IOException("costume not encoded as map.");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;
		
		// Get the image for this costume.
		String md5 = (String)map.get("baseLayerMD5");
		Long baseLayerID = (Long)map.get("baseLayerID");
		String[] parts = md5.split("\\.",2);
		if(parts.length<2)
			throw new IOException("Invalid baseLayerMD5: "+md5);
		String resourceName = baseLayerID.toString()+"."+parts[1];
		BufferedImage image;
		if("svg".equals(parts[1])) {
			try(InputStream in = scratchFile.getResource(resourceName)){
			    String parser = XMLResourceDescriptor.getXMLParserClassName();
			    SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			    SVGDocument doc = (SVGDocument)f.createDocument(resourceName, in);

			    { // Handle the SVG spec vs. Scratch implementation discrepancy re: multiline text.
					NodeList textNodes = doc.getElementsByTagName("text");
					for(int i=0;i<textNodes.getLength();i++) {
						Node textNode = textNodes.item(i);
						String[] lines = textNode.getTextContent().split("\\n");
						if(lines.length==0)
							continue;
						textNode.setTextContent("");
						int l=0;
						int j=0;
						for(String line:lines) {
							j++;
							l++;
							if(line.length()==0)
								continue;
							line = line.replace(" ", "\u00A0");
							if(j>1) {
								Element lineElement = doc.createElementNS("http://www.w3.org/2000/svg", "tspan");
								lineElement.setAttribute("dy", l+"em");
								lineElement.setAttribute("x", ((Element)textNode).getAttribute("x"));
								lineElement.setTextContent(line);
								textNode.appendChild(lineElement);
							}
							else {
								textNode.setTextContent(line);
							}
							l=0;
						}
					}
			    }

				NodeList svgNodes = doc.getElementsByTagName("svg");
				if(svgNodes.getLength()==0)
					throw new IOException("No svg node found");
				NamedNodeMap attributes = svgNodes.item(0).getAttributes();
				Node node = attributes.getNamedItem("viewBox");
				if(node!=null) { // Handle Scratch's annoying tendency to define a viewBox that truncates the image (yet it still displays the portions of the defined image outside the viewBox)
					String[] oldViewBoxParts = node.getNodeValue().split(" ");
					Rectangle2D oldViewBoxRectangle = new Rectangle2D.Double(
							Double.parseDouble(oldViewBoxParts[0]),
							Double.parseDouble(oldViewBoxParts[1]),
							Double.parseDouble(oldViewBoxParts[2]),
							Double.parseDouble(oldViewBoxParts[3]));

					GVTBuilder builder = new GVTBuilder();
					BridgeContext ctx;
					ctx = new BridgeContext(new UserAgentAdapter());
					GraphicsNode gvtRoot = builder.build(ctx, doc);
					Rectangle2D rect = gvtRoot.getSensitiveBounds();
					if(rect==null)
						rect = oldViewBoxRectangle;
					else
						Rectangle2D.union(oldViewBoxRectangle, rect, rect);
					oldViewBoxParts[0] = ""+rect.getX();
					oldViewBoxParts[1] = ""+rect.getY();
					oldViewBoxParts[2] = ""+(rect.getWidth());
					oldViewBoxParts[3] = ""+(rect.getHeight());

					String viewBox = "";
					for(String part:oldViewBoxParts)
						viewBox+=" "+part;
					node.setNodeValue(viewBox.trim());
					attributes.getNamedItem("viewBox").setNodeValue(viewBox);
					node = attributes.getNamedItem("x");
					if(node!=null)
						node.setNodeValue(oldViewBoxParts[0]+"px");
					node = attributes.getNamedItem("y");
					if(node!=null)
						node.setNodeValue(oldViewBoxParts[1]+"px");
					node = attributes.getNamedItem("width");
					if(node!=null)
						node.setNodeValue(oldViewBoxParts[2]+"px");
					node = attributes.getNamedItem("height");
					if(node!=null)
						node.setNodeValue(oldViewBoxParts[3]+"px");
				}
				node = attributes.getNamedItem("enable-background");
				if(node!=null)
					attributes.removeNamedItem("enable-background");

				TranscoderInput svgIn = new TranscoderInput(doc);
				ByteArrayOutputStream pngBytesOut = new ByteArrayOutputStream();
				TranscoderOutput pngOut = new TranscoderOutput(pngBytesOut);
				PNGTranscoder transcoder = new PNGTranscoder();
				try {
					transcoder.transcode(svgIn, pngOut);
				}
				catch(TranscoderException t) {
					throw new RuntimeException(t);
				}
				ByteArrayInputStream pngIn = new ByteArrayInputStream(pngBytesOut.toByteArray());
				ImageInputStream iis = ImageIO.createImageInputStream(pngIn);
		        Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
				if(!iter.hasNext())
					throw new IOException("Reader not found for transcoded png originating from: "+resourceName);
			    ImageReader reader = iter.next();
			    reader.setInput(iis);
			    image = reader.read(0);
			}
		}
		else {
			try(InputStream in = scratchFile.getResource(resourceName)){
				ImageInputStream iis = ImageIO.createImageInputStream(in);
		        Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
				if(!iter.hasNext())
					throw new IOException("Reader not found for specified image: "+resourceName);
			    ImageReader reader = iter.next();
			    reader.setInput(iis);
			    image = reader.read(0);
			}
		}
		
		return new CostumeImplementation((String)map.get("costumeName"), baseLayerID, md5, ((Long)map.get("bitmapResolution")).intValue(), ((Long)map.get("rotationCenterX")).intValue(), ((Long)map.get("rotationCenterY")).intValue(), image);
	}

	/**
	 * 
	 * @param node
	 * @return A stringification of the Node.
	 */
	private static String nodeToString(Node node) {
	    StringWriter sw = new StringWriter();
	    try {
	    	Transformer t = TransformerFactory.newInstance().newTransformer();
	    	t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    	t.setOutputProperty(OutputKeys.INDENT, "yes");
	    	t.transform(new DOMSource(node), new StreamResult(sw));
	    }
	    catch (TransformerException te) {
	    	System.out.println("nodeToString Transformer Exception");
	    }
	    return sw.toString();
	}
	
	  /**
	   * 
	   * @param object
	   * @param listConsolidation A list of already known lists to consolidated with redundantly defined lists.
	   * @return The ListImplementation.
	   *         If a list with the same name is already defined in listConsolidation, then the value in listConsolidation is used.
	   *         Otherwise, the newly parsed ListImplementation is added to listConsolidation.
	   * @throws IOException
	   */
	private static ListImplementation parseList(Object object, HashMap<String,ListImplementation> listConsolidation) throws IOException{
		if(!(object instanceof Map<?,?>))
			throw new IOException("list not encoded as map");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;
		String name = (String)map.get("listName");
		if(listConsolidation.containsKey(name))
			return listConsolidation.get(name);
		java.util.List<Object> value = (java.util.List<Object>)map.get("contents");
		Double x = (map.get("x") instanceof Double)?(Double)map.get("x"):new Double(((Long)map.get("x")).doubleValue());
		Double y = (map.get("y") instanceof Double)?(Double)map.get("y"):new Double(((Long)map.get("y")).doubleValue());
		Double width = (map.get("width") instanceof Double)?(Double)map.get("width"):new Double(((Long)map.get("width")).doubleValue());
		Double height = (map.get("height") instanceof Double)?(Double)map.get("height"):new Double(((Long)map.get("height")).doubleValue());
		Boolean visible = (Boolean)map.get("visible");
		ListImplementation retval = new ListImplementation(
				name, value.toArray(),
				x, y, width, height, visible);
		listConsolidation.put(name,retval);
		return retval;
	}
	
	private StageMonitorImplementation parseStageMonitor(Object object) throws IOException{
		if(!(object instanceof Map<?,?>))
			throw new IOException("Stage monitor not encoded as map");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;
		try {
			StageMonitorImplementation retval = new StageMonitorImplementation((String)map.get("target"), (String)map.get("cmd"), (String)map.get("param"), ((Long)map.get("color")).intValue(), (String)map.get("label"), ((Long)map.get("mode")).intValue(), ((Long)map.get("sliderMin")).intValue(), ((Long)map.get("sliderMax")).intValue(), (Boolean)map.get("isDiscrete"), ((Number)map.get("x")).intValue(), ((Number)map.get("y")).intValue(), (Boolean)map.get("visible"));
			stageMonitors.add(retval);
			return retval;
		}
		catch(Throwable t) {
			System.err.println("target: "+map.get("target"));
			System.err.println("cmd: "+map.get("cmd"));
			System.err.println("param: "+map.get("param"));
			System.err.println("color: "+map.get("color"));
			System.err.println("label: "+map.get("label"));
			System.err.flush();
			throw t;
		}
	}
	
	private Sprite parseSprite(Stage stage, Object spriteObject, ScratchFile scratchFile, HashMap<String,ListImplementation> listConsolidation) throws IOException{
		if(!(spriteObject instanceof Map<?,?>))
			throw new IOException("Sprite not encoded as map");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)spriteObject;
		
		VariableImplementation[] variables = null;
		if(map.get("variables")!=null) {
			Object variablesJson = map.get("variables");
			if(!(variablesJson instanceof java.util.List<?>))
				throw new IOException("variables not encoded in list");
			java.util.List<Object> variablesList = (java.util.List<Object>)variablesJson;
			variables = new VariableImplementation[variablesList.size()];
			int i=0;
			for(Object variableObject:variablesList) {
				if(!(variableObject instanceof Map<?,?>))
					throw new IOException("variable not encoded as map");
				String name = (String)((Map<String,Object>)variableObject).get("name");
				String value = (String)((Map<String,Object>)variableObject).get("name");
				variables[i] = new VariableImplementation(name, value);
				i++;
			}
		}

		
		HashMap<String,ListImplementation> listsByName = null;
		if(map.get("lists")!=null) {
			Object listsJson = map.get("lists");
			if(!(listsJson instanceof java.util.List<?>))
				throw new IOException("lists not encoded in list");
			java.util.List<Object> listsList = (java.util.List<Object>)listsJson;
			listsByName = new HashMap<>(listsList.size());
			int i=0;
			for(Object object:listsList) {
				ListImplementation list = parseList(object,listConsolidation);
				listsByName.put(list.getListName(), list);
				i++;
			}
		}
				
		SoundImplementation[] sounds = null;
		if(map.get("sounds")!=null) {
			Object json = map.get("sounds");
			if(!(json instanceof java.util.List<?>))
				throw new IOException("sounds not encoded in list");
			java.util.List<Object> list = (java.util.List<Object>)json;
			sounds = new SoundImplementation[list.size()];
			int i=0;
			for(Object object:list) {
				sounds[i] = parseSound(object);
				i++;
			}
		}
		
		CostumeImplementation[] costumes = null;
		if(map.get("sounds")!=null) {
			Object json = map.get("costumes");
			if(!(json instanceof java.util.List<?>))
				throw new IOException("costumes not encoded in list");
			java.util.List<Object> list = (java.util.List<Object>)json;
			costumes = new CostumeImplementation[list.size()];
			int i=0;
			for(Object object:list) {
				costumes[i] = parseCostume(object, scratchFile);
				i++;
			}
		}
		
		Double scratchX = (map.get("scratchX") instanceof Double)?(Double)map.get("scratchX"):new Double(((Long)map.get("scratchX")).doubleValue());
		Double scratchY = (map.get("scratchY") instanceof Double)?(Double)map.get("scratchY"):new Double(((Long)map.get("scratchY")).doubleValue());
		Double scale = (map.get("scale") instanceof Double)?(Double)map.get("scale"):new Double(((Long)map.get("scale")).doubleValue());
		Double direction = (map.get("direction") instanceof Double)?(Double)map.get("direction"):new Double(((Long)map.get("direction")).doubleValue());
		
		SpriteImplementation retval = new SpriteImplementation(
				(String)map.get("objName"),
				variables, listsByName, sounds, costumes,
				((Long)map.get("currentCostumeIndex")).intValue(), scratchX, scratchY, scale, direction,
				(String)map.get("rotationStyle"), (Boolean)map.get("isDraggable"),
				((Long)map.get("indexInLibrary")).intValue(), (Boolean)map.get("visible"),
				(Map<String,Object>)map.get("spriteInfo"), stage);
		spritesByName.put(retval.getObjName(),retval);

		if(map.get("scripts")!=null) {
			Object scriptsJson = map.get("scripts");
			if(!(scriptsJson instanceof java.util.List<?>))
				throw new IOException("scripts not encoded in list");
			java.util.List<Object> scriptsList = (java.util.List<Object>)scriptsJson;
			for(Object scriptObject:scriptsList)
				retval.addScript(parseScriptTuple(retval,scriptObject));
		}
		
		return retval;
	}
	
	private RenderableChild parseStageChild(Stage stage, Object object, ScratchFile scratchFile, HashMap<String,ListImplementation> listConsolidation) throws IOException{
		if(!(object instanceof java.util.Map<?,?>))
			throw new IOException("Stage child not encoded as map.");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;
		
		if(map.containsKey("objName")) { // sprite
			return parseSprite(stage, object, scratchFile, listConsolidation);
		}
		if(map.containsKey("target")) { // stage monitor
			return parseStageMonitor(object);
		}
		if(map.containsKey("listName")) { // list
			return parseList(object, listConsolidation);
		}
		throw new IOException("Unexpected element found as stage child.");
	}
}
