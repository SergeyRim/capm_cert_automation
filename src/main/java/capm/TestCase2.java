package capm;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Pages.DataSourcesPage;
import Pages.DiscoveryProfilesPage;
import Pages.LoginPage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

@Listeners({ScreenshotListener.class})
public class TestCase2 {

	String version = "5.1.9";
	String build = "06082019";

	private static final Logger log = LogManager.getLogger("MainTest");
	RemoteWebDriver driver;

	//To pass driver to ScreenshotListener
	public WebDriver getDriver() {
		return driver;
	}

	@BeforeMethod(alwaysRun = true)
	@Parameters({"Driver", "RemoteDriverURL","logLevel"})
	public void beforeSetup(String browserDriver, @Optional("http://127.0.0.1:4444/wd/hub") String RemoteDriverURL, @Optional("") String logLevel) throws MalformedURLException {

		log.info("CAPM Cert Automation Testing, version "+version+" (build "+build+")");

		//Bypass SSHj logging to ERROR level
		org.apache.logging.log4j.core.config.Configurator.setLevel("net.schmizz.sshj",Level.ERROR);

		if (!logLevel.equals("") && !logLevel.toLowerCase().equals("info")) {

			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
			LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

			switch (logLevel.toLowerCase()) {
				case "debug":	log.info("Changing log level to DEBUG according to XML file configuration.");
					loggerConfig.setLevel(Level.DEBUG);
					ctx.updateLoggers();
					//Bypass debug output for org.apache.http logger
					org.apache.logging.log4j.core.config.Configurator.setLevel("org.apache.http",Level.WARN);
					break;
				case "error":	log.info("Changing log level to ERROR according to XML file configuration.");
					loggerConfig.setLevel(Level.ERROR);
					ctx.updateLoggers();
					break;

				default: 		log.warn("Unsupported log level specified. Switching to INFO log level.");
					break;
			}
		}

		DesiredCapabilities dc = new DesiredCapabilities();
		switch (browserDriver.toLowerCase()) {
			case "firefox":			System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
									driver = new FirefoxDriver();
									driver.manage().window().maximize();
									break;
			case "chrome":			driver = new ChromeDriver();
									driver.manage().window().maximize();
	 								break;
			case "ie"	 :			driver = new InternetExplorerDriver();
									driver.manage().window().maximize();
	 								break;
			case "remotefirefox" :  dc.setBrowserName("firefox");
									dc.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
									driver = new RemoteWebDriver(new URL(RemoteDriverURL), dc);
									//driver.manage().window().maximize();
									driver.manage().window().setSize(new Dimension(1920,1080));
									driver.setFileDetector(new LocalFileDetector());
									break;
			case "remotechrome"  :	dc.setBrowserName("chrome");
									dc.setCapability(CapabilityType.TAKES_SCREENSHOT, true);

									ChromeOptions chromeOptions = new ChromeOptions();
									chromeOptions.addArguments("--headless");
									chromeOptions.addArguments("--start-maximized");
									chromeOptions.addArguments("--disable-gpu");
									chromeOptions.addArguments("--window-size=1920,1080");
                                    //chromeOptions.addArguments("--disable-dev-shm-usage");
									dc.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
									driver = new RemoteWebDriver(new URL(RemoteDriverURL), dc);
									driver.setFileDetector(new LocalFileDetector());
									break;
		 	default: 	log.info("No correct driver specified. Will use FireFox driver by default.");
	 					driver = new FirefoxDriver();
	 			 		break;
		}

	}
		 
	@AfterMethod(alwaysRun = true)
	public void afterSetup(ITestResult result) throws IOException, AWTException {
		/*
		//Can make a screenshot in aftermethod instead of did it in listener
		if (result.getStatus() == ITestResult.FAILURE) {
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat formater = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss");
			String methodName = result.getName();

			BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			ImageIO.write(image, "png", new File(outputDir+"\\"+formater.format(calendar.getTime())+".png"));
			//Reporter.log("ERR: Error on method \""+methodName+"\". Screenshot saved under "+outputDir+"\\"+formater.format(calendar.getTime())+".png");
		} */
		driver.quit();
		//driver.close();
	}
	
