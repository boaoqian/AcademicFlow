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

    public ArrayList<Paper> GetByName(String name) throws IOException {
        ArrayList<Paper> papers = new ArrayList<>();
            try {
                String url = now_url + "?hl=zh-CN&q=" + name.replace(" ", "+");
                Document doc;
                if (this.proxy_port>0){
                    doc = Jsoup.connect(url).proxy(this.proxy_host, this.proxy_port).get();
                }
                else {
                    doc = Jsoup.connect(url).get();
                }
                Elements result = doc.selectXpath("//*[@id=\"gs_res_ccl_mid\"]").getFirst().getElementsByClass("gs_ri");
                for (Element element : result) {
                    String title = element.getElementsByClass("gs_rt").getFirst().getElementsByTag("a").text();
                    String author = element.getElementsByClass("gs_a").text();
                    String cite_url = element.selectXpath("div[3]/a[4]").attr("href");
                    if (!cite_url.isEmpty()) {
                        cite_url = now_url.substring(0, now_url.indexOf("/", 9)) + cite_url;
                    }
                    papers.add(new Paper(title, author, cite_url));
                }
            } catch (IOException e) {
                System.out.println("从url: " + now_url+"\n获取信息失败");
                e.printStackTrace();
            }
        return papers;
    }

    public static void main(String[] args) throws IOException {
        GoogleAPI api = new GoogleAPI();
        api.setProxy("127.0.0.1",7890);
        api.setNow_url(1);
        ArrayList<Paper> papers = api.GetByName("attention is all you need");
        System.out.println(papers.stream().map(Paper::toString).collect(Collectors.joining()));
    }
}
