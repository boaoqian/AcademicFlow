import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import qba.Paper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Math.max;


public class GoogleAPI {
    private String proxy_host = "";
    private int proxy_port = 0;
    private String[] root_urls = {
            "https://scholar.lanfanshu.cn/scholar",
            "https://scholar.google.com/scholar" // 示例额外镜像
    };
    private String now_url = root_urls[0];

    public void setNow_url(int i) {
        if (i>this.now_url.length()) {
            this.now_url = this.root_urls[0];
            throw new RuntimeException("url out of range");
        }
        this.now_url = root_urls[i];
    }
    public String getNow_url() {
        return now_url;
    }
    public void setProxy(String proxy_host, int proxy_port) {
        this.proxy_host = proxy_host;
        this.proxy_port = proxy_port;
    }
    public String getProxy() {
        return proxy_host + ":" + proxy_port;
    }

    public String constructURL(String url, String title, String lang, int start) {

        String result;
        if (start==0){
            result = "%s?hl=%s&q=%s".formatted(url,lang,title.replace(" ", "+"));
        }
        else {
            result = "%s?start=%d&hl=%s&q=%s".formatted(url,start,lang,title.replace(" ", "+"));
        }
        return result;
    }

    public String constructURL(String url, String title, String lang) {
        return constructURL(url,title,lang,0);
    }

    public String constructURL(String title) {
        return constructURL(this.now_url,title,"zh-CN",0);
    }
    public String constructURL(String url, int start) {
        return  url+"&start="+start;
    }

    public Connection.Response get(String url) throws IOException {
        Connection connection = Jsoup.connect(url);
        // 判断是否需要使用代理
        if (this.proxy_port > 0) {
            connection = connection.proxy(this.proxy_host, this.proxy_port);
        }
        // 执行请求并获取响应
        Connection.Response response = connection.execute();
        // 检查响应码
        if (response.statusCode() != 200) {
            throw new IOException("HTTP请求失败，状态码：" + response.statusCode());
        }
        return response;
    }

    public ArrayList<Paper> GetByName(String name,int max_items) throws IOException {
        ArrayList<Paper> papers = new ArrayList<>();
        int max_size = max_items;
        try {
            String url = constructURL(name);
            Document doc;
            Connection.Response response = get(url);
            doc = response.parse();
            max_size = max(max_size,ParseSize(doc));
            papers.addAll(ParsePaper(doc));
            if (max_size>10&&max_items>10){
                for(int i = 10;i<max_items;i+=10){
                    url = constructURL(url,i);
                    response = get(url);
                    doc = response.parse();
                    papers.addAll(ParsePaper(doc));
                }
            }
        } catch (IOException e) {
            System.out.println("从url: " + now_url + "\n获取信息失败");
            e.printStackTrace();
        }
        return papers;
    }

    public ArrayList<Paper> GetByName(String name) throws IOException {
        return GetByName(name,10);
    }

    public int ParseSize(Document doc){
        String info = doc.selectXpath("/html/body/div/div[7]/div[3]/div").text();
        int size = -1;
        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(info);
        if (matcher.find()) {
           size  = Integer.parseInt(matcher.group());
        }
        return size;
    }

    public ArrayList<Paper> ParsePaper(Document doc) {
        //提取页面的paper信息,返回信息列表
        ArrayList<Paper> papers = new ArrayList<>();
        Elements result = doc.getElementsByClass("gs_r gs_or gs_scl");
        for (Element element : result) {
            String title = element.getElementsByClass("gs_rt").getFirst().getElementsByTag("a").text();
            String author = element.getElementsByClass("gs_a").text();
            String cited_url = element.selectXpath("div[2]/div[3]/a[3]").attr("href");
            String relation_url = element.selectXpath("div[2]/div[3]/a[4]").attr("href");
            String pdf_url = element.selectXpath("div[1]/div/div/a").attr("href");
            if (!relation_url.isEmpty()) {
                relation_url = now_url.substring(0, now_url.indexOf("/", 9)) + relation_url;
            }
            if (!cited_url.isEmpty()) {
                cited_url = now_url.substring(0, now_url.indexOf("/", 9)) + cited_url;
            }
            papers.add(new Paper(title, author, relation_url, cited_url, pdf_url));
        }
        return papers;
    }


    public static void main(String[] args) throws IOException {
        GoogleAPI api = new GoogleAPI();
        api.setNow_url(0);
        System.out.println(api.GetByName("llm math",30));
    }
}
