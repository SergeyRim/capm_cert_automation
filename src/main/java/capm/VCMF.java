package capm;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xpath.operations.Bool;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
//import com.jcraft.jsch.*;
//import java.util.Properties;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;


public class VCMF {

	private static final Logger log = LogManager.getLogger("VCMF");
	WebDriver driver;

	public VCMF (WebDriver driver) {
		this.driver = driver;
	}
	
	
	public String putVendorCertPriorities (String id, String xml, String daServer) throws IOException {
		// Get MF ID by sending POST request
				
		URL url = new URL("http://"+daServer+":8581/rest/vendorpriorities/"+id);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("PUT");
		httpCon.setRequestProperty("Content-Type", "application/xml");
		OutputStreamWriter out = new OutputStreamWriter( httpCon.getOutputStream());
		out.write(xml);
		out.close();
		
		InputStream is = httpCon.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuffer response = new StringBuffer(); 
		while((line = rd.readLine()) != null) {
		   	response.append(line);
		    response.append('\r');
		}
		rd.close();
		log.info("PUT response for Metric Family ID "+id+" is: "+response.toString());
		return response.toString();
	}
	
	
	
	public String[] getVendorCertPriorities (String mfFacetName, String daServer) throws IOException {
		
		String[] credentials = new String[2];
		log.info("Getting Vendor Certification Priorities for \""+mfFacetName+"");

		// Get MF ID by sending POST request
		String post_request;
		post_request="<FilterSelect xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"filter.xsd\"> "
			+ "<Filter>"
			+ "<MetricFamilyVendorPriority.MetricFamilyID type=\"EQUAL\">{http://im.ca.com/normalizer}"+mfFacetName+"</MetricFamilyVendorPriority.MetricFamilyID>"
			+ "</Filter>"
			+ "<Select use=\"exclude\" isa=\"exclude\">"
			+ "<MetricFamilyVendorPriority use=\"exclude\"/>"
			+ "</Select>"
			+ "</FilterSelect>";		
		
		URL url = new URL("http://"+daServer+":8581/rest/vendorpriorities/filtered");
				
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		httpCon.setRequestProperty("Content-Type", "application/xml");
		OutputStreamWriter out = new OutputStreamWriter( httpCon.getOutputStream());
		out.write(post_request);
		out.close();
		
		InputStream is = httpCon.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuffer response = new StringBuffer(); 
	    while((line = rd.readLine()) != null) {
	    	response.append(line);
	        response.append('\r');
	    }
	    rd.close();
	    
	   	
	    //Get Metric Family ID    
	    Matcher matcher = Pattern.compile("(?<=<ID>)(.+)(?=</ID>)").matcher(response.toString());
		matcher.find();
		String path2 = matcher.group();
	    
		//System.out.println("ID is "+path2);
		credentials[0]=path2;
		log.info("INFO: ID for "+mfFacetName+" is: "+path2);
	    
	    //Get current priority xml by sending GET request
		url = new URL("http://"+daServer+":8581/rest/vendorpriorities/"+path2);
		httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("GET");
		httpCon.setRequestProperty("Content-Type", "application/xml");
				
		is = httpCon.getInputStream();
		rd = new BufferedReader(new InputStreamReader(is));
		response = new StringBuffer(); 
		while((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\r');
		}
		rd.close();
		
		credentials[1]=response.toString();
		
		return credentials;
	}
	
	
	public Boolean deleteDiscoveredDevices () throws InterruptedException {
		
		Navigation navi = new Navigation (driver);
		
		navi.selectDataAggregator();
		navi.gotoAllDevices();
		
		boolean isClicked;
		
		while (driver.findElements(By.xpath("//li[2][@class='x-tree-node']/ul[@class='x-tree-node-ct']/li/div/span[2]/a/span[not (contains(text(),'DataAggregator'))]")).size()>0) {

			if (!navi.protectedClick("//li[2][@class='x-tree-node']/ul[@class='x-tree-node-ct']/li/div/span[2]/a/span[not (contains(text(),'DataAggregator'))]", "WARN: Unable to click on device. Retrying."))
				return false;

			WebElement deleteButton = navi.getWebElement("//button[text()='Delete']");
			deleteButton.click();
			Thread.sleep(200);
			
			//Wait for Loading message
			navi.waitForElement("//div[contains(text(),'Loading')]", 2);
			
			WebElement yesButton = navi.getWebElement("//button[text()='Yes']");
			yesButton.click();	
			Thread.sleep(200);
			
			//Wait for Loading message
			navi.waitForElement("//div[contains(text(),'Wait')]", 2);
			
			//Wait while device list will be reloaded 
			WebElement selectDevice = navi.getWebElement("//li[2][@class='x-tree-node']/ul[@class='x-tree-node-ct']/li");

		}
		return true;
	}
	
	public Boolean changeVCPriority (ArrayList<String[]> vcList) throws InterruptedException {
		
		log.debug("Run VCMF.changeVCPriority.");

		Navigation navi;
		navi = new Navigation(driver);
		navi.selectDataAggregator();
		
		for (int i=0;i<vcList.size();i++) {
			log.info("Change vendor priority for \""+vcList.get(i)[0]+"\"");
			navi.gotoVendorCertifications();
			
			//Wait while Vendor Cert List will be loaded
			if (!navi.waitForElement("//div[text()='Factory']", 1))
				return false;
			
			WebElement search = navi.getWebElement("//input[@class='x-form-text x-form-field grid-filterbox x-form-empty-field']");
			search.clear();
			search.sendKeys(vcList.get(i)[0]);
			search.sendKeys(Keys.ENTER);
						
			while (driver.findElements(By.xpath("//div[contains(text(),'Loading')]")).size()>0)
				Thread.sleep(500);
			
			//Wait while VC list will show only one VC
			while (driver.findElements(By.xpath("//div[@class='x-panel polarisSelector x-box-item']/div[2]/div[1]/div/div/div/div[2]/div/div[1]/div/div[1]/div[2]/div/div")).size()!=1 && driver.findElements(By.xpath("//div[@class='x-panel polarisSelector x-box-item']/div[2]/div[1]/div/div/div/div[2]/div/div[1]/div[3]/div[text()='No Data To Display']")).size()<1)
				Thread.sleep(500);
			
			//Get VC human readable name
			WebElement hrVC = driver.findElement(By.xpath("//div[@class='x-panel polarisSelector x-box-item']/div[2]/div[1]/div/div/div/div[2]/div/div[1]/div/div[1]/div[2]/div/div[1]/table/tbody/tr/td[2]/div"));
			String hrVCname = hrVC.getText();
			
			while (driver.findElements(By.xpath("//div[contains(text(),'Loading')]")).size()>0 || driver.findElements(By.xpath("//div[contains(text(),'No Data To Display')]")).size()>0)
				Thread.sleep(500);
			
			//Click on parent MF link
			if (!navi.protectedClick("//a[@title='Navigate to Metric Family definition' and contains(@href,'"+vcList.get(i)[1]+"')]", "Unable to click on parent Metric Family link. Retrying."))
				return false;

			//Click on Vendor Certification Priority Tab
			WebElement vcPriorityTab = navi.getWebElement("//span[text()='Vendor Certification Priorities']");
			vcPriorityTab.click();
			
			while (driver.findElements(By.xpath("//div[contains(text(),'Loading')]")).size()>0)
				Thread.sleep(500);
			
			Thread.sleep(1000);

			//Check the number of VCs in priority list
			if (driver.findElements(By.xpath("//span[text()='Source']/../../../../../../../../../div[2]/div/div")).size()==1) {
				String only_vc = driver.findElement(By.xpath("//span[text()='Source']/../../../../../../../../../div[2]/div/div")).getText().trim();
				log.info("Only one VC \""+only_vc+"\" exists in Priority List. Not need to change vendor priority.");
			} else {
				if (!navi.protectedClick("//button[text()='Manage']", "WARN: Unable to click on 'Manage' button. Retrying."))
					return false;

				//Wait wile "Manage Vendor Certification Priority" page is loading
				if (!navi.waitForElement("//span[text()='Manage Vendor Certification Priority']", 1))
					return false;

				Thread.sleep(500);
				WebElement selectVC = navi.getWebElement("//div[text()='"+hrVCname+"']");
				selectVC.click();

				//Verify if ToTOP button is visible
				if (driver.findElements(By.xpath("//button[contains(@style,'up_arrow') or contains(@class,'iconUp')]/../../../../../../../td[3][not(contains(@class,'x-hide-display'))]")).size()>0) {
					WebElement toTopButton = driver.findElement(By.xpath("//button[contains(@style,'totop_arrow') or contains(@class,'iconTop')]"));
					toTopButton.click();
				} else {
					//Click 10 times to top button. TODO: need to improve this section by count how many time we need to click
					for (int j=0; j<10; j++) {
						WebElement toTopButton = driver.findElement(By.xpath("//button[contains(@style,'up_arrow') or contains(@class,'iconUp')]"));
						toTopButton.click();
					}
				}

				WebElement saveButton = driver.findElement(By.xpath("//button[text()='Save']"));
				saveButton.click();

				//Wait wile "Manage Vendor Certification Priority" page is closing
				if (!navi.waitForElement("//span[text()='Manage Vendor Certification Priority']", 2))
					return false;

				while (driver.findElements(By.xpath("//div[contains(text(),'Loading')]")).size()>0)
					Thread.sleep(500);

				log.info("Succesfully changed VC priority for \""+hrVCname+"\"");
			}

		}
		return true;
	}
	
	
	public String convertVCToHumanReadable (String VC) throws InterruptedException {
		
		Navigation navi;
		navi = new Navigation(driver);
		navi.selectDataAggregator();
		navi.gotoVendorCertifications();		
		
		WebElement search = driver.findElement(By.xpath("//input[@class='x-form-text x-form-field grid-filterbox x-form-empty-field']"));
		search.sendKeys(VC);
		search.sendKeys(Keys.ENTER);
		Thread.sleep(1000);
		
		WebElement hrVC = driver.findElement(By.xpath("//div[@class='x-panel polarisSelector x-box-item']/div[2]/div[1]/div/div/div/div[2]/div/div[1]/div/div[1]/div[2]/div/div[1]/table/tbody/tr/td[2]/div"));
		String hrVCname = hrVC.getText();
				
		return hrVCname;
	}
	
	
	public String [] getVcElement (String deviceName, String vcname, String mfname, String outputDir) throws InterruptedException, IOException {

		log.debug("Run VCMF.getVcElement");

		String[] returnArray = new String[6];
		// vc_mf_names
		// [0] -> Element
		// [1] -> MF name
		// [2] -> MF facet name
		// [3] -> VC name
		// [4] -> VC facet name
		// [5] -> SysDescr

		returnArray[2] = mfname;
		returnArray[4] = vcname;

		Navigation navi;
		navi = new Navigation(driver);
		Assert.assertNotNull(navi.selectDataAggregator());

		String sysDescr = navi.gotoMonitoredDevice(deviceName);
		returnArray[5] = sysDescr;

		Boolean isClicked = false;

		FileWriter vcElementsFile = new FileWriter(outputDir + "\\VC_elements.txt", true);

		log.info("Looking for " + vcname + " VC in Polled Metric Families.");

		Boolean isElementFound = false;
		do {
			log.debug("Wait for table to be populated on Polled Metric Families page.");
			if (!navi.waitForElement("//div[@class='x-grid3-cell-inner x-grid3-col-0 x-unselectable']/a[@title='Navigate to Metric Family definition' and text() !='']", 1))
				return null;
			log.debug("Trying to find a requred element.");
			Thread.sleep(1000);
			if (driver.findElements(By.xpath("//a[contains(@href,'" + vcname + "') and contains(@href,'" + mfname + "')]")).size() == 1) {
				isElementFound = true;
				log.info("Succesfully Found VC \"" + vcname + "\" with \"" + mfname + "\" MF");
			} else {
				//Check if button "Next page" is disabled
				if (driver.findElements(By.xpath("//div[@class='x-panel grid_border x-grid-panel']//button[contains(@class,'x-btn-text x-tbar-page-next')]/../../../../../../table[contains(@class,'x-item-disabled')]")).size() == 1) {
					log.error("Cab't find VC \"" + vcname + "\" with \"" + mfname + "\" MF");
					return null;
				} else {
					isElementFound = false;
					log.info("Can't find element on this page. Switch to next element page.");
					navi.protectedClick("//div[@class='x-panel grid_border x-grid-panel']//button[contains(@class,'x-btn-text x-tbar-page-next')]", "Unable to click on Next page button. Retrying.");
					Thread.sleep(1000);
					navi.waitForElement("//div[contains(text(),'Loading')]", 2);
				}
			}
		} while (!isElementFound);

		log.debug("Getting human readable VC name from table.");
		String hrVcName = driver.findElement(By.xpath("//a[contains(@href,'" + vcname + "') and contains(@href,'" + mfname + "')]")).getText().trim();
		returnArray[3] = hrVcName;
		log.debug("HR VC name is \"" + hrVcName + "\"");

		log.debug("Getting human readable MF name from table.");
		String hrMfName = driver.findElement(By.xpath("//a[contains(@href,'" + vcname + "') and contains(@href,'" + mfname + "')]/../../../td[1]/div/a")).getText().trim();
		returnArray[1] = hrMfName;
		log.debug("HR MF name is \"" + hrMfName + "\"");


		if (!navi.protectedDoubleClick("//a[contains(@href,'" + vcname + "') and contains(@href,'" + mfname + "')]/../../..", "WARN: Unable to click on VC link. Retrying."))
			return null;

		while (driver.findElements(By.xpath("//span[text()='Name']")).size() < 1 && driver.findElements(By.xpath("//div[text()='No components to display for this metric family']")).size() < 1)
			Thread.sleep(500);

		List<WebElement> elements = driver.findElements(By.xpath("//div[contains (@class,'x-panel') and contains (@class,'x-box-item')]/div[2]/div[1]/div/div/div/div[2]/div/div[1]/div/div[1]/div[2]/div/div/table/tbody/tr/td[2]/div"));

		if (driver.findElements(By.xpath("//div[text()='No components to display for this metric family']")).size() > 0 && elements.size() == 0) {
			vcElementsFile.write("No Components for VC: " + vcname + "\n");
			vcElementsFile.write("\n");
			vcElementsFile.flush();
			vcElementsFile.close();
			log.error("No components for VC \"" + vcname + "\".");
			return null;
		}

		vcElementsFile.write("Components for VC: " + vcname + "\n");

		for (int j = 0; j < elements.size(); j++) {
			vcElementsFile.write(elements.get(j).getText());
			vcElementsFile.write("\n");
			vcElementsFile.flush();
		}
		vcElementsFile.write("\n");
		vcElementsFile.flush();
		vcElementsFile.close();

		//Randomly take an element
		if (elements.size() > 1) {
			Random rand = new Random();
			returnArray[0] = elements.get(rand.nextInt(elements.size() - 1)).getText();
		} else
			returnArray[0] = elements.get(0).getText();

		return returnArray;
		
	}
	
	
public Boolean verifyVcAlreadyCertified (String deviceName, ArrayList<String> vcList, String outputDir) throws InterruptedException, IOException {
		
		log.debug("Run VCMF.verifyVcAlreadyCertified");

		Navigation navi;
		navi = new Navigation(driver);
		navi.selectDataAggregator();
		String sysDescr = navi.gotoMonitoredDevice(deviceName);
		if (sysDescr==null)
			return false;

		FileWriter metricsFile = new FileWriter (outputDir+"\\certifiedVCs.txt", false);

		String vendorInfoFile = outputDir+"\\VendorInfo.txt";

		FileWriter vendorCertifiedVCsFile;
		if (!new File(vendorInfoFile).exists())
		{
			log.debug("Vendor Info file \""+vendorInfoFile+"\" does not exists.");
			vendorCertifiedVCsFile = new FileWriter (vendorInfoFile, true);
			vendorCertifiedVCsFile.write("Device vendor: \n"+sysDescr+"\n\nAlready Supported vendor certs:\n\n");
		} else
		{
			vendorCertifiedVCsFile = new FileWriter (vendorInfoFile, true);
			log.debug("Vendor Info file \""+vendorInfoFile+"\" already exists.");
			vendorCertifiedVCsFile.write("\n\nAlready Supported vendor certs:\n\n");
		}

		log.debug("Wait for table to be populated on Polled Metric Families page.");
		navi.waitForElement("//div[@class='x-grid3-cell-inner x-grid3-col-0 x-unselectable']/a[@title='Navigate to Metric Family definition' and text() !='']", 1);

		for (int i=0; i<vcList.size(); i++) {
//			log.info("Looking for '"+vcList.get(i)+"' VC in Polled Metric Families.");
//
//			log.debug("Check if we need to return to first page.");
//			if (driver.findElements(By.xpath("//div[1]/div/div[2]/div/table/tbody/tr/td[1]/table/tbody/tr/td[5]/div/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table[not(contains(@class,'x-item-disabled'))]")).size()==1) {
//				log.debug("Click on FirstPage button.");
//				navi.protectedClick("//div[1]/div/div[2]/div/table/tbody/tr/td[1]/table/tbody/tr/td[5]/div/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table/tbody/tr[2]/td[2]/em/button","Unable to click on FirstPage button. Retrying.");
//				Thread.sleep(1000);
//				navi.waitForElement("//div[contains(text(),'Loading')]", 2);
//			}
//			WebElement vc, parent;
//
//			Boolean isElementFound = false;
//			Boolean isLastElementPage = false;
//			do {
//				log.debug("Wait for table to be populated on Polled Metric Families page.");
//				if (!navi.waitForElement("//div[@class='x-grid3-cell-inner x-grid3-col-0 x-unselectable']/a[@title='Navigate to Metric Family definition' and text() !='']", 1))
//					return null;
//				log.debug("Looking for xpath: //a[text()=' "+vcList.get(i)+"']/../../..");
//				Thread.sleep(1000);
//				if (driver.findElements(By.xpath("//a[text()=' "+vcList.get(i)+"']/../../..")).size() == 1) {
//					isElementFound = true;
//					log.info("Succesfully Found VC '"+vcList.get(i)+"'");
//				} else {
//					//Check if button "Next page" is disabled
//					if (driver.findElements(By.xpath("//div[1]/div/div[2]/div/table/tbody/tr/td[1]/table/tbody/tr/td[5]/div/table/tbody/tr/td[1]/table/tbody/tr/td[8]/table[contains(@class,'x-item-disabled')]")).size() == 1) {
//						log.error("Can't find VC '"+vcList.get(i)+"'");
//						isElementFound = false;
//						isLastElementPage = true;
//					} else {
//						log.info("Can't find VC on this page. Switch to next element page.");
//						isElementFound = false;
//						navi.protectedClick("//div[1]/div/div[2]/div/table/tbody/tr/td[1]/table/tbody/tr/td[5]/div/table/tbody/tr/td[1]/table/tbody/tr/td[8]/table/tbody/tr[2]/td[2]/em/button", "Unable to click on Next page button. Retrying.");
//						Thread.sleep(1000);
//						navi.waitForElement("//div[contains(text(),'Loading')]", 2);
//					}
//				}
//			} while (!isElementFound && !isLastElementPage);
//
//			//Check if all element's pages verified and no required VC found.
//			if (isLastElementPage && !isElementFound) {
//				continue;
//			}

			WebElement searchField = navi.getWebElement("//div[1]/div/div[2]/div/table/tbody/tr/td[1]/table/tbody/tr/td[2]/div/input");
			searchField.clear();
			searchField.sendKeys(vcList.get(i));
			searchField.sendKeys(Keys.ENTER);

			//Wait for Loading message
			Thread.sleep(500);
			navi.waitForElement("//div[contains(text(),'Loading')]", 2);
			Thread.sleep(500);

			if (driver.findElements(By.xpath("//a[text()=' "+vcList.get(i)+"']")).size()>0) {
				log.info("Succesfully Found VC '"+vcList.get(i)+"'");
				vendorCertifiedVCsFile.write(vcList.get(i)+"\n");
				if (!navi.protectedDoubleClick("//a[text()=' "+vcList.get(i)+"']/../../..", "WARN: Unable to click on VC. Retrying."))
					return false;
				Thread.sleep(1000);

				log.debug("Waiting while components list will be loaded.");
				while ((driver.findElements(By.xpath("//div[contains (@class,'x-panel') and contains (@class,'x-box-item')]/div[2]/div[1]/div/div/div/div[2]/div/div[1]/div/div[1]/div[2]/div/div/table/tbody/tr/td[2]/div")).size()<1 && driver.findElements(By.xpath("//div[text()='No components to display for this metric family']")).size()<1 && driver.findElements(By.xpath("//div[text()='Select a metric family to view components']")).size()<1 ) || driver.findElements(By.xpath("//div[contains(text(),'Loading')]")).size()>0)
					Thread.sleep(500);

				List<WebElement> elements = driver.findElements(By.xpath("//div[contains (@class,'x-panel') and contains (@class,'x-box-item')]/div[2]/div[1]/div/div/div/div[2]/div/div[1]/div/div[1]/div[2]/div/div/table/tbody/tr/td[2]/div"));

				metricsFile.write("Files for VC: "+vcList.get(i)+"\n");
				log.debug("Writing list of components to file.");
				for (int j=0; j<elements.size(); j++) {
					metricsFile.write(elements.get(j).getText());
					metricsFile.write("\n");
					metricsFile.flush();
				}
				metricsFile.write("\n");
				metricsFile.flush();
			} else {
				log.error("Can't find VC '"+vcList.get(i)+"'");
			}
		}
		
		metricsFile.close();
		
		vendorCertifiedVCsFile.write("\n");
		vendorCertifiedVCsFile.flush();
		vendorCertifiedVCsFile.close();

		return true;
	}
	
	
/*		
	public ArrayList<ArrayList<String>> convertMetricsToHumanReadableFormat (ArrayList<ArrayList<String>> mibMetrics, String daHostName, String rootPassword) {
		
		// Need import com.jcraft.jsch.*;
		
		ArrayList<ArrayList<String>> hrMetrics = new ArrayList<>();
		
		try {
            JSch jsch = new JSch();

            Session session = jsch.getSession("root", daHostName, 22);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            // Skip prompting for the password info and go direct...
            session.setPassword(rootPassword);
            session.connect();

            //Channel channel = session.openChannel("exec");
            String command; 
            Boolean isFinded;
            
            for (int i=0; i<mibMetrics.size(); i++) {
            	hrMetrics.add(new ArrayList<String>());
            	hrMetrics.get(i).add(mibMetrics.get(i).get(0));
            	hrMetrics.get(i).add(mibMetrics.get(i).get(1));
            	for (int j=2; j<mibMetrics.get(i).size(); j++) {
            		isFinded = false;
            		command = "cat /opt/IMDataAggregator/apache-karaf-2.4.3/certifications/CA/metric_families/properties/"+mibMetrics.get(i).get(1)+".properties |grep 'attribute."+mibMetrics.get(i).get(j).toLowerCase()+".attributedisplayname' | awk -F= '{print $2}'";
    				Channel channel = session.openChannel("exec");
    				((ChannelExec) channel).setCommand(command);
    	            ((ChannelExec) channel).setErrStream(System.err);
    	            InputStream in = channel.getInputStream();
    	            channel.connect();
    	            byte[] tmp = new byte[1024];
    	            while (true) {
    	                while (in.available() > 0) {
    	                    int ii = in.read(tmp, 0, 1024);
    	                    if (ii < 0) {
    	                    	break;
    	                    }
    	                    //System.out.print(new String(tmp, 0, ii));
    	                    hrMetrics.get(i).add(new String(tmp, 0, ii).trim());
    	                    isFinded = true;
    	                }
    	                
    	                if (channel.isClosed()) {
    	                    //System.out.println("exit-status: " + channel.getExitStatus());
    	                    break;
    	                }
    	                try {
    	                    Thread.sleep(50);
    	                } catch (Exception ee) {
    	                }
    	            }
    	            
    	            if (isFinded == false)
	                	hrMetrics.get(i).add(mibMetrics.get(i).get(j));
    	            channel.disconnect(); 
    			}
    		}
            
            //channel.disconnect();
            session.disconnect();
            
        } catch (Exception e) {
            System.out.println(e);
        }
		
		
		return hrMetrics;
	}
*/


	public ArrayList<ArrayList<String>> convertMetricsToHRFormat (ArrayList<ArrayList<String>> mibMetrics, String daHostName, String rootPassword, String outputPath) throws IOException, InterruptedException {
		
		log.debug("Run VCMF.convertMetricsToHRFormat");

		ArrayList<ArrayList<String>> hrMetrics = new ArrayList<>();
		String command; 
		FileWriter metricsFile = new FileWriter (outputPath+"\\metrics.txt", false);
      	
		SSHClient ssh2 = new SSHClient();
		ssh2.addHostKeyVerifier(new PromiscuousVerifier());
		ssh2.connect(daHostName);
		try {
			ssh2.authPassword("root", rootPassword);
			Session session = ssh2.startSession();
		    Shell shell = session.startShell();
		    try {
		    	InputStream sshIn =  shell.getInputStream();
		        OutputStream sshOut = shell.getOutputStream();
		        for (int i=0; i<mibMetrics.size(); i++) {
		        	hrMetrics.add(new ArrayList<String>());
		            hrMetrics.get(i).add(mibMetrics.get(i).get(0));
		            hrMetrics.get(i).add(mibMetrics.get(i).get(1));
		            
		            //Write VC to metrics documaentation file
		            metricsFile.write(mibMetrics.get(i).get(0));
		        	metricsFile.write("\n");
		            
		        	for (int j=2; j<mibMetrics.get(i).size(); j++) {
		               	log.debug("Get display name for metric: "+mibMetrics.get(i).get(1));
		        		//Get Display Name for metric
		            	//command = "PR=`cat /opt/IMDataAggregator/apache-karaf-2.4.3/certifications/CA/metric_families/properties/"+mibMetrics.get(i).get(1)+".properties |grep 'attribute."+mibMetrics.get(i).get(j).toLowerCase()+".attributedisplayname' | awk -F= '{print $2}'`; if [ \"$PR\" == \"\" ]; then echo 'NotFound'; else echo $PR; fi\n";
						command = "PR=`cat /opt/IMDataAggregator/*/certifications/CA*/metric_families/properties/"+mibMetrics.get(i).get(1)+".properties |grep 'attribute."+mibMetrics.get(i).get(j).toLowerCase()+".attributedisplayname' | awk -F= '{print $2}' | head -1`; if [ \"$PR\" == \"\" ]; then echo 'NotFound'; else echo $PR; fi\n";
						log.debug("Search command: "+command);
		            	sshOut.write(command.getBytes());
		                sshOut.flush();
		                char[] buffer = new char[512];
			            Reader in = new InputStreamReader(sshIn, "UTF-8");
			            int test = in.read(buffer,0,512);
			            String result = String.valueOf(buffer).trim();
			            if (result.equals("NotFound")) {
			            	hrMetrics.get(i).add(mibMetrics.get(i).get(j));
			            	log.debug("Metric display name not found.");
			            } else {
			            	hrMetrics.get(i).add(result);
			            	metricsFile.write(result);
			            	metricsFile.write("\t| ");
			            	log.debug("Metric display name is: \""+result+"\".");
			            }
			            
			            //Get Documentation for metric
			            //command = "PR=`cat /opt/IMDataAggregator/apache-karaf-2.4.3/certifications/CA/metric_families/properties/"+mibMetrics.get(i).get(1)+".properties |grep 'attribute."+mibMetrics.get(i).get(j).toLowerCase()+".documentation' | awk -F= '{print $2}'`; if [ \"$PR\" == \"\" ]; then echo 'NotFound'; else echo $PR; fi\n";
						command = "PR=`cat /opt/IMDataAggregator/*/certifications/CA*/metric_families/properties/"+mibMetrics.get(i).get(1)+".properties |grep 'attribute."+mibMetrics.get(i).get(j).toLowerCase()+".documentation' | awk -F= '{print $2}' | head -1`; if [ \"$PR\" == \"\" ]; then echo 'NotFound'; else echo $PR; fi\n";
						sshOut.write(command.getBytes());
		                sshOut.flush();
		                in = new InputStreamReader(sshIn, "UTF-8");
			            test = in.read(buffer,0,512);
			            result = String.valueOf(buffer).trim();
			            if (!result.equals("NotFound")) {
			            	metricsFile.write(result);
				            metricsFile.write("\n");			            	
			            }
			            metricsFile.flush();
			            
		            }
		        	metricsFile.write("\n\r");
		        }
		    } finally {
		    	session.close();
		    }
		} finally {
			ssh2.disconnect();
		}
		ssh2.close();
		metricsFile.close();
		return hrMetrics;
	}
	

}
