package com.orasi.utils;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.StopWatch;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.orasi.core.interfaces.Element;
import com.orasi.core.interfaces.impl.internal.ElementFactory;
import com.orasi.exception.automation.PageInitialization;

/**
 * Several different methods of waiting for a page to finish loading.
 * 
 * 
 * @version 10/16/2014
 * @author Justin Phlegar
 * 
 */
public class PageLoaded {
	private static boolean defaultSyncHandler = true;

	/**
	 * Show what the current sync handler is. True will cause the sync to throw an exception  while false will just have the element sync return a boolean
	 * 
	 * @version 1/14/2016
	 * @author Justin Phlegar
	 * @return boolean
	 */

	public static boolean getSyncToFailTest() {
	    return defaultSyncHandler;
	}

	/**
	 * Set on how to handle element sync failures. True will cause the sync to throw an exception  while false will just have the element sync return a boolean
	 * 
	 * @param defaultSyncHandler True/False 
	 * @version 1/14/2016
	 * @author Justin Phlegar
	 */
	public static void setSyncToFailTest(boolean defaultSyncHandler) {
	    PageLoaded.defaultSyncHandler = defaultSyncHandler;
	}
	
	/**
	 * This waits for a specified element on the page to be found on the page by
	 * the driver Uses the default test time out set by WebDriverSetup
	 * 
	 * @param clazz
	 *            the class calling this method - used so can initialize the
	 *            page class repeatedly
	 * @param oDriver
	 *            The webDriver
	 * @param obj
	 *            The element you are waiting to display on the page
	 * @version 10/16/2014
	 * @author Justin Phlegar
	 * @return False if the element is not found after the timeout, true if is
	 *         found
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isElementLoaded(Class clazz, OrasiDriver oDriver, Element obj) {		
		return isElementLoaded(clazz, oDriver, obj, oDriver.getElementTimeout());
	}

	/**
	 * Overloaded method where you can specify the timeout This waits for a
	 * specified element on the page to be found on the page by the driver
	 * 
	 * 
	 * @param clazz
	 *            the class calling this method - used so can initialize the
	 *            page class repeatedly
	 * @param oDriver
	 *            The webDriver
	 * @param obj
	 *            The element you are waiting to display on the page
	 * @version 12/16/2014
	 * @author Jessica Marshall
	 * @return False if the element is not found after the timeout, true if is
	 *         found
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isElementLoaded(Class clazz, OrasiDriver oDriver, Element obj, int timeout) {
	   	int count = 0;
		int driverTimeout = oDriver.getElementTimeout();
		// set the timeout for looking for an element to 1 second as we are
		// doing a loop and then refreshing the elements
		oDriver.setElementTimeout(1, TimeUnit.MILLISECONDS);

		try {

			while (!obj.elementWired()) {
			    if (count == timeout) {
				break;
			    } else {
				count++;
				initializePage(clazz, oDriver);
			    }
			}
			
		} catch (NullPointerException | NoSuchElementException | StaleElementReferenceException | PageInitialization e){
		    return false;
		} finally{
    			// set the timeout for looking for an element back to the default timeout
    			oDriver.setElementTimeout(driverTimeout, TimeUnit.SECONDS);
		}
		
		if (count < timeout) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This uses the HTML DOM readyState property to wait until a page is
	 * finished loading. It will wait for the ready state to be either
	 * 'interactive' or 'complete'.
	 *
	 * @version 12/16/2014
	 * @author Jessica Marshall
	 * @return False if the element is not found after the timeout, true if is
	 *         found
	 */
	public static boolean isDomInteractive(OrasiDriver oDriver) {
		return isDomInteractive(oDriver, oDriver.getPageTimeout());
	}

