package capm;


import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.interactions.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;


public class Navigation {

	private static final Logger log = LogManager.getLogger("Navigation");
	WebDriver driver;
	Wait<WebDriver> wait;
	
	public Navigation (WebDriver driver) {

		this.driver = driver;
		wait = new WebDriverWait(driver,60, 200).withMessage("ExpectedConditions timeout.");
	}

	protected WebElement getWebElement (String xpath) throws InterruptedException {
		int timeout = 1000*40;
		int waiting = 0;
		
		while (driver.findElements(By.xpath(xpath)).size()<1) {
			Thread.sleep(500);
			waiting+=500;
			if (waiting>=timeout)
				break;
		}
		
		if (waiting>=timeout) {
			log.fatal("getWebElement: Timed out waiting for element "+xpath);
			return null;
		} else {
			log.debug("getWebElement: Succesful on "+xpath);
			return driver.findElement(By.xpath(xpath));
		}

	}

	protected Boolean waitForElement (String xpath, int type) throws InterruptedException {
		// int type
		// "1" = Wait for element which should BE PRESENT
		// "2" = Wait for element which should BE ABSENT
		
		int timeout = 1000*40;
		int waiting = 0;
		
		if (type==1) {
			while (driver.findElements(By.xpath(xpath)).size()<1) {
				Thread.sleep(500);
				waiting+=500;
				if (waiting>=timeout)
					break;
			}
		}
		
		if (type==2) {
			while (driver.findElements(By.xpath(xpath)).size()>0) {
				Thread.sleep(500);
				waiting+=500;
				if (waiting>=timeout)
					break;
			}
		}
		
		if (waiting>=timeout) {
			log.fatal("waitForElement: Timed out waiting for element "+xpath);
			return false;
		} else {
			log.debug("waitForElement: Successful on "+xpath);
			return true;
		}

	}

	protected Boolean protectedClick (String xpath, String errorMessage) throws InterruptedException {

		int maxNumberToTry=15;
		Boolean isClicked = false;
		int tryNum=0;

		wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
		Thread.sleep(200);

		do {
			try {
				isClicked=true;
				WebElement elementToClick = driver.findElement(By.xpath(xpath));
				elementToClick.click();
			} catch (Exception e) {
				isClicked=false;
				tryNum++;
				Thread.sleep(1000);
				log.warn(errorMessage);
			}
		} while (!isClicked && tryNum<maxNumberToTry);

		if (tryNum==maxNumberToTry) {
			log.fatal("protectedClick: Failed on "+xpath);
			return false;
		} else {
			log.debug("protectedClick: Successful on "+xpath);
			return true;
		}

	}

	protected Boolean protectedDoubleClick (String xpath, String errorMessage) throws InterruptedException {

		int maxNumberToTry=15;
		Boolean isClicked = false;
		int tryNum=0;
		Actions action = new Actions (driver);

		do {
			try {
				isClicked=true;
				doubleClickOnXpath(xpath);
			} catch (Exception e) {
				isClicked=false;
				tryNum++;
				Thread.sleep(1000);
				log.warn(errorMessage);
			}
		} while (!isClicked && tryNum<maxNumberToTry);

		if (tryNum==maxNumberToTry) {
			log.fatal("protectedDoubleClick: Failed on "+xpath);
			return false;
		} else {
			log.debug("protectedDoubleClick: Successful on "+xpath);
			return true;
		}

	}

	public Boolean selectDataSources () throws InterruptedException {

		int tryNum=0;
		int maxNumberToTry=15;
		Thread.sleep(4000);

		boolean isClicked;
		do {
			try {
				isClicked=true;
				driver.findElement(By.xpath("//button[text()='Administration']")).click();
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[starts-with(text(),'Data Sources')]")));
				driver.findElement(By.xpath("//a[starts-with(text(),'Data Sources')]")).click();
			} catch (Exception e) {
				isClicked=false;
				tryNum++;
				Thread.sleep(500);
				log.warn("Unable to click on Data Sources. Retrying.");
			}
		} while (!isClicked && tryNum<maxNumberToTry);

		if (tryNum==maxNumberToTry) {
			log.fatal("Unable to click on Data Sources.");
			return false;
		}

		//wait while page is loading (by checking 'All Data Aggregators' link is appears)
		waitForElement("//div[contains(text(), 'EventManager')]", 1);

		return true;

	}

