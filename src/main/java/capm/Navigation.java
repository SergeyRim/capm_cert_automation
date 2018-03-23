package capm;


import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
				WebElement elementToClick = driver.findElement(By.xpath(xpath));
				action.doubleClick(elementToClick).build().perform();
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

		while (driver.findElements(By.id("menuId6")).size()<1)
			Thread.sleep(1000);

		int tryNum=0;
		int maxNumberToTry=15;

		boolean isClicked;
		do {
			try {
				isClicked=true;
				WebElement admintab = driver.findElement(By.xpath("//h2[contains(.,'Administration')]"));

				Actions action = new Actions(driver);
				action.moveToElement(admintab).perform();
				Thread.sleep(500);

				//WebElement dalink = getWebElement("//a[contains(text(),'Data Aggregator@')]");
				WebElement dalink = driver.findElement(By.xpath(("//a[text()='Data Sources']")));

				Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
				String browserName = cap.getBrowserName().toLowerCase();
				if (browserName.equals("chrome")) {
					//Alternative click
					JavascriptExecutor executor = (JavascriptExecutor)driver;
					executor.executeScript("arguments[0].click();", dalink);
				} else {
					dalink.click();
				}

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
		
		while (driver.findElements(By.id("globalSearchField")).size()<1)
			Thread.sleep(1000);

		int tryNum=0;
		int maxNumberToTry=15;
		
		boolean isClicked;
		do {
			try {
				isClicked=true;
				Actions action = new Actions(driver);

				//New CAPC Navigation bar logic
				if (driver.findElements(By.xpath("//button[text()='Administration']")).size()>0) {
					WebElement admintab = driver.findElement(By.xpath("//button[text()='Administration']"));
					action.moveToElement(admintab).perform();
					Thread.sleep(500);
					WebElement dataSourcesMenu = driver.findElement(By.xpath("//span[text()='Data Sources']"));
					action.moveToElement(dataSourcesMenu).perform();
					Thread.sleep(500);
					WebElement dalink = driver.findElement(By.xpath(("//span[contains(text(),'Data Aggregator@')]")));
					dalink.click();
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
	
	public Navigation gotoVendorCertifications() throws InterruptedException {
		
						
		WebElement monitoringConfiguration = getWebElement("//span[text()='Monitoring Configuration']");
		
		//Check if "Monitoring Configuration" tab is collapsed (not expanded)
		// Alternate ->> //span[text()='Monitoring Configuration']/../../../../div[2][contains(@class,'x-panel-collapsed')]
		
		if (driver.findElements(By.xpath("//span[text()='Monitoring Configuration']/../../parent::div[contains(@class,'x-panel-collapsed')]")).size()>0) {
			monitoringConfiguration.click();
			Thread.sleep(500);
		}
		
		WebElement vendorCertifications = getWebElement("//span[text()='Vendor Certifications']");
		vendorCertifications.click();
		
		return this;
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
			WebElement admintab = driver.findElement(By.xpath("//button[text()='Inventory']"));
			action.moveToElement(admintab).perform();
			Thread.sleep(500);
			//wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Items']")));
			WebElement items_menu = driver.findElement(By.xpath("//span[text()='Items']"));
			action.moveToElement(items_menu).perform();
			//Wait while menu tab will appears
			//wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("/html/body/div[16][not(contains(@class,'x-hide-offsets'))]")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[16]")));

			WebElement device_components = driver.findElement(By.xpath("//span[text()='Device Components']"));
			device_components.click();
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
			WebElement admintab = driver.findElement(By.xpath("//button[text()='Inventory']"));
			action.moveToElement(admintab).perform();
			Thread.sleep(500);
			WebElement items_menu = driver.findElement(By.xpath("//span[text()='Items']"));
			action.moveToElement(items_menu).perform();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[16]")));

			WebElement device_components = driver.findElement(By.xpath("//span[text()='Interfaces']"));
			device_components.click();
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
			WebElement admintab = driver.findElement(By.xpath("//button[text()='Inventory']"));
			action.moveToElement(admintab).perform();
			Thread.sleep(500);
			WebElement items_menu = driver.findElement(By.xpath("//span[text()='Items']"));
			action.moveToElement(items_menu).perform();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[16]")));

			WebElement device_components = driver.findElement(By.xpath("//span[text()='Virtual Interfaces']"));
			device_components.click();
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
	
	
	public Navigation selectDevices() {

		Actions action = new Actions(driver);
		//New CAPC Navigation bar logic
		if (driver.findElements(By.xpath("//button[text()='Inventory']")).size()>0) {
			log.debug("Using new CAPC navigation logic.");
			WebElement admintab = driver.findElement(By.xpath("//button[text()='Inventory']"));
			action.moveToElement(admintab).perform();

			if (driver.findElements(By.xpath("//span[text()='Items']")).size()>0) {
				WebElement items_menu = driver.findElement(By.xpath("//span[text()='Items']"));
				action.moveToElement(items_menu).perform();
			}

			WebElement device_components = driver.findElement(By.xpath("//span[text()='Devices']"));
			device_components.click();
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
	
}