	@Test(description="Create/Modify Monitoring Profile", groups = {"CreateModifyMP"})
	@Parameters({"readmeFile","capcServer","MonitoringDiscoveryProfileName"})
	public void createModifyMP(String readmeFile, String capcServer, String MonitoringDiscoveryProfileName) throws Exception {
			
		log.info("Host: "+capcServer);
		driver.get(capcServer);
		
		MonitoringProfiles mp = new MonitoringProfiles(driver);
		readmeParser parser = new readmeParser();

		LoginPage loginpage = PageFactory.initElements(driver,LoginPage.class);
		loginpage.enterUsername("admin");
		loginpage.enterPassword("admin");
		loginpage.logIn();
		
		ArrayList<String> mfList = new ArrayList<String>();
		mfList = parser.getMFs(readmeFile);
		
		if (mfList==null) {
			log.fatal("No applicapable data parsed. Stopping current test-cases execution.");
			return;
		}

		Assert.assertTrue(mp.editMonitoringProfiles(1,mfList,MonitoringDiscoveryProfileName));
		
	}
	
	
	@Test(description="Change Vendor Priority", groups = {"ChangeVP"})
	@Parameters({"readmeFile","capcServer"})
	public void changeVP(String readmeFile, String capcServer) throws Exception {
				
		log.info("Host: "+capcServer);
		driver.get(capcServer);
		
		readmeParser parser = new readmeParser();
		VCMF vcmf = new VCMF (driver);

		LoginPage loginpage = PageFactory.initElements(driver,LoginPage.class);
		loginpage.enterUsername("admin");
		loginpage.enterPassword("admin");
		loginpage.logIn();
		
		ArrayList<String[]> vcmfList = new ArrayList<String[]>();
		vcmfList = parser.getMFsVCs(readmeFile);
		
		if (vcmfList==null) {
			log.fatal("No applicapable data parsed. Stopping current test-cases execution.");
			return;
		}
		
		Assert.assertTrue(vcmf.changeVCPriority(vcmfList));

	}
	
	
	@Test(description="Create/Modify Discovery Profile", groups = {"CreateModifyDP"})
	@Parameters({"simIDs","capcServer","MonitoringDiscoveryProfileName","PMF_User","PMF_Password"})
	public void createModifyDP(String simIDs, String capcServer, String MonitoringDiscoveryProfileName, @Optional ("nhuser") String username, @Optional ("1QAZ2wsx") String password) throws Exception {

		log.info("Host: "+capcServer);
		Simdepot sim = new Simdepot(driver);
		DiscoveryProfiles dp = new DiscoveryProfiles(driver);

		driver.get("http://"+username+":"+password+"@simdepot.ca.com");

		String[] sims = simIDs.split(",");
		ArrayList<String> ips = sim.getSimIP(sims);

		if (ips==null) {
			log.fatal("No IP addresses got from simdepot. Stopping.");
			return;
		}

		driver.get(capcServer);
		LoginPage loginpage = PageFactory.initElements(driver,LoginPage.class);
		loginpage.enterUsername("admin");
		loginpage.enterPassword("admin");
		loginpage.logIn();

		//Assert.assertTrue(dp.editDiscoveryProfiles(MonitoringDiscoveryProfileName, ips));

		DiscoveryProfilesPage dpp = new DiscoveryProfilesPage(driver);
		dpp.navigateToDataAggregator();
		dpp.gotoDiscoveryProfiles();
		Assert.assertTrue (dpp.editDiscoveryProfiles(MonitoringDiscoveryProfileName, ips));

	}


	//THIS IS TEST METHOD
	@Test(description="Create/Modify Discovery Profile", groups = {"CreateModifyDP1"})
	@Parameters({"simIDs","capcServer","MonitoringDiscoveryProfileName","PMF_User","PMF_Password"})
	public void createModifyDPtest(String simIDs, String capcServer, String MonitoringDiscoveryProfileName, @Optional ("nhuser") String username, @Optional ("1QAZ2wsx") String password) throws Exception {

		log.info("Host: "+capcServer);
		Simdepot sim = new Simdepot(driver);
		DiscoveryProfiles dp = new DiscoveryProfiles(driver);

		//driver.get("http://"+username+":"+password+"@simdepot.ca.com");

		String[] sims = simIDs.split(",");
		//ArrayList<String> ips = sim.getSimIP(sims);
		ArrayList<String> ips = new ArrayList();
		ips.add("192.168.1.1");

		if (ips==null) {
			log.fatal("No IP addresses got from simdepot. Stopping.");
			return;
		}

		driver.get(capcServer);
		LoginPage loginpage = PageFactory.initElements(driver,LoginPage.class);
		loginpage.enterUsername("admin");
		loginpage.enterPassword("admin");
		loginpage.logIn();

		//Assert.assertTrue(dp.editDiscoveryProfiles(MonitoringDiscoveryProfileName, ips));

		//DiscoveryProfilesPage dpp = PageFactory.initElements(driver,DiscoveryProfilesPage.class);
		DiscoveryProfilesPage dpp = new DiscoveryProfilesPage(driver);
		dpp.navigateToDataAggregator();
		dpp.gotoDiscoveryProfiles();
		dpp.editDiscoveryProfiles(MonitoringDiscoveryProfileName, ips);

	}



