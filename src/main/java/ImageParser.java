/*
* Traverses web forums downloading all images posted to the forums.
*
* Working on:
* Handling urls without a valid image more quickly.
* */

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

    public void parseImagesInChrome() throws IOException{
        //Set up webdriver
        System.setProperty("webdriver.chrome.driver", "C:\\Includes\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        WebDriver imgDriver = new ChromeDriver();

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

                   //Check for valid image url
                   imgDriver.get(src);
                   if ((imgDriver.findElements(By.tagName("img")).size() == 1)
                           && (imgDriver.findElement(By.tagName("img")).getAttribute("src").equals(src))
                           && (imgDriver.findElements(By.tagName("title")).size() != 0)) {
                       //read image into a BufferedImage and write to file
                       URL url = new URL(src);
                       BufferedImage img = ImageIO.read(url);
                       ImageIO.write(img, type, new File("..\\..\\Pictures\\snsd\\" + loc + "\\" + name));
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
        imgDriver.quit();
        driver.quit();
    }

    public void getURLS() throws ParserConfigurationException, IOException, SAXException {
        elem = new Vector<>();

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
}