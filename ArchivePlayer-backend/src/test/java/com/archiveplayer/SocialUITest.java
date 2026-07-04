package com.archiveplayer;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SocialUITest extends BaseUITest {

    private void navigateToAdminProfile() {
        WebElement socialNav = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[.//span[text()='Social Hub']]")));
        socialNav.click();

        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Search for people...']")));
        searchInput.clear();
        searchInput.sendKeys("admin");

        WebElement profileCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("person-card")));
        profileCard.click();
    }

    @Test
    @Order(1)
    @DisplayName("Social: Test Follow and Unfollow")
    void testFollowUnfollow() {
        driver.get(getBaseUrl());
        
        // Register a separate user to follow/unfollow admin
        String testUser = "social_test_" + System.currentTimeMillis();
        registerAndLogin(testUser, "password123");

        navigateToAdminProfile();

        // 1. Follow
        WebElement followBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Follow']")));
        followBtn.click();

        // 2. Verify button text changes to Unfollow
        WebElement unfollowBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[text()='Unfollow']")));
        assertTrue(unfollowBtn.isDisplayed());

        // 3. Unfollow
        unfollowBtn.click();
        
        // 4. Verify it reverts to Follow
        WebElement followBtnAgain = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[text()='Follow']")));
        assertTrue(followBtnAgain.isDisplayed());
    }

    @Test
    @Order(2)
    @DisplayName("Social: Test Block and Unblock")
    void testBlockUnblock() {
        driver.get(getBaseUrl());
        
        // Use a new user for isolation
        String testUser = "block_test_" + System.currentTimeMillis();
        registerAndLogin(testUser, "password123");

        navigateToAdminProfile();

        // 1. Block
        WebElement blockBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Block']")));
        blockBtn.click();

        // 2. Verify button text changes to Unblock User
        WebElement unblockBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[text()='Unblock User']")));
        assertTrue(unblockBtn.isDisplayed());

        // 3. Unblock
        unblockBtn.click();

        // 4. Verify it reverts to Block
        WebElement blockBtnAgain = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[text()='Block']")));
        assertTrue(blockBtnAgain.isDisplayed());
    }

    private void registerAndLogin(String username, String password) {
        // Go to registration
        WebElement toggleLink = wait.until(ExpectedConditions.elementToBeClickable(By.className("toggle-link")));
        toggleLink.click();
        
        WebElement userIn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='Enter username']")));
        WebElement passIn = driver.findElement(By.cssSelector("input[placeholder='Enter password']"));
        
        userIn.sendKeys(username);
        passIn.sendKeys(password);
        driver.findElement(By.className("submit-btn")).click();
        
        // Wait for registration to complete and log in
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("login-message")));
        
        // Switch to login if not already there
        WebElement title = driver.findElement(By.className("login-title"));
        if (title.getText().contains("Sign up")) {
            driver.findElement(By.className("toggle-link")).click();
        }

        userIn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='Enter username']")));
        passIn = driver.findElement(By.cssSelector("input[placeholder='Enter password']"));
        
        userIn.clear();
        userIn.sendKeys(username);
        passIn.clear();
        passIn.sendKeys(password);
        driver.findElement(By.className("submit-btn")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("sidebar")));
    }
}