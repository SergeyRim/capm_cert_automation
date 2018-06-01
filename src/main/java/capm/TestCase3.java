package capm;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class TestCase3 {

	private static final Logger log = LogManager.getLogger("PriorityGroupTest");


	@BeforeMethod(alwaysRun = true)
	@Parameters({"logLevel"})
	public void beforeSetup(@Optional("") String logLevel) throws MalformedURLException {

		if (!logLevel.equals("") && !logLevel.toLowerCase().equals("info")) {
			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
			LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

			switch (logLevel.toLowerCase()) {
				case "debug":
					log.info("Changing log level to DEBUG according to XML file configuration.");
					loggerConfig.setLevel(Level.DEBUG);
					ctx.updateLoggers();
					break;
				case "error":
					log.info("Changing log level to ERROR according to XML file configuration.");
					loggerConfig.setLevel(Level.ERROR);
					ctx.updateLoggers();
					break;
				default:
					log.warn("Unsupported log level specified. Switching to INFO log level.");
					break;
			}
		}
	}
		
	@Test(description="Add Vendor Priority Grouping", groups = {"AddVendorPriorityGrouping"})
	@Parameters({"priorityFile","daServer"})
	public void createModifyVPGroup(String priorityFile, String daServer) throws Exception {

		log.info("Host: " + daServer);

		readmeParser parser = new readmeParser();
		PriorityGrouping vcmf = new PriorityGrouping ();
		String prevMF;
		String XMLandID[] = new String[2];
		
		
		ArrayList<String[]> vpData = new ArrayList<String[]>();
		vpData = parser.getVP(priorityFile);
		if (vpData==null) {
			log.error("No applicapable data parsed. Stopping current test-cases execution.");
			return;
		}
		
		prevMF="";

		for (int i=0;i<vpData.size();i++) {

			if (!vpData.get(i)[0].equals(prevMF) || i==0) {
				if (i!=0) {
					//Make a PUT request
					vcmf.putVendorCertPriorities(XMLandID[0], XMLandID[1], daServer);
				}
				XMLandID = vcmf.getVendorCertPriorities(vpData.get(i)[0], daServer);
				XMLandID[1] = XMLandID[1].replaceAll("<ID>(.*?)</ID>", "");
				XMLandID[1] = XMLandID[1].replaceAll("<MetricFamilyID>(.*?)</MetricFamilyID>", "");
			}

			//Check if VC exists in XML
			if (!XMLandID[1].contains("<VendorCertID>{"+vpData.get(i)[2]+"</VendorCertID>")) {
				log.error("VC \""+vpData.get(i)[2]+"\" not found!");
			} else {
				//Need to check if the group already exists for specific VC and if other groups are present
				Matcher matcher = Pattern.compile("(?<="+vpData.get(i)[2]+"</VendorCertID><PriorityGroup>)(.*?)(?=</PriorityGroup>)").matcher(XMLandID[1]);
				matcher.find();
				String existing_groups = matcher.group();
				
				if (existing_groups.equals("")) {
					log.info("Empty priority group for \""+vpData.get(i)[2]+"\" detected. Add new group \""+vpData.get(i)[1]+"\"");
					XMLandID[1] = XMLandID[1].replaceAll(vpData.get(i)[2]+"</VendorCertID><PriorityGroup>", vpData.get(i)[2]+"</VendorCertID><PriorityGroup>"+vpData.get(i)[1]);
				} else if (existing_groups.contains(vpData.get(i)[1])) {
					log.info("Priority group \""+vpData.get(i)[1]+"\" already exists for \""+vpData.get(i)[2]+"\"");
				} else {
					log.info("\""+vpData.get(i)[2]+"\" already has other groups. Add new group \""+vpData.get(i)[1]+"\"");
					XMLandID[1] = XMLandID[1].replaceAll(vpData.get(i)[2]+"</VendorCertID><PriorityGroup>", vpData.get(i)[2]+"</VendorCertID><PriorityGroup>"+vpData.get(i)[1]+",");
				}				
			}

			//If this is a last element, need to PUT data
			if (i==vpData.size()-1) {
				//Make a PUT Request
				vcmf.putVendorCertPriorities(XMLandID[0], XMLandID[1], daServer);
			}
			
			
			prevMF=vpData.get(i)[0];			
		}
	  
	}

}
