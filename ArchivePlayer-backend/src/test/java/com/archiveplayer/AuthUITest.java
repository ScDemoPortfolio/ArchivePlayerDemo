package com.archiveplayer;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthUITest extends BaseUITest {

    @Test
    @Order(1)
    @DisplayName("Register a new user")
    void testRegister() {
        driver.get(getBaseUrl());
        WebElement toggleLink = wait.until(ExpectedConditions.elementToBeClickable(By.className("toggle-link")));
        toggleLink.click();

        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Enter username']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[placeholder='Enter password']"));
        WebElement submitBtn = driver.findElement(By.className("submit-btn"));

        usernameInput.sendKeys("ui_test_" + System.currentTimeMillis());
        passwordInput.sendKeys("password123");
        submitBtn.click();

        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("login-message")));
        assertTrue(message.getText().contains("Account created"), "Registration success message not found");
    }

    @Test
    @Order(2)
    @DisplayName("Login as Admin")
    void testLogin() {
        loginAsAdmin();
        WebElement sidebar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("sidebar")));
        assertTrue(sidebar.isDisplayed());
    }

    @Test
    @Order(3)
    @DisplayName("Logout user")
    void testLogout() {
        loginAsAdmin();
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[contains(text(), 'Logout')]/ancestor::button")));
        logoutBtn.click();

        WebElement loginTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("login-title")));
        assertTrue(loginTitle.getText().contains("Log in"));
    }
}
