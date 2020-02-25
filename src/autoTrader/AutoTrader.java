package autoTrader;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import engine.HaulJob;
import util.Util;

/**
 * @author Nicholas Contreras
 */

public class AutoTrader {

	private static Robot ROBOT;

	static {
		try {
			ROBOT = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Util.sleep(10000);
		// run("Tsuguwa IV - Moon 13 - Caldari Constructions Warehouse");
		checkForDocked();
	}

	public static void run(String dest) {

		setDestTo(dest);

		moveMouseTo(Locations.undockButton);
		leftClick();
		Util.sleep(25000);
		
		autopilotToDest();
	}
	
	private static void autopilotToDest() {
		moveMouseTo(Locations.autopilotButton);
		leftClick();

		Util.sleep(1000);

		while (true) {
			if (checkForDocked()) {
				break;
			}
			Util.sleep(10000);
		}
	}
	
	private static void setDestTo(String dest) {
		moveMouseTo(Locations.mapOpenButton);
		leftClick();
		Util.sleep(5000);
		moveMouseTo(Locations.mapSearchButton);
		leftClick();
		Util.sleep(1000);
		backspaceAll();
		Util.sleep(1000);

		// typeString(haulJob.getPickupOrder().getStation().getName());
		typeString(dest);

		Util.sleep(5000);
		moveMouseTo(Locations.mapFirstItemButton);
		rightClick();
		Util.sleep(1000);
		moveMouseTo(Locations.mapSetDestinationButton);
		leftClick();
		Util.sleep(1000);
		moveMouseTo(Locations.mapCloseButton);
		leftClick();
		Util.sleep(5000);
	}

	private static void typeString(String s) {
		for (char curChar : s.toCharArray()) {
			ROBOT.keyPress(KeyEvent.getExtendedKeyCodeForChar((int) curChar));
			Util.sleep(50);
			ROBOT.keyRelease(KeyEvent.getExtendedKeyCodeForChar((int) curChar));
			Util.sleep(50);
		}
	}

	private static void backspaceAll() {
		for (int i = 0; i < 250; i++) {
			ROBOT.keyPress(KeyEvent.VK_BACK_SPACE);
			ROBOT.keyRelease(KeyEvent.VK_BACK_SPACE);
		}
	}

	private static void moveMouseTo(Point p) {
		ROBOT.mouseMove(p.x, p.y);
		Util.sleep(50);
	}

	private static void leftClick() {
		ROBOT.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		Util.sleep(50);
		ROBOT.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		Util.sleep(50);
	}

	private static void rightClick() {
		ROBOT.mousePress(InputEvent.BUTTON3_DOWN_MASK);
		Util.sleep(50);
		ROBOT.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
		Util.sleep(50);
	}

	private static boolean checkForDocked() {

		File screenshotsFolder = new File("C:/Users/Nicholas/Documents/EVE/capture/Screenshots");

		for (File f : screenshotsFolder.listFiles()) {
			f.delete();
		}

		Util.sleep(500);

		ROBOT.keyPress(KeyEvent.VK_PRINTSCREEN);

		Util.sleep(50);

		ROBOT.keyRelease(KeyEvent.VK_PRINTSCREEN);

		Util.sleep(5000);

		File screenshot = null;

		try {
			screenshot = screenshotsFolder.listFiles()[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}

		try {
			BufferedImage img = ImageIO.read(screenshot);

			int rgb = img.getRGB(865, 845);

			screenshot.delete();

			return rgb != new Color(57, 218, 84).getRGB();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
