/*
 * @(#)Neko.java  1.1  2019-01-25
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
 *         Chris Parent (original code)
 * @version 1.0.1 2010-07-17 Fixes timers. Sets longer sleep times when the
 * cat sleeps.
 * <br>1.0 2010-07-16 Created.
 */
public class Neko {
    //
    //Constants

    private static final double pi = Math.PI;
    private static final int over = 1;
    private static final int under = 2;
    private static final int left = 3;
    private static final int right = 4;
    private static Dimension MINSIZE=new Dimension(64,64);
    private static Dimension PRFSIZE=new Dimension(64*16,64*9);


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
    //
    //Variables
    private boolean windowMode;
    private int pos;      //neko's position (over,under,left,right)
    private int x, y;     //mouse pos.
    private int ox, oy;   //image pos.
    private int no;       //image number.
    private int init;     //for image loading initialize counter
    private int slp;      //sleep time
    private int ilc1;     //image loop counter
    private int ilc2;     //second loop counter
    private boolean move; //mouse move, flag
    private boolean out;  //mouse exiseted, flag
    private double theta;//image-mouse polar data
    private double dist;  //distance
    private ImageIcon image[];  //images
    private Point windowOffset = new Point(-16, -30);
    private Rectangle nekoBounds = new Rectangle();
    private Timer timer;

    //
    // UI Components
    private JFrame catbox;
    private JWindow invisibleWindow;
    private JLabel freeLabel,boxLabel;
    private Settings settings;

