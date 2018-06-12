/**
 * 
 */
package com.shtick.utils.scratch;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

/**
 * @author scox
 *
 */
public class ScratchImageRenderer {
	/**
	 * 
	 * @param resourceName The file name for the image file.
	 * @param scratchFile
	 * @return The image loaded as a BufferedImage.
	 * @throws IOException
	 */
	public static BufferedImage renderImageResource(String resourceName, ScratchFile scratchFile) throws IOException{
		String[] filenameParts = resourceName.split("\\.",2);
		if(filenameParts.length<2)
			throw new IOException("Invalid image file name.");
		BufferedImage image;
		if("svg".equals(filenameParts[1])) {
			SVGDocument doc = getSVGDocument(resourceName,scratchFile);

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
			try(ImageInputStream iis = ImageIO.createImageInputStream(pngIn)){
		        Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
				if(!iter.hasNext())
					throw new IOException("Reader not found for transcoded png originating from: "+resourceName);
			    ImageReader reader = iter.next();
			    reader.setInput(iis);
			    image = reader.read(0);
			}
		}
		else {
			try(InputStream in = scratchFile.getResource(resourceName); ImageInputStream iis = ImageIO.createImageInputStream(in)){
		        Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
				if(!iter.hasNext())
					throw new IOException("Reader not found for specified image: "+resourceName);
			    ImageReader reader = iter.next();
			    reader.setInput(iis);
			    image = reader.read(0);
			}
		}
		
		return image;
	}
	
	/**
	 * 
	 * @param resourceName
	 * @param scratchFile
	 * @return The SVG document for the given SVG file. (Modified to simulate Scratch idiosyncracies.)
	 * @throws IOException
	 */
	public static SVGDocument getSVGDocument(String resourceName, ScratchFile scratchFile) throws IOException{
		SVGDocument doc = null;
		try(InputStream in = scratchFile.getResource(resourceName)){
		    String parser = XMLResourceDescriptor.getXMLParserClassName();
		    SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		    doc = (SVGDocument)f.createDocument(resourceName, in);
		}
		
	    { // Handle the SVG spec vs. Scratch implementation discrepancy re: multiline text.
			NodeList textNodes = doc.getElementsByTagName("text");
			for(int i=0;i<textNodes.getLength();i++) {
				Node textNode = textNodes.item(i);
				textNode.getAttributes().removeNamedItem("x");
				textNode.getAttributes().removeNamedItem("y");
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
					if(j>0) {
						Element lineElement = doc.createElementNS("http://www.w3.org/2000/svg", "tspan");
						lineElement.setAttribute("dy", l+"em");
						lineElement.setAttribute("x", "0"/*((Element)textNode).getAttribute("x")*/);
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

	    { // Handle the SVG spec vs. Scratch implementation discrepancy re: text transform handling. Seems to not affect other elements.
			NodeList textNodes = doc.getElementsByTagName("text");
			for(int i=0;i<textNodes.getLength();i++) {
				Node textNode = textNodes.item(i);
				Node transform = textNode.getAttributes().getNamedItem("transform");
				if(transform == null)
					continue;
				String value = transform.getNodeValue().trim();
				if(!value.matches("^matrix\\(([^,]*,){5}[^,]*\\)$"))
					continue;
				value = value.substring("matrix(".length(),value.length()-1);
				String[] parts = value.split(",");
				if(parts.length != 6)
					continue;
				for(int j=0;j<parts.length;j++)
					parts[j] = parts[j].trim();
				try {
					parts[0] = ""+(Double.parseDouble(parts[0])*1.66);
					parts[3] = ""+(Double.parseDouble(parts[3])*1.66);
				}
				catch(NumberFormatException t) {
					t.printStackTrace();
					continue;
				}
				
				// Put it back together
				value = "";
				for(int j=0;j<parts.length;j++) {
					if(value.length()>0)
						value+=", ";
					value+=parts[j];
				}
				
				transform.setNodeValue("matrix("+value+")");
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

		return doc;
	}
}
