import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import qba.Paper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

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

    public String ConstructURL(String url, String title, String lang, int start) {

        String result;
        if (start==0){
            result = "%s?hl=%s&q=%s".formatted(url,lang,title.replace(" ", "+"));
        }
        else {
            result = "%s?start=%d&hl=%s&q=%s".formatted(url,start,lang,title.replace(" ", "+"));
        }
        return result;
    }

    public String ConstructURL(String url, String title, String lang) {
        return ConstructURL(url,title,lang,0);
    }

    public String ConstructURL(String url, String title) {
        return ConstructURL(url,title,"zh-CN",0);
    }

    public ArrayList<Paper> GetByName(String name,int max_items) throws IOException {
        ArrayList<Paper> papers = null;
        try {
            String url = ConstructURL(now_url,name);
            Document doc;
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
            papers = ParsePaper(response.parse());
        } catch (IOException e) {
            System.out.println("从url: " + now_url + "\n获取信息失败");
            e.printStackTrace();
        }
        return papers;
    }

    public ArrayList<Paper> GetByName(String name) throws IOException {
        return GetByName(name,10);
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
        ArrayList<Paper> papers = api.GetByName("attention is all you need",10);
        System.out.println(papers.stream().map(Paper::toString).collect(Collectors.joining()));
    }
}