	public Boolean selectDataAggregator() throws InterruptedException {

		Thread.sleep(1000);
		int tryNum=0;
		int maxNumberToTry=15;
		Thread.sleep(3000);
		boolean isClicked;
		do {
			try {
				isClicked=true;
				Actions action = new Actions(driver);

				//New CAPC Navigation bar logic
				if (driver.findElements(By.xpath("//button[text()='Administration']")).size()>0) {
//					WebElement admintab = driver.findElement(By.xpath("//button[text()='Administration']"));
//					action.moveToElement(admintab).perform();
//					Thread.sleep(500);
//					WebElement dataSourcesMenu = driver.findElement(By.xpath("//span[text()='Data Sources']"));
//					action.moveToElement(dataSourcesMenu).perform();
//					Thread.sleep(500);
//					WebElement dalink = driver.findElement(By.xpath(("//span[contains(text(),'Data Aggregator@')]")));
//					dalink.click();

					driver.findElement(By.xpath("//button[text()='Administration']")).click();
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[starts-with(text(),'Data Aggregator@')]")));
					driver.findElement(By.xpath("//a[starts-with(text(),'Data Aggregator@')]")).click();


				} else {
					//Old CAPC Navigation bar logic
					WebElement admintab = driver.findElement(By.xpath("//h2[contains(.,'Administration')]"));
					action.moveToElement(admintab).perform();
					Thread.sleep(500);
					WebElement dalink = driver.findElement(By.xpath(("//a[contains(text(),'Data Aggregator@')]")));

					Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
					String browserName = cap.getBrowserName().toLowerCase();
					if (browserName.equals("chrome")) {
						//Alternative click
						JavascriptExecutor executor = (JavascriptExecutor)driver;
						executor.executeScript("arguments[0].click();", dalink);
					} else {
						dalink.click();
					}
				}
			    
			} catch (Exception e) {
				isClicked=false;
				tryNum++;
				Thread.sleep(500);
				log.warn("Unable to click on Data Aggregator. Retrying.");
			}
		} while (!isClicked && tryNum<maxNumberToTry);

		if (tryNum==maxNumberToTry) {
			log.fatal("Unable to click on Data Aggregator.");
			return false;
		}

		//wait while page is loading (by checking 'All Data Aggregators' link is appears)
		waitForElement("//span[text()='All Data Aggregators']", 1);
		
		return true;
	}
	
	public Navigation gotoMonitoringProfiles() throws InterruptedException {
		
		WebElement monitoringConfiguration = getWebElement("//span[text()='Monitoring Configuration']");
		monitoringConfiguration.click();
		Thread.sleep(500);

		log.debug("Move mouse from Monitoring Configuration link to avoid popup text.");
		Actions builder = new Actions(driver);
		builder.moveToElement(monitoringConfiguration,300,300).build().perform();
		Thread.sleep(500);

		WebElement monitoringProfiles = getWebElement("//span[text()='Monitoring Profiles']");
		monitoringProfiles.click();
		Thread.sleep(500);
		
		return this;
	}


	public Navigation gotoDataCollector() throws InterruptedException {

		//Check if "System Status" tab is collapsed (not expanded)
		if (driver.findElements(By.xpath("//span[text()='System Status']/../../parent::div[contains(@class,'x-panel-collapsed')]")).size()>0) {
			driver.findElement(By.xpath("//span[text()='System Status']")).click();
			Thread.sleep(500);
		}

		//Click on Data Collector link
		driver.findElement(By.xpath("//span[text()='Data Collectors']")).click();
		Thread.sleep(500);

		while (driver.findElements(By.xpath("//div[text()='Loading...']")).size()>0)
			Thread.sleep(500);

		return this;
	}

