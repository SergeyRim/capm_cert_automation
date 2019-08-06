package Pages;

import capm.Navigation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DataAggregatorPage extends BasePage {

    private static final Logger log = LogManager.getLogger("DataAggregatorPage");
    //WebDriver driver;
    //Wait<WebDriver> wait;

    public DataAggregatorPage (WebDriver driver) {

        super (driver);
        //this.driver = driver;
        //wait = new WebDriverWait(driver,60, 200).withMessage("ExpectedConditions timeout.");
    }

    By dicoveryProfiles = By.xpath("//span[text()='Discovery Profiles']");

    public DataAggregatorPage gotoMonitoringProfiles() throws InterruptedException {

        WebElement monitoringConfiguration = getWebElement("//span[text()='Monitoring Configuration']");
        monitoringConfiguration.click();
        Thread.sleep(500);

        log.debug("Move mouse from Monitoring Configuration link to avoid popup text.");
        Actions builder = new Actions(driver);
        builder.moveToElement(monitoringConfiguration,300,300).build().perform();
        Thread.sleep(500);

        WebElement monitoringProfiles = getWebElement("//span[text()='Monitoring Profiles']");
        monitoringProfiles.click();
        Thread.sleep(500);

        return this;
    }


    public DataAggregatorPage gotoDataCollector() throws InterruptedException {

        //Check if "System Status" tab is collapsed (not expanded)
        if (driver.findElements(By.xpath("//span[text()='System Status']/../../parent::div[contains(@class,'x-panel-collapsed')]")).size()>0) {
            driver.findElement(By.xpath("//span[text()='System Status']")).click();
            Thread.sleep(500);
        }

        //Click on Data Collector link
        driver.findElement(By.xpath("//span[text()='Data Collectors']")).click();
        Thread.sleep(500);

        while (driver.findElements(By.xpath("//div[text()='Loading...']")).size()>0)
            Thread.sleep(500);

        return this;
    }

    public DataAggregatorPage gotoDiscoveryProfiles() throws InterruptedException {

        wait.until(ExpectedConditions.visibilityOfElementLocated(dicoveryProfiles));
        driver.findElement(dicoveryProfiles).click();
        return this;
    }


}
