package Pages;

import capm.Navigation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class DataSourcesPage extends BasePage {

    private static final Logger log = LogManager.getLogger("DiscoverySourcesPage");

    public DataSourcesPage (WebDriver driver) {
        super (driver);
    }

    By addButton = By.xpath("//button[@type='button' and text()='Add']");
    By sourceTypeMenu = By.xpath("//span[text()='Source Type:']/../../div[1]/div/img");
    By sourceTypeMenuItemDataAggregator = By.xpath("//div[text()='Data Aggregator']");
    By hostNameField = By.xpath("//span[text()='Host Name']/../../div/input");
    By displayNameField = By.xpath("//span[text()='Display Name']/../../div/input");
    By testButton = By.xpath("//td[1]/table/tbody/tr[2]/td[2]/em/button[text()='Test']");
    By okButton = By.xpath("//button[text()='OK']");
    By saveButton = By.xpath("//button[text()='Save']");


    public Boolean addDataAggreagtor (String dataAggregator) throws InterruptedException {

        //Wait for loading page
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[contains(text(),'EventManager@')]")));
        Thread.sleep(100);

        wait.until(ExpectedConditions.elementToBeClickable(addButton));
        driver.findElement(addButton).click();

        //Wait while Add Data Source windows is loaded
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//span[text()='Add Data Source']")));

        //Click on  Source type and select Data Aggregator
        wait.until(ExpectedConditions.elementToBeClickable(sourceTypeMenu));
        driver.findElement(sourceTypeMenu).click();

        wait.until(ExpectedConditions.elementToBeClickable(sourceTypeMenuItemDataAggregator));
        driver.findElement(sourceTypeMenuItemDataAggregator).click();

        //Add DA to Host Name
        //wait.until(ExpectedConditions.visibilityOfElementLocated(hostNameField));
        wait.until(ExpectedConditions.elementToBeClickable(hostNameField));
        driver.findElement(hostNameField).click();
        driver.findElement(hostNameField).sendKeys(dataAggregator);

        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(displayNameField));
        driver.findElement(displayNameField).click();

        wait.until(ExpectedConditions.not(ExpectedConditions.attributeContains(displayNameField,"class","x-form-invalid")));

        //Click on Test button
        wait.until(ExpectedConditions.elementToBeClickable(testButton));
        driver.findElement(testButton).click();

        //Wait for OK button
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(okButton));

        if (driver.findElements(By.xpath("//span[text()='Failed']")).size()>0) {
            log.fatal("Data Source test Failed!");
            return false;
        } else if (driver.findElements(By.xpath("//span[text()='Success']")).size()>0) {
            log.info("Succesfully tested data source \"" + dataAggregator + "\"");
            wait.until(ExpectedConditions.elementToBeClickable(okButton));
            driver.findElement(okButton).click();

            Thread.sleep(100);
            wait.until(ExpectedConditions.elementToBeClickable(saveButton));
            driver.findElement(saveButton).click();

            //Wait until window closed
            wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[text()='Add Data Source']"))));

            //Wait while DA string will appears
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[text()='Data Aggregator@"+dataAggregator+"']")));

            return waitForDaBecameAvailable();

        } else {
            log.fatal("Failed to test Data Aggregator. Unexpected return code.");
            return false;
        }
    }


    private Boolean waitForDaBecameAvailable () throws java.lang.InterruptedException {
        int waitForDa = 180;
        //Wait while current status
        int waitNum=0;
        Boolean isStatusChecked=false;

        String currentDAStatus;
        while (!isStatusChecked) {
            try {
                currentDAStatus = driver.findElement(By.xpath("//div[text()='Data Aggregator@"+dataAggregator+"']/../../td[2]/div")).getText();
                while (!currentDAStatus.equals("Available") && waitNum <= waitForDa) {
                    log.info("Current DA status is \""+currentDAStatus+"\". Waiting for update.");
                    driver.navigate().refresh();
                    Thread.sleep(10000);
                    waitNum++;
                    currentDAStatus = driver.findElement(By.xpath("//div[text()='Data Aggregator@"+dataAggregator+"']/../../td[2]/div")).getText();
                }
                isStatusChecked=true;
            } catch (Exception e) {
                log.warn("WARN: Unable to get current DA status. Retrying.");
                waitNum++;
                Thread.sleep(1000);
            }
        }
        if (waitNum==waitForDa) {
            log.fatal("Timedout waiting for DA to be available.");
            return false;
        } else {
            log.info("DA status became \"Available\".");
        }
        return true;
    }

}
