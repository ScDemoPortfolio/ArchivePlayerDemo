package com.archiveplayer;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SearchUITest extends BaseUITest {

    @Test
    @Order(1)
    @DisplayName("SearchController: Test /search")
    void testGlobalSearch() {
        driver.get(getBaseUrl());
        loginAsAdmin();

        WebElement searchNav = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[.//span[text()='Search']]")));
        searchNav.click();

        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("search-input")));
        searchInput.sendKeys("Time");

        WebElement songResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("search-song-row")));
        assertTrue(songResult.isDisplayed(), "Search results not visible");
    }
}
