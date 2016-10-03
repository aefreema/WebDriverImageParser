/*
* Currently only traverses pages.
* For current image downloading code see test/java/imageParseTest saveAllImagesOnPage()
* */

public class WebDriverImageParser {
    public static void main (String [] args){

        ImageParser ip = new ImageParser();
        try{
            ip.parseImagesInChrome();
        } catch (Exception e){
            System.err.println("Exception Message: " + e.getMessage());
        }
    }
}
