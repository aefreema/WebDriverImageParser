public class SearchElement {
    private final String url;
    private final String loc;

    SearchElement(String u, String l){
        url = u;
        loc = l;
    }

    public String getUrl(){
        return url;
    }

    public String getLoc(){
        return loc;
    }
}
