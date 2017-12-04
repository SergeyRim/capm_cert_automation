package capm;

import org.openqa.selenium.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegressionTests {

    private static final Logger log = LogManager.getLogger("RegressionTests");

    WebDriver driver;
    public RegressionTests (WebDriver driver) {

        this.driver = driver;
    }

    public Boolean searchTest () throws InterruptedException {

        Navigation navi = new Navigation (driver);
        //navi.selectDataAggregator();
        log.info("Hello, World!");
        log.debug("Oh my god!");
        log.fatal("FATAL LOG");

        /*
        navi.waitForElement("html/body/div[3]/div[2]/div/div/div/div[2]/div/div[2]/div/div/div/div[1]/div/div[2]/div/div/div/div/div/div/div[2]/div/div[1]/ul/div/li[3]/div/span[2]/a/span", 1);
        String test1 = driver.findElement(By.xpath("html/body/div[3]/div[2]/div/div/div/div[2]/div/div[2]/div/div/div/div[1]/div/div[2]/div/div/div/div/div/div/div[2]/div/div[1]/ul/div/li[3]/div/span[2]/a/span")).getText();
        Reporter.log("Text is "+test1);
        if (test1.equals("All ESX Hosts"))
            return true;
        else
            return false;
        */

        //File sourceFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        //FileUtils.copyFile(sourceFile, new File ("d:\\testScr.png"));
        return true;

    }

    public Boolean assignDataCollector () throws InterruptedException {
        Navigation navi = new Navigation (driver);

        navi.selectDataAggregator();
        navi.gotoDataCollector();
        Thread.sleep(500);

        //Check if at least one Data Collector exists
        if (driver.findElements (By.xpath("//div[@class='x-grid3-viewport']/div[2]/div/div/table/tbody/tr")).size()>0) {
            String dcname = driver.findElement(By.xpath("//div[@class='x-grid3-viewport']/div[2]/div/div/table/tbody/tr/td[3]/div")).getText();
            log.info("Find Data Collector \""+dcname+"\".");
            driver.findElement(By.xpath("//div[@class='x-grid3-viewport']/div[2]/div/div/table/tbody/tr/td[1]/div")).click();

            while (driver.findElements(By.xpath("//div[text()='Loading']")).size()>0)
                Thread.sleep(1000);
            Thread.sleep(500);

            log.info("Assigning Data Collector.");
            if (!navi.protectedClick("//button[text()='Assign']", "Unable to click on Assign button. Retrying."))
                return false;

            while (driver.findElements(By.xpath("//div[text()='Loading']")).size()>0)
                Thread.sleep(1000);

            //Wait while "Tenant:" field will be populated by "Default Tenant"
            navi.waitForElement("//span[text()='Tenant:']/../../div[1]/div/input[@type='text']", 1);
            while (!driver.findElement(By.xpath("//span[text()='Tenant:']/../../div[1]/div/input[@type='text']")).getAttribute("value").equals("Default Tenant")) {
                Thread.sleep(500);
            }

            //Wait while "IP Domain:" field will be populated by "Default Domain"
            navi.waitForElement("//span[text()='IP Domain:']/../../div[1]/div/input[@type='text']", 1);
            while (!driver.findElement(By.xpath("//span[text()='IP Domain:']/../../div[1]/div/input[@type='text']")).getAttribute("value").equals("Default Domain")) {
                Thread.sleep(500);
            }

            if (!navi.protectedClick("//button[text()='Save']", "Unable to click on Save button. Retrying."))
                return false;

            if (!navi.waitForElement("//span[text()='Assign Data Collector']", 2))
                return false;
            Thread.sleep(1000);
            log.info("Data Collector assigned succesfully.");

        } else {
            log.error("No Data Collector exists!");
            return false;
        }
        return true;
    }


    public Boolean addDataSource (String dataAggregator) throws InterruptedException {

        Navigation navi = new Navigation (driver);
        int waitForDa = 180;

        navi.selectDataSources();

        navi.protectedClick("//button[@type='button' and text()='Add']", "Unable to click Add button. Retrying.");

        //Wait while Add Data Source windows is loaded
        navi.waitForElement("//span[text()='Add Data Source']", 1);

        //Click on  Source type and select Data Aggregator
        navi.protectedClick("//span[text()='Source Type:']/../../div[1]/div/img", "Unable to click select button. Retrying.");
        navi.protectedClick("//div[text()='Data Aggregator']", "Unable to select Data Aggregator. Retrying.");

        //Add DA to Host Name
        driver.findElement(By.xpath("//span[text()='Host Name']/../../div/input")).sendKeys(dataAggregator);
        WebElement displayName = driver.findElement(By.xpath("//span[text()='Display Name']/../../div/input"));
        displayName.click();
        Thread.sleep(200);
        displayName.clear();
        Thread.sleep(200);
        displayName.sendKeys("Data Aggregator@"+dataAggregator);

        Thread.sleep(1000);

        //Click on Test button
        navi.protectedClick("//td[1]/table/tbody/tr[2]/td[2]/em/button[text()='Test']", "Unable to click on Test button. Retrying.");

        //Wait for OK button
        navi.waitForElement("//button[text()='OK']", 1);

        if (driver.findElements(By.xpath("//span[text()='Failed']")).size()>0) {
            log.fatal("Data Source test Failed!");
            return false;
        } else if (driver.findElements(By.xpath("//span[text()='Success']")).size()>0) {
            log.info("Succesfully tested data source \"" + dataAggregator + "\"");
            driver.findElement(By.xpath("//button[text()='OK']")).click();
            Thread.sleep(500);
            driver.findElement(By.xpath("//button[text()='Save']")).click();

            //Wait until window closed
            navi.waitForElement("//span[text()='Add Data Source']", 2);

            //Wait while DA string will appears
            navi.waitForElement("//div[text()='Data Aggregator@"+dataAggregator+"']", 1);

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
                    Thread.sleep(5000);

                }
            }

            if (waitNum==waitForDa) {
                log.fatal("Timedout waiting for DA to be available.");
                return false;
            } else {
                log.info("DA status became \"Available\".");
            }

        }

        return true;
    }


}
