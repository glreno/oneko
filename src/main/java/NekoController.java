/*
 * @(#)NekoController.java  1.1  2019-01-27
 *
 * Copyright (c) 2019 Jerry Reno
 * This is public domain software, under the terms of the UNLICENSE
 * http://unlicense.org 
 * 
 * This is an extended version of the earlier Java Neko:
 * Copyright (c) 2010 Werner Randelshofer
 * Hausmatt 10, Immensee, CH-6405, Switzerland.
 *
 * This source code is free to everyone.
 *
 * This is a desktop adaptation of the applet
 * JAVA NEKO V1.0 by Chris Parent, 1999.
 * http://mysite.ncnetwork.net/res8t1xo/class/neko.htm
 */

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.Timer;

/**
 * Neko the cat.
 * <p>
 * This program loads in 32 images of Neko, and tests them. (to show you that
 * they've been loaded). Neko will chase you mouse cursor around the desktop.
 * Once she's over it and the mouse doesn't move she'll prepare to take a nap.
 * If the mouse go's outside the desktop she will reach the border and try
 * to dig for it. She'll eventually give up, and fall asleep.
 *
 *
 * @author Werner Randelshofer (adaption for desktop)
 *		 Chris Parent (original code)
 * @version 1.0.1 2010-07-17 Fixes timers. Sets longer sleep times when the
 * cat sleeps.
 * <br>1.0 2010-07-16 Created.
 */
public class NekoController {
	//
	//Constants

	private static final double pi = Math.PI;
	private static final int over = 1;
	private static final int under = 2;
	private static final int left = 3;
	private static final int right = 4;

	//
	//Variables
	private boolean windowMode;
	private int pos;	  //neko's position (over,under,left,right)
	private int x, y;	 //mouse pos.
	private int ox, oy;   //image pos.
	private int no;	   //image number.
	private int init;	 //for image loading initialize counter
	private int state;
	private int slp;	  //sleep time
	private int ilc1;	 //image loop counter
	private int ilc2;	 //second loop counter
	private boolean mouseMoved; //mouse move, flag
	private boolean out;  //mouse exiseted, flag
	private double theta;//image-mouse polar data
	private double dist;  //distance
	private ImageIcon image[];  //images
	private Point windowOffset = new Point(-16, -30);
	private Rectangle nekoBounds = new Rectangle();
	private Timer timer;
	private int w,h; // size of icons

	//
	// UI Components
	private JWindow invisibleWindow;
	private JFrame catbox;
	private JLabel freeLabel,boxLabel;
	private NekoSettings settings;

