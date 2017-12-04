package capm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;



public class MonitoringProfiles {

	private static final Logger log = LogManager.getLogger("MonitoringProfiles");
	WebDriver driver;
		
	public MonitoringProfiles (WebDriver driver) {
		this.driver = driver;
	}
		
	public Boolean editMonitoringProfiles (int mpType, ArrayList<String> metrics, String mpName) throws InterruptedException {
		
		log.info("Edit monitoring profile \""+mpName+"\"");

		// mpType = 1     Metric Families in mib format like NormalizedIntegratedAdaptiveRateDSLInfo
		// mpType = 2     Metric Families in human readable format like Integrated Adaptive Rate DSL
		
		Actions action = new Actions (driver);
		
		Navigation navi = new Navigation (driver);
		navi.selectDataAggregator();
		navi.gotoMonitoringProfiles();
		boolean isNewMP = false;
		boolean isClicked = true;
		
		//Wait while monitoring profiles list will be loaded
		if (!navi.waitForElement("//span[text()='Availability']", 1))
			return false;

		//Check if our profile already exists
		List<WebElement> selectProfile = driver.findElements(By.xpath("//span[@class='x-grid3-cell-imagetext' and text()='"+mpName+"']"));
		if (selectProfile.size()==0) {
			log.info("Monitoring Profile '"+mpName+"' not found. Creating a new one.");
			isNewMP = true;

			navi.waitForElement("//div[contains(text(),'Loading')]", 2);

			if (!navi.protectedClick("//button[@type='button' and text()='New']", "WARN: Unable to click on button 'New'. Retrying."))
				return false;

			if (!navi.waitForElement("//input[@name='mpName']", 1))
				return false;
			
			WebElement mpNameFiled = driver.findElement(By.xpath("//input[@name='mpName']"));
			mpNameFiled.sendKeys(mpName);
			
			//Wait while available metric list will be loaded
			navi.waitForElement("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl", 1);
			
		} else {
			log.info("Edit existing monitoring profile '"+mpName+"'.");
			//Edit existing monitoring profile

			if (!navi.protectedClick("//span[@class='x-grid3-cell-imagetext' and text()='"+mpName+"']", "WARN: Unable to click on existing Monitoring Profile "+mpName+". Retrying."))
				return false;

			//Wait for reloading page after clicking to Monitoring Profile
			navi.waitForElement("//div[contains(text(),'Loading')]", 2);

			if (!navi.protectedClick("//button[@type='button' and text()='Edit']", "WARN: Unable to click on 'Edit' button. Retrying."))
				return false;

			//Wait while available metric list will be loaded
			navi.waitForElement("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl", 1);
						
			//Remove all selected metrics
			log.info("Removing existing selected MFs.");
			List<WebElement> selectedMetrics = driver.findElements(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[3]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl"));
			for (int i=0; i<selectedMetrics.size(); i++) {
				WebElement metricToClick = driver.findElement(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[3]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl[1]"));
				action.doubleClick(metricToClick);
				action.build().perform();
			}
			
		}
		
		if (mpType==1) {
			//Add metrics
			log.info("Adding new Metric Families.");

			//Wait while available metric list will be loaded
			navi.waitForElement("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl", 1);
			
			for (int i=0; i<metrics.size(); i++) {
				WebElement searchInput = driver.findElement(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[2]/div/table/tbody/tr/td[1]/table/tbody/tr/td/div/input"));
				searchInput.clear();
				searchInput.sendKeys(metrics.get(i));
				searchInput.sendKeys(Keys.ENTER);
				Thread.sleep(500);
				List<WebElement> findedMetric = driver.findElements(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl[1]"));
				if (findedMetric.size()>0) {
					action.doubleClick(findedMetric.get(0));
					action.build().perform();
				} else {
					log.error("Unable to find "+metrics.get(i)+" metric family.");
				}
			}			
		}
		
		if (mpType==2) {
			//Add metrics
			log.info("Adding new Metric Families (in human readable format).");
			for (int i=0; i<metrics.size(); i++) {
				try {
					WebElement searchMP = driver.findElement(By.xpath("//em[text()='"+metrics.get(i)+"']"));
					action.doubleClick(searchMP);
					action.build().perform();
					Thread.sleep(100);					
				} catch (Exception e) {
					log.error("Unable to find '"+metrics.get(i)+"' metric family.");
				}
			}			
		}
		
		
		WebElement saveButton = driver.findElement(By.xpath("//button[@type='button' and text()='Save']"));
		saveButton.click();
		
		//Wait for closing "Create / Edit Monitoring Profile" window
		if (!navi.waitForElement("//span[text()='Create / Edit Monitoring Profile']", 2))
			return false;
				
		//If a new Monitoring profile was created, need to add collections to it
		if (isNewMP) {
			log.info("Manage collections for newly created Monitoring Profile");

			if (!navi.protectedClick("//span[@class='x-grid3-cell-imagetext' and text()='"+mpName+"']", "WARN: Unable to click on existing Monitoring Profile "+mpName+". Retrying."))
				return false;

			WebElement collections = navi.getWebElement("//span[@class and text()='Collections']");
			collections.click();
			
			//Wait while page with 'No Data To Display' text will be loaded
			navi.waitForElement("//div[text()='No Data To Display']", 1);
			
			//Replace with WAIT method
			Thread.sleep(1000);

			if (!navi.protectedClick("//button[@type='button' and text()='Manage']", "WARN: Unable to click on button 'Manage'. Retrying."))
				return false;

			//Wait while available collections list will be loaded
			navi.waitForElement("//div[@class='x-list-body-inner']/dl", 1);
			
			WebElement collection = driver.findElement(By.xpath("//dl/dt/em[text()='All Manageable Devices']"));
			Actions dclick = new Actions (driver);
			dclick.doubleClick(collection).build().perform();				
			
			collection = driver.findElement(By.xpath("//dl/dt/em[text()='All Routers']"));
			dclick = new Actions (driver);
			dclick.doubleClick(collection).build().perform();				
			
			collection = driver.findElement(By.xpath("//dl/dt/em[text()='All Servers']"));
			dclick = new Actions (driver);
			dclick.doubleClick(collection).build().perform();
			
			collection = driver.findElement(By.xpath("//dl/dt/em[text()='All Switches']"));
			dclick = new Actions (driver);
			dclick.doubleClick(collection).build().perform();
			
			saveButton = driver.findElement(By.xpath("//button[@type='button' and text()='Save']"));
			saveButton.click();	
			
			//Wait for closing "Assign Collections to Monitoring Profiles" window
			if (!navi.waitForElement("//span[text()='Assign Collections to Monitoring Profiles']", 2))
				return false;
			
		}
				
		return true;
	}
	
	
}