	/**
	 * Overloaded method - gives option of specifying a timeout. This uses the
	 * HTML DOM readyState property to wait until a page is finished loading. It
	 * will wait for the ready state to be either 'interactive' or 'complete'.
	 * 
	 * @param oDriver
	 *            The webDriver
	 * @param timeout
	 *            Integer value of number seconds to wait for a page to finish
	 *            loaded before quiting
	 * @version 12/16/2014
	 * @author Jessica Marshall
	 * @return False if the element is not found after the timeout, true if is
	 *         found
	 */
	public static boolean isDomInteractive(OrasiDriver oDriver, int timeout) {
        int count = 0;
        Object obj = null;

        do {
            // this returns a boolean
            obj = oDriver.executeJavaScript(
                    "var result = document.readyState; return (result == 'complete' || result == 'interactive');");
            if (count == timeout)
                break;
            else {
                Sleeper.sleep(500);
                count++;

            }
        } while (obj.equals(false));

        if (count < timeout * 2) {
            return true;
        } else {
            return false;
        }
	}

	/**
	 * This uses protractor method to wait until a page is ready -
	 * notifyWhenNoOutstandingRequests
	 *
	 * @version 10/16/2014
	 * @author Justin Phlegar
	 * 
	 */
	public static void isAngularComplete(OrasiDriver oDriver) {
		try {
            oDriver.executeAsyncJavaScript("var callback = arguments[arguments.length - 1];"
					+ "angular.element(document.body).injector().get('$browser').notifyWhenNoOutstandingRequests(callback);");
		} catch (WebDriverException wde) {
			TestReporter.logFailure(
					"Unable to perform Angular sync. This is most likely because the $browser service is not injected within the Angular Controller. Performing a IsDomComplete instead");
			isDomComplete(oDriver);
		}

	}


	/**
	 * A more strict version of isDomInteractive. This uses the HTML DOM
	 * readyState property to wait until a page is finished loading. It will
	 * wait for the ready state to be 'complete'.
	 * 
	 * @param oDriver
	 *            The webDriver
	 * @version 12/16/2014
	 * @author Jessica Marshall
	 * @return False if the element is not found after the timeout, true if is
	 *         found
	 */
	public static boolean isDomComplete(OrasiDriver oDriver) {
		return isDomComplete(oDriver, oDriver.getPageTimeout());
	}

	/**
	 * Overloaded method - gives option of specifying a timeout. A more strict
	 * version of isDomInteractive This uses the HTML DOM readyState property to
	 * wait until a page is finished loading. It will wait for the ready state
	 * to be 'complete'.
	 * 
	 * @param oDriver
	 *            The webDriver
	 * @param timeout
	 *            Integer value of number seconds to wait for a page to finish
	 *            loaded before quiting
	 * @version 12/16/2014
	 * @author Jessica Marshall
	 * @return False if the element is not found after the timeout, true if is
	 *         found
	 */
	public static boolean isDomComplete(OrasiDriver oDriver, int timeout) {
        int count = 0;
        Object obj = null;
    
            do {
                // this returns a boolean
                obj = oDriver.executeJavaScript("var result = document.readyState; return (result == 'complete');");
                
                if (count == timeout)
                    break;
                else {
                    Sleeper.sleep(500);
                    count++;    
                }
                
            } while (obj.equals(false));
    
            if (count < timeout * 2) {
                return true;
            } else {
                return false;
            }
	}