	@Test(description="Delete Discovered Devices", groups = {"DeleteDiscoveredDevices"})
	@Parameters({"capcServer"})
	public void deleteDiscoveredDevices (String capcServer) throws Exception {
					
		driver.get(capcServer);
		
		VCMF vcmf = new VCMF(driver);

		LoginPage loginpage = PageFactory.initElements(driver,LoginPage.class);
		loginpage.enterUsername("admin");
		loginpage.enterPassword("admin");
		loginpage.logIn();
		
		Assert.assertTrue(vcmf.deleteDiscoveredDevices());
		
	}
		
	
	@Test(description="Create/Modify Monitoring Profile For Already Certified US", groups = {"CreateModifyMPAlreadyCertified"})
	@Parameters({"readmeFile","MonitoringDiscoveryProfileName","capcServer"})
	public void CAPMtests(String readmeFile, String MonitoringDiscoveryProfileName, String capcServer) throws Exception {
		
		driver.get(capcServer);
		
		MonitoringProfiles mp = new MonitoringProfiles(driver);
		readmeParser parser = new readmeParser();

		LoginPage loginpage = PageFactory.initElements(driver,LoginPage.class);
		loginpage.enterUsername("admin");
		loginpage.enterPassword("admin");
		loginpage.logIn();
		
		ArrayList<String> mfList = new ArrayList<String>();
		mfList = parser.getMFsForAlreadyCertified(readmeFile);
		
		if (mfList==null) {
			log.fatal("No applicapable data parsed. Stopping current test-cases execution.");
			return;
		}
		
		Assert.assertTrue(mp.editMonitoringProfiles(2,mfList,MonitoringDiscoveryProfileName));
	}
	
	
	@Test(description="Verify VCs and components For Already Certified US", groups = {"VerifyVCandComponentsAlreadyCertified"})
	@Parameters({"capcServer", "readmeFile", "deviceName", "outputDir"})
	public void verifyVCalreadyCertified(String capcServer, String readmeFile, String deviceName, String outputDir) throws Exception {
		
		log.info("Host: " + capcServer);
		driver.get(capcServer);
		
		//Remove all special characters from deviceName
		String path1 = deviceName.replaceAll("[-+.^:,]","");
				
		//Get substring with CAPC server from capcServer
		Matcher matcher = Pattern.compile("(?<=http://)(.+)(?=:8181)").matcher(capcServer);
		matcher.find();
		String path2 = matcher.group();
				
		//Build a full output directory
		String fullOutputDir = outputDir+"\\"+path2+"_"+path1;
				
		//Create an output directory if not exists
		File outputDirectory = new File (fullOutputDir);
		if (!outputDirectory.exists()) {
			log.info("Create an output directory "+fullOutputDir);
			outputDirectory.mkdirs();
		} else {
			log.info("Directory "+fullOutputDir+" already exists.");
		}
		
		readmeParser parser = new readmeParser();
		VCMF vcmf = new VCMF (driver);

		LoginPage loginpage = PageFactory.initElements(driver,LoginPage.class);
		loginpage.enterUsername("admin");
		loginpage.enterPassword("admin");
		loginpage.logIn();

		ArrayList<String> vcList = new ArrayList<String>();
		vcList = parser.getVCsForAlreadyCertified(readmeFile);
		
		if (vcList==null) {
			log.fatal("No applicapable data parsed. Stopping current test-cases execution.");
			return;
		}
		
		Assert.assertTrue(vcmf.verifyVcAlreadyCertified(deviceName, vcList, fullOutputDir));
	}
	
	
	@Test(description="Create custom report", groups = {"CreateCustomReport"})
	@Parameters({"capcServer", "deviceName", "daServer", "daPassword", "outputDir", "readmeFile", "reportType"})
	public void createCustomReport(String capcServer, String deviceName, String daServer, String daPassword, String outputDir, String readmeFile, String reportType) throws Exception {

		log.info("Host: " + capcServer);
		readmeParser readme = new readmeParser();;
		CreateReport createReport = new CreateReport (driver);
		VCMF vcmf = new VCMF (driver);
		 
		 //Remove all special characters from deviceName
		String path1 = deviceName.replaceAll("[-+.^:,]","");
		
		//Get substring with CAPC server from capcServer
		Matcher matcher = Pattern.compile("(?<=http://)(.+)(?=:8181)").matcher(capcServer);
		matcher.find();
		String path2 = matcher.group();
		
		//Build a full output directory
		String fullOutputDir = outputDir+"\\"+path2+"_"+path1;
		
		//Create an output directory if not exists
		File outputDirectory = new File (fullOutputDir);
		if (!outputDirectory.exists()) {
			log.info("Create an output directory "+fullOutputDir);
			outputDirectory.mkdirs();
		} else {
			log.info("Directory "+fullOutputDir+" already exists.");
		}
		
		
		ArrayList<ArrayList<String>> metrics = new ArrayList<>();
		metrics = readme.getMetrics(readmeFile);	
		
		if (metrics==null) {
			log.fatal("No applicapable data parsed. Stopping current test-cases execution.");
			return;
		}

		ArrayList<ArrayList<String>> metricsHR = new ArrayList<>();	
		metricsHR = vcmf.convertMetricsToHRFormat(metrics,daServer,daPassword,fullOutputDir);

		driver.get(capcServer);

		LoginPage loginpage = PageFactory.initElements(driver,LoginPage.class);
		loginpage.enterUsername("admin");
		loginpage.enterPassword("admin");
		loginpage.logIn();
		
		//Add newly certified VC to a file
		String vendorInfoFile = fullOutputDir+"\\VendorInfo.txt";
		Boolean isVendorInfoExists=false;
		Boolean isFirstString=true;

		//IF Vendor Info file does not exists, will add a System Desription to it
		if (!new File(vendorInfoFile).exists()) {
			log.debug("Vendor Info file \""+vendorInfoFile+"\" does not exists.");
			isVendorInfoExists=false;
		} else {
			log.debug("Vendor Info file \""+vendorInfoFile+"\" already exists.");
			isVendorInfoExists=true;
		}

		FileWriter vendorCertifiedVCsFile = new FileWriter (vendorInfoFile, true);

		for (int i=0; i<metricsHR.size(); i++) {
			String [] element = vcmf.getVcElement(deviceName, metricsHR.get(i).get(0), metricsHR.get(i).get(1), fullOutputDir);			// Send VC name in mib format
			//String element = vcmf.getVcElement(vcmf.convertVCToHumanReadable(metrics.get(0).get(0)));   	// Send VC name in human readable format
			if (element != null) {
				// If Vendor Info file does not exists, will add a System Desription to it
				if (!isVendorInfoExists) {
					vendorCertifiedVCsFile.write("Device vendor: \n"+element[5]+"\n");
					isVendorInfoExists=true;
				}
				if (isFirstString) {
					vendorCertifiedVCsFile.write("Newly certified VCs:\n\n");
					isFirstString=false;
				}
				//Add VC in HR format to Vendor Info file
				vendorCertifiedVCsFile.write(element[3]+"\n");

				//Check if current VC has no components. Shoul try to run Device report in this case.
				if (element[0]==null) {
					log.warn("No components for this VC. Will run a Device Report instead.");
					Assert.assertTrue(createReport.createCustomTab(deviceName, 2, "QA", metricsHR.get(i),element[0],fullOutputDir,"Device"));
                } else {
					Assert.assertTrue(createReport.createCustomTab(deviceName, 2, "QA", metricsHR.get(i),element[0],fullOutputDir,reportType));
                }
			}
		}
		
		vendorCertifiedVCsFile.flush();
		vendorCertifiedVCsFile.close();
		
		/*
		for (int i=0; i<metrics.size(); i++) {
			for (int j=0; j<metrics.get(i).size(); j++) {
				System.out.println (metrics.get(i).get(j));
			}
		}
		*/
	}

