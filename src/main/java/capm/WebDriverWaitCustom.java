package capm;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Clock;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Sleeper;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebDriverWaitCustom<T> extends FluentWait<T> {


    public WebDriverWaitCustom (T input) {
        super(input);
    }

    public WebDriverWaitCustom (T input, Clock clock, Sleeper sleeper) {
        super (input, clock, sleeper);
    }

    public String newFunc() {
        return "This is test";
    }

}
