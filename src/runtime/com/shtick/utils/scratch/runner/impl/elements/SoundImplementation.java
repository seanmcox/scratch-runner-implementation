/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.shtick.utils.scratch.runner.core.elements.Sound;

/**
 * @author sean.cox
 *
 */
public class SoundImplementation implements Sound{
	private String soundName;
	private long soundID;
	private String md5;
	private long sampleCount;
	private long rate;
	private String format;
	private byte[] soundData;
	
	/**
	 * @param soundName
	 * @param soundID
	 * @param md5
	 * @param sampleCount
	 * @param rate
	 * @param format
	 * @param soundData 
	 */
	public SoundImplementation(String soundName, long soundID, String md5, long sampleCount, long rate, String format, byte[] soundData) {
		super();
		this.soundName = soundName;
		this.soundID = soundID;
		this.md5 = md5;
		this.sampleCount = sampleCount;
		this.rate = rate;
		this.format = format;
		this.soundData = soundData;
	}

	@Override
	public String getSoundName() {
		return soundName;
	}

	@Override
	public long getSoundID() {
		return soundID;
	}

	@Override
	public String getMd5() {
		return md5;
	}

	@Override
	public long getSampleCount() {
		return sampleCount;
	}

	@Override
	public long getRate() {
		return rate;
	}

	@Override
	public String getFormat() {
		return format;
	}
	
	/**
	 * 
	 * @return An InoutStream containing the audio file data.
	 */
	public InputStream getSoundData() {
		return new ByteArrayInputStream(soundData);
	}
	
	String getResourceName(){
		String[] parts = md5.split("\\.",2);
		if(parts.length<0)
			throw new IllegalStateException();
		return ""+soundID+"."+parts[1];
	}
}
