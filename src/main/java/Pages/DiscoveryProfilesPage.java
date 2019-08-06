package Pages;

import capm.Navigation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryProfilesPage extends DataAggregatorPage {

    private static final Logger log = LogManager.getLogger("DiscoveryProfilesPage");

    public DiscoveryProfilesPage (WebDriver driver) {
        super (driver);
    }

    By dpNameField = By.xpath("//input[@name='name']");
    By editButton = By.xpath("//button[text()='Edit']");
    By newButton = By.xpath("//button[text()='New']");
    By runButton = By.xpath("//button[text()='Run']");



    public Boolean editDiscoveryProfiles (String dpName, ArrayList<String> ips) throws InterruptedException {

        log.info("Edit Discovery profile \""+dpName+"\".");
        Boolean isClicked = false;

        log.debug("Wait while Discovery Profiles list will be loaded");
        wait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[@class='x-grid3-header-label' and text()='Name']")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//div[text()='No Data To Display']"))));

        log.debug("Check if our profile already exists");
        List<WebElement> selectDP = driver.findElements(By.xpath("//div[text()='"+dpName+"']"));
        if (selectDP.size()==0) {
            log.info("Discovery profile "+dpName+" not found. Create a new one.");
            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(newButton));
            driver.findElement(newButton).click();
            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(dpNameField));
            driver.findElement(dpNameField).sendKeys(dpName);
        } else {
            log.info("Found existing "+dpName+" profile. Modify it.");
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='"+dpName+"']")));
            driver.findElement(By.xpath("//div[text()='"+dpName+"']")).click();
            wait.until(ExpectedConditions.visibilityOfAllElements(driver.findElements(editButton)));
            driver.findElement(editButton).click();

            log.debug("Remove all existing IPs.");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='x-tab-panel-body x-tab-panel-body-noborder x-tab-panel-body-top']/div[1]/div/div/div/div/div/div/div[2]/div/div[1]/div[2]/div/div")));

            List<WebElement> existingIPs = driver.findElements(By.xpath("//div[@class='x-tab-panel-body x-tab-panel-body-noborder x-tab-panel-body-top']/div[1]/div/div/div/div/div/div/div[2]/div/div[1]/div[2]/div/div"));

            if (existingIPs.size()>0) {
                Actions action = new Actions (driver);
                action.keyDown(Keys.CONTROL).click(existingIPs.get(0));
                for (int i=1; i<existingIPs.size(); i++) {
                    action.click(existingIPs.get(i));
                }
                action.keyUp(Keys.CONTROL);
                action.build().perform();
                WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[5]/table/tbody/tr[2]/td[2]/em/button[text()='Delete']")));
                deleteButton.click();
            }
        }

        WebElement ipaddrFiled = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[1]/div/table/tbody/tr/td[1]/table/tbody/tr/td[1]/div/input")));
        for (int i=0; i<ips.size(); i++) {

            ipaddrFiled.sendKeys(ips.get(i));
            WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Add']")));
            addButton.click();
        }

        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Save']")));
        saveButton.click();

        log.debug("Waiting for closing Discovery Profile window");
        wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[@class='x-window-header-text' and text()='Discovery Profile']"))));

        //Need to wait while list can be reloaded several times
        Thread.sleep(2000);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='"+dpName+"']")));
        driver.findElement(By.xpath("//div[text()='"+dpName+"']")).click();

        //Run discover
        log.info("Running discover profile...");

        wait.until(ExpectedConditions.elementToBeClickable(runButton));
        driver.findElement(runButton).click();

        log.debug("Wait for \"Run Discovery Profile\" window opened");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='Run Discovery Profile']")));

        WebElement yesButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Yes']")));
        yesButton.click();

        log.debug("Wait for \"Run Discovery Profile\" window closed");
        wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[text()='Run Discovery Profile']"))));

        Thread.sleep(2000);
        log.info("Success.");

        return true;
    }
}
