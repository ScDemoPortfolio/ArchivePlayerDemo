package com.archiveplayer;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlaylistUITest extends BaseUITest {

    @Test
    @Order(1)
    @DisplayName("PlaylistController: Test /create")
    void testCreatePlaylist() {
        driver.get(getBaseUrl());
        loginAsAdmin();

        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.className("add-playlist-btn")));
        addBtn.click();

        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Playlist name...']")));
        String playlistName = "UI_Test_" + System.currentTimeMillis();
        nameInput.sendKeys(playlistName);
        driver.findElement(By.className("save-btn")).click();

        WebElement playlistToggle = wait.until(ExpectedConditions.elementToBeClickable(By.className("playlists-toggle-btn")));
        playlistToggle.click();

        WebElement playlistInSidebar = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[@class='playlist-name-sidebar' and text()='" + playlistName + "']")));
        
        assertTrue(playlistInSidebar.isDisplayed(), "New playlist not found in sidebar after expansion");
    }
}