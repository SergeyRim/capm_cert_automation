package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class NavigationPage {

    public NavigationPage (WebDriver driver) {

        this.driver = driver;
        wait = new WebDriverWait(driver,60, 200).withMessage("ExpectedConditions timeout.");
    }

    WebDriver driver;
    Wait<WebDriver> wait;

    //@FindBy(how=How.XPATH, using = "//button[text()='Administration']")
    @FindBy(xpath = "//button[text()='Administration']")
    private WebElement administration;

    @FindBy(xpath = "//span[text()='Data Sources']")
    private WebElement dataSource;

    @FindBy(xpath = "//span[starts-with(text(),'Data Aggregator@')]")
    private WebElement dataAggregator;

    public void navigateToDataAggregator () throws InterruptedException {
        Actions action = new Actions(driver);
        wait.until(ExpectedConditions.visibilityOfAllElements(administration));
        action.moveToElement(administration).perform();
        wait.until(ExpectedConditions.visibilityOfAllElements(dataSource));
        action.moveToElement(dataSource).perform();
        wait.until(ExpectedConditions.visibilityOfAllElements(dataAggregator));
        dataAggregator.click();
    }



}