	/**
	 * @summary Used to create all page objects WebElements as proxies (not
	 *          actual objects, but rather placeholders) or to reinitialize all
	 *          page object WebElements to allow for the state of a page to
	 *          change and not fail a test
	 * @return N/A
	 * @param clazz - page class that is calling this method for which to initialize elements
	 * 
	 * @param oDriver - The webDriver
	 */
	public static void initializePage(Class<?> clazz, OrasiDriver oDriver) {
		try {
			ElementFactory.initElements(oDriver, clazz.getConstructor(TestEnvironment.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new PageInitialization("Unable to initialize page", oDriver);
		}
	}

	/**
	 * Used in conjunction with WebElementPresent to determine if the desired
	 * element is present in the DOM Will loop for the time out listed in
	 * OrasiDriver.getElementTimeout If object is not present within the time, throw
	 * an error
	 * 
	 * @author Justin
	 */
	public static boolean syncPresent(OrasiDriver driver, Element element) {
		return syncPresent(driver, driver.getElementTimeout(),getSyncToFailTest(), element);
	}

	/**
	 * Used in conjunction with WebElementPresent to determine if the desired
	 * element is present in the DOM Will loop for the time out passed in
	 * parameter timeout If object is not present within the time, throw an
	 * error
	 * 
	 * @author Justin
	 */
	public static boolean syncPresent(OrasiDriver driver, int timeout, Element element) {
		return syncPresent(driver, timeout, getSyncToFailTest(), element);
	}

	/**
	 * Used in conjunction with WebElementPresent to determine if the desired
	 * element is present in the DOM Will loop for the time out passed in
	 * parameter timeout If object is not present within the time, handle error
	 * based on returnError
	 * 
	 * @author Justin
	 */
	public static boolean syncPresent(OrasiDriver driver, int timeout, boolean returnError, Element element) {
		boolean found = false;
		long timeLapse;
		StopWatch stopwatch = new StopWatch();
		By locator = element.getElementLocator();
		TestReporter.interfaceLog("<i> Syncing to element [ <b>@FindBy: " + element.getElementLocatorInfo()
				+ "</b> ] to be <b>PRESENT</b> in DOM within [ " + timeout + " ] seconds.</i>");
		int currentTimeout = driver.getElementTimeout();
		driver.setElementTimeout(1, TimeUnit.MILLISECONDS);

		stopwatch.start();
		do {
			if (webElementPresent(driver, locator)) {
				found = true;
				break;
			}

		} while (stopwatch.getTime() / 1000.0 < (long) timeout);
		stopwatch.stop();
		timeLapse = stopwatch.getTime();
		stopwatch.reset();

		driver.setElementTimeout(currentTimeout);

		if (!found && returnError) {
			TestReporter.interfaceLog("<i>Element [<b>@FindBy: " + element.getElementLocatorInfo()
					+ " </b>] is not <b>PRESENT</b> on the page after [ "
					+ (timeLapse) / 1000.0 + " ] seconds.</i>");
			throw new RuntimeException(
					"Element [ @FindBy: " + element.getElementLocatorInfo() + " ] is not PRESENT on the page after [ "
							+ (timeLapse) / 1000.0 + " ] seconds.");
		}
		return found;
	}

	/**
	 *
	 * Used in conjunction with WebObjectVisible to determine if the desired
	 * element is visible on the screen Will loop for the time out listed in
	 * org.orasi.chameleon.CONSTANT.TIMEOUT If object is not visible within the
	 * time, throw an error
	 * 
	 * @author Justin
	 */
	public static boolean syncVisible(OrasiDriver driver, Element element) {
		return syncVisible(driver, driver.getElementTimeout(),getSyncToFailTest(),  element);
	}

	/**
	 * Used in conjunction with WebObjectVisible to determine if the desired
	 * element is visible on the screen Will loop for the time out passed in the
	 * variable timeout If object is not visible within the time, throw an error
	 * 
	 * @author Justin
	 * 
	 */
	public static boolean syncVisible(OrasiDriver driver, int timeout, Element element) {
		return syncVisible(driver, timeout, getSyncToFailTest(), element);
	}

	/**
	 * Used in conjunction with WebObjectVisible to determine if the desired
	 * element is visible on the screen Will loop for the time out passed in the
	 * variable timeout If object is not visible within the time, handle the
	 * error based on the boolean
	 *
	 * @author Justin
	 *
	 */
	public static boolean syncVisible(OrasiDriver driver, int timeout, boolean returnError, Element element) {
		boolean found = false;
		long timeLapse;
		StopWatch stopwatch = new StopWatch();
		TestReporter.interfaceLog("<i>Syncing to element [<b>@FindBy: " + element.getElementLocatorInfo()
				+ "</b> ] to be <b>VISIBLE<b> within [ " + timeout + " ] seconds.</i>");
		int currentTimeout = driver.getElementTimeout();
		driver.setElementTimeout(1, TimeUnit.MILLISECONDS);

		stopwatch.start();
		do {
			if (webElementVisible(driver, element)) {
				found = true;
				break;
			}

		} while (stopwatch.getTime() / 1000.0 < (long) timeout);
		stopwatch.stop();
		timeLapse = stopwatch.getTime();
		stopwatch.reset();

		driver.setElementTimeout(currentTimeout, TimeUnit.SECONDS);
		if (!found && returnError) {
			TestReporter.interfaceLog("<i>Element [<b>@FindBy: " + element.getElementLocatorInfo()
					+ " </b>] is not <b>VISIBLE</b> on the page after [ "
					+ (timeLapse) / 1000.0 + " ] seconds.</i>");
			throw new RuntimeException(
					"Element [ @FindBy: " + element.getElementLocatorInfo() + " ] is not VISIBLE on the page after [ "
							+ (timeLapse) / 1000.0 + " ] seconds.");
		}
		return found;
	}

	/**
	 * Used in conjunction with WebObjectVisible to determine if the desired
	 * element is hidden from the screen Will loop for the time out listed in
	 * org.orasi.chameleon.CONSTANT.TIMEOUT If object is not visible within the
	 * time, throw an error
	 * 
	 * @author Justin
	 */
	public static boolean syncHidden(OrasiDriver driver, Element element) {
		return syncHidden(driver, driver.getElementTimeout(),getSyncToFailTest(),  element);
	}

	/**
	 * Used in conjunction with WebObjectVisible to determine if the desired
	 * element is hidden from the screen Will loop for the time out listed in
	 * org.orasi.chameleon.CONSTANT.TIMEOUT If object is not visible within the
	 * time, throw an error
	 * 
	 * @author Justin
	 */
	public static boolean syncHidden(OrasiDriver driver, int timeout, Element element) {
		return syncHidden(driver, timeout, getSyncToFailTest(), element);
	}

	/**
	 * Used in conjunction with WebObjectVisible to determine if the desired
	 * element is visible on the screen Will loop for the time out passed in the
	 * variable timeout If object is not visible within the time, handle the
	 * error based on the boolean
	 * 
	 * @author Justin
	 */
	public static boolean syncHidden(OrasiDriver driver, int timeout, boolean returnError, Element element) {
		boolean found = false;
		long timeLapse;
		StopWatch stopwatch = new StopWatch();
		TestReporter.interfaceLog("<i>Syncing to element [<b>@FindBy: " + element.getElementLocatorInfo()
				+ "</b> ] to be <b>HIDDEN</b> within [ <b>" + timeout + "</b> ] seconds.</i>");
		int currentTimeout = driver.getElementTimeout();
		driver.setElementTimeout(1, TimeUnit.MILLISECONDS);
		stopwatch.start();
		do {
			if (!webElementVisible(driver, element)) {
				found = true;
				break;
			}
		} while (stopwatch.getTime() / 1000.0 < (long) timeout);
		stopwatch.stop();
		timeLapse = stopwatch.getTime();
		stopwatch.reset();

		driver.setElementTimeout(currentTimeout, TimeUnit.SECONDS);
		if (!found && returnError) {
			TestReporter.interfaceLog("<i>Element [<b>@FindBy: " + element.getElementLocatorInfo()
					+ " </b>] is not <b>HIDDEN</b> on the page after [ "
					+ (timeLapse) / 1000.0 + " ] seconds.</i>");
			throw new RuntimeException(
					"Element [ @FindBy: " + element.getElementLocatorInfo() + " ] is not HIDDEN on the page after [ "
							+ (timeLapse) / 1000.0 + " ] seconds.");
		}
		return found;
	}

	/**
	 *
	 * Used in conjunction with WebObjectEnabled to determine if the desired
	 * element is enabled on the screen Will loop for the time out listed in
	 * org.orasi.chameleon.CONSTANT.TIMEOUT If object is not enabled within the
	 * time, throw an error
	 * 
	 * @author Justin
	 */
	public static boolean syncEnabled(OrasiDriver driver, Element element) {
		return syncEnabled(driver, driver.getElementTimeout(),getSyncToFailTest(),  element);
	}

	/**
	 * 
	 * Used in conjunction with WebObjectEnabled to determine if the desired
	 * element is enabled on the screen Will loop for the time out passed in the
	 * variable timeout If object is not enabled within the time, throw an error
	 * 
	 * @author Justin
	 * 
	 */
	public static boolean syncEnabled(OrasiDriver driver, int timeout, Element element) {
		return syncEnabled(driver, timeout, getSyncToFailTest(), element);
	}

	/**
	 * Used in conjunction with WebObjectEnabled to determine if the desired
	 * element is enabled on the screen Will loop for the time out passed in the
	 * variable timeout If object is not enabled within the time, handle the
	 * error based on the boolean
	 *
	 * @author Justin
	 *
	 */
	public static boolean syncEnabled(OrasiDriver driver, int timeout, boolean returnError, Element element) {
		boolean found = false;
		long timeLapse;
		StopWatch stopwatch = new StopWatch();
		TestReporter.interfaceLog("<i>Syncing to element [<b>@FindBy: " + element.getElementLocatorInfo()
				+ "</b> ] to be <b>ENABLED</b> within [ <b>" + timeout + "</b> ] seconds.</i>");
		int currentTimeout = driver.getElementTimeout();
		driver.setElementTimeout(1, TimeUnit.MILLISECONDS);

		stopwatch.start();
		do {
			if (webElementEnabled(driver, element)) {
				found = true;
				break;
			}

		} while (stopwatch.getTime() / 1000.0 < (long) timeout);
		stopwatch.stop();
		timeLapse = stopwatch.getTime();
		stopwatch.reset();

		driver.setElementTimeout(currentTimeout, TimeUnit.SECONDS);
		if (!found && returnError) {
			TestReporter.interfaceLog("<i>Element [<b>@FindBy: " + element.getElementLocatorInfo()
					+ " </b>] is not <b>ENABLED</b> on the page after [ "
					+ (timeLapse) / 1000.0 + " ] seconds.</i>");
			throw new RuntimeException(
					"Element [ @FindBy: " + element.getElementLocatorInfo() + " ] is not ENABLED on the page after [ "
							+ (timeLapse) / 1000.0 + " ] seconds.");
		}
		return found;
	}

	/**
	 *
	 * Used in conjunction with WebObjectEnabled to determine if the desired
	 * element is disabled on the screen Will loop for the time out listed in
	 * org.orasi.chameleon.CONSTANT.TIMEOUT If object is not disabled within the
	 * time, throw an error
	 * 
	 * @author Justin
	 */
	public static boolean syncDisabled(OrasiDriver driver, Element element) {
		return syncDisabled(driver, driver.getElementTimeout(), getSyncToFailTest(),  element);
	}

	/**
	 * 
	 * Used in conjunction with WebObjectDisabled to determine if the desired
	 * element is disabled on the screen Will loop for the time out passed in
	 * the variable timeout If object is not disabled within the time, throw an
	 * error
	 * 
	 * @author Justin
	 * 
	 */
	public static boolean syncDisabled(OrasiDriver driver, int timeout, Element element) {
		return syncDisabled(driver, timeout, getSyncToFailTest(), element);
	}

	/**
	 * Used in conjunction with WebObjectDisabled to determine if the desired
	 * element is disabled on the screen Will loop for the time out passed in
	 * the variable timeout If object is not disabled within the time, handle
	 * the error based on the boolean
	 *
	 * @author Justin
	 *
	 */
	public static boolean syncDisabled(OrasiDriver driver, int timeout, boolean returnError, Element element) {
		boolean found = false;
		long timeLapse;
		StopWatch stopwatch = new StopWatch();
		TestReporter.interfaceLog("<i>Syncing to element [<b>@FindBy: " + element.getElementLocatorInfo()
				+ "</b> ] to be <b>DISABLED</b> within [ <b>" + timeout + "</b> ] seconds.</i>");
		int currentTimeout = driver.getElementTimeout();
		driver.setElementTimeout(1, TimeUnit.MILLISECONDS);

		stopwatch.start();
		do {
			if (!webElementEnabled(driver, element)) {
				found = true;
				break;
			}
		} while (stopwatch.getTime() / 1000.0 < (long) timeout);
		stopwatch.stop();
		timeLapse = stopwatch.getTime();
		stopwatch.reset();

		driver.setElementTimeout(currentTimeout, TimeUnit.SECONDS);
		if (!found && returnError) {
			TestReporter.interfaceLog("<i>Element [<b>@FindBy: " + element.getElementLocatorInfo()
					+ " </b>] is not <b>DISABLED</b> on the page after [ "
					+ (timeLapse) / 1000.0 + " ] seconds.</i>");
			throw new RuntimeException(
					"Element [ @FindBy: " + element.getElementLocatorInfo() + " ] is not DISABLED on the page after [ "
							+ (timeLapse) / 1000.0 + " ] seconds.");
		}
		return found;
	}

	/**
	 *
	 * Used in conjunction with WebObjectText Present to determine if the
	 * desired text is present in the desired element Will loop for the time out
	 * listed in org.orasi.chameleon.CONSTANT.TIMEOUT If text is not present
	 * within the time, throw an error
	 * 
	 * @author Justin
	 */
	public static boolean syncTextInElement(OrasiDriver driver, String text, Element element) {
		return syncTextInElement(driver, text, driver.getElementTimeout(), getSyncToFailTest(), element);
	}

	/**
	 * 
	 * Used in conjunction with WebObjectText Present to determine if the
	 * desired text is present in the desired element Will loop for the time out
	 * passed in the variable timeout If text is not present within the time,
	 * throw an error
	 * 
	 * @author Justin
	 * 
	 */
	public static boolean syncTextInElement(OrasiDriver driver, String text, int timeout, Element element) {
		return syncTextInElement(driver, text, timeout, getSyncToFailTest(), element);
	}

	/**
	 * Used in conjunction with WebObjectText Present to determine if the
	 * desired text is present in the desired element Will loop for the time out
	 * passed in the variable timeout If text is not present within the time,
	 * handle the error based on the boolean
	 *
	 * @author Justin
	 *
	 */
	public static boolean syncTextInElement(OrasiDriver driver, String text, int timeout, boolean returnError, Element element) {
		boolean found = false;
		long timeLapse;
		StopWatch stopwatch = new StopWatch();
		TestReporter.interfaceLog("<i>Syncing to text [<b>" + text + "</b> ] in element [<b>@FindBy: "
				+ element.getElementLocatorInfo() + "</b> ] to be displayed within [ <b>" + timeout + "</b> ] seconds.</i>");
		int currentTimeout = driver.getElementTimeout();
		driver.setElementTimeout(1, TimeUnit.MILLISECONDS);
		stopwatch.start();
		do {
			if (webElementTextPresent(driver, element, text)) {
				found = true;
				break;
			}

		} while (stopwatch.getTime() / 1000.0 < (long) timeout);
		stopwatch.stop();
		timeLapse = stopwatch.getTime();
		stopwatch.reset();

		driver.setElementTimeout(currentTimeout, TimeUnit.SECONDS);
		if (!found && returnError) {
			TestReporter.interfaceLog(
					"<i>Element [<b>@FindBy: " + element.getElementLocatorInfo() + " </b>] did not contain the text [ " + text
							+ " ] after [ " + (timeLapse) / 1000.0 + " ] seconds.</i>");
			throw new RuntimeException(
					"Element [ @FindBy: " + element.getElementLocatorInfo() + " ] did not contain the text [ " + text
							+ " ] after [ " + (timeLapse) / 1000.0 + " ] seconds.");
		}
		return found;
	}

	/**
	 * Use WebDriver Wait to determine if object is present in the DOM or not
	 * 
	 * @author Justin
	 * @param driver
	 *            Main WebDriver
	 * @param locator
	 *            {@link By} object to search for
	 * @return TRUE if element is currently present in the DOM, FALSE if the
	 *         element is not present in the DOM
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean webElementPresent(OrasiDriver driver, By locator) {
		Wait wait = new WebDriverWait(driver, 0);

		try {
			return wait.until(ExpectedConditions.presenceOfElementLocated(locator)) != null;
		} catch (NoSuchElementException | ClassCastException | StaleElementReferenceException | TimeoutException e) {
			return false;
		}

	}

	/**
	 * Use WebDriver Wait to determine if object is visible on the screen or not
	 * 
	 * @author Justin
	 * @param driver
	 *            Main WebDriver
	 * @param element
	 *            Element to search for
	 * @return TRUE if element is currently visible on the screen, FALSE if the
	 *         element is not visible on the screen
	 */
	private static boolean webElementVisible(OrasiDriver driver, WebElement element) {
		try {
			Point location = element.getLocation();

			Dimension size = element.getSize();
			if ((location.getX() > 0 & location.getY() > 0) | (size.getHeight() > 0 & size.getWidth() > 0)) {
				if (element.getAttribute("hidden") != null)
                    if (element.getAttribute("hidden").toLowerCase().equals("true")) return false;
				if (element.getAttribute("type") != null) {
					if (element.getAttribute("type").equals("hidden"))
						return false;
				}
				return true;
			} else {
				return false;
			}

		} catch (WebDriverException | ClassCastException e) {
			return false;
		}
	}

	/**
	 * Use WebDriver Wait to determine if object is enabled on the screen or not
	 * 
	 * @author Justin
	 * @param driver
	 *            Main WebDriver
	 * @param element
	 *            Element to search for
	 * @return TRUE if element is currently enabled on the screen, FALSE if the
	 *         element is not enabled on the screen
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean webElementEnabled(OrasiDriver driver, WebElement element) {
		Wait wait = new WebDriverWait(driver, 0);

		try {
			return wait.until(ExpectedConditions.elementToBeClickable(element)) != null;
			// return element.isEnabled();
		} catch (NoSuchElementException | ClassCastException | StaleElementReferenceException | TimeoutException e) {
			return false;
		}

	}

	/**
	 * Use WebDriver Wait to determine if object contains the expected text
	 * 
	 * @author Justin
	 * @param driver
	 *            Main WebDriver
	 * @param element
	 *            Element to search for
	 * @param text
	 *            The text to search for
	 * @return TRUE if element is currently visible on the screen, FALSE if the
	 *         element is not visible on the screen
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean webElementTextPresent(OrasiDriver driver, WebElement element, String text) {
		Wait wait = new WebDriverWait(driver, 0);
		try {

			if (wait.until(ExpectedConditions.textToBePresentInElement(element, text)) != null) return true;
			else if (wait.until(ExpectedConditions.textToBePresentInElementValue(element, text)) != null) {
				return true;
			} else {
				return false;
			}

		} catch (NoSuchElementException | ClassCastException | StaleElementReferenceException | TimeoutException e) {
			try {
				if (wait.until(ExpectedConditions.textToBePresentInElementValue(element, text)) != null) {
					return true;
				} else {
					return false;
				}
			} catch (NoSuchElementException | ClassCastException | StaleElementReferenceException
					| TimeoutException e2) {
				return false;
			} catch (WebDriverException wde) {
				return false;
			}

		}
	}	
}
