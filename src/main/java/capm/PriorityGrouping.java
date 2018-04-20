package capm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriorityGrouping {

    private static final Logger log = LogManager.getLogger("PriorityGrouping");

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


}
