package capm;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class readmeParser {

	private static final Logger log = LogManager.getLogger("readmeParser");
	
	public ArrayList<String[]> getVP (String vpFileLocation) {
		
		ArrayList<String[]> vpData = new ArrayList<String[]>();
		int mfCount=0, groupCount=0, vcCount=0;
		
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(vpFileLocation), StandardCharsets.UTF_8))){
			String line, line_prev="";
	        	        
	        while ((line = reader.readLine()) != null) {
	        	line = line.replace(" ", "");
	        	line = line.trim();
	        	if (line.contains("Normalized")) {
	        		
	        		// vpElement[0] = MF (NormalizedPortInfo)
	        		// vpElement[1] = VP group name (DSLIFStatLS)
	        		// vpElement[2] = VC ({http://im.ca.com/certifications/snmp}ADSLwithMIB2StatsMib)
	        		mfCount++;
	        		groupCount++;
	        		vcCount++;
	        		String[] vpElement = new String[3];
	       		
	        		vpElement[0]=line.substring(0,line.length()-1);
	            	
	        		line = reader.readLine().trim();
	        		vpElement[1]=line.substring(0,line.length()-1);
	        		
	        		line = reader.readLine().trim();
	        		vpElement[2]=line.substring(1,line.length());
	        		
	            	//Add String array to ArrayList
	        		vpData.add(vpElement);
	            	line_prev=line;
	            	continue;
	        	}
	            
	        	if (line.startsWith("{")) {
	        		vcCount++;
	        		String[] vpElement = new String[3];
            		vpElement[0]=vpData.get(vpData.size()-1)[0];
            		vpElement[1]=vpData.get(vpData.size()-1)[1];
            		vpElement[2]=line.substring(1,line.length());
	        		//Add String array to ArrayList
	            	vpData.add(vpElement);
	            	line_prev=line;
	            	continue;
        		}
	        	
	        	if (line.contains(":") && !line.contains("Normalized") && line_prev.startsWith("{")){
	        		groupCount++;
	        		vcCount++;
	        		String[] vpElement = new String[3];
	        		vpElement[0]=vpData.get(vpData.size()-1)[0];
	        		vpElement[1]=line.substring(0,line.length()-1);
	        		line = reader.readLine().trim();
	        		vpElement[2]=line.substring(1,line.length());
	        		vpData.add(vpElement);	
	        		line_prev=line;
	        	}
		        	
	        }
	                  
	        } catch (IOException e) {
			log.fatal("Can't read from buffer "+e.toString());
	        }
		
		log.info("Parsed "+mfCount+" facet MFs, "+groupCount+" priority groups and "+vcCount+" vendor certs from priority file.");
		if (mfCount==0 || groupCount==0 || vcCount==0)
			return null; 
		else 
			return vpData;
	}
	
	public ArrayList<ArrayList<String>> getMetrics(String readmeFileLocation){
		
		log.info("Run readmeParser.getMetrics");
		ArrayList<ArrayList<String>> metricsList = new ArrayList<>();
		int variable=0,metric=0;

		// metricsList array
		//
		//	VC1name	| MF1name	| metric1	| metric2	|	...
		//	VC2name	| MF2name	| metric1	| metric2	|	...
		//	VC3name	| MF3name	| metric1	| metric2	|	...
		//	...		| ...		| ...		| ...		|	...
		
				
		try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(readmeFileLocation), StandardCharsets.UTF_8))){
            String line;
            String[] parts = new String [2];
            String[] parts2 = new String [2];
            
            while ((line = reader.readLine()) != null) {
            	line = line.replace(" ", "");
            	if (line.contains("VC:")) {
                	parts = line.split("VC:");
                	
                	if (parts[1].trim().charAt(0) != '<') {
                		parts2 = parts[1].split("\\|");
                		metricsList.add(new ArrayList<String>());
                		
                		//Add VC name as 1st element in array
                		metricsList.get(variable).add(parts2[0].trim());
                		
                		//Add MF Name as 2nd element in array
                		parts2 = parts[1].split("MF:");
                		metricsList.get(variable).add(parts2[1].trim());
                		
                    	variable++;
                    	//System.out.println(parts2[0]);              		
                	}
                	
                }
                
                if (line.contains("Metric:")) {
                	parts = line.split("Metric:");
                	if (parts[1].charAt(0) != '<' ) {
                		metric++;
                    	metricsList.get(variable-1).add(parts[1].trim());
                    	//System.out.println(parts[1]);
                	}
                	
                }
            }
        } catch (IOException e) {
			log.fatal("Can't read from buffer "+e.toString());
        }

        log.info("Loaded "+variable+" vendor cert(s) and "+metric+" metrics from readme file.");

	if (variable==0 || metric==0)
		return null; 
	else 
		return metricsList;
	
	}
	

