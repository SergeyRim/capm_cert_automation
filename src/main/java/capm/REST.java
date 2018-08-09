package capm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class REST {

    private static final Logger log = LogManager.getLogger("REST");

    public String getVCxml (String da, String vc) throws IOException {
        String response = new HttpConn().get(new URL("http://"+da+":8581/typecatalog/certifications/snmp/"+vc));
        return response;
    }

    public String getMFxml (String da, String mf) throws IOException {
        String response = new HttpConn().get(new URL("http://"+da+":8581/typecatalog/metricfamilies/"+mf));
        return response;
    }
}