	/** Creates new form Neko */
	public NekoController(NekoSettings settings,JWindow invisibleWindow,JFrame visibleWindow,JLabel free,JLabel boxed) {
		this.init=0;
		this.state=0;
		this.settings=settings;
		this.invisibleWindow=invisibleWindow;
		this.catbox=visibleWindow;
		this.freeLabel=free;
		this.boxLabel=boxed;
		this.windowMode=false;
		loadKitten();
		w=image[1].getIconWidth();
		h=image[1].getIconHeight();
		invisibleWindow.setSize(w,h);
		//invisibleWindow.setLocation(ox + windowOffset.x, oy + windowOffset.y);
		boxLabel.setSize(w,h);
		catbox.setSize(16*w, 9*h);


		timer = new Timer(50, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					locateMouseAndAnimateCat();
				}
				catch (Exception ex) {}
			}
		});
		timer.setRepeats(true);
		timer.start();

	}

	private void loadKitten() {
		// Note: The files start with 1. image[0] is a copy of 25.
		// image[0] is a default, image[25] is part of the wash animation with image[31]
		image = new ImageIcon[33];
		for (int i = 1; i <= 32; i++) {
			image[i] = new ImageIcon(Neko.class.getResource("images/" + i + ".GIF"));
		}
		image[0]=image[25];
	}

	public int getWidth() { return w;}
	public int getHeight() { return h;}
	public void setWindowMode(boolean windowed) { this.windowMode=windowed;}
	public boolean getWindowMode() { return this.windowMode;}


	/** Locates the mouse on the screen and determines what the cat shall do. */
	public void locateMouseAndAnimateCat() {
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		if (pointerInfo==null) return;
		Point mouseLocation = pointerInfo.getLocation();
		if (mouseLocation==null) return;

		int mx = mouseLocation.x;
		int my = mouseLocation.y;

		if(windowMode)
		{
			// nekoBounds is the area of the window that the Neko can be in
			Point panePoint = catbox.getContentPane().getLocationOnScreen();
			Insets paneInsets=catbox.getContentPane().getInsets();
			Dimension sz=catbox.getContentPane().getSize();

			nekoBounds.x = panePoint.x + paneInsets.left + w/2;
			nekoBounds.y = panePoint.y + paneInsets.top + h;
			nekoBounds.width = sz.width - paneInsets.left - paneInsets.right - w;
			nekoBounds.height = sz.height - paneInsets.left - paneInsets.top - h;
		}
		else
		{
			GraphicsDevice mouseMonitor=pointerInfo.getDevice();
			if (mouseMonitor==null) return;
			GraphicsConfiguration gc = mouseMonitor.getDefaultConfiguration();
			if ( gc==null) return;
			Rectangle screenBounds = gc.getBounds();
			if ( screenBounds==null) return;
			Toolkit tk = invisibleWindow.getToolkit();
			Insets screenInsets = tk.getScreenInsets(gc);
			if ( screenInsets==null) return;
			// nekoBounds is the area of the screen that the Neko can be in
			nekoBounds.x = screenBounds.x + screenInsets.left + w/2;
			nekoBounds.y = screenBounds.y + screenInsets.top + h;
			nekoBounds.width = screenBounds.width - screenInsets.left - screenInsets.right - w;
			nekoBounds.height = screenBounds.height - screenInsets.left - screenInsets.top - h;
		}

		//Determines what the cat should do, if the mouse moves
		out = !nekoBounds.contains(mx, my);
		x = mx;
		y = my;
		if (out) {
			if (y < nekoBounds.y) {
				y = nekoBounds.y;
				pos = over;
			}
			if (y > nekoBounds.y + nekoBounds.height) {
				y = nekoBounds.y + nekoBounds.height;
				pos = under;
			}
			if (x < nekoBounds.x) {
				x = nekoBounds.x;
				pos = left;
			}
			if (x > nekoBounds.x + nekoBounds.width) {
				x = nekoBounds.x + nekoBounds.width;
				pos = right;
			}
		}

		//image-mouse distance
		// x,y are the mouse location on screen
		// ox,oy are the old neko location on screen
		int dx = x - ox;
		int dy = oy - y;
		dist = Math.sqrt(dx * dx + dy * dy); //distance formula (from mouse to cat)
		theta = Math.atan2(dy, dx);	 //angle from mouse to cat

		mouseMoved = mouseMoved || dist > settings.getTriggerDist();
		//
		slp = Math.max(0, slp - timer.getDelay());
		if (slp == 0)
		{
			animateCat();
		}
	}

	private void animateCat()
	{
		// There are four states.
		// 0. Initialization (init<33)
		// 1. Chasing the mouse (doMove=true)
		// 2. sleeping or preparing to sleep
		// 3. Surprised (was asleep, but the mouse moved) (no=32)
		boolean doMove=false;
		int triggerDist = settings.getTriggerDist();
		if ( state==0 ) {
			if (init < 33) {
				doMove=true;
				//tells the user the program is testing the images, and tells them
				//when the test is done.
				slp = settings.getLoadDelay();
				ox = nekoBounds.x + nekoBounds.width / 2;
				oy = nekoBounds.y + nekoBounds.height / 2;
				no = init;
				init++;
			}
			else {
				state=1;
			}
		}
		else if ( state==1 ) {
			doMove=true;
			slp = settings.getRunDelay(); // 5fps, 200ms
			// note that ox,oy are screen-relative
			double run=settings.getRunDist(); // 16
			if ( run>dist ) {
				run=dist;
			}
			ox = (int) (ox + Math.cos(theta) * run);
			oy = (int) (oy - Math.sin(theta) * run);
			dist = dist - run;
			if ( dist < settings.getCatchDist() ) {
				// mouse has been caught!
				state=2;
			}

			/*
			The following conditions determine what image should be shown.
			Remember there are two images for each action. For example if the cat's
			going right, display the cat with open legs and then with close legs,
			open, and so on.
			 */
			if (theta >= -pi / 8 && theta <= pi / 8) //right
			{
				no = (no == 5) ? 6 : 5;
			}
			if (theta > pi / 8 && theta < 3 * pi / 8) //upper-right
			{
				no = (no == 3) ? 4 : 3;
			}
			if (theta >= 3 * pi / 8 && theta <= 5 * pi / 8) //up
			{
				no = (no == 1) ? 2 : 1;
			}
			if (theta > 5 * pi / 8 && theta < 7 * pi / 8) //upper-left
			{
				no = (no == 15) ? 16 : 15;
			}
			if (theta >= 7 * pi / 8 || theta <= -7 * pi / 8) //left
			{
				no = (no == 13) ? 14 : 13;
			}
			if (theta > -7 * pi / 8 && theta < -5 * pi / 8) //bottom-left
			{
				no = (no == 11) ? 12 : 11;
			}
			if (theta >= -5 * pi / 8 && theta <= -3 * pi / 8) //down
			{
				no = (no == 9) ? 10 : 9;
			}
			if (theta > -3 * pi / 8 && theta < -pi / 8) //bottom-right
			{
				no = (no == 7) ? 8 : 7;
			}
			//We have moved toward the mouse, so set mouseMoved back to false
			mouseMoved = false;
		}
		else {   //-if the mouse hasn't moved or the cat's over the mouse-
			// state is 2 ; it stays at 2 until the mouse moves
			// When it moves, go to state 3, show surprise, and then to state 1
//			ox = x;
//			oy = y;
			switch (no) {
				case 0: //<cat sit>
					//If the mouse is outside the applet
					if (out == true) {
						slp = settings.getSharpenDelay();
						switch (pos) {
							case over:
								no = 17;
								break;
							case under:
								no = 21;
								break;
							case left:
								no = 23;
								break;
							case right:
								no = 19;
								break;
							default:
								slp = settings.getSitDelay();
								no = 31;
								break;
						}
						pos = 0;
						break;
					}
					slp = settings.getSitDelay();
					no = 31;
					break; //<31: cat lick>
				//
				case 17: //The mouse is outside, above applet
					slp = settings.getSharpenDelay();
					no = 18;	//show images 17 & 18, 6 times
					ilc1++;
					if (ilc1 == 6) {
						no = 27;
						ilc1 = 0;
					}
					break;
				//
				case 18:
					slp = settings.getSharpenDelay();
					no = 17;
					break;
				//
				case 21: //The mouse is outside, under applet
					slp = settings.getSharpenDelay();
					no = 22;	//show images 21 & 22, 6 times
					ilc1++;
					if (ilc1 == 6) {
						no = 27;
						ilc1 = 0;
					}
					break;
				//
				case 22:
					slp = settings.getSharpenDelay();
					no = 21;
					break;
				//
				case 23: //the mouse is outside, left
					slp = settings.getSharpenDelay();
					no = 24;	//show images 23 & 24, 6 times
					ilc1++;
					if (ilc1 == 6) {
						no = 27;
						ilc1 = 0;
					}
					break;
				//
				case 24:
					slp = settings.getSharpenDelay();
					no = 23;
					break;
				//
				case 19: //The mouse is outside, right
					slp = settings.getSharpenDelay();
					no = 20;	//show images 19 & 20, 6 times
					ilc1++;
					if (ilc1 == 6) {
						no = 27;
						ilc1 = 0;
					}
					break;
				//
				case 20:
					slp = settings.getSharpenDelay();
					no = 19;
					break;
				//
				case 31: //cat lick (6  times)
					slp = settings.getSitDelay();
					no = 25;
					ilc1++;
					if (ilc1 == 6) {
						slp = settings.getScratchDelay();
						no = 27;
						ilc1 = 0;
					}
					break;
				//
				case 25:
					slp = settings.getSitDelay();
					no = 31;
					break;
				//
				case 27:
					slp = settings.getScratchDelay();
					no = 28;
					break; //cat scratch (27 & 28, 4 times)
				case 28:
					no = 27;
					ilc2++;
					if (ilc2 == 4) {
						no = 26;
						slp = settings.getYawnDelay();
						ilc2 = 0;
					}
					break;
				case 26:
					no = 29;
					slp = settings.getSleepDelay();
					break; //cat yawn (26)
				case 29:
					no = 30;
					slp = settings.getSleepDelay();
					break; //cat sleep (29 & 30, forever)
				case 30:
					no = 29;
					slp = settings.getSleepDelay();
					break;
				default:
					no = 0;
					break;
			}
			if (mouseMoved ) {
				// State 3. Go to state 1 and chase the mouse,
				// after a suitable delay.
				//re-initialize some variables
				slp = settings.getSurpriseDelay();
				no = 32;
				ilc1 = 0;
				ilc2 = 0;
				//mouseMoved = false;
				state=1;
			}
		}
		//draw the new image
		if ( doMove )
		{
			if(windowMode) {
				// note that ox,oy are screen-relative
				boxLabel.setLocation(ox-nekoBounds.x, oy-nekoBounds.y);
			}
			else {
				invisibleWindow.setLocation(ox + windowOffset.x, oy + windowOffset.y);
			}
		}
		freeLabel.setIcon(image[no]);
		boxLabel.setIcon(image[no]);
	}

}

