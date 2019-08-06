package Pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.interactions.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage {

    WebDriver driver;
    Wait<WebDriver> wait;
    private static final Logger log = LogManager.getLogger("BasePage");

    public BasePage (WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver,30, 200).withMessage("ExpectedConditions timeout.");
    }

    //@FindBy(xpath = "//button[text()='Administration']")
    //public WebElement administration;
    //public WebElement administration = driver.findElement(By.xpath("//button[text()='Administration']"));
    By administration = By.xpath("//button[text()='Administration']");

    //@FindBy(xpath = "//span[text()='Data Sources']")
    //public WebElement dataSource;
    //public WebElement dataSource = driver.findElement(By.xpath("//span[text()='Data Sources']"));
    By dataSource = By.xpath("//span[text()='Data Sources']");
    By dataSourceLink = By.xpath("//a[@class='x-menu-item x-unselectable']/span[text()='Data Sources']");

    //@FindBy(xpath = "//span[starts-with(text(),'Data Aggregator@')]")
    //public WebElement dataAggregator;
    //public WebElement dataAggregator = driver.findElement(By.xpath("//span[starts-with(text(),'Data Aggregator@')]"));
    By dataAggregator = By.xpath("//span[starts-with(text(),'Data Aggregator@')]");


    public void navigateToDataAggregator () {
        Actions action = new Actions(driver);
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(administration));
        action.moveToElement(driver.findElement(administration)).perform();
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(dataSource));
        action.moveToElement(driver.findElement(dataSource)).perform();
        wait.until(ExpectedConditions.elementToBeClickable(dataAggregator));
        driver.findElement(dataAggregator).click();
    }


    public void navigateToDataSources () {
        Actions action = new Actions(driver);
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(administration));
        action.moveToElement(driver.findElement(administration)).perform();
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(dataSource));
        action.moveToElement(driver.findElement(dataSource)).perform();
        wait.until(ExpectedConditions.elementToBeClickable(dataSourceLink));
        driver.findElement(dataSourceLink).click();
    }

    protected WebElement getWebElement (String xpath) throws InterruptedException {
        int timeout = 1000*40;
        int waiting = 0;

        while (driver.findElements(By.xpath(xpath)).size()<1) {
            Thread.sleep(500);
            waiting+=500;
            if (waiting>=timeout)
                break;
        }

        if (waiting>=timeout) {
            log.fatal("getWebElement: Timed out waiting for element "+xpath);
            return null;
        } else {
            log.debug("getWebElement: Succesful on "+xpath);
            return driver.findElement(By.xpath(xpath));
        }

    }

    protected Boolean waitForElement (String xpath, int type) throws InterruptedException {
        // int type
        // "1" = Wait for element which should BE PRESENT
        // "2" = Wait for element which should BE ABSENT

        int timeout = 1000*40;
        int waiting = 0;

        if (type==1) {
            while (driver.findElements(By.xpath(xpath)).size()<1) {
                Thread.sleep(500);
                waiting+=500;
                if (waiting>=timeout)
                    break;
            }
        }

        if (type==2) {
            while (driver.findElements(By.xpath(xpath)).size()>0) {
                Thread.sleep(500);
                waiting+=500;
                if (waiting>=timeout)
                    break;
            }
        }

        if (waiting>=timeout) {
            log.fatal("waitForElement: Timed out waiting for element "+xpath);
            return false;
        } else {
            log.debug("waitForElement: Successful on "+xpath);
            return true;
        }

    }

    protected Boolean protectedClick (String xpath, String errorMessage) throws InterruptedException {

        int maxNumberToTry=15;
        Boolean isClicked = false;
        int tryNum=0;

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        Thread.sleep(200);

        do {
            try {
                isClicked=true;
                WebElement elementToClick = driver.findElement(By.xpath(xpath));
                elementToClick.click();
            } catch (Exception e) {
                isClicked=false;
                tryNum++;
                Thread.sleep(1000);
                log.warn(errorMessage);
            }
        } while (!isClicked && tryNum<maxNumberToTry);

        if (tryNum==maxNumberToTry) {
            log.fatal("protectedClick: Failed on "+xpath);
            return false;
        } else {
            log.debug("protectedClick: Successful on "+xpath);
            return true;
        }

    }

    protected Boolean protectedDoubleClick (String xpath, String errorMessage) throws InterruptedException {

        int maxNumberToTry=15;
        Boolean isClicked = false;
        int tryNum=0;
        Actions action = new Actions (driver);

        do {
            try {
                isClicked=true;
                doubleClickOnXpath(xpath);
            } catch (Exception e) {
                isClicked=false;
                tryNum++;
                Thread.sleep(1000);
                log.warn(errorMessage);
            }
        } while (!isClicked && tryNum<maxNumberToTry);

        if (tryNum==maxNumberToTry) {
            log.fatal("protectedDoubleClick: Failed on "+xpath);
            return false;
        } else {
            log.debug("protectedDoubleClick: Successful on "+xpath);
            return true;
        }

    }

    public boolean doubleClickOnXpath (String xpath) throws InterruptedException {

        Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
        String browserName = cap.getBrowserName().toLowerCase();

        if (browserName.equals("firefox")) {
            log.debug("Firefox browser detected. Performing a javascript doubleclick.");
            scrollToWebElement(driver.findElement(By.xpath(xpath)));
            driver.findElement(By.xpath(xpath)).click();
            ((JavascriptExecutor)driver).executeScript("var evt = document.createEvent('MouseEvents');" + "evt.initMouseEvent('dblclick',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);" + "arguments[0].dispatchEvent(evt);",driver.findElement(By.xpath(xpath)));

        } else {
            log.debug("Non-Firefox browser detected. Performing an Actions doubleclick.");
            Actions action = new Actions (driver);
            WebElement elementToClick = driver.findElement(By.xpath(xpath));
            action.doubleClick(elementToClick).build().perform();
        }
        return true;
    }

    public boolean scrollToWebElement (WebElement element) {
        log.debug("Scroll to WebElement "+ element);
        Coordinates coordinate = ((Locatable)element).getCoordinates();
        coordinate.onPage();
        coordinate.inViewPort();
        return true;
    }


}
