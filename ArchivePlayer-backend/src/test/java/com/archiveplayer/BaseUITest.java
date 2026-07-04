package com.archiveplayer;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public abstract class BaseUITest {

    protected static Properties testProps = new Properties();
    protected WebDriver driver;
    protected WebDriverWait wait;
    
    // Create a new user per test to avoid password mismatch or "User exists" errors
    protected final String TEST_USER = "user_" + System.currentTimeMillis();
    protected final String TEST_PASS = "pass123";

    @BeforeAll
    static void loadProperties() throws IOException {
        try (InputStream input = BaseUITest.class.getClassLoader()
                .getResourceAsStream("test-settings.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find test-settings.properties");
            }
            testProps.load(input);
        }
    }

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(testProps.getProperty("test.webdriver.headless"))) {
            options.addArguments("--headless");
        }
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(
                Long.parseLong(testProps.getProperty("test.webdriver.wait-seconds"))));
        
       // driver.get(getBaseUrl() + "/?key=arch-access-99");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected String getBaseUrl() {
        return testProps.getProperty("test.frontend.url");
    }

    protected void loginAsAdmin() {
        driver.get(getBaseUrl());
        
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("login-title")));
        if (!title.getText().toLowerCase().contains("sign up")) {
            driver.findElement(By.className("toggle-link")).click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.className("login-title"), "Sign up"));
        }

        driver.findElement(By.cssSelector("input[placeholder='Enter username']")).sendKeys(TEST_USER);
        driver.findElement(By.cssSelector("input[placeholder='Enter password']")).sendKeys(TEST_PASS);
        driver.findElement(By.className("submit-btn")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.className("login-title"), "Log in"));

        WebElement loginUserIn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='Enter username']")));
        WebElement loginPassIn = driver.findElement(By.cssSelector("input[placeholder='Enter password']"));
        
        loginUserIn.clear();
        loginUserIn.sendKeys(TEST_USER);
        loginPassIn.clear();
        loginPassIn.sendKeys(TEST_PASS);
        
        driver.findElement(By.className("submit-btn")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("sidebar")));
    }
}