	public Navigation gotoDiscoveryProfiles() throws InterruptedException {
		
		WebElement discoveryProfiles = getWebElement("//span[text()='Discovery Profiles']");
		discoveryProfiles.click();
		
		return this;
	}
	
	public Boolean gotoVendorCertifications() throws InterruptedException {

		//Check if "Monitoring Configuration" tab is collapsed (not expanded)
		// Alternate ->> //span[text()='Monitoring Configuration']/../../../../div[2][contains(@class,'x-panel-collapsed')]

		//while (driver.findElements(By.xpath("//span[text()='Monitoring Configuration']/../../parent::div[contains(@class,'x-panel-collapsed')]")).size()>0) {
		while (driver.findElements(By.xpath("//div[contains(@class,'leftHandMenu x-box-item')]/div[2][contains(@class,'x-panel-collapsed')]")).size()>0) {
			WebElement monitoringConfiguration = getWebElement("//span[text()='Monitoring Configuration']");
			monitoringConfiguration.click();
			Thread.sleep(500);
		}

		WebElement vendorCertifications = getWebElement("//span[text()='Vendor Certifications']");

		//NOTE: Standart click not working properly in RemoteWebDriver in this point. Use Actions click instead.
		//vendorCertifications.click();
		Actions act = new Actions(driver);
		act.click(vendorCertifications).build().perform();

		//Wait while Vendor Cert List will be loaded
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[text()='Factory']")));
//		wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("//div[text()='Factory']"),1));
		
		return true;
	}
	
	
	public String gotoMonitoredDevice(String deviceName) throws InterruptedException {

		if (!protectedClick("//span[contains(text(),'All Devices')]/../../img[1]", "Unable to click on 'All Devices' expand list. Retrying."))
			return null;

		//Wait while device list will be loaded and expanded 
		WebElement selectDevice = getWebElement("//span[starts-with (text(),'DataAggregator')]");

		if (!protectedClick("//span[starts-with (text(),'"+deviceName+"')]", "Unable to click on device '"+deviceName+"'. Retrying."))
			return null;

		//Wait while Details page is loaded
		waitForElement("//label[text()='Name:']/../div[1]/div/a", 1);
		
		//Get "System Description"
		String sysDescr = driver.findElement (By.xpath("//label[text()='System Description:']/../div[1]/div")).getText();
						
		WebElement polledMetricFamilies = driver.findElement(By.xpath("//span[text()='Polled Metric Families']"));
		polledMetricFamilies.click();
			
		return sysDescr;
	}
	
	
	public Boolean gotoAllDevices() throws InterruptedException {

		if (!protectedDoubleClick("//span[contains(.,'All Devices')]", "Unable to click on 'All Devices' expand list. Retrying."))
			return false;

		//Wait while device list will be loaded and expanded 
		WebElement selectDevice = getWebElement("//li[2][@class='x-tree-node']/ul[@class='x-tree-node-ct']/li");
		
		return true;
	}
	
	
	public Navigation selectDeviceComponents() throws InterruptedException {

		Actions action = new Actions(driver);
		//New CAPC Navigation bar logic
		if (driver.findElements(By.xpath("//button[text()='Inventory']")).size()>0) {
			log.debug("Using new CAPC navigation logic.");
//			WebElement admintab = driver.findElement(By.xpath("//button[text()='Inventory']"));
//			action.moveToElement(admintab).perform();
//			WebElement device_components = driver.findElement(By.xpath("//span[text()='Device Components']"));
//			device_components.click();
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Inventory']")));
			driver.findElement(By.xpath("//button[text()='Inventory']")).click();
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='Inventory']")));
			driver.findElement(By.xpath("//a[text()='Device Components ']")).click();
		} else {
			//Old CAPC Navigation bar logic
			log.debug("Using old CAPC navigation logic.");
			WebElement inventory = driver.findElement(By.xpath("//h2[contains(.,'Inventory')]"));
			//inventory.click();
			action.moveToElement(inventory).perform();

			WebElement device_components = driver.findElement(By.linkText("Device Components"));
			device_components.click();
		}

