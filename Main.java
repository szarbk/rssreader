import org.jsoup.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.*;
import java.io.BufferedReader;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static Scanner x = new Scanner(System.in);
    public static String extractPageTitle(String html)
    {
        try
        {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("title").first().text();
        }
        catch (Exception e)
        {
            return "Error: no title tag found in page source!";
        }
    }
    public static void retrieveRssContent(String rssUrl) {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");
            int MAX_ITEMS = itemNodes.getLength();
            for (int i = 0; i < MAX_ITEMS; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).getTextContent());
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }
    public static String extractRssUrl(String url) throws IOException
    {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }
    public static String fetchPageSource(String urlString) throws Exception
    {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
    }
    private static String toString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);
        return stringBuilder.toString();
    }
    public static ArrayList<String>load_list(String file){
        ArrayList<String> list = new ArrayList<>();
        try{
            BufferedReader in = new BufferedReader(new FileReader(file));
            String info;
            while ((info = in.readLine()) != null) {//stackoverflow.com
                String title="";
                for(int i=0;i<info.length();i++) {
                    if (info.charAt(i) == ';')
                        break;
                    title+=info.charAt(i);
                }
                list.add(title);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    public static String get_rssurl(int index){
        String rssurl="";
        try {
            BufferedReader in = new BufferedReader(new FileReader("data.txt"));
            String info;
            while ((info = in.readLine()) != null) {//stackoverflow.com
                if(--index<0)
                    break;
            }
            int num=0;
            for(int i=0;i<info.length();i++){
                if(num==2)
                    rssurl+=info.charAt(i);
                if(info.charAt(i)==';')
                    num++;
            }
        }
        catch (IOException e) {
            e.getMessage();
        }
        return rssurl;
    }
    public static void showlist(){
        ArrayList<String> list = load_list("data.txt");
        if (list.size()==0) {
            System.out.println("No RSS found!");
            return;
        }
        System.out.println("Show updates for:\n[0] All website");
        for(int i=1;i<=list.size();i++)
            System.out.println("["+(i)+"] "+list.get(i-1));
        System.out.println("Enter -1 to return.");
        int get_act=x.nextInt();
        if(get_act==0){
            for(int i=0;i<list.size();i++){
                retrieveRssContent(get_rssurl(i));
            }
        }
        else if(get_act==-1){
            System.out.println();
            return;
        }
        else if(get_act<=list.size()&&get_act>=1){
            retrieveRssContent(get_rssurl(get_act-1));
        }
        else{
            System.out.println("Invalid choice!");
        }
    }
    public static void addtolist() throws IOException {
        System.out.println("please enter wbsite URL to add:");
        String url=x.next();
        String rssurl=extractRssUrl(url);
        String htmlurl=extractPageTitle(url);
        String addtemp=url+";"+htmlurl+";"+rssurl;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data.txt", true))) {
            writer.write(addtemp);
            writer.newLine();
            System.out.println("URL "+url+" added successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void removefromlist(){
        System.out.println("please enter wbsite URL to remove:");
        String url=x.next();
        ArrayList<String> list = new ArrayList<>();
        try{
            BufferedReader in = new BufferedReader(new FileReader("data.txt"));
            String info;
            while ((info = in.readLine()) != null) {//stackoverflow.com
                String htmlurl="";
                int num=0;
                for(int i=0;i<info.length();i++) {
                    if (info.charAt(i) == ';')
                        num++;
                    if(num==2)
                        break;
                    if(num==0||info.charAt(i) == ';')
                        continue;
                    htmlurl+=info.charAt(i);
                }
                if(htmlurl!=extractPageTitle(url)) {
                    //System.out.println(htmlurl+" "+extractPageTitle(url)+"---------");
                    list.add(info);
                }
            }
            FileWriter writer = new FileWriter("data.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for(int i=0;i<list.size();i++) {
                bufferedWriter.write(list.get(i));
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        Boolean check=true;
        while (check){
            for(int i=0;i<4;i++){
                System.out.println("Type a valid number for your desired action:");
                System.out.println("[1] Show updates");
                System.out.println("[2] Add URL");
                System.out.println("[3] Remove URL");
                System.out.println("[4] Exit");
                int get_act=x.nextInt();
                if(get_act==1)
                    showlist();
                else if(get_act==2)
                    addtolist();
                else if(get_act==3)
                    removefromlist();
                else if(get_act==4){
                    System.out.println("Exiting...");
                    check=false;
                    break;
                }
                else
                    System.out.println("Invalid choice! Please enter a valid number.");

            }
        }
    }
}