/*
 * @(#)NekoSettings.java  1.1  2019-01-27
 *
 * Copyright (c) 2019 Jerry Reno
 * This is public domain software, under the terms of the UNLICENSE
 * http://unlicense.org 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.WindowConstants;

public class NekoSettings {

	// Settings keys:
	private static final String HELLO = "hello";
	private static final String TITLE = "windowTitle";
	private static final String TRIGGER_DIST = "triggerDistance";
	private static final String RUN_DIST = "runDistancePerFrame";

	private static final String MAX_FRAMERATE = "maxFramerate";
	private static final String RUN_FRAMERATE = "runFramerate";
	private static final String SIT_FRAMERATE = "sitFramerate";
	private static final String SCRATCH_FRAMERATE = "scratchFramerate";
	private static final String SLEEP_FRAMERATE = "sleepFramerate";
	private static final String LOAD_FRAMERATE = "loadFramerate";

	private Settings settings;
	private int triggerDist;
	private int minDelay;
	private int runDelay;
	private int sitDelay;
	private int scratchDelay;
	private int sleepDelay;
	private int loadDelay;

	public NekoSettings() {
		settings=new Settings("neko.properties");
		load();
	}

	public String getTitle() { return settings.getString(TITLE);}

	/** Convert frames-per-seconds to milliseconds */
	private int getDelay(String key)
	{
		// Convert the framerate settings into milliseconds
		Integer ret = settings.getInt(key);
		if ( ret==null )
			return 100;

		return 1000/ret;
	}

	public int getTriggerDist() { return triggerDist;}
	public int getMinDelay() { return minDelay;}
	public int getRunDelay() { return runDelay;}
	public int getSitDelay() { return sitDelay;}
	public int getScratchDelay() { return scratchDelay;}
	public int getSleepDelay() { return sleepDelay;}
	public int getLoadDelay() { return loadDelay;}

	public void load()
	{
		settings.load();
		String hello=settings.getString(HELLO);
		if ( hello!=null ) System.out.println(hello);

		triggerDist=16;
		Integer i = settings.getInt(TRIGGER_DIST);
		triggerDist=(i==null)?16:i;

		minDelay=getDelay(MAX_FRAMERATE);
		runDelay=getDelay(RUN_FRAMERATE);
		sitDelay=getDelay(SIT_FRAMERATE);
		scratchDelay=getDelay(SCRATCH_FRAMERATE);
		sleepDelay=getDelay(SLEEP_FRAMERATE);
		loadDelay=getDelay(LOAD_FRAMERATE);
	}

}
