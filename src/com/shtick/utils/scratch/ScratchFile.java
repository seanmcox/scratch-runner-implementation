/**
 * 
 */
package com.shtick.utils.scratch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author sean.cox
 *
 */
public class ScratchFile {
	private File file;
	private ZipFile zipFile = null;

	/**
	 * @param file
	 * @throws IOException 
	 */
	public ScratchFile(File file) throws IOException{
		super();
		if(!file.exists())
			throw new FileNotFoundException(file.getPath());
		if(!file.isFile())
			throw new IOException("File not a file.");
		this.file = file;
		zipFile = new ZipFile(file);
	}

	/**
	 * 
	 * @param name
	 * @return An InputStream containing the resource data.
	 * @throws IOException 
	 */
	public InputStream getResource(String name) throws IOException{
		ZipEntry entry;
		try{
			entry = zipFile.getEntry(name);
		}
		catch(IllegalStateException t) {
			zipFile.close();
			zipFile = new ZipFile(file);
			entry = zipFile.getEntry(name);
		}
		if(entry==null)
			throw new FileNotFoundException();
		return zipFile.getInputStream(entry);
	}

	/**
	 * 
	 * @param name
	 * @return true if the resource exists and false otherwise.
	 * @throws IOException 
	 */
	public boolean hasResource(String name) throws IOException{
		ZipEntry entry;
		try{
			entry = zipFile.getEntry(name);
		}
		catch(IllegalStateException t) {
			zipFile.close();
			zipFile = new ZipFile(file);
			entry = zipFile.getEntry(name);
		}
		return (entry!=null);
	}
	
	/**
	 * 
	 * @param filter
	 * @return The list of matching resources.
	 */
	public Collection<String> getFileList(FilenameFilter filter) {
		Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();
		LinkedList<String> retval = new LinkedList<>();
		while(entryEnum.hasMoreElements()) {
			ZipEntry element = entryEnum.nextElement();
			File dir = new File("");
			String name = element.getName();
			int index = name.lastIndexOf("/");
			if(index>=0) {
				dir=new File(name.substring(0,index));
				name=name.substring(index+1,name.length()-index-1);
			}
			if((filter==null)||(filter.accept(dir, name)))
				retval.add(element.getName());
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScratchFile [file=" + file + "]";
	}
}
