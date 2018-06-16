package com.shtick.utils.scratch.runner.impl;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Time;
import javax.media.protocol.DataSource;
import javax.media.protocol.InputSourceStream;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import com.shtick.util.tokenizers.json.NumberToken;
import com.shtick.utils.data.json.JSONDecoder;
import com.shtick.utils.data.json.JSONNumberDecoder;
import com.shtick.utils.scratch.ScratchFile;
import com.shtick.utils.scratch.ScratchImageRenderer;
import com.shtick.utils.scratch.runner.core.GraphicEffectRegistry;
import com.shtick.utils.scratch.runner.core.InvalidScriptDefinitionException;
import com.shtick.utils.scratch.runner.core.Opcode;
import com.shtick.utils.scratch.runner.core.OpcodeHat;
import com.shtick.utils.scratch.runner.core.OpcodeRegistry;
import com.shtick.utils.scratch.runner.core.OpcodeUtils;
import com.shtick.utils.scratch.runner.core.ScratchRuntime;
import com.shtick.utils.scratch.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch.runner.core.SoundMonitor;
import com.shtick.utils.scratch.runner.core.StageMonitorCommandRegistry;
import com.shtick.utils.scratch.runner.core.Opcode.DataType;
import com.shtick.utils.scratch.runner.core.elements.BlockTuple;
import com.shtick.utils.scratch.runner.core.elements.RenderableChild;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.ScriptTuple;
import com.shtick.utils.scratch.runner.core.elements.Sprite;
import com.shtick.utils.scratch.runner.core.elements.Stage;
import com.shtick.utils.scratch.runner.core.elements.StageMonitor;
import com.shtick.utils.scratch.runner.core.elements.Tuple;
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
	private static final String RESOURCE_PROJECT_FILE = "project.json";
	private final ThreadTaskQueue TASK_QUEUE = new ThreadTaskQueue();
	private final ScriptTupleThread SCRIPT_TUPLE_THREAD = new ScriptTupleThread(this);
	private StageImplementation stage;
	private OpcodeRegistry opcodeRegistry;
	private GraphicEffectRegistry graphicEffectRegistry;
	private StageMonitorCommandRegistry stageMonitorCommandRegistry;
	
	/**
	 * Map of named (non-clone) sprites.
	 */
	private TreeMap<String, Sprite> spritesByName;
	private LinkedList<StageMonitorImplementation> stageMonitors = new LinkedList<>();

	private int stageWidth;
	private int stageHeight;
	private boolean repaintNeeded = true;
	
	// Mouse properties
	private final Object MOUSE_LOCK = new Object();
	private double mouseStageX;
	private double mouseStageY;
	private LinkedList<MouseListener> stageMouseListeners = new LinkedList<>();
	private LinkedList<MouseMotionListener> stageMouseMotionListeners = new LinkedList<>();
	
	private boolean running = false;
	private boolean stopped = false;
	private BubbleImage bubbleImage;
	private ScratchFile scratchFile;
	private HashSet<SoundMonitor> activeSoundMonitors = new HashSet<>(10);
	private HashSet<Integer> pressedKeys = new HashSet<>(10);
	private boolean mouseDown = false;
	private StagePanel stagePanel;
	
	private MouseMotionListener primaryStageMouseMotionListener = new MouseMotionListener() {
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
				mouseStageX = e.getX()*stageWidth/stagePanel.getWidth()-stageWidth/2;
				mouseStageY = -e.getY()*stageHeight/stagePanel.getHeight()+stageHeight/2;

				return new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), (int)mouseStageX, (int)mouseStageY, e.getClickCount(), e.isPopupTrigger());
			}
		}
	};
	
	private MouseListener primaryStageMouseListener = new MouseListener() {
		
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
			stagePanel.grabFocus();
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
			stagePanel.grabFocus();
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
			stagePanel.grabFocus();
		}
		
		private MouseEvent getStageMouseEventAndProcess(MouseEvent e) {
			synchronized(MOUSE_LOCK) {
				mouseStageX = e.getX()*stageWidth/stagePanel.getWidth()-stageWidth/2;
				mouseStageY = -e.getY()*stageHeight/stagePanel.getHeight()+stageHeight/2;

				return new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), (int)mouseStageX, (int)mouseStageY, e.getClickCount(), e.isPopupTrigger());
			}
		}
	};
	private KeyListener primaryStageKeyListener = new KeyListener() {
		
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
	};
	
	/**
	 * @param projectFile 
	 * @param stageWidth 
	 * @param stageHeight 
	 * @param opcodeRegistry 
	 * @param graphicEffectRegistry 
	 * @param stageMonitorCommandRegistry 
	 * @throws IOException 
	 * 
	 */
	public ScratchRuntimeImplementation(File projectFile, int stageWidth, int stageHeight, OpcodeRegistry opcodeRegistry, GraphicEffectRegistry graphicEffectRegistry, StageMonitorCommandRegistry stageMonitorCommandRegistry) throws IOException{
		this.stageWidth = stageWidth;
		this.stageHeight = stageHeight;
		this.opcodeRegistry = opcodeRegistry;
		this.graphicEffectRegistry = graphicEffectRegistry;
		this.stageMonitorCommandRegistry = stageMonitorCommandRegistry;
		loadProject(projectFile);
		
		stagePanel = new StagePanel(this);
	}
	
	/**
	 * 
	 */
	public void start(){
		if(running)
			return;
		running = true;
		System.out.println("Adding listeneres.");
		stagePanel.addMouseMotionListener(primaryStageMouseMotionListener);
		stagePanel.addMouseListener(primaryStageMouseListener);
		stagePanel.addKeyListener(primaryStageKeyListener);

		greenFlagClicked();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#startScript(com.shtick.utils.scratch.runner.core.elements.ScriptTuple)
	 */
	@Override
	public ScriptTupleRunner startScript(ScriptTuple script) {
		return SCRIPT_TUPLE_THREAD.startScriptTuple((ScriptTupleImplementation)script);
	}

	private void greenFlagClicked() {
		Set<Opcode> opcodes = opcodeRegistry.getOpcodes();
		for(Opcode opcode:opcodes) {
			if(!(opcode instanceof OpcodeHat))
				continue;
			OpcodeHat hat = (OpcodeHat)opcode;
			hat.applicationStarted(this);
		}
		SCRIPT_TUPLE_THREAD.start();
	}

	/**
	 * 
	 * @return A ThreadTaskQueue being executed by a Thread that has authority to manage the Scratch script Threads.
	 */
	public ThreadTaskQueue getThreadManagementTaskQueue() {
		return TASK_QUEUE;
	}
	
	/**
	 * 
	 * @return Not really a thread, but an object that manages the thread that runs the scripts.
	 */
	public ScriptTupleThread getScriptTupleThread() {
		return SCRIPT_TUPLE_THREAD;
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
		synchronized(stagePanel) {
			repaintNeeded = true;
		}
	}
	
	/**
	 * Called by the ScriptTupleThread.
	 * If repaint was requested, then this triggers the repaint to occur.
	 * 
	 * @return true if repaint called, and false otherwise.
	 */
	protected boolean repaintStageFinal() {
		if((!repaintNeeded)||(JOptionPane.getFrameForComponent(stagePanel)==JOptionPane.getRootFrame()))
			return false;
		synchronized(stagePanel) {
			try {
				SwingUtilities.invokeAndWait(()->{
					stagePanel.paint(stagePanel.getGraphics());
				});
			}
			catch(InvocationTargetException t) {
				t.printStackTrace();
			}
			catch(InterruptedException t) {};
			repaintNeeded = false;
		}
		return true;
	}
	
	/**
	 * Exits the application.
	 */
	public void stop(){
		synchronized(this){
			stagePanel.removeMouseMotionListener(primaryStageMouseMotionListener);
			stagePanel.removeMouseListener(primaryStageMouseListener);
			stagePanel.removeKeyListener(primaryStageKeyListener);
			SCRIPT_TUPLE_THREAD.stopAllScripts();
			stopped=true;
			running = false;
			this.notify();
		}
	}

	@Override
	public boolean isStopped() {
		return stopped;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public JPanel getStagePanel() {
		return stagePanel;
	}

	@Override
	public int getStageWidth() {
		return stageWidth;
	}

	@Override
	public int getStageHeight() {
		return stageHeight;
	}
	
	/**
	 * @return the opcodeRegistry
	 */
	public OpcodeRegistry getOpcodeRegistry() {
		return opcodeRegistry;
	}

	/**
	 * @return the graphicEffectRegistry
	 */
	public GraphicEffectRegistry getGraphicEffectRegistry() {
		return graphicEffectRegistry;
	}

	/**
	 * @return the stageMonitorCommandRegistry
	 */
	public StageMonitorCommandRegistry getStageMonitorCommandRegistry() {
		return stageMonitorCommandRegistry;
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
		stagePanel.addKeyListener(listener);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void removeKeyListener(KeyListener listener) {
		stagePanel.removeKeyListener(listener);
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
	 * @param sound 
	 * @param volume 
	 * @return The Clip
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 * @throws LineUnavailableException
	 */
	public SoundMonitor playSound(SoundImplementation sound, double volume) throws IOException, UnsupportedAudioFileException, LineUnavailableException{
	    SoundMonitor retval=null;
	    LinkedList<Throwable> errors = new LinkedList<Throwable>();

	    InputStream in = sound.getSoundData();
	    try {
		    AudioInputStream stream;
		    AudioFormat format;
		    DataLine.Info info;
		    Clip clip;
    		stream = AudioSystem.getAudioInputStream(sound.getSoundData());
		    format = stream.getFormat();
		    info = new DataLine.Info(Clip.class, format);
		    synchronized(activeSoundMonitors) {
			    clip = (Clip) AudioSystem.getLine(info);
			    clip.addLineListener(new LineListener() {
					@Override
					public void update(LineEvent event) {
						if((event.getType()==LineEvent.Type.STOP)||(event.getType()==LineEvent.Type.CLOSE)) {
							synchronized(activeSoundMonitors) {
								activeSoundMonitors.remove(clip);
								if(event.getType()==LineEvent.Type.STOP) {
									clip.close();
								}
							}
						}
					}
				});
			    clip.open(stream);
			    retval = new ClipSoundMonitor(clip);
		    }
	    }
	    catch(UnsupportedAudioFileException t) {
	    	// Save error to report just in case of total failure.
	    	errors.add(t);
	    }
	    if(retval == null) {
	    	in.close();
	    }
		InputSourceStream iss = new InputSourceStream(sound.getSoundData(), null);
	    if(retval==null) {
    		DataSource ds=new PullDataSource() {
				
				@Override
				public void stop() throws IOException {
				}
				
				@Override
				public void start() throws IOException {
				}
				
				@Override
				public Time getDuration() {
					return null;
				}
				
				@Override
				public Object[] getControls() {
					return null;
				}
				
				@Override
				public Object getControl(String arg0) {
					return null;
				}
				
				@Override
				public String getContentType() {
					return null;
				}
				
				@Override
				public void disconnect() {
					try {
						iss.close();
					}
					catch(IOException t) {
					}
				}
				
				@Override
				public void connect() throws IOException {}
				
				@Override
				public PullSourceStream[] getStreams() {
					return new PullSourceStream[] {iss};
				}
			};
			try {
	    		Player player = Manager.createPlayer(ds);
	    		retval = new JMFSoundMonitor(player);
			}
			catch(NoPlayerException t) {
		    	// Save error to report just in case of total failure.
		    	errors.add(t);
			}
	    }
	    if(retval == null) {
	    	iss.close();
	    }
	    if(retval==null) {
	    	for(Throwable error:errors) {
	    		error.printStackTrace();
	    	}
	    	return null;
	    }
	    if(volume>100)
		    retval.setVolume(100);
	    else if(volume<0)
		    retval.setVolume(0);
	    else
	    	retval.setVolume(volume);
	    activeSoundMonitors.add(retval);
	    return retval;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#stopAllSounds()
	 */
	@Override
	public void stopAllSounds() {
	    synchronized(activeSoundMonitors) {
    		for(SoundMonitor soundMonitor:activeSoundMonitors)
    			soundMonitor.stop();
    		activeSoundMonitors.clear();
	    }
	}

	/**
	 * 
	 * @return A Graphics2D object that will let you paint on the background using a coordinate system similar to scratch's, but with the y-axis reversed. (ie. centered on the middle, with x increasing to the right and y increasing to the bottom. The width and the height are the stage width and height.)
	 */
	public Graphics2D getPenLayerGraphics() {
		return stagePanel.getPenLayerGraphics();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#clearPenLayer()
	 */
	@Override
	public void clearPenLayer() {
		stagePanel.clearPenLayer();
		repaintStage();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addComponent(java.awt.Component, int, int, int, int)
	 */
	@Override
	public void addComponent(Component component, int x, int y, int width, int height) {
		stagePanel.addComponent(component, x, y, width, height);
		repaintStage();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeComponent(java.awt.Component)
	 */
	@Override
	public void removeComponent(Component component) {
		stagePanel.removeComponent(component);
		repaintStage();
	}
	
	/**
	 * 
	 * @param sprite
	 */
	public void addClone(Sprite sprite) {
		if(!sprite.isClone())
			throw new IllegalStateException();
		stage.addChild(sprite);
		repaintStage();
	}
	
	/**
	 * 
	 * @param sprite Removes the sprite from the list of sprites, but only if it is a clone.
	 */
	public void deleteClone(Sprite sprite) {
		if(!sprite.isClone())
			throw new IllegalStateException();
		stage.removeChild(sprite);
		repaintStage();
	}

	protected void loadProject(File file) throws IOException{
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
				String name = (String)((Map<?,?>)variableObject).get("name");
				Object value = ((Map<?,?>)variableObject).get("value");
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
				sounds[i] = parseSound(object, scratchFile);
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
				(Map<String,Object>)jsonMap.get("info"),
				this);
		
		if(jsonMap.get("scripts")!=null) {
			Object scriptsJson = jsonMap.get("scripts");
			if(!(scriptsJson instanceof java.util.List<?>))
				throw new IOException("scripts not encoded in list");
			java.util.List<Object> scriptsList = (java.util.List<Object>)scriptsJson;
			for(Object scriptObject:scriptsList) {
				ScriptTupleImplementation scriptParsed = parseScriptTuple(retval,scriptObject);
				if(scriptParsed!=null)
					retval.addScript(scriptParsed);
			}
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
		ScriptTupleImplementation retval;
		if(blockTuples.length>0) {
			BlockTuple maybeHat = blockTuples[0];
			String opcode = maybeHat.getOpcode();
			Opcode opcodeImplementation = opcodeRegistry.getOpcode(opcode);
			if((opcodeImplementation != null)&&(opcodeImplementation instanceof OpcodeHat)) {
				java.util.List<Object> arguments = maybeHat.getArguments();
				Object[] executableArguments = new Object[arguments.size()];
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
				try {
					retval = new ScriptTupleImplementation(context, blockTuples, this);
				}
				catch(InvalidScriptDefinitionException t) {
					throw new IOException(t);
				}
				((OpcodeHat)opcodeImplementation).registerListeningScript(retval, executableArguments);
				return retval;
			}
		}
		// Why bother remembering the script?
		return null;
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
	
	private static SoundImplementation parseSound(Object object, ScratchFile scratchFile) throws IOException{
		if(!(object instanceof java.util.Map<?,?>))
			throw new IOException("sound not encoded as map.");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;

		String md5 = (String)map.get("md5");
		Long baseLayerID = (Long)map.get("soundID");
		String[] filenameParts = md5.split("\\.",2);
		if(filenameParts.length<2)
			throw new IOException("Invalid sound md5: "+md5);
		String resourceName = baseLayerID.toString()+"."+filenameParts[1];
		byte[] data;
		{ // TODO If dropping Java 8 compatibility. In Java 9 there s a readAllBytes function that would be nice to use.
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int l;
			try(InputStream in = scratchFile.getResource(resourceName)){
				l = in.read(buffer);
				while(l>=0) {
					if(l>0)
						out.write(buffer, 0, l);
					l = in.read(buffer);
				}
			}
			data = out.toByteArray();
		}
		
		return new SoundImplementation((String)map.get("soundName"), (Long)map.get("soundID"), (String)map.get("md5"), (Long)map.get("sampleCount"), (Long)map.get("rate"), (String)map.get("format"), data);
	}
	
	private static CostumeImplementation parseCostume(Object object, ScratchFile scratchFile) throws IOException{
		if(!(object instanceof java.util.Map<?,?>))
			throw new IOException("costume not encoded as map.");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;
		
		// Get the image for this costume.
		String md5 = (String)map.get("baseLayerMD5");
		Long baseLayerID = (Long)map.get("baseLayerID");
		String[] filenameParts = md5.split("\\.",2);
		if(filenameParts.length<2)
			throw new IOException("Invalid baseLayerMD5: "+md5);
		String resourceName = baseLayerID.toString()+"."+filenameParts[1];
		BufferedImage image = ScratchImageRenderer.renderImageResource(resourceName, scratchFile);

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
	    	System.err.println("nodeToString Transformer Exception");
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
	private ListImplementation parseList(Object object, HashMap<String,ListImplementation> listConsolidation) throws IOException{
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
				x, y, width, height, visible, this);
		listConsolidation.put(name,retval);
		return retval;
	}
	
	private StageMonitorImplementation parseStageMonitor(Object object) throws IOException{
		if(!(object instanceof Map<?,?>))
			throw new IOException("Stage monitor not encoded as map");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;
		try {
			StageMonitorImplementation retval = new StageMonitorImplementation((String)map.get("target"), (String)map.get("cmd"), (String)map.get("param"), ((Long)map.get("color")).intValue(), (String)map.get("label"), ((Long)map.get("mode")).intValue(), ((Long)map.get("sliderMin")).intValue(), ((Long)map.get("sliderMax")).intValue(), (Boolean)map.get("isDiscrete"), ((Number)map.get("x")).intValue(), ((Number)map.get("y")).intValue(), (Boolean)map.get("visible"), this);
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
				Object value = ((Map<String,Object>)variableObject).get("value");
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
				sounds[i] = parseSound(object, scratchFile);
				i++;
			}
		}
		
		CostumeImplementation[] costumes = null;
		if(map.get("costumes")!=null) {
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
				(Map<String,Object>)map.get("spriteInfo"), stage, this);
		spritesByName.put(retval.getObjName(),retval);

		if(map.get("scripts")!=null) {
			Object scriptsJson = map.get("scripts");
			if(!(scriptsJson instanceof java.util.List<?>))
				throw new IOException("scripts not encoded in list");
			java.util.List<Object> scriptsList = (java.util.List<Object>)scriptsJson;
			for(Object scriptObject:scriptsList) {
				ScriptTupleImplementation scriptParsed = parseScriptTuple(retval,scriptObject);
				if(scriptParsed!=null)
					retval.addScript(scriptParsed);
			}
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
