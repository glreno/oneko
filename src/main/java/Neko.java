/*
 * @(#)Neko.java  2.0.1  2019-02-29
 *
 * Copyright (c) 2019 Jerry Reno
 * This is public domain software, under the terms of the UNLICENSE
 * http://unlicense.org 
 * 
 * This is an extended version of the earlier Java Neko 1.0:
 * Copyright (c) 2010 Werner Randelshofer
 * Hausmatt 10, Immensee, CH-6405, Switzerland.
 *
 * This source code is free to everyone.
 *
 * This is a desktop adaptation of the applet
 * JAVA NEKO V1.0 by Chris Parent, 1999.
 * http://mysite.ncnetwork.net/res8t1xo/class/neko.htm
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.WindowConstants;

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
public class Neko {

	//
	//Constants
	private static Dimension MINSIZE=new Dimension(64,64);
	private static Dimension PRFSIZE=new Dimension(64*16,64*9);

	//
	// UI Components
	private JFrame catbox;
	private JWindow invisibleWindow;
	private JLabel freeLabel,boxLabel;
	private NekoSettings settings;
	private NekoController controller;

	/** Creates new form Neko */
	public Neko() {
		settings=new NekoSettings();
		initComponents();
		invisibleWindow.setLocation(100,100);
		controller=new NekoController(settings,invisibleWindow,catbox,freeLabel,boxLabel);
		setWindowMode(false);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		String title=settings.getTitle();
		if ( title==null ) title="Neko";
		catbox=new JFrame(title);
		catbox.setBackground(new Color(200,200,200,255));
		catbox.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		invisibleWindow=new JWindow();
		invisibleWindow.getRootPane().putClientProperty("Window.shadow", false);
		invisibleWindow.setBackground(new Color(200,200,200,0)); // transparent, light grey of not supported
		invisibleWindow.setAlwaysOnTop(true);

		freeLabel = new JLabel();
		boxLabel = new JLabel();

		MouseListener mouseMoveListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if ((evt.getSource() == freeLabel)||(evt.getSource() == boxLabel)) {
					imageClicked(evt);
				}
			}
		};

		freeLabel.addMouseListener(mouseMoveListener);
		boxLabel.addMouseListener(mouseMoveListener);
		invisibleWindow.getContentPane().add(freeLabel, BorderLayout.CENTER);
		catbox.getContentPane().add(boxLabel);
		catbox.pack();

		// We really don't want a layout manager messing with us.
		// Maybe this class should BE a layout manager.
		catbox.getContentPane().setLayout(new LayoutManager() {
			public void addLayoutComponent(String n,Component c) {}
			public void layoutContainer(Container p){}
			public Dimension minimumLayoutSize(Container p) { return MINSIZE;}
			public Dimension preferredLayoutSize(Container p) { return PRFSIZE;}
			public void removeLayoutComponent(Component c) {}
		});

		catbox.addComponentListener(new ComponentAdapter() {
			public void componentMoved(ComponentEvent e) {
				controller.catboxMoved();
			}
		});
		catbox.addWindowListener(new WindowAdapter() {
			public void windowDeiconified(WindowEvent e) {
				controller.catboxDeiconified();
			}
		});

		invisibleWindow.pack();
	}

	private void imageClicked(MouseEvent evt) {
		setWindowMode(!controller.getWindowMode());
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				new Neko();
			}
		});
	}

	public void setWindowMode(boolean windowed)
	{
		controller.setWindowMode(windowed);
		settings.load();
		if (windowed) {
			String title=settings.getTitle();
			if ( title==null ) title="Neko";
			catbox.setTitle(title);
			invisibleWindow.setVisible(false);

			catbox.setVisible(true);
		}
		else {
			catbox.setVisible(false);
			invisibleWindow.setVisible(true);
		}
		controller.moveCatInBox();
	}

}

