import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import qba.Paper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static java.lang.Math.random;


public class GoogleAPI {
    private ExecutorService threadPool; // 5 个线程
    private String proxy_host = "";
    private int proxy_port = 0;
    private String[] root_urls = {
            "https://scholar.lanfanshu.cn/scholar",
            "https://scholar.google.com/scholar" // 示例额外镜像
    };
    private String now_url = root_urls[0];
    GoogleAPI(int numThreads) {
        threadPool = Executors.newFixedThreadPool(numThreads);
    }
    GoogleAPI() {
        threadPool = Executors.newFixedThreadPool(4);
    }

    public static void main(String[] args) throws IOException {
        GoogleAPI api = new GoogleAPI();
        api.setNow_url(0);
        var list = api.GetRelation("https://scholar.lanfanshu.cn/scholar?q=related:QrO2S2mGYh8J:scholar.lanfanshu.cn/&scioq=llm+math&hl=zh-CN&as_sdt=0,5");
        list = Paper.filter(list.stream(), 0.5F);
        System.out.println(list.size());
        api.shutdown();
    }

    public void shutdown(){
        threadPool.shutdown();
    }

    public String getNow_url() {
        return now_url;
    }

    public void setNow_url(int i) {
        if (i > this.now_url.length()) {
            this.now_url = this.root_urls[0];
            throw new RuntimeException("url out of range");
        }
        this.now_url = root_urls[i];
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
        if (start == 0) {
            result = "%s?hl=%s&q=%s".formatted(url, lang, title.replace(" ", "+"));
        } else {
            result = "%s?start=%d&hl=%s&q=%s".formatted(url, start, lang, title.replace(" ", "+"));
        }
        return result;
    }

    public String constructURL(String url, String title, String lang) {
        return constructURL(url, title, lang, 0);
    }

    public String constructURL(String title) {
        return constructURL(this.now_url, title, "zh-CN", 0);
    }

    public String constructURL(String url, int start) {
        return url + "&start=" + start;
    }

    public Connection.Response get(String url) throws IOException, InterruptedException {
        Connection connection = Jsoup.connect(url);
        // 判断是否需要使用代理
        if (this.proxy_port > 0) {
            connection = connection.proxy(this.proxy_host, this.proxy_port);
        }
        // 执行请求并获取响应
        Thread.sleep((long) (random() * 1000));
        // 检查响应码
        Connection.Response response = null;
        for (int i = 0; i < 3; i++) {
            try {
                response = connection.execute();
            } catch (Exception e) {
                Thread.sleep(1000);
                continue;
            }
            if (response.statusCode() == 200) {
                break;
            } else {
                Thread.sleep(1000);
            }
        }
        if (response.statusCode() != 200) {
            System.out.println(response.statusCode() + " " + response.statusMessage());
            throw new IOException("请求失败:" + response.statusCode());
        }
        return response;
    }

    public List<Paper> GetByName(String name, int max_items) throws IOException {
        ArrayList<Paper> papers = new ArrayList<>();
        int max_size = max_items;
        try {
            String url = constructURL(name);
            Document doc;
            Connection.Response response = get(url);
            doc = response.parse();
            max_size = min(max_size, ParseSize(doc));
            papers.addAll(ParsePaper(doc));
            if (max_size > 10) {
                List<Future<Connection.Response>> futures = new ArrayList<>();
                for (int i = 10; i < max_size; i += 10) {
                    String qurl = constructURL(url, i);
                    futures.add(threadPool.submit(() -> get(qurl)));
                }
                for (Future<Connection.Response> future : futures) {
                    doc = future.get().parse();
                    papers.addAll(ParsePaper(doc).stream().filter(Paper::isComplated).collect(Collectors.toList()));
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("从url: " + now_url + "\n获取信息失败");
            e.printStackTrace();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return papers;
    }

    public List<Paper> GetByName(String name) throws IOException {
        return GetByName(name, 10);
    }

    public List<Paper> GetRelation(String url, int max_items) {
        ArrayList<Paper> papers = new ArrayList<>();
        int max_size = max_items;
        try {
            Document doc;
            Connection.Response response = get(url);
            doc = response.parse();
            max_size = min(max_size, ParseSize(doc));
            papers.addAll(ParsePaper(doc));
            if (max_size > 10) {
                List<Future<Connection.Response>> futures = new ArrayList<>();
                for (int i = 10; i < max_size; i += 10) {
                    String qurl = constructURL(url, i);
                    futures.add(threadPool.submit(() -> get(qurl)));
                }
                for (Future<Connection.Response> future : futures) {
                    doc = future.get().parse();
                    papers.addAll(ParsePaper(doc).stream().filter(Paper::isComplated).collect(Collectors.toList()));
                }

            }
        } catch (IOException e) {
            System.out.println("从url: " + now_url + "\n获取信息失败");
            e.printStackTrace();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return papers;
    }

    public List<Paper> GetRelation(String url) {
        return GetRelation(url, Integer.MAX_VALUE);
    }

    public int ParseSize(Document doc) {
        String info = doc.selectXpath("/html/body/div/div[7]/div[3]/div").text();
        int size = -1;
        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(info);
        if (matcher.find()) {
            size = Integer.parseInt(matcher.group());
        }
        return size;
    }

    public List<Paper> ParsePaper(Document doc) {
        //提取页面的paper信息,返回信息列表
        ArrayList<Paper> papers = new ArrayList<>();
        Elements result = doc.getElementsByClass("gs_r gs_or gs_scl");
        for (Element element : result) {
            String title = element.getElementsByClass("gs_rt").getFirst().getElementsByTag("a").text();
            String author = element.getElementsByClass("gs_a").text();
            String cited_url = element.selectXpath("div[2]/div[3]/a[3]").attr("href");
            String relation_url = element.selectXpath("div[2]/div[3]/a[4]").attr("href");
            String pdf_url = element.selectXpath("div[1]/div/div/a").attr("href");
            String cited_count = element.selectXpath("div[2]/div[3]/a[3]").text();
            if (!relation_url.isEmpty()) {
                relation_url = now_url.substring(0, now_url.indexOf("/", 9)) + relation_url;
            }
            if (!cited_url.isEmpty()) {
                cited_url = now_url.substring(0, now_url.indexOf("/", 9)) + cited_url;
            }
            papers.add(new Paper(title, author, relation_url, cited_url, pdf_url,cited_count));
        }
        return papers;
    }
}
