package capm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;


public class OnDemand {

    private static final Logger log = LogManager.getLogger("OnDemand");
    RemoteWebDriver driver;
    WebDriverWait wait;

    public OnDemand (RemoteWebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver,20);
    }


    public Boolean uploadOndemandFromGUI (String ondemandLocation) throws InterruptedException {

        Navigation navi = new Navigation(driver);

        navi.selectDataAggregator();
        navi.gotoVendorCertifications();

        navi.protectedClick("//button[text()='Import']","Unable to click on Import button");
        Thread.sleep(500);

        WebElement upload = driver.findElement(By.xpath("//input[@class='x-form-file']"));
        upload.sendKeys(ondemandLocation);
        Thread.sleep(500);
        navi.protectedClick("//tr[@class='x-toolbar-right-row']/.//button[text()='Import']","Unable to click on Import button.");
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.xpath("//span[text()='Import Vendor Certification']"))));
        Thread.sleep(500);
        log.info("On-demand imported succesfully.");

        return true;
    }





//       <test name="Test">
//    <parameter name="capcServer" value="http://rimse01-pc-dev.bluesky.ca.com:8181" />
//        <parameter name="ondemandLocation" value="d:\CAPM\DE343355_Oneaccess_ondemand_3.2_3.5.zip" />
//    <groups> <run>
//    <include name="ImportOnDemandFromGUI"/>
//    </run></groups>
//    <classes> <class name="capm.TestCase2" /> </classes>
//    </test>

}
