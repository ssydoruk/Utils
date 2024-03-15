/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.awt.*;
import javax.swing.*;

import static java.awt.Frame.MAXIMIZED_BOTH;
import static java.awt.Frame.NORMAL;

/**
 * @author ssydoruk
 */
public class ScreenInfo {

    public static int getScreenID(Window jf) {
        int scrID = 1;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        for (int i = 0; i < gd.length; i++) {
            GraphicsConfiguration gc = gd[i].getDefaultConfiguration();
            Rectangle r = gc.getBounds();
            if (r.contains(jf.getLocation())) {
                scrID = i + 1;
            }
        }
        return scrID;
    }

    public static Dimension getScreenDimension(int scrID) {
        Dimension d = new Dimension(0, 0);
        if (scrID > 0) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle maximumWindowBounds = ge.getMaximumWindowBounds();
            d.setSize(maximumWindowBounds.width, maximumWindowBounds.height);

            // DisplayMode mode = ge.getScreenDevices()[scrID - 1].getDisplayMode();
            // d.setSize(mode.getWidth(), mode.getHeight());
        }
        return d;
    }

    public static int getScreenWidth(int scrID) {
        Dimension d = getScreenDimension(scrID);
        return d.width;
    }

    public static int getScreenHeight(int scrID) {
        Dimension d = getScreenDimension(scrID);
        return d.height;
    }

    public static void CenterWindow(Window aThis) {
        int screenID = ScreenInfo.getScreenID(aThis);
        // System.out.println("Centering " + aThis.toString() + "; screen: " +
        // screenID);
        // aThis.setLocationRelativeTo(null);
        aThis.setLocation((ScreenInfo.getScreenWidth(screenID) - aThis.getWidth()) / 2,
                (ScreenInfo.getScreenHeight(screenID) - aThis.getHeight()) / 2);
    }

    public static void CenterWindowMaxWidth(Window aThis) {
        int screenID = ScreenInfo.getScreenID(aThis);
        aThis.setLocation(0,
                (ScreenInfo.getScreenHeight(screenID) - aThis.getHeight()));
        aThis.setSize(ScreenInfo.getScreenWidth(screenID), aThis.getHeight());
    }

    public static void fixOversizedWindow(Window aThis) {
        Rectangle windowScreenBounds = getWindowScreenBounds(aThis);
        if (windowScreenBounds != null) {
            aThis.setSize(windowScreenBounds.width / 3 * 2, windowScreenBounds.height / 3 * 2);
        }
        // Dimension screenDimension =
        // getScreenDimension(ScreenInfo.getScreenID(aThis));
        // double newHeight = (screenDimension.getHeight() < aThis.getHeight()) ?
        // screenDimension.getHeight() : aThis.getHeight();
        // double newWidth = (screenDimension.getWidth() < aThis.getWidth()) ?
        // screenDimension.getWidth() : aThis.getWidth();
        // aThis.setMaximumSize(new Dimension((int) newHeight, (int) newWidth));
    }

    public static void CenterWindowTopMaxWidth(JFrame aThis) {
        int screenID = ScreenInfo.getScreenID(aThis);
        // aThis.setLocationRelativeTo(null);
        aThis.setLocation(0,
                0);
        aThis.setSize(ScreenInfo.getScreenWidth(screenID), ScreenInfo.getScreenHeight(screenID) / 2);
    }

    public static void setVisible(Window frm, boolean b) {
        setVisible(null, frm, b);
    }

    public static void CenterWindowMaxWidth(Window parent, Window frm) {
        // Rectangle windowScreenBounds = getWindowScreenBounds(parent);
        // if (windowScreenBounds != null) {
        // frm.setLocation(0,
        // windowScreenBounds.height);
        //// frm.setSize(windowScreenBounds.height/2, windowScreenBounds.width);
        //// frm.setSize();
        // }
    }

    static private Rectangle getWindowScreenBounds(Window win) {
        // GraphicsDevice gd =
        // GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        // return gd.getDefaultConfiguration().getBounds();

        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();
            Rectangle windowBounds = win.getBounds();
            Point location = win.getLocation();
            Point loc = new Point((int) (location.getX() + windowBounds.getWidth() / 2),
                    (int) (location.getY() + windowBounds.getHeight() / 2));
            if (loc.getX() >= screenBounds.getMinX()
                    && loc.getX() < screenBounds.getMaxX()
                    && loc.getY() >= screenBounds.getMinY()
                    && loc.getY() < screenBounds.getMaxY()) {
                // frm.setLocationRelativeTo(parent);
                return screenBounds;
            }
        }
        return null;
    }

    public static void setVisible(Window parent, Window frm, boolean b) {
        // if (parent != null) {
        // GraphicsDevice myDevice = parent.getGraphicsConfiguration().getDevice();
        // inquirer.logger.info("myDevice before:" + myDevice);
        // for (GraphicsDevice gd :
        // GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
        // if (parent.getLocation().getX() >=
        // gd.getDefaultConfiguration().getBounds().getMinX()
        // && parent.getLocation().getX() <
        // gd.getDefaultConfiguration().getBounds().getMaxX()
        // && parent.getLocation().getY() >=
        // gd.getDefaultConfiguration().getBounds().getMinY()
        // && parent.getLocation().getY() <
        // gd.getDefaultConfiguration().getBounds().getMaxY()) {
        //// frm.setLocationRelativeTo(parent);
        // myDevice = gd;
        // inquirer.logger.info("myDevice found:" + myDevice);
        // }
        // }
        // } else {
        // GraphicsDevice myDevice = frm.getGraphicsConfiguration().getDevice();
        // inquirer.logger.info("myDevice before:" + myDevice);
        // for (GraphicsDevice gd :
        // GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
        // if (frm.getLocation().getX() >=
        // gd.getDefaultConfiguration().getBounds().getMinX()
        // && frm.getLocation().getX() <
        // gd.getDefaultConfiguration().getBounds().getMaxX()
        // && frm.getLocation().getY() >=
        // gd.getDefaultConfiguration().getBounds().getMinY()
        // && frm.getLocation().getY() <
        // gd.getDefaultConfiguration().getBounds().getMaxY()) {
        //// frm.setLocationRelativeTo(parent);
        // myDevice = gd;
        // inquirer.logger.info("myDevice found:" + myDevice);
        // }
        // }
        // }
        //
        if (parent != null) {
            frm.setLocationRelativeTo(parent);
            parent.toBack();
        }
        frm.toFront();

        frm.setVisible(b);
    }

    public static void windowOccupyTopThird(Window rptForm) {
        Rectangle windowScreenBounds = getWindowScreenBounds(rptForm);
        if (windowScreenBounds != null) {
            rptForm.setLocation(windowScreenBounds.x,
                    windowScreenBounds.y);
            rptForm.setSize(windowScreenBounds.width, windowScreenBounds.height / 3);
        }

    }

    public static void refitMainToMsg(JFrame rptForm, JFrame fullMsgWindow, int position) {
        Rectangle windowScreenBounds = getWindowScreenBounds(rptForm);
        if (windowScreenBounds != null && rptForm != null) {
            Point formLocation = new Point();
            Dimension formDimension = new Dimension();
            Point msgLocation = new Point();
            Dimension msgDimension = new Dimension();
            switch (position) {
                case SwingConstants.TOP:
                    formLocation.setLocation(windowScreenBounds.x,
                            windowScreenBounds.y + windowScreenBounds.height / 3);
                    formDimension.setSize(windowScreenBounds.width, windowScreenBounds.height * 2 / 3);

                    msgLocation.setLocation(windowScreenBounds.x, windowScreenBounds.y);
                    msgDimension.setSize(windowScreenBounds.width, windowScreenBounds.height / 3);
                    break;

                case SwingConstants.BOTTOM:
                    formLocation.setLocation(windowScreenBounds.x, windowScreenBounds.y);
                    formDimension.setSize(windowScreenBounds.width, windowScreenBounds.height * 2 / 3);

                    msgLocation.setLocation(windowScreenBounds.x,
                            windowScreenBounds.y + windowScreenBounds.height * 2 / 3);
                    msgDimension.setSize(windowScreenBounds.width, windowScreenBounds.height / 3);
                    break;

                case SwingConstants.RIGHT:
                    formLocation.setLocation(windowScreenBounds.x, windowScreenBounds.y);
                    formDimension.setSize(windowScreenBounds.width * 2 / 3, windowScreenBounds.height);

                    msgLocation.setLocation(windowScreenBounds.x + windowScreenBounds.width * 2 / 3,
                            windowScreenBounds.y);
                    msgDimension.setSize(windowScreenBounds.width / 3, windowScreenBounds.height);
                    break;

                case SwingConstants.LEFT:
                    formLocation.setLocation(windowScreenBounds.x + windowScreenBounds.width / 3, windowScreenBounds.y);
                    formDimension.setSize(windowScreenBounds.width * 2 / 3, windowScreenBounds.height);

                    msgLocation.setLocation(windowScreenBounds.x, windowScreenBounds.y);
                    msgDimension.setSize(windowScreenBounds.width / 3, windowScreenBounds.height);
                    break;

            }
            rptForm.setLocation(formLocation);
            rptForm.setSize(formDimension);
            fullMsgWindow.setLocation(msgLocation);
            fullMsgWindow.setSize(msgDimension);
//            java.awt.EventQueue.invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    int extendedState = rptForm.getExtendedState();
//                    if( (extendedState & NORMAL) != NORMAL)
//                        rptForm.setExtendedState(fullMsgWindow.getExtendedState() & JFrame.NORMAL);
//                }
//            });
        }

    }
}
