package capm;

import java.util.Date;
import java.util.List;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;


public class CreateReport {
	private static final Logger log = LogManager.getLogger("CreateReport");
	WebDriver driver;
	Wait<WebDriver> wait;

	public CreateReport (WebDriver driver) {

		this.driver = driver;
		wait = new WebDriverWait(driver,60, 200).withMessage("ExpectedConditions timeout.");
	}
	
	public Boolean createCustomTab(String deviceName, int metricType, String tabName, ArrayList<String> metrics, String element, String outputDir, String reportType) throws InterruptedException, Exception {
		
		// metricType == 1     <-- mib metric format
		// metricType == 2     <-- Human Readable metric format

		log.info("Will create report for element \""+element+"\"");

		boolean isDeviceReport = false;
		boolean isElementSelected = false;
		JavascriptExecutor je = (JavascriptExecutor) driver;
		
		Navigation navi;
		navi = new Navigation (driver);
		
		//Check if we run a device report from test parameters
		if (reportType.toLowerCase().equals("device")) {
			log.info("Device report type is selected.");
			isDeviceReport = true;			
		} else {
			//If legacy report type selected from test parameters, need to permorm checks
			//Check if "Device Components" page exists
			try {
				navi.selectDeviceComponents();
				if (navi.selectElement(element, deviceName))
					isElementSelected = true;
				else {
					isElementSelected = false;
					log.info("Unbale to find element in 'Device Components' page. Will try to run Interface report.");
				}
			} catch (Exception e) {
				log.info("Unbale to find 'Device Components' page. Will try to run Interface report.");
				isElementSelected = false;
			}
			
			//If element was not found on "Device Components" page, check if "Interfaces" page exists
			if (!isElementSelected) {
				try {
					navi.selectInterfaces();
					if (navi.selectInterfaceElement(element, deviceName))
						isElementSelected = true; 
					else {
						isElementSelected = false;
						log.info("Unbale to find element in 'Interfaces' page. Will try to run Virtual Interface report.");
					}
				} catch (Exception e) {
					log.info("Unbale to find 'Interfaces' page. Perform Device report.");
					isElementSelected = false;
				}
			}
			
			
			//If element was not found on "Interfaces" page, check if "Virtual Interfaces" page exists
			if (!isElementSelected) {
				try {
					navi.selectVirtualInterfaces();
					if (navi.selectInterfaceElement(element, deviceName))
						isElementSelected = true; 
					else {
						isElementSelected = false;
						log.info("Unbale to find element in 'Virtual Interfaces' page. Perform Device report.");
					}
				} catch (Exception e) {
					log.info("Unbale to find 'Virtual Interfaces' page. Perform Device report.");
					isElementSelected = false;
				}
			}
			
			//If we fail to find an element on "Device Components" and "Interfaces" pages, perform Device report
			if (!isElementSelected)
				isDeviceReport = true;
			else isDeviceReport = false;
		}
	
		
		Actions action = new Actions (driver);
		
		int totalMetrics = 0 ;
		int maxMetricCount = 9; 
		int tableCount = 1;
		int metricNumToClick=1;
		boolean isClicked = false;
		
		if (isDeviceReport) {
			navi.selectDevices();
			WebElement setup2 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[starts-with(text(),'"+deviceName+"')]")));
			setup2.click();
		}		
	
		WebElement setup = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@class='x-tab-strip-close']")));
		setup.click();
		
