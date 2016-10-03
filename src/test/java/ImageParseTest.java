import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;


public class ImageParseTest {
    private WebDriver driver;
    private String loc;

    @Test
    public void setup(){
        //Set up system properties for ChromeDriver and instantiate a new driver
        System.setProperty("webdriver.chrome.driver", "C:\\Includes\\chromedriver.exe");
        driver = new ChromeDriver();

        //navigate to URL extracted from supplied XML
        driver.get( extractURL() );

        Assert.assertNotNull(driver); //titles on this page change frequently, checking title risks failure
    }

    @Test
    public void visitNextPage(){ //traverse forum
        if (driver == null){
            setup();
        }

        //get url to check page change
        String url = driver.getCurrentUrl();

        //Check if next page exists, navigate to it if it does
        if(driver.findElements(By.className("next")).size() != 0){
            WebElement nextElement = driver.findElement(By.className("next"));
            WebElement nextLink = nextElement.findElement(By.tagName("a"));
            nextLink.click();

            //Check page change
            Assert.assertEquals(url + "page-2", driver.getCurrentUrl());
        }
        else {
            //Make sure page did not change
            Assert.assertEquals(url, driver.getCurrentUrl());
        }
    }

    @Test
    public void saveImage() throws InterruptedException, AWTException{
        if (driver == null){
            setup();
        }
        //Find Image on web page, src for navigation, name for save file
        WebElement imgElement = driver.findElement(By.className("bbc_img"));
        String src = imgElement.getAttribute("src");
        String name = imgElement.getAttribute("alt");

        //open new browser window
        WebDriver imgDriver = new ChromeDriver();
        imgDriver.get(src);

        //Store save file location in clipboard
        String location = "C:\\Users\\aefre\\Pictures\\snsd\\" + loc + "\\" + name;
        StringSelection stringSelection = new StringSelection(location);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, stringSelection);

        //locate image element
        WebElement imgPg = imgDriver.findElement(By.tagName("img"));

        //Right click and select save as
        Actions action = new Actions(imgDriver);
        action.contextClick(imgPg).build().perform();
        action.sendKeys(Keys.CONTROL, "s").build().perform();

        //paste in save folder location and save
        Robot robot = new Robot();
        Thread.sleep(3000); //give time for robot to be set up and interact with dialog, should avoid using, find workaround?
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        Thread.sleep(3000); //give time for download, should avoid using, find workaround?

        //close driver for download window
        imgDriver.quit();
    }

    //Timing issue, only downloading every other image. WebDriver not waiting for Windows save process to finish.
    //Adding a Thread.sleep() does not help, imgDriver moves on to next image while previous one saving, so every other
    //image is skipped.
    @Test
    public void saveAllImagesOnPage() throws AWTException, InterruptedException{
        if (driver == null){
            setup();
        }

        //setup driver, action for right click, robot to interact with Windows' dialog box
        WebDriver imgDriver = new ChromeDriver();
        Actions action = new Actions(imgDriver);
        Robot robot = new Robot();

        //find all images on page
        java.util.List<WebElement> imgList = driver.findElements(By.className("bbc_img"));
        for (WebElement imgElement : imgList) {
            //get image url and image name
            String src = imgElement.getAttribute("src");
            String name = imgElement.getAttribute("alt");

            //navigate to image url
            imgDriver.get(src);

            //check if page has a valid image
            if (imgDriver.findElements(By.tagName("img")).size() != 0) {
                //Store save file location in clipboard
                String location = "C:\\Users\\aefre\\Pictures\\snsd\\" + loc + "\\" + name;
                StringSelection stringSelection = new StringSelection(location);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);

                WebElement imgPg = imgDriver.findElement(By.tagName("img"));

                //Right click and select save as
                action.contextClick(imgPg).build().perform();
                action.sendKeys(Keys.CONTROL, "s").build().perform();

                //paste in save folder location and save
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                //Thread.sleep(3000);
            }
        }
        //close driver
        imgDriver.quit();
    }

    @Test
    public void closeWebDriver(){
        if (driver == null)
            return;

        driver.quit();
    }

    private String extractURL(){
        try{
            //Store XML contents in Document
            File fXMLFile = new File("src\\main\\resources\\pages.xml");

            //check if file found
            Assert.assertNotNull(fXMLFile);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(fXMLFile);
            doc.getDocumentElement().normalize();

            //check file read
            Assert.assertNotNull(doc);

            //Extract URL and file location from Document
            NodeList nList = doc.getElementsByTagName("page");
            Node nNode = nList.item(0);
            Element eElement = (Element) nNode;
            loc = eElement.getElementsByTagName("member").item(0).getTextContent();
            return eElement.getElementsByTagName("url").item(0).getTextContent();
        } catch(Exception e){
            System.err.println(e.getMessage());
        }
        return null;
    }
}
