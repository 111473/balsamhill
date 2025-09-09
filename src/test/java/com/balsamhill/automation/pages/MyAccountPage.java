package com.balsamhill.automation.pages;

import com.balsamhill.automation.drivers.DriverManager;
import com.balsamhill.automation.logger.LoggerWrapper;
import com.balsamhill.automation.utils.WaitUtils;
import com.balsamhill.automation.utils.WebElementUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class MyAccountPage {

    private static final LoggerWrapper log = new LoggerWrapper(MyAccountPage.class);

    private final WebDriver driver;

    private final By searchInput = By.id("constructor-search-input");

    /**
     * Constructor initializes the WebDriver instance.
     */
    public MyAccountPage(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        this.driver = driver;
    }

    /**
     * Enter a search term into the search input field.
     *
     * @param term The search term to enter.
     */
    public void searchTerm(String term) {
        WebElementUtils.type(searchInput, term);
    }

    /**
     * Press Enter key to submit search
     */
    public void pressEnter() {
        WebElementUtils.pressEnter(searchInput);
    }

    /**
     * Perform a search for the given term.
     *
     * @param term The search term to enter and submit.
     */
    public void search(String term) {
        searchTerm(term);
        pressEnter();
        WaitUtils.waitForPageLoad();
        log.step("Search completed for term: {}", term);
    }

}