		//Check that table with "Add Tab" link exists
		if (isDeviceReport) {
			while (driver.findElements(By.xpath("//div[contains(@class,'x-menu x-menu-floating x-layer tabMenu x-menu-nosep')]")).size()<1)
				setup.click();			
		} else {
			while (driver.findElements(By.xpath("//ul[@class='x-menu-list']")).size()<1)
				setup.click();
		}

		
		setup = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Add Tab']")));
		setup.click();
		setup = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@class=' x-form-text x-form-field']")));
		
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		log.info("Create new report tab QA"+timeStamp);
		setup.sendKeys("QA-"+timeStamp);
		setup = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='Custom View - Infrastructure Management']")));
		setup.click();
		
		if (isDeviceReport) 
			createIMTableGraph (tableCount,2,metrics.get(1));
		else createIMTableGraph (tableCount,1,metrics.get(1));
	
		String metricAlreadyClicked = "";
		String[] metricNameAndDimension = new String [2];
		WebElement metrcsSearchField = driver.findElement(By.xpath("//input[@class='x-form-text x-form-field x-form-empty-field']"));

		log.info("Adding metrics to report table.");
		for (int i=2; i<metrics.size(); i++) {
						
			log.debug("Adding \""+metrics.get(i)+"\" metric.");
			//If metrics in Human Readable format
			if (metricType==2) {

				if (metrics.get(i).equals("Names") || metrics.get(i).equals("Indexes") || metrics.get(i).equals("Descriptions") || metrics.get(i).equals("Admin Status") || metrics.get(i).equals("Oper Status") || metrics.get(i).equals("Alias")) {
					log.info("Skipping metric \""+metrics.get(i)+"\".");
					continue;
				}

				int tryNum=0;
				do {
					try {
						isClicked=true;
						Thread.sleep(100);
						navi.doubleClickOnXpath("//div[@class='x-list-body-inner']/dl/dt/em[starts-with(text(),'"+metrics.get(i)+" - ') and (contains(text(),'Average') or contains(text(),'Total'))]");
						tryNum++;
					} catch (Exception e) {
						if (e.toString().contains("Unable to locate element")) {
							isClicked=true;
							log.debug(e.toString());
							log.error("Unable to locate metric \""+metrics.get(i)+"\"");
						} else {
							isClicked=false;
							Thread.sleep(500);
							log.warn("Unable to click on metric \""+metrics.get(i)+"\". Retrying.");
						}
					}
				} while (!isClicked && tryNum<10);
				
				totalMetrics++;
								
				//If total number of metrics=Max number metrics per page, need to add one more table
				if ( (totalMetrics==maxMetricCount) && (i<metrics.size()-1)) {
					log.info("Maximum metrics per table exceeded. Creating new table "+(tableCount+1));
					//Save current table
					Thread.sleep(1000);

					//Click on Save button
					navi.protectedClick("//table[contains(@id,'ext')]/tbody/tr[2]/td[2]/em/button[text()='Save']", "Unable to click Save button. Retrying.");

					Thread.sleep(500);
					//navi.waitForElement("//div[contains(text(),'Wait')]",2);
					wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[contains(text(),'Wait')]"))));				


					//Need to check if Save button was clicked but window still not closed
					if (driver.findElements(By.xpath("//table[contains(@id,'ext')]/tbody/tr[2]/td[2]/em/button[text()='Save']")).size()>0) {
						log.warn("\"Save\" button clicked but not processed. Will click one more time.");
						navi.protectedClick("//table[contains(@id,'ext')]/tbody/tr[2]/td[2]/em/button[text()='Save']", "Unable to click Save button. Retrying.");

					}
					
					log.debug("Wait while table saved and closed");
					if (isDeviceReport){
						//if (!navi.waitForElement("//div/div/span[text()='IM Table (Device)']", 2))
						//	return false;
						wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div/div/span[text()='IM Table (Device)']"))));
					} else {
						//if (!navi.waitForElement("//div/div/span[text()='IM Table (Interface - Component)']", 2))
						//	return false;
						wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div/div/span[text()='IM Table (Interface - Component)']"))));
					}
						
					//Create a new one
					tableCount++;
					if (isDeviceReport) 
						createIMTableGraph (tableCount,2,metrics.get(1));
					else createIMTableGraph (tableCount,1,metrics.get(1));
					
					totalMetrics=0;
				}
			}
			
			
			//If metrics in mib format      TODO:  NEED TO REWRITE THIS SECTION. WILL NOT WORK CORRECTLY WITH DEVICE REPORTING
			if (metricType==1) {
				metrcsSearchField.sendKeys(metrics.get(i));
				metrcsSearchField.sendKeys(Keys.ENTER);
				Thread.sleep(500);
				int j = driver.findElements(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div/dl")).size();  //path to "Metric Fields Available" frame
				Thread.sleep(300);
				WebElement findedMetrics;
				metricNumToClick=1;
			
				for (int k=1; k<=j; k++){
					Thread.sleep(100);
					findedMetrics = driver.findElement(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div/dl["+metricNumToClick+"]/dt/em"));  //path to first metric in "Metric Fields Available" frame
					String metricText = findedMetrics.getText();
					metricNameAndDimension = metricText.split(" - ");
								
					//Check if we already clicked on metric with the same name, should skip is already clicked
					if (!metricNameAndDimension[0].equals(metricAlreadyClicked)) {
						//Select only metrics which starts with a first symbol like a sought metric || check the same with UpperCase
						//Check if Dimension has Average or Total
						if ( (metricText.startsWith(String.valueOf(metrics.get(i).charAt(0))) || metricText.startsWith(String.valueOf(metrics.get(i).charAt(0)).toUpperCase())) && (metricNameAndDimension[1].contains("Average") || metricNameAndDimension[1].contains("Total"))) {
							metricAlreadyClicked = metricNameAndDimension[0];
							action.doubleClick(findedMetrics);
							action.build().perform();
							totalMetrics++;
						} else 
							metricNumToClick++;
					} else 
						metricNumToClick++;
				
					//If total number of metrics=Max number metrics per page, need to add one more table
					if (totalMetrics==maxMetricCount) {
						Thread.sleep(1000);						
						//Save current table
						setup = driver.findElement(By.xpath("//table[@class='x-btn x-btn-noicon' and contains(@id,'ext')]/tbody/tr[2]/td[2]/em/button[text()='Save']"));
						setup.click();
						
						//Wait while table saved and closed
						if (isDeviceReport){
							//if (!navi.waitForElement("//div/div/span[text()='IM Table (Device)']", 2))
							//	return false;
							wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div/div/span[text()='IM Table (Device)']"))));
						} else {
							//if (!navi.waitForElement("//div/div/span[text()='IM Table (Interface - Component)']", 2))
							//	return false;
							wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div/div/span[text()='IM Table (Interface - Component)']"))));
						}
							
						//Create a new one
						tableCount++;
						if (isDeviceReport) 
							createIMTableGraph (tableCount,2,metrics.get(1));
						else createIMTableGraph (tableCount,1,metrics.get(1));
										
						//Search for metric's search field in a new table
						metrcsSearchField = driver.findElement(By.xpath("//input[@class='x-form-text x-form-field x-form-empty-field']"));
						Thread.sleep(2000);
					
						//Search for metrics again in a new table
						metrcsSearchField.sendKeys(metrics.get(i));
						Thread.sleep(1000);
						metrcsSearchField.sendKeys(Keys.ENTER);
					
						totalMetrics=0;
					
						//Now need to resume adding metrics (not need to click on the first metric again)
						metricNumToClick=k+1;
					}
				}
			}
			
			if (metricType==1)
				metrcsSearchField.clear();
		}
		
		Thread.sleep(1000);

		//Click on Save button
		navi.protectedClick("//table[contains(@id,'ext')]/tbody/tr[2]/td[2]/em/button[text()='Save']", "Unable to click Save button. Retrying.");
		Thread.sleep(500);

		//navi.waitForElement("//div[contains(text(),'Wait')]",2);
		wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[contains(text(),'Wait')]"))));

		//Need to check if Save button was clicked but window still not closed
		if (driver.findElements(By.xpath("//table[contains(@id,'ext')]/tbody/tr[2]/td[2]/em/button[text()='Save']")).size()>0) {
			log.warn("\"Save\" button clicked but not processed. Will click one more time.");
			navi.protectedClick("//table[contains(@id,'ext')]/tbody/tr[2]/td[2]/em/button[text()='Save']", "Unable to click Save button. Retrying.");

		}
		
		//Wait while table saved and closed
		if (isDeviceReport){
			wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div/div/span[text()='IM Table (Device)']"))));
		} else {
			wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div/div/span[text()='IM Table (Interface - Component)']"))));
		}

		setup = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Save']")));
		setup.click();
		
		//Wait while report will be generated
		int timeout = 1000*40;
		int waiting = 0;
		while (driver.findElements(By.xpath("//span[@class='x-grid3-header-label' and text()='Name']")).size()==0 && driver.findElements(By.xpath("//div[text()='No Data To Display']")).size()<1 || driver.findElements(By.xpath("//div[contains(text(),'Loading')]")).size()>0) {
			waiting+=500;
			if (waiting>=timeout)
				break;
			Thread.sleep(1000);
		}
		if (waiting>=timeout) {
			log.fatal("Timedout waiting for report page generation.");
			return false;
		}


		Thread.sleep(500);
		
		int tabsPerPage=4;
		
		//Take screenshot
		takeScreenshot (metrics.get(0)+"-"+metrics.get(1)+"_"+tabsPerPage,outputDir);
		
		//If Device Report, need to find requested element (on all tables)
		if (isDeviceReport && driver.findElements(By.xpath("//div[1][@class='x-panel localViewContainer']/div[2]/div/div/div/div/div[3]/div/div[1]/div/div[1]/div[2]/div/div")).size()>1) {
			List<WebElement> searchFields = driver.findElements(By.xpath("//td/div[contains(@class,'filterCompoundElement')]/input"));
			for (int i=0;i<searchFields.size();i++) {
				searchFields.get(i).sendKeys(element);
				searchFields.get(i).sendKeys(Keys.ENTER);
				Thread.sleep(500);
				wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[contains(text(),'Loading')]"))));
			}
			Thread.sleep(500);
			
			takeScreenshot (metrics.get(0)+"-"+metrics.get(1)+"_search_"+tabsPerPage,outputDir);
		}
		
		//Check if there are more than 4 reports on the page
		//List<WebElement> setup1 = driver.findElements(By.xpath("//span[text()='Name']"));
		List<WebElement> setup1 = driver.findElements(By.xpath("//div[@class='x-panel localViewContainer']"));
		
		while (tabsPerPage+1 <= setup1.size()) {
			//Scroll page by clicking on tab field
			je.executeScript("arguments[0].scrollIntoView(true);",setup1.get(tabsPerPage));
			Thread.sleep(1000);
			tabsPerPage+=4;
			takeScreenshot (metrics.get(0)+"-"+metrics.get(1)+"_"+tabsPerPage,outputDir);			
		}
			
				log.debug("Delete created tab");
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='QA-"+timeStamp+"']"))).click();
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Delete Tab']"))).click();
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Yes']"))).click();

		log.debug("Wait while page will be reloaded after deleting tab");
		wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[text()='QA-"+timeStamp+"']"))));
		
		log.debug("Finished CreateReport.createCustomTab execution.");
		return true;
	}
	
	
	protected CreateReport takeScreenshot (String fileName, String path) throws InterruptedException, Exception {

		File sourceFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(sourceFile, new File (path+"\\"+fileName+".png"));

		return this;
	}
	
	
	protected Boolean createIMTableGraph (int tableNum, int reportType, String mfFacetName) throws InterruptedException {
		
		Actions dragndrop = new Actions (driver);
		Actions action = new Actions (driver);	
		Boolean isClicked = false;
		
		Navigation navi = new Navigation (driver);
		log.debug("DragAndDrop new table");

		Thread.sleep(500);
		WebElement moveFrom;
		switch (reportType) {
			case 1: moveFrom = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='x-panel-body pageBuilder-accordion-container-body x-panel-body-noheader x-panel-body-noborder']/div/div[1 or 2]/div[2]/div/div[2]/div/div/div/div/div[2]/div/div[4]/table/tbody/tr/td/div[text()='IM Table (Interface - Component)']")));
					break;
					
			//case 2: moveFrom = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[text()='IM Table (Device)']")));
			case 2: wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='IM Table (Device)' and @class='x-grid3-cell-inner x-grid3-col-viewname x-unselectable']")));
					moveFrom = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[text()='IM Table (Device)' and @class='x-grid3-cell-inner x-grid3-col-viewname x-unselectable']")));
					break;
			
			default: moveFrom = null;
					 break;

		}

		WebElement moveTo = driver.findElement(By.xpath("//div[@class='x-panel-body']/div/div/div[2][@class='x-grid3-scroller']"));
		log.debug("Performing drag'n'drop operation.");
		Thread.sleep(500);
		Action dragAndDrop = dragndrop.dragAndDrop(moveFrom, moveTo).build();
		dragAndDrop.perform();
		Thread.sleep(200);
		
		//Edit newly created table
		log.debug("//Edit newly created table");
		WebElement setup = driver.findElement(By.xpath("//div[@class='x-grid3-body']/div["+tableNum+"]/table/tbody/tr/td[2]/div/div"));
		setup.click();
		
		log.debug("Wait while available and selected metric list will be loaded");
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl")));
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[3]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl")));

		//Remove "Device Name" and "Description" metrics from selected metrics
		if (!navi.protectedDoubleClick("//div[@class='x-list-body-inner']/dl/dt/em[text()='Device Name']", "WARN: Unable to remove 'Device Name' metric. Retrying."))
			return false;

		if (!navi.protectedDoubleClick("//div[@class='x-list-body-inner']/dl/dt/em[text()='Description']", "WARN: Unable to remove 'Description' metric. Retrying."))
			return false;

		//If a device report, we need to change Metric Family to an appropriate one
		if (reportType==2) {
						
			//Wait while available metric list will be loaded
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl")));

			log.debug("Verify if we need to change \"Metric Family\"...");
			String currentMF = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='RIBTableItem/SettingHiddenValue']"))).getAttribute("value");
			
			if (!currentMF.contains(mfFacetName)) {
				log.debug("Getting the 1st metric in Available metric list and metric's count.");
				String firstMetricName = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl[1]/dt/em"))).getText();
				int metricsCount=driver.findElements(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl")).size();

				log.debug("Changing \"Metric Family\" to "+mfFacetName);
				wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[3]/div[1]/div[@class='x-form-field-wrap x-form-field-trigger-wrap']/img"))).click();
				Thread.sleep(2000);
				wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@id,'"+mfFacetName+"')]"))).click();
				Thread.sleep(500);
				
				log.debug("Wait while metric's list will start reloading by comparing \"firstMetricName\" with current first metric name AND compare number of metrics (before and after reloading)");

				String tmpMetric;
				int tmpMetricsCount=0;
				do {
					log.debug("Sleeping 500ms.");
					Thread.sleep(500);
					tmpMetric = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl[1]/dt/em"))).getText();
					tmpMetricsCount=driver.findElements(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl")).size();
					
				} while ((tmpMetric.equals(firstMetricName) && tmpMetricsCount==metricsCount) || tmpMetricsCount==0);
									
				log.debug("Wait while available metric list will be reloaded after changing MF");
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[1]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl")));
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@class='x-table-layout']/tbody/tr/td[3]/div/fieldset/div/div[1]/div/div[2]/div[@class='x-list-body-inner']/dl")));
				
			}

		}
		
		return true;
	}
	
}
