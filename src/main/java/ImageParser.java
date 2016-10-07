/*
* Traverses web forums downloading all images posted to the forums.
*
* Working on:
* Handling urls without a valid image. Currently catching exception and doing nothing to move onto next image.
* Slow and not the bad use of exceptions. Working on a better way.
* */

//import org.apache.commons.validator.routines.UrlValidator;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;


public class ImageParser {
    private Vector<SearchElement> elem = null;

    public void parseImagesInChrome() throws MalformedURLException, IOException{
        //Set up webdriver
        System.setProperty("webdriver.chrome.driver", "C:\\Includes\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        //extract urls and save folder locations from XML
        try {
            getURLS();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        for (SearchElement se: elem){
            //Go to URL
            driver.get(se.getUrl());
            String loc = se.getLoc();

            //used to check for a next page
            boolean hasNext;

            //navigate forum pages
            do {
               //locate images
               List<WebElement> imgList = driver.findElements(By.className("bbc_img"));
               for (WebElement imgElement: imgList){
                   String src = imgElement.getAttribute("src");
                   String name = imgElement.getAttribute("alt");
                   String type = name.substring(name.indexOf(".") + 1);

                   //Check for valid url
                   /*UrlValidator uv = new UrlValidator();
                   if (uv.isValid(src)) {
                       URL url = new URL(src);
                       BufferedImage img = ImageIO.read(url);
                       ImageIO.write(img, type, new File("C:\\Users\\aefre\\Pictures\\snsd\\" + loc + "\\" + name));
                   }*/

                   //download image
                   //find a way to check for valid image on page
                   try {
                       URL url = new URL(src);
                       BufferedImage img = ImageIO.read(url);
                       ImageIO.write(img, type, new File("C:\\Users\\aefre\\Pictures\\snsd" + loc + "\\" + name));
                   } catch (Exception e){
                       //Do nothing, move on to next url
                   }
               }

               //if a link to next page exists, click link, otherwise, at last page, break
               if(driver.findElements(By.className("next")).size() != 0){
                   hasNext = true;

                   //locate next button and click
                   WebElement nextElement = driver.findElement(By.className("next"));
                   WebElement nextLink = nextElement.findElement(By.tagName("a"));
                   nextLink.click();
               }
               else
                   hasNext = false;
            } while(hasNext);
        }

        //close driver
        driver.quit();
    }

    public void getURLS() throws ParserConfigurationException, IOException, SAXException {
        elem = new Vector<SearchElement>();

        try {
            //Store XML contents in Document
            File fXmlFile = new File("src\\main\\resources\\pages.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("page");

            //Extract URL and file locations from Document
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                Element eElement = (Element) nNode;

                SearchElement se = new SearchElement(eElement.getElementsByTagName("url").item(0).getTextContent(),
                        eElement.getElementsByTagName("member").item(0).getTextContent());

                elem.add(se);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /*public void saveImage(WebDriver imgDriver, Actions action, Robot robot, String name, String src, String loc) {
        //open new browser window
        imgDriver.get(src);

        //Store save file location in clipboard
        String location = "C:\\Users\\aefre\\Pictures\\snsd\\" + loc + "\\" + name;
        StringSelection stringSelection = new StringSelection(location);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);

        if (imgDriver.findElements(By.tagName("img")).size() != 0) {
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
        }
    }*/

}