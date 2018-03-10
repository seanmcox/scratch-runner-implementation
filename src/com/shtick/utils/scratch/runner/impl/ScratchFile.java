/**
 * 
 */
package com.shtick.utils.scratch.runner.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
}