		Thread.sleep(200);
		waitForElement("//div[contains(text(),'Loading')]", 2);
		return this;
	}
	
	
	public Navigation selectInterfaces() throws InterruptedException {

		Actions action = new Actions(driver);
		//New CAPC Navigation bar logic
		if (driver.findElements(By.xpath("//button[text()='Inventory']")).size()>0) {
			log.debug("Using new CAPC navigation logic.");
//			WebElement admintab = driver.findElement(By.xpath("//button[text()='Inventory']"));
//			action.moveToElement(admintab).perform();
//			WebElement device_components = driver.findElement(By.xpath("//span[text()='Interfaces']"));
//			device_components.click();
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Inventory']")));
			driver.findElement(By.xpath("//button[text()='Inventory']")).click();
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='Inventory']")));
			driver.findElement(By.xpath("//a[text()='Interfaces ']")).click();
		} else {
			//Old CAPC Navigation bar logic
			log.debug("Using old CAPC navigation logic.");
			WebElement inventory = driver.findElement(By.xpath("//h2[contains(.,'Inventory')]"));
			//inventory.click();
			action.moveToElement(inventory).perform();

			WebElement device_components = driver.findElement(By.linkText("Interfaces"));
			device_components.click();
		}

		Thread.sleep(200);
		waitForElement("//div[contains(text(),'Loading')]", 2);

		return this;
	}
	
	
	public Navigation selectVirtualInterfaces() throws InterruptedException {

		Actions action = new Actions(driver);
		//New CAPC Navigation bar logic
		if (driver.findElements(By.xpath("//button[text()='Inventory']")).size()>0) {
			log.debug("Using new CAPC navigation logic.");
//			WebElement admintab = driver.findElement(By.xpath("//button[text()='Inventory']"));
//			action.moveToElement(admintab).perform();
//			WebElement device_components = driver.findElement(By.xpath("//span[text()='Virtual Interfaces']"));
//			device_components.click();
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Inventory']")));
			driver.findElement(By.xpath("//button[text()='Inventory']")).click();
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='Inventory']")));
			driver.findElement(By.xpath("//a[text()='Virtual Interfaces ']")).click();
		} else {
			//Old CAPC Navigation bar logic
			log.debug("Using old CAPC navigation logic.");
			WebElement inventory = driver.findElement(By.xpath("//h2[contains(.,'Inventory')]"));
			//inventory.click();
			action.moveToElement(inventory).perform();

			WebElement device_components = driver.findElement(By.linkText("Virtual Interfaces"));
			device_components.click();
		}

		Thread.sleep(200);
		waitForElement("//div[contains(text(),'Loading')]", 2);

		return this;
	}
	
	
	public Navigation selectDevices() throws InterruptedException {

		Actions action = new Actions(driver);
		//New CAPC Navigation bar logic
		if (driver.findElements(By.xpath("//button[text()='Inventory']")).size()>0) {
			log.debug("Using new CAPC navigation logic.");
//			WebElement admintab = driver.findElement(By.xpath("//button[text()='Inventory']"));
//			action.moveToElement(admintab).perform();
//			WebElement device_components = driver.findElement(By.xpath("//span[text()='Devices']"));
//			device_components.click();
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Inventory']")));
			driver.findElement(By.xpath("//button[text()='Inventory']")).click();
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='Inventory']")));
			driver.findElement(By.xpath("//a[text()='Devices ']")).click();
		} else {
			log.debug("Using old CAPC navigation logic.");
			WebElement inventory = driver.findElement(By.xpath("//h2[contains(.,'Inventory')]"));
			//inventory.click();

			action.moveToElement(inventory).perform();
			WebElement device_components = driver.findElement(By.linkText("Devices"));
			device_components.click();
		}
		
		return this;
	}
	
	public boolean selectElement (String element, String deviceName) throws InterruptedException {
		
		
		WebElement findField = getWebElement("//div[@class=' filterCompoundElement']/input");
		findField.sendKeys(element);
		findField.sendKeys(Keys.ENTER);
		Thread.sleep(1000);
		
		while (driver.findElements(By.xpath("//div[contains(text(),'Loading')]")).size()>0)
			Thread.sleep(1000);
		
		//List<WebElement> clickOnElement = driver.findElements(By.linkText(element));
		List<WebElement> clickOnElement = driver.findElements(By.xpath("//tr/td[2]/div/a[text()='"+element+"']"));
						
		if (clickOnElement.size() > 0) {
			//Check that element belongs to an appropriate Device
			for (int i=0; i<clickOnElement.size(); i++) {
				if (clickOnElement.get(i).findElement(By.xpath("..//..//..//td[5]/div")).getText().startsWith(deviceName)) {
					clickOnElement.get(i).click();
					break;
				}
			}
			//clickOnElement.get(0).click();			
		} else return false;
		
		return true;
	}
	
	
	public boolean selectInterfaceElement (String element, String deviceName) throws InterruptedException {
		
		
		WebElement findField = getWebElement("//td[2]/div/input[@type='text']");
		findField.sendKeys(element);
		findField.sendKeys(Keys.ENTER);
		Thread.sleep(1000);
		
		while (driver.findElements(By.xpath("//div[text()='Loading...']")).size()>0)
			Thread.sleep(500);
		
		//List<WebElement> clickOnElement = driver.findElements(By.linkText(element));
		List<WebElement> clickOnElement = driver.findElements(By.xpath("//tr/td[2]/div/a[text()='"+element+"']"));
						
		if (clickOnElement.size() > 0) {
			//Check that element belongs to an appropriate Device
			for (int i=0; i<clickOnElement.size(); i++) {
				if (clickOnElement.get(i).findElement(By.xpath("..//..//..//td[5]/div")).getText().startsWith(deviceName)) {
					clickOnElement.get(i).click();
					break;
				}
			}
			//clickOnElement.get(0).click();			
		} else return false;
		
		return true;
	}


	public boolean doubleClickOnXpath (String xpath) throws InterruptedException {

		Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
		String browserName = cap.getBrowserName().toLowerCase();

		if (browserName.equals("firefox")) {
			log.debug("Firefox browser detected. Performing a javascript doubleclick.");
			scrollToWebElement(driver.findElement(By.xpath(xpath)));
			driver.findElement(By.xpath(xpath)).click();
			((JavascriptExecutor)driver).executeScript("var evt = document.createEvent('MouseEvents');" + "evt.initMouseEvent('dblclick',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);" + "arguments[0].dispatchEvent(evt);",driver.findElement(By.xpath(xpath)));

//			String doubleClickJS = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('dblclick',"+
//			"true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject)"+
//			"{ arguments[0].fireEvent('ondblclick');}";
//			JavascriptExecutor js = (JavascriptExecutor) driver;
//			js.executeScript(doubleClickJS, driver.findElement(By.xpath(xpath)));

		} else {
			log.debug("Non-Firefox browser detected. Performing an Actions doubleclick.");
			Actions action = new Actions (driver);
			WebElement elementToClick = driver.findElement(By.xpath(xpath));
			action.doubleClick(elementToClick).build().perform();
		}
		return true;
	}



	public boolean doubleClickOnWebElement (WebElement element) throws InterruptedException {

		Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
		String browserName = cap.getBrowserName().toLowerCase();

		if (browserName.equals("firefox")) {
			log.debug("Firefox browser detected. Performing a javascript doubleclick.");
			scrollToWebElement(element);
			element.click();
			((JavascriptExecutor)driver).executeScript("var evt = document.createEvent('MouseEvents');" + "evt.initMouseEvent('dblclick',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);" + "arguments[0].dispatchEvent(evt);",element);
		} else {
			log.debug("Non-Firefox browser detected. Performing an Actions doubleclick.");
			Actions action = new Actions (driver);
			action.doubleClick(element).build().perform();
		}
		return true;
	}



	public boolean scrollToWebElement (WebElement element) {
		log.debug("Scroll to WebElement "+ element);
		Coordinates coordinate = ((Locatable)element).getCoordinates();
		coordinate.onPage();
		coordinate.inViewPort();
		return true;
	}
	
}