public ArrayList<String> getMFs(String readmeFileLocation){
	
	log.info("Executing readmeParser.getMFs");
	int mfCount = 0;
	
	//ArrayList<String> mfList = new ArrayList<String>();
	HashSet<String> mfList = new HashSet<String>();
	
	try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(
					new FileInputStream(readmeFileLocation), StandardCharsets.UTF_8))){
		String line;
        String[] parts = new String [2];
        
        while ((line = reader.readLine()) != null) {
        	line = line.replace(" ", "");
        	if (line.contains("MF:")) {
        		parts = line.split("MF:");
                if (parts[1].trim().charAt(0) != '<') {
                	mfList.add(parts[1].trim());
                	mfCount++;
                }
                	                    	
            }
        }
           
        } catch (IOException e) {
			log.fatal("Can't read from buffer "+e.toString());
        }
	
	log.info("Loaded "+mfCount+" metric families from readme file.");

	//ArrayList<String> result = new ArrayList<String>(new HashSet<String>(mfList));
	ArrayList<String> result = new ArrayList<String>(mfList);
	
	if (mfCount==0)
		return null; 
	else 
		return result;
		
	}



public ArrayList<String[]> getMFsVCs(String readmeFileLocation){
	
	log.info("Executing readmeParser.getMFsVCs");
	int vcCount = 0;
	
	ArrayList<String[]> vcmfList = new ArrayList<String[]>();
	
	
	try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(
					new FileInputStream(readmeFileLocation), StandardCharsets.UTF_8))){
		String line;
        String[] parts, parts2 = new String [2];
        
        
        while ((line = reader.readLine()) != null) {
        	line = line.replace(" ", "");
        	if (line.contains("VC:")) {
            	parts = line.split("VC:");
            	
            	if (parts[1].trim().charAt(0) != '<') {
            		parts2 = parts[1].split("\\|");
            		
            		String[] vcmf = new String[2];
            		
            		//Add a VC name as 1st element of String array
            		vcmf[0]=parts2[0].trim();
            		
            		//Add a MF name as 2nd element of String array
            		parts2 = parts[1].split("MF:");
            		vcmf[1]=parts2[1].trim();
            		
            		//Add String array to ArrayList
            		vcmfList.add(vcmf);  
            		
            		vcCount++;
            	}
            }
        }       
                  
        } catch (IOException e) {
			log.fatal("Can't read from buffer "+e.toString());
        }
	
	log.info("Loaded "+vcCount+" vendor certs from readme file.");

	if (vcCount==0)
		return null; 
	else 
		return vcmfList;
}

	

