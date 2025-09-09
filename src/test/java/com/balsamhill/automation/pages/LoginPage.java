package com.balsamhill.automation.pages;

import com.balsamhill.automation.drivers.DriverManager;
import com.balsamhill.automation.logger.LoggerWrapper;
import com.balsamhill.automation.utils.CredentialsUtils;
import com.balsamhill.automation.utils.WebElementUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {

    private static final LoggerWrapper log = new LoggerWrapper(LoginPage.class);

    private final WebDriver driver;

    private final By email = By.cssSelector("input[name='emailAddress']");
    private final By password = By.cssSelector("input[name='password']");
    private final By loginButton = By.cssSelector("button[data-testid='login-btn-login']");

    /**
     * Constructor initializes the WebDriver instance.
     */
    public LoginPage(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        this.driver = driver;
    }

    /**
     * Perform login using credentials from configuration
     * @throws InterruptedException
     */
    public void login() throws InterruptedException {

        setUsername();
        setPassword();
        WebElementUtils.click(loginButton);
        Thread.sleep(5000);
        log.step("Login submitted");
    }

    /**
     * Username should not contain special characters, so remove them
     */
    private void setUsername() {
        String username = CredentialsUtils.getUsername();

        WebElementUtils.type(email, username);
        log.step("Username entered: {}", username);
    }

    /**
     * Password may contain special characters, so only trim whitespace
     */
    private void setPassword() {
        String pass = CredentialsUtils.getPassword();
        WebElementUtils.type(password, pass);
        log.step("Password entered: {}", pass);
    }



}