    /** Creates new form Neko */
    public Neko() {
        settings=new Settings("neko.properties");
        settings.load();
        String hello=settings.getString(HELLO);
        if ( hello!=null ) System.out.println(hello);
        String title=settings.getString(TITLE);
        if ( title==null ) title="Neko";
        System.out.println(title);
        catbox=new JFrame(title);
        catbox.setBackground(new Color(200,200,200,255));
        catbox.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        invisibleWindow=new JWindow();
        invisibleWindow.getRootPane().putClientProperty("Window.shadow", false);
        invisibleWindow.setBackground(new Color(200,200,200,0)); // transparent, light grey of not supported
        invisibleWindow.setAlwaysOnTop(true);

        initComponents();
        loadKitten();
        int w=image[1].getIconWidth();
        int h=image[1].getIconHeight();
        invisibleWindow.setSize(w,h);
        invisibleWindow.setLocation(ox + windowOffset.x, oy + windowOffset.y);
        boxLabel.setSize(w,h);
        catbox.setSize(16*w, 9*h);

        timer = new Timer(200, new ActionListener() {

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

    /** This method is called from within the constructor to
     * initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        freeLabel = new JLabel();
        boxLabel = new JLabel();

        FormListener formListener = new FormListener();

        freeLabel.addMouseListener(formListener);
        boxLabel.addMouseListener(formListener);
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

        invisibleWindow.pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements MouseListener {
        FormListener() {}
        public void mouseClicked(MouseEvent evt) {
            if ((evt.getSource() == freeLabel)||(evt.getSource() == boxLabel)) {
                Neko.this.imageClicked(evt);
            }
        }

        public void mouseEntered(MouseEvent evt) {
        }

        public void mouseExited(MouseEvent evt) {
        }

        public void mousePressed(MouseEvent evt) {
        }

        public void mouseReleased(MouseEvent evt) {
        }
    }

    private void imageClicked(MouseEvent evt) {
        setWindowMode(!windowMode);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                new Neko().setWindowMode(false);
            }
        });
    }

    public void setWindowMode(boolean windowed)
    {
        windowMode=windowed;
        if (windowed) {
            catbox.setVisible(true);
            invisibleWindow.setVisible(false);
        }
        else {
            catbox.setVisible(false);
            invisibleWindow.setVisible(true);
        }
    }

    private void loadKitten() {
        image = new ImageIcon[33];
        for (int i = 1; i <= 32; i++) {
            image[i] = new ImageIcon(Neko.class.getResource("images/" + i + ".GIF"));
        }
    }

    /** Locates the mouse on the screen and determines what the cat shall do. */
    private void locateMouseAndAnimateCat() {
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

            nekoBounds.x = panePoint.x + paneInsets.left + 16;
            nekoBounds.y = panePoint.y + paneInsets.top + 32;
            nekoBounds.width = sz.width - paneInsets.left - paneInsets.right - 32;
            nekoBounds.height = sz.height - paneInsets.left - paneInsets.top - 32;
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
            nekoBounds.x = screenBounds.x + screenInsets.left + 16;
            nekoBounds.y = screenBounds.y + screenInsets.top + 32;
            nekoBounds.width = screenBounds.width - screenInsets.left - screenInsets.right - 32;
            nekoBounds.height = screenBounds.height - screenInsets.left - screenInsets.top - 32;
        }

        //Determines what the cat should do, if the mouse moves
        out = !nekoBounds.contains(mx, my);
        if (out) {
            x = mx;
            y = my;
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
        } else {
            move = (x != mx || y != my);
            x = mx;
            y = my;
            if (move) {
                slp = Math.min(slp, getDelay(MAX_FRAMERATE) ); // 10fps, 100ms
            }

        }
        //image-mouse distance
        // x,y are the mouse location on screen
        // ox,oy are the old neko location on screen
        int dx = x - ox;
        int dy = oy - y;
        dist = Math.sqrt(dx * dx + dy * dy); //distance formula (from mouse to cat)
        theta = Math.atan2(dy, dx);     //angle from mouse to cat
        //
        slp = Math.max(0, slp - timer.getDelay());
        if (slp == 0)
        {
            animateCat();
        }
    }

    private void animateCat()
    {
        boolean doMove=false;
        Integer triggerDist = settings.getInt(TRIGGER_DIST);
        if ( triggerDist==null ) triggerDist=16;
        if (dist > triggerDist )
        {
            doMove=true;
            slp = getDelay(RUN_FRAMERATE); // 5fps, 200ms
            // note that ox,oy are screen-relative
            ox = (int) (ox + Math.cos(theta) * 16);
            oy = (int) (oy - Math.sin(theta) * 16);
            dist = dist - 16;

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
            //sets move back to false
            move = false;
        } else {   //-if the mouse hasn't moved or the cat's over the mouse-
            ox = x;
            oy = y;
            slp = getDelay(SIT_FRAMERATE);
            switch (no) {
                case 25: //<cat sit>
                    //If the mouse is outside the applet
                    if (out == true) {
                        slp = getDelay(SCRATCH_FRAMERATE);
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
                                no = 31;
                                break;
                        }
                        pos = 0;
                        break;
                    }
                    no = 31;
                    break; //<31: cat lick>
                //
                case 17: //The mouse is outside, above applet
                    slp = getDelay(SCRATCH_FRAMERATE);
                    no = 18;    //show images 17 & 18, 6 times
                    ilc1++;
                    if (ilc1 == 6) {
                        no = 27;
                        ilc1 = 0;
                    }
                    break;
                //
                case 18:
                    no = 17;
                    break;
                //
                case 21: //The mouse is outside, under applet
                    slp = getDelay(SCRATCH_FRAMERATE);
                    no = 22;    //show images 21 & 22, 6 times
                    ilc1++;
                    if (ilc1 == 6) {
                        no = 27;
                        ilc1 = 0;
                    }
                    break;
                //
                case 22:
                    no = 21;
                    break;
                //
                case 23: //the mouse is outside, left
                    slp = getDelay(SCRATCH_FRAMERATE);
                    no = 24;    //show images 23 & 24, 6 times
                    ilc1++;
                    if (ilc1 == 6) {
                        no = 27;
                        ilc1 = 0;
                    }
                    break;
                //
                case 24:
                    no = 23;
                    break;
                //
                case 19: //The mouse is outside, right
                    slp = getDelay(SCRATCH_FRAMERATE);
                    no = 20;    //show images 19 & 20, 6 times
                    ilc1++;
                    if (ilc1 == 6) {
                        no = 27;
                        ilc1 = 0;
                    }
                    break;
                //
                case 20:
                    no = 19;
                    break;
                //
                case 31: //cat lick (6  times)
                    no = 25;
                    ilc1++;
                    if (ilc1 == 6) {
                        no = 27;
                        ilc1 = 0;
                    }
                    break;
                //
                case 27:
                    slp = getDelay(SCRATCH_FRAMERATE);
                    no = 28;
                    break; //cat scratch (27 & 28, 4 times)
                case 28:
                    no = 27;
                    ilc2++;
                    if (ilc2 == 4) {
                        no = 26;
                        ilc2 = 0;
                    }
                    break;
                case 26:
                    no = 29;
                    slp = getDelay(SLEEP_FRAMERATE);
                    break; //cat yawn (26)
                case 29:
                    no = 30;
                    slp = getDelay(SLEEP_FRAMERATE);
                    break; //cat sleep (29 & 30, forever)
                case 30:
                    no = 29;
                    slp = getDelay(SLEEP_FRAMERATE);
                    break;
                default:
                    no = 25;
                    break;
            }
            if (move == true) {
                //re-initialize some variables
                no = 32;
                ilc1 = 0;
                ilc2 = 0;
                move = false;
            }
        }
        if (init < 33) {
            //tells the user the program is testing the images, and tells them
            //when the test is done.
            slp = getDelay(LOAD_FRAMERATE);
            ox = nekoBounds.x + nekoBounds.width / 2;
            oy = nekoBounds.y + nekoBounds.height / 2;
            no = init;
            init++;
        }
        //draw the new image
        if(windowMode) {
            // note that ox,oy are screen-relative
            boxLabel.setLocation(ox-nekoBounds.x, oy-nekoBounds.y);
        }
        else {
            if ( doMove )
            {
                invisibleWindow.setLocation(ox + windowOffset.x, oy + windowOffset.y);
            }
        }
        freeLabel.setIcon(image[no]);
        boxLabel.setIcon(image[no]);
    }

    private int getDelay(String key)
    {
        // Convert the framerate settings into milliseconds
        Integer ret = settings.getInt(key);
        if ( ret==null )
            return 100;

        return 1000/ret;
    }
}

