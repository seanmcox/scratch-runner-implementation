/**
 * 
 */
package com.shtick.utils.scratch.imager.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.svg.SVGDocument;

import com.shtick.utils.scratch.ScratchFile;
import com.shtick.utils.scratch.ScratchImageRenderer;

/**
 * @author scox
 *
 */
public class MainFrame extends JFrame {
	private ImagePanel imagePanel;
	/**
	 * 
	 * @param scratchFile 
	 * @throws HeadlessException
	 */
	public MainFrame(ScratchFile scratchFile) throws HeadlessException {
		this("Scratch Image Viewer", scratchFile);
	}

	/**
	 * 
	 * @param title
	 * @param scratchFile 
	 * @throws HeadlessException
	 */
	public MainFrame(String title, ScratchFile scratchFile) throws HeadlessException {
		super(title);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setBounds(0, 0, 640, 480);
		setUpUI(scratchFile);
	}
	
	private void setUpUI(ScratchFile scratchFile) {
		JTree fileSelector = new JTree(new ImageViewTreeModel(scratchFile));
		fileSelector.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if(e.getPath()==null)
					return;
				Object selectedComponent = e.getPath().getLastPathComponent();
				if((selectedComponent == null)||(selectedComponent instanceof ScratchFile))
					return;
				imagePanel.setImage((ScratchFile)e.getPath().getParentPath().getLastPathComponent(), (String)selectedComponent);
			}
		});
		imagePanel = new ImagePanel();

		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());

		JSplitPane centerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		centerPane.add(new JScrollPane(fileSelector));
		centerPane.add(new JScrollPane(imagePanel));
		contentPane.add(centerPane,BorderLayout.CENTER);
	}
	
	private class ImagePanel extends JPanel{
		private BufferedImage image;
		private JTextArea message = new JTextArea(10,50);
		
		public ImagePanel() {
			super(new BorderLayout());
			add(new JScrollPane(message),BorderLayout.NORTH);
		}

		/**
		 * 
		 * @param scratchFile
		 * @param resource
		 */
		public void setImage(ScratchFile scratchFile, String resource) {
			this.image = null;
			try {
				this.image = ScratchImageRenderer.renderImageResource(resource, scratchFile);
				if(resource.endsWith(".svg")) {
					SVGDocument doc = ScratchImageRenderer.getSVGDocument(resource, scratchFile);
			        StringWriter sw = new StringWriter();
			        TransformerFactory tf = TransformerFactory.newInstance();
			        Transformer transformer = tf.newTransformer();
			        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			        transformer.transform(new DOMSource(doc), new StreamResult(sw));
			        message.setText(sw.toString());
				}
			}
			catch(IOException|TransformerException t) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				t.printStackTrace(new PrintStream(out));
				message.setText(out.toString());
			}
			repaint();
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.drawImage(image, 5, message.getHeight()+message.getY()+5, null);
		}
	}

	private class ImageViewTreeModel implements TreeModel{
		private ScratchFile scratchFile;
		private String[] imageFiles;
		
		/**
		 * 
		 * @param scratchFile
		 */
		public ImageViewTreeModel(ScratchFile scratchFile) {
			Collection<String> imageFiles = scratchFile.getFileList(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".svg")||name.endsWith(".png")||name.endsWith(".jpg")||name.endsWith(".gif")||name.endsWith(".bmp");
				}
			});
			String[] array = imageFiles.toArray(new String[imageFiles.size()]);
			Arrays.sort(array);
			this.scratchFile = scratchFile;
			this.imageFiles = array;
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeModel#getRoot()
		 */
		@Override
		public Object getRoot() {
			return scratchFile;
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
		 */
		@Override
		public Object getChild(Object parent, int index) {
			if(!(parent instanceof ScratchFile))
				return null;
			return imageFiles[index];
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
		 */
		@Override
		public int getChildCount(Object parent) {
			// Now for a binary search.
			return imageFiles.length;
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
		 */
		@Override
		public boolean isLeaf(Object node) {
			return !(node instanceof ScratchFile);
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
		 */
		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			// Do nothing
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if(parent == scratchFile)
				return -1;
			if(child==null)
				return -1;
			for(int i=0;i<imageFiles.length;i++) {
				if(child.equals(imageFiles[i]))
					return i;
			}
			return -1;
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
		 */
		@Override
		public void addTreeModelListener(TreeModelListener l) {
			// Do nothing for now.
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
		 */
		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			// Do nothing for now.
		}
	}
}