	@Test(description="Add Data Aggregator", groups = {"AddDataAggregator"})
	@Parameters({"capcServer","dataAggregator"})
	public void addDataAggregator (String capcServer, String dataAggregator) throws InterruptedException {

		driver.get(capcServer);

		RegressionTests regTest = new RegressionTests(driver);

		LoginPage loginpage = PageFactory.initElements(driver,LoginPage.class);
		loginpage.enterUsername("admin");
		loginpage.enterPassword("admin");
		loginpage.logIn();;

		//Assert.assertTrue(regTest.addDataSource(dataAggregator));
		DataSourcesPage dsp = new DataSourcesPage(driver);
		dsp.navigateToDataSources();
		Assert.assertTrue(dsp.addDataAggreagtor(dataAggregator));


	}

	@Test(description="Assign Data Collector", groups = {"AssignDataCollector"})
	@Parameters({"capcServer"})
	public void assignDataCollector (String capcServer) throws InterruptedException {

		driver.get(capcServer);

		RegressionTests regTest = new RegressionTests(driver);

		LoginPage loginpage = PageFactory.initElements(driver,LoginPage.class);
		loginpage.enterUsername("admin");
		loginpage.enterPassword("admin");
		loginpage.logIn();

		Assert.assertTrue(regTest.assignDataCollector());

	}


