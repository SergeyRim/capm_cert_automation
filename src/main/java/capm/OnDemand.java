package capm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;


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

        Thread.sleep(1000);

        driver.setFileDetector(new LocalFileDetector());
        WebElement upload = driver.findElement(By.xpath("//input[@class='x-form-file']"));
        upload.sendKeys(ondemandLocation);
        Thread.sleep(500);
        navi.protectedClick("//tr[@class='x-toolbar-right-row']/.//button[text()='Import']","Unable to click on Import button.");

        Thread.sleep(2000);

        return true;
    }

}
