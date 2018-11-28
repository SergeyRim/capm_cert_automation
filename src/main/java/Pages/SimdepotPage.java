package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SimdepotPage {

    public SimdepotPage (WebDriver driver) {

        this.driver = driver;
        wait = new WebDriverWait(driver,60, 200).withMessage("ExpectedConditions timeout.");
    }

    WebDriver driver;
    Wait<WebDriver> wait;

    @FindBy(id="simidIn")
    private WebElement searchField;

    //@FindBy(how=How.PARTIAL_LINK_TEXT, using = simID)
    //private WebElement simIDLink;

    public void search (final String simID) throws InterruptedException {
        driver.switchTo().frame("control");
        searchField.clear();
        searchField.sendKeys(simID);
        searchField.sendKeys(Keys.ENTER);
        Thread.sleep(500);
        driver.switchTo().defaultContent();
        driver.switchTo().frame("present_selection");
    }

    public void clickOnSimID (final String simID) {
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText(simID)));
        driver.findElement(By.partialLinkText(simID)).click();
    }






}