public ArrayList<String> getMFsForAlreadyCertified(String readmeFileLocation){
	
	log.debug("Executing readmeParser.getMFsForAlreadyCertifierd");

	HashSet<String> mfList = new HashSet<String>();
	int mfCount = 0;
	
	try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(
					new FileInputStream(readmeFileLocation), StandardCharsets.UTF_8))){
		String line;
		String[] parts = new String [2];
        
        while ((line = reader.readLine()) != null) {
        	if (line.contains(" :")) 
        		line=line.replace(" :", ":");
        	if (line.contains("Metric Family:") || line.contains("MF:")) {
				if (line.contains("Metric Family:"))
        			parts = line.split("Metric Family:");
				if (line.contains("MF:"))
					parts = line.split("MF:");
        		if (parts[1].contains("|")) {
					mfList.add(parts[1].split("\\|")[0].trim());
				} else {
					mfList.add(parts[1].trim());
				}
        		mfCount++;
        	}
        		                    	
        }
           
        } catch (IOException e) {
			log.fatal("Can't read from buffer "+e.toString());
        }
	
	log.info("Loaded "+mfCount+" metric families from MF file.");

	ArrayList<String> result = new ArrayList<String>(mfList);
	
	if (mfCount==0)
		return null; 
	else
		result = replaceWrongMfNames(result);
		return result;
	}


public ArrayList<String> getVCsForAlreadyCertified(String readmeFileLocation){
	
	log.debug("Executing readmeParser.getVCFsForAlreadyCertifierd");

	HashSet<String> vcList = new HashSet<String>();
	int mfCount = 0;
	
	try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(
					new FileInputStream(readmeFileLocation), StandardCharsets.UTF_8))){
		String line;
		String[] parts = new String [2];
        
        while ((line = reader.readLine()) != null) {
        	if (line.contains(" :")) 
        		line=line.replace(" :", ":");
        	if (line.contains("Vendor Cert:") || line.contains("VC:")) {
				if (line.contains("Vendor Cert:"))
        			parts = line.split("Vendor Cert:");
				if (line.contains("VC:"))
					parts = line.split("VC:");
				if (parts[1].contains("|")) {
					vcList.add(parts[1].split("\\|")[0].trim());
				} else {
					vcList.add(parts[1].trim());
				}
        		mfCount++;
        	}
        		                    	
        }
           
        } catch (IOException e) {
			log.fatal("Can't read from buffer "+e.toString());
        }
	
	log.info("Loaded "+mfCount+" vendor certs from MF file.");

	ArrayList<String> result = new ArrayList<String>(vcList);
	
	if (mfCount==0)
		return null; 
	else 
		return result;
	}


	public ArrayList<String> replaceWrongMfNames (ArrayList<String> mfArray) {

		log.debug("Executing replaceWrongMfNames method.");
		ArrayList<String[]> replaceList = new ArrayList<String[]>();

		//Fill in a replaceList  TODO: Can change to read it from a separate configuration file
		log.debug("Filling pre-defined replacce list.");
		replaceList.add(new String[]{"IPv6 Stats","IPv6"});
		replaceList.add(new String[]{"IPv4 Stats","IPv4"});
		replaceList.add(new String[]{"Physical Memory","Memory"});
		replaceList.add(new String[]{"Udp Stats","UDP Statistics"});
		replaceList.add(new String[]{"UDP Stats","UDP Statistics"});
		replaceList.add(new String[]{"TCP Stats","TCP Statistics"});
		replaceList.add(new String[]{"Disk","Generic Disk"});
		replaceList.add(new String[]{"Environmental Sensor - VoltageDC","Environmental Sensor - Voltage DC"});
		replaceList.add(new String[]{"Environmental Sensor - Sound","Environmental Sensor - Sound Intensity"});
		replaceList.add(new String[]{"Qos ClassMap","QoS ClassMap"});
		replaceList.add(new String[]{"Environmental Sensor - VoltageAC","Environmental Sensor - Voltage AC"});
		replaceList.add(new String[]{"Environmental Sensor - Electric Current","Environmental Sensor - Electric Current Status"});

		log.debug("Changing wrong metric families if needed.");
		for (int i=0; i<mfArray.size();i++) {
			for (int j=0; j<replaceList.size();j++){
				if (mfArray.get(i).equals(replaceList.get(j)[0])) {
					log.info("Replacing wrong metric family name \""+replaceList.get(j)[0]+"\" with \""+replaceList.get(j)[1]+"\".");
					mfArray.set(i,replaceList.get(j)[1]);
				}
			}
		}

		return mfArray;
	}
	

	
}
	

