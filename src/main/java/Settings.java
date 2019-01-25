/*
 * @(#)Neko.java  1.1  2019-01-24
 *
 * Copyright (c) 2019 Jerry Reno
 * This is public domain software, under the terms of the UNLICENSE
 * http://unlicense.org 
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/** Simple file-overridable resources */
public class Settings {
	private String fn;
	private File homefile;
	private Path homepath;
	private Properties builtin;
	private Properties override;

	/** Load all of the properties in src/main/resources/filename
	 * and the file ~/filename; and start a watcher
	 * on the file.
	 */
	public Settings(String filename)
	{
		fn=filename;
		builtin=new Properties();
		override=new Properties();
		String home=System.getProperty("user.home");
		homefile=new File(home,fn);
		homepath=homefile.toPath();
		// Need to register a Watcher on that to reload on the fly
	}

	public void load()
	{
		InputStream in=null;
		try
		{
			in=Settings.class.getResourceAsStream(fn);
			builtin.load(in);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if ( in!=null ) {
				try{
				in.close();
				} catch(IOException e) {}
			}
		}

		loadOverride();
	}

	public void loadOverride()
	{
		if ( homefile==null || !homefile.exists() ) return;
		InputStream in=null;
		try
		{
			in=new FileInputStream(homefile);
			builtin.load(in);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if ( in!=null ) {
				try{
				in.close();
				} catch(IOException e) {}
			}
		}
	}

	public String getString(String key)
	{
		Object ret=null;
		ret=override.get(key);
		if ( ret!=null ) return ret.toString();
		ret=builtin.get(key);
		if ( ret!=null ) return ret.toString();
		return null;
	}

	public Integer getInt(String key)
	{
		Object ret=null;
		ret=override.get(key);
		if ( ret!=null )
		{
			if ( ret instanceof Integer ) return (Integer) ret;
			try {
				Integer i = Integer.valueOf(ret.toString());
				override.put(key,i);
				return i;
			}
			catch (NumberFormatException e) { }
		}
		ret=builtin.get(key);
		if ( ret!=null )
		{
			if ( ret instanceof Integer ) return (Integer) ret;
			try {
				Integer i = Integer.valueOf(ret.toString());
				override.put(key,i);
				return i;
			}
			catch (NumberFormatException e) { }
		}
		return null;
	}

}
