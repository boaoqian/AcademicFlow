import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Math.*;


public class GoogleAPI {
    private final ExecutorService threadPool; // 5 个线程
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
    public static void log(String mas){
        System.out.println(mas);
    }
    public static void main(String[] args) {
        GoogleAPI api = new GoogleAPI();
        api.setNow_url(0);
        var list = api.GetRelation("https://scholar.lanfanshu.cn/scholar?q=related:QrO2S2mGYh8J:scholar.lanfanshu.cn/&scioq=llm+math&hl=zh-CN&as_sdt=0,5");
        var list1 = Paper.filter(list, new Paper.CitedCountFilter(0.5F));
        var list2 = Paper.filter(list, new Paper.CitedCountFilter(1000));
        System.out.println(list.size());
        System.out.println(list1.count());
        System.out.println(list2.count());

        api.shutdown();
    }

    public void shutdown() {
        threadPool.shutdown();
    }

    public void close() {
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
        url = url.replace(" ", "+");
        String result;
        if (start == 0) {
            result = "%s?lookup=0&hl=%s&q=%s".formatted(url, lang, title.replace(" ", "+"));
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
        int failed_count = 0;
        final int max_try = 10;
        // 判断是否需要使用代理
        if (this.proxy_port > 0) {
            connection = connection.proxy(this.proxy_host, this.proxy_port);
        }
        // 执行请求并获取响应
        Thread.sleep((long) (random() * 1000));
        // 检查响应码
        Connection.Response response = null;
        while (true) {
            if(failed_count >= max_try) {
                throw new IOException("请求失败");
            }
            try {
                response = connection.execute();
            } catch (Exception e) {
                Thread.sleep((long) (1000+random()*2000*failed_count));
                failed_count++;
                log("url failed :"+url+"\nfailed_count:"+failed_count);
                continue;
            }
            if (response == null) {
                Thread.sleep((long) (1000+random()*2000*failed_count));
                failed_count++;
                log("url failed :"+url+"\nfailed_count:"+failed_count);
                continue;
            }
            if (response.statusCode() == 200) {
                break;
            } else {
                Thread.sleep((long) (1000+random()*2000*failed_count));
                failed_count++;
                log("url failed :"+url+"\nfailed_count:"+failed_count);
                continue;
            }
        }
        if(response==null){
            log("未知错误");
            throw new IOException("请求失败");
        }
        if ( response.statusCode() != 200) {
            log(response.statusCode() + " " + response.statusMessage());
            throw new IOException("请求失败:" + response.statusCode());
        }
        return response;
    }

    public ArrayList<Paper> getPapers(String url) throws IOException, InterruptedException {
        ArrayList<Paper> papers;
        try{
            Connection.Response response = get(url);
            Document document = response.parse();
            papers= ParsePapers(document);
        }catch (IOException e){
            log(e.getMessage());
            papers = new ArrayList<>();
        }
        return papers;
    }

    public ArrayList<Future<ArrayList<Paper>>> GetFutureFromCited(String url, int max_items){
        ArrayList<Paper> papers = new ArrayList<>();
        ArrayList<Future<ArrayList<Paper>>> futures = new ArrayList<>();
        if (url.length() <= 2) {
            return futures;
        }
        int max_size = max_items;
        try {
            Document doc;
            Connection.Response response = get(url);
            doc = response.parse();
            max_size = max(min(max_size, ParseSize(doc)), 0);
            futures.add(threadPool.submit(() -> ParsePapers(doc)));
            if (max_size > 10) {
                for (int i = 10; i <= max_size; i += 10) {
                    String qurl = constructURL(url, i);
                    futures.add(threadPool.submit(() -> getPapers(qurl)));
                }
            }
        } catch (InterruptedException e) {
            log(e.getMessage());
            return futures;
        } catch (IOException e) {
            log(e.getMessage());
            return futures;
        }
        return futures;
    }

    public ArrayList<Paper> FuturestoPapers(List<Future<Connection.Response>> futures) throws ExecutionException {
        ArrayList<Paper> papers = new ArrayList<>();
        try {
            for (Future<Connection.Response> future : futures) {
                Document doc = future.get().parse();
                papers.addAll(ParsePapers(doc));
            }
        }catch (IOException | InterruptedException e) {
            log("从url: " + now_url + "\n获取信息失败");
            e.printStackTrace();
        }
        return papers;
    }

    public ArrayList<Paper> GetByName(String name, int max_items) {
        ArrayList<Paper> papers = new ArrayList<>();
        List<Future<Connection.Response>> futures = new ArrayList<>();
        if (name.length() <= 2) {
            return papers;
        }
        int max_size = max_items;
        try {
            String url = constructURL(name);
            Document doc;
            Connection.Response response = get(url);
            doc = response.parse();
            max_size = max(min(max_size, ParseSize(doc)), 0);
            papers.addAll(ParsePapers(doc));
            if (max_size > 10) {
                for (int i = 10; i <= max_size; i += 10) {
                    String qurl = constructURL(url, i);
                    futures.add(threadPool.submit(() -> get(qurl)));
                }
                for (Future<Connection.Response> future : futures) {
                    doc = future.get().parse();
                    papers.addAll(ParsePapers(doc));
                }
            }
        } catch (IOException | InterruptedException e) {
            log("从url: " + now_url + "\n获取信息失败");
            e.printStackTrace();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return papers;
    }

    public ArrayList<Paper> GetByName(String name) throws IOException {
        return GetByName(name, 10);
    }

    public ArrayList<Paper> GetRelation(String url, int max_items) {
        ArrayList<Paper> papers = new ArrayList<>();
        if (url.length() < 9) {
            return papers;
        }
        int max_size = max_items;
        try {
            Document doc;
            Connection.Response response = get(url);
            doc = response.parse();
            max_size = min(max_size, ParseSize(doc));
            papers.addAll(ParsePapers(doc));
            if (max_size > 10) {
                List<Future<Connection.Response>> futures = new ArrayList<>();
                for (int i = 10; i <= max_size; i += 10) {
                    String qurl = constructURL(url, i);
                    futures.add(threadPool.submit(() -> get(qurl)));
                }
                for (Future<Connection.Response> future : futures) {
                    doc = future.get().parse();
                    papers.addAll(ParsePapers(doc));
                }

            }
        } catch (IOException e) {
            log("从url: " + now_url + "\n获取信息失败");
            e.printStackTrace();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return papers;
    }

    public ArrayList<Paper> GetRelation(String url) {
        return GetRelation(url, Integer.MAX_VALUE);
    }

    public ArrayList<Paper> GetCited(String url, int max_items) {
        return GetRelation(url, max_items);
    }

    public ArrayList<Paper> GetCited(String url) {
        return GetCited(url, Integer.MAX_VALUE);
    }

    public List<Paper> GetCitedGraph(Paper paper,int depth) throws ExecutionException, InterruptedException {
        Queue<Paper> search_paper_queue = new LinkedList<>();
        Queue<Paper> wait_paper_queue = new LinkedList<>();
        Queue<Integer> search_depth = new LinkedList<Integer>();
        ArrayList<ArrayList<Future<ArrayList<Paper>>>> results = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Paper> paperMap = new HashMap<>();
        search_paper_queue.add(paper);
        search_depth.add(0);
        while (true) {
            if (search_paper_queue.isEmpty()) {
                if (!search_depth.isEmpty()){
                    for (var futures : results) {
                        int d = search_depth.poll();
                        Paper p = wait_paper_queue.poll();
                        List<Paper> temp = new ArrayList<>();
                        for (var future : futures) {
                            try{
                                temp.addAll(future.get());
                            }catch (ExecutionException e){
                                log(e.getMessage());
                            }
                        }
                        p.setCited_uid(temp.stream().map(Paper::get_uid).map(String::valueOf).collect(Collectors.joining(",")));
                        paperMap.put(p.get_uid(), p);
                        temp.stream().filter(pp->!visited.contains(pp.get_uid())).forEach(pp -> paperMap.put(pp.get_uid(), pp));
                        temp = Paper.filter(temp, new Paper.CitedFilter()).toList();
                        if(temp.size()>25){
                            temp = Paper.filter(temp, new Paper.CitedCountFilter(25)).toList();
                        }
                        temp.stream().forEach(pp->{
                            search_depth.offer(d);
                            search_paper_queue.offer(pp);
                        });
                    }
                    results.clear();
                }
                else {
                    break;
                }
            }
            Paper p = search_paper_queue.poll();
            wait_paper_queue.add(p);
            int d = search_depth.poll();
            log("Search:"+p.getTitle());
            if (d > depth) {
                break;
            }
            if (visited.contains(p.get_uid())) {
                continue;
            }
            visited.add(p.get_uid());
            results.add(GetFutureFromCited(p.getCited_url(),100));
            search_depth.add(d+1);
        }
        return new ArrayList<>(paperMap.values());
    }

    public List<Paper> GetCitedGraph(Paper paper) throws ExecutionException, InterruptedException {
        return GetCitedGraph(paper, 1);
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

    public ArrayList<Paper> ParsePapers(Document doc) {
        //提取页面的paper信息,返回信息列表
        ArrayList<Paper> papers = new ArrayList<>();
        Elements result = doc.getElementsByClass("gs_r gs_or gs_scl");//gs_r gs_or gs_scl gs_fmar
        for (Element element : result) {
            String title = element.getElementsByClass("gs_rt").getFirst().getElementsByTag("a").text();
            if (title.isEmpty()) {
                continue;
            }
            String author = element.getElementsByClass("gs_a").text();
            String pdf_url = element.selectXpath("div[1]/div/div/a").attr("href");
            String abstract_text = element.getElementsByClass("gs_rs").text();
            String cited_url = "";
            String cited_count = "0";
            String relation_url = "";
            Elements info = element.getElementsByClass("gs_fl gs_flb").first().getElementsByTag("a");
            for (Element a : info) {
                if (a.attr("href").contains("cites")) {
                    cited_url = a.attr("href");
                    cited_count = a.text();
                }
                if (a.attr("href").contains("related")) {
                    relation_url = a.attr("href");
                }

            }
            if (!relation_url.isEmpty()) {
                relation_url = now_url.substring(0, now_url.indexOf("/", 9)) + relation_url;
            }
            if (!cited_url.isEmpty()) {
                cited_url = now_url.substring(0, now_url.indexOf("/", 9)) + cited_url;
            }
            papers.add(new Paper(title, author, relation_url, cited_url, pdf_url, cited_count, abstract_text));
        }
        return papers;
    }
}
