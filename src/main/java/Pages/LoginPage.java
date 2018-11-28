package Pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage {

	private static final Logger log = LogManager.getLogger("LoginPage");
	WebDriver driver;

	/*
	By usernamefield = By.id ("txtUsername");
	By passwordfield = By.id("txtPassword");
	By loginbutton = By.className("sso-button");
	*/

	@FindBy(id="txtUsername")
	private WebElement usernamefield;

	@FindBy(id="txtPassword")
	private WebElement passwordfield;

	@FindBy(className = "sso-button")
	private WebElement loginbutton;
	
	public LoginPage enterUsername(String txt_username) {
		//WebElement username = driver.findElement(usernamefield);
		usernamefield.click();
		usernamefield.sendKeys(txt_username);
		return this;
	}
	
	public LoginPage enterPassword(String txt_password) {
		//WebElement password = driver.findElement(passwordfield);
		passwordfield.clear();
		passwordfield.sendKeys(txt_password);
		return this;
	}
	
	public LoginPage logIn() {
		//WebElement login = driver.findElement(loginbutton);
		loginbutton.click();
		return this;
	}
	
	public LoginPage (WebDriver driver) {
		this.driver = driver;
	}
}