	@Test(description="Search Test", groups = {"Regression: SearchTest"})
	@Parameters({"capcServer"})
	public void searchTest (String capcServer) throws InterruptedException {

		driver.get(capcServer);
		RegressionTests regTest = new RegressionTests(driver);

		LoginPage loginpage = PageFactory.initElements(driver,LoginPage.class);
		loginpage.enterUsername("admin");
		loginpage.enterPassword("admin");
		loginpage.logIn();

		Assert.assertTrue(regTest.searchTest());

	}

	@Test (description = "Import on-demand from CAPC GUI", groups = {"ImportOnDemandFromGUI"})
	@Parameters({"capcServer", "daServer", "ondemandLocation", "readmeFile"})
	public void importOndemand (String capcServer, String daServer, String ondemandLocation, String readmeFile) throws InterruptedException, IOException {


		driver.get(capcServer);

		LoginPage loginpage = PageFactory.initElements(driver,LoginPage.class);
		loginpage.enterUsername("admin");
		loginpage.enterPassword("admin");
		loginpage.logIn();


		//Get current MFs versions
		ArrayList<String> mfList = new readmeParser().getMFs(readmeFile);
		ArrayList<String[]> mfListWithVersion = new ArrayList<String[]>();
		for (int i=0; i<mfList.size(); i++) {
			String[] mfver = {mfList.get(i), new XMLParser().getVCMFVersionFromXMLResponse(new REST().getMFxml(daServer,mfList.get(i)))};
			mfListWithVersion.add(mfver);
		}

		//Get current VCs versions
		ArrayList<String> vcList = new readmeParser().getVCs(readmeFile);
		ArrayList<String[]> vcListWithVersion = new ArrayList<String[]>();
		for (int i=0; i<vcList.size(); i++) {
			String[] vcver = {vcList.get(i), new XMLParser().getVCMFVersionFromXMLResponse(new REST().getVCxml(daServer,vcList.get(i)))};
			vcListWithVersion.add(vcver);
		}


		//Apply on-demand
		OnDemand ondemand = new OnDemand(driver);
		Assert.assertTrue(ondemand.uploadOndemandFromGUI(ondemandLocation));


		//Get updated MF/VC vesrions and compare
		for (int i=0; i<mfList.size(); i++) {
			String newVer = new XMLParser().getVCMFVersionFromXMLResponse(new REST().getMFxml(daServer,mfList.get(i)));
			if (mfListWithVersion.get(i)[1]==null) {
				log.info("MF: " + mfList.get(i)+" - New Metric Mafily.");
			} else
				if (mfListWithVersion.get(i)[1].equals(newVer)) {
					log.info("MF: " + mfList.get(i) + " - version not changed ("+newVer+").");
				} else {
				log.info("MF: " + mfList.get(i) + " - version changed from "+mfListWithVersion.get(i)[1]+" to "+newVer);
			}
		}


		for (int i=0; i<vcList.size(); i++) {
			String newVer = new XMLParser().getVCMFVersionFromXMLResponse(new REST().getVCxml(daServer,vcList.get(i)));
			if (vcListWithVersion.get(i)[1]==null) {
				log.info("VC: " + vcList.get(i)+" - New Vendor Cert.");
			} else
				if (vcListWithVersion.get(i)[1].equals(newVer)) {
				log.info("VC: " + vcList.get(i)+" - version not changed ("+newVer+").");
				} else {
					log.info("VC: " + vcList.get(i) + " - version changed from "+vcListWithVersion.get(i)[1]+" to "+newVer);
				}
		}


	}


}
