/*
 * @(#)NekoSettings.java  2.0.1  2019-02-02
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
	private static final String CATCH_DIST = "catchDistance";
	private static final String RUN_DIST = "runDistancePerFrame";
	private static final String OFFSETX = "offsetx";
	private static final String OFFSETY = "offsety";

	private static final String MAX_FRAMERATE = "maxFramerate";
	private static final String RUN_FRAMERATE = "runFramerate";
	private static final String SIT_FRAMERATE = "sitFramerate";
	private static final String SHARPEN_FRAMERATE = "sharpenFramerate";
	private static final String SCRATCH_FRAMERATE = "scratchFramerate";
	private static final String LOAD_FRAMERATE = "loadFramerate";

	private static final String SLEEP_DELAY = "sleepDelay";
	private static final String YAWN_DELAY = "yawnDelay";
	private static final String SURPRISE_DELAY = "surpriseDelay";

	private Settings settings;
	private int triggerDist;
	private int catchDist;
	private int runDist;
	private int minDelay;
	private int runDelay;
	private int sitDelay;
	private int scratchDelay;
	private int sharpenDelay;
	private int loadDelay;
	private int sleepDelay;
	private int yawnDelay;
	private int surpriseDelay;
	private int offsetX;
	private int offsetY;

	public NekoSettings() {
		settings=new Settings("neko.properties");
		load();
	}

	public String getTitle() { return settings.getString(TITLE);}

	/** Convert frames-per-seconds to milliseconds */
	private int getDelay(String key)
	{
		// Convert the framerate settings into milliseconds
		Integer ret = settings.getInt(key,100);

		return 1000/ret;
	}

	public int getTriggerDist() { return triggerDist;}
	public int getCatchDist() { return catchDist;}
	public int getRunDist() { return runDist;}
	public int getOffsetX() { return offsetX;}
	public int getOffsetY() { return offsetY;}
	public int getMinDelay() { return minDelay;}
	public int getRunDelay() { return runDelay;}
	public int getSitDelay() { return sitDelay;}
	public int getScratchDelay() { return scratchDelay;}
	public int getSharpenDelay() { return sharpenDelay;}
	public int getLoadDelay() { return loadDelay;}
	public int getSleepDelay() { return sleepDelay;}
	public int getYawnDelay() { return yawnDelay;}
	public int getSurpriseDelay() { return surpriseDelay;}

	public void load()
	{
		settings.load();
		String hello=settings.getString(HELLO);
		if ( hello!=null ) System.out.println(hello);

		triggerDist = settings.getInt(TRIGGER_DIST,16);
		catchDist = settings.getInt(CATCH_DIST,16);
		runDist = settings.getInt(RUN_DIST,16);
		offsetX = settings.getInt(OFFSETX,0);
		offsetY = settings.getInt(OFFSETY,0);

		sleepDelay = settings.getInt(SLEEP_DELAY,1000);
		yawnDelay = settings.getInt(YAWN_DELAY,1000);
		surpriseDelay = settings.getInt(SURPRISE_DELAY,1000);

		minDelay=getDelay(MAX_FRAMERATE);
		runDelay=getDelay(RUN_FRAMERATE);
		sitDelay=getDelay(SIT_FRAMERATE);
		scratchDelay=getDelay(SCRATCH_FRAMERATE);
		sharpenDelay=getDelay(SHARPEN_FRAMERATE);
		loadDelay=getDelay(LOAD_FRAMERATE);

	}

}
