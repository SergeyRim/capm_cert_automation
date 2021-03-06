package capm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryProfiles {

	private static final Logger log = LogManager.getLogger("DiscoveryProfiles");
	WebDriver driver;
	Wait<WebDriver> wait;

	public DiscoveryProfiles (WebDriver driver) {

		this.driver = driver;
		wait = new WebDriverWait(driver,60, 200).withMessage("ExpectedConditions timeout.");
	}

	public Boolean editDiscoveryProfiles (String dpName, ArrayList<String> ips) throws InterruptedException {
		
		log.info("Edit Discovery profile \""+dpName+"\".");

		Boolean isClicked = false;
		Navigation navi = new Navigation (driver);
		navi.selectDataAggregator();
		navi.gotoDiscoveryProfiles();
		
		log.debug("Wait while Discovery Profiles list will be loaded");
//		while (driver.findElements(By.xpath("//span[@class='x-grid3-header-label' and text()='Name']")).size()<1 && driver.findElements(By.xpath("//div[text()='No Data To Display']")).size()<1)
//			Thread.sleep(500);
		wait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[@class='x-grid3-header-label' and text()='Name']")),
				ExpectedConditions.presenceOfElementLocated(By.xpath("//div[text()='No Data To Display']"))));
				
		log.debug("Check if our profile already exists");
		List<WebElement> selectDP = driver.findElements(By.xpath("//div[text()='"+dpName+"']"));
		if (selectDP.size()==0) {
			log.info("Discovery profile "+dpName+" not found. Create a new one.");

			if (!navi.protectedClick("//button[text()='New']", "WARN: Unable to click on existing discover profile "+dpName+". Retrying."))
				return false;

			WebElement dpNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='name']")));
			dpNameField.sendKeys(dpName);
		} else {
			log.info("Found existing "+dpName+" profile. Modify it.");

			if (!navi.protectedClick("//div[text()='"+dpName+"']", "WARN: Unable to click on existing discover profile "+dpName+". Retrying."))
				return false;

			if (!navi.protectedClick("//button[text()='Edit']", "WARN: Unable to click on EDIT button. Retrying."))
				return false;

			log.debug("Remove all existing IPs.");
			//if (!navi.waitForElement("//div[@class='x-tab-panel-body x-tab-panel-body-noborder x-tab-panel-body-top']/div[1]/div/div/div/div/div/div/div[2]/div/div[1]/div[2]/div/div", 1))
			//	return false;
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='x-tab-panel-body x-tab-panel-body-noborder x-tab-panel-body-top']/div[1]/div/div/div/div/div/div/div[2]/div/div[1]/div[2]/div/div")));
		
			List<WebElement> existingIPs = driver.findElements(By.xpath("//div[@class='x-tab-panel-body x-tab-panel-body-noborder x-tab-panel-body-top']/div[1]/div/div/div/div/div/div/div[2]/div/div[1]/div[2]/div/div"));
			
			if (existingIPs.size()>0) {
				Actions action = new Actions (driver);
				action.keyDown(Keys.CONTROL).click(existingIPs.get(0));
				for (int i=1; i<existingIPs.size(); i++) {
					action.click(existingIPs.get(i));
				}
				action.keyUp(Keys.CONTROL);
				action.build().perform();
				
				WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[5]/table/tbody/tr[2]/td[2]/em/button[text()='Delete']")));
				deleteButton.click();
			}
						
		}
		
		WebElement ipaddrFiled = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[1]/div/table/tbody/tr/td[1]/table/tbody/tr/td[1]/div/input")));
		for (int i=0; i<ips.size(); i++) {
		
			ipaddrFiled.sendKeys(ips.get(i));
			
			WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Add']")));
			addButton.click();
		}
		
		Thread.sleep(1000);
		WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Save']")));
		saveButton.click();
		
		log.debug("Waiting for closing Discovery Profile window");
		//if (!navi.waitForElement("//span[@class='x-window-header-text' and text()='Discovery Profile']", 2))
		//	return false;
		wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[@class='x-window-header-text' and text()='Discovery Profile']"))));
			
		//Need to wait while list can be reloaded several times
		Thread.sleep(2000);

		if (!navi.protectedClick("//div[text()='"+dpName+"']", "WARN: Unable to click on existing discover profile "+dpName+". Retrying."))
			return false;

		//Run discover
		log.info("Running discover profile...");

		if (!navi.protectedClick("//button[text()='Run']", "WARN: Unable to click on RUN button. Retrying."))
			return false;

		log.debug("Wait for \"Run Discovery Profile\" window opened");
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='Run Discovery Profile']")));
			
		WebElement yesButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Yes']")));
		yesButton.click();
		
		log.debug("Wait for \"Run Discovery Profile\" window closed");
		wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[text()='Run Discovery Profile']"))));

		Thread.sleep(3000);
		log.info("Success.");
		
		return true;
	}
	

}
