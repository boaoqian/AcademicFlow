import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MainCMD {
    List<Paper> papers;
    GoogleAPI api;
    MainCMD(){
        api = new GoogleAPI();
    }
    public void invailed_cmd(){
        System.out.println("Invalid command");
    }
    public void help(){
        System.out.println("Usage:");
    }
    public void exit(){
        api.close();
        System.exit(0);
    }
    public void get(String title) throws IOException {
        if(title.equals("")){
            invailed_cmd();
            return;
        }
        System.out.println("Searching for: " + title);
        papers = api.GetByName(title);
        System.out.println("get:"+papers.size());
    }
    public void SQLclean() throws SQLException {
        Utils.clean();
    }
    public void print(){
        for(int i = 0; i<papers.size(); i++){
            System.out.println(i);
            System.out.println(papers.get(i).toString());
        }
    }
    public void cited(String idx){
        int i;
        try {
            i = Integer.parseInt(idx);
        }
        catch (NumberFormatException e){
            invailed_cmd();
            return;
        }
        if (i>papers.size()){
            invailed_cmd();
            return;
        }
        papers = api.GetCited(papers.get(i).getCited_url());
    }
    public void delete(String uid) throws SQLException {
        int i;
        try {
            i = Integer.parseInt(uid);
        }
        catch (NumberFormatException e){
            invailed_cmd();
            return;
        }
        Utils.delete(i);
    }
    public void submit(String idx) throws SQLException {
        int i;
        try {
            i = Integer.parseInt(idx);
        }
        catch (NumberFormatException e){
            invailed_cmd();
            return;
        }
        if (i>papers.size()){
            invailed_cmd();
            return;
        }
        Paper p = papers.get(i);
        Utils.insert(p);
    }
    public void SQL() throws SQLException {
        papers = Utils.getPaperList();
    }
    public void submits() throws SQLException{
        Utils.batchInsert(papers);
    }
    public void CiteGraph(String idx) throws ExecutionException, InterruptedException {
        int i;
        try {
            i = Integer.parseInt(idx);
        }
        catch (NumberFormatException e){
            invailed_cmd();
            return;
        }
        papers = api.GetCitedGraph(papers.get(i),1);
    }
    public void buildGraph(String uid) throws SQLException {
        int i;
        try {
            i = Integer.parseInt(uid);
        }
        catch (NumberFormatException e){
            invailed_cmd();
            return;
        }
        Paper p = Utils.getPaper(i);
        System.out.println(p.getCited_uid());
        Map<Paper, List<Paper>> graph = Utils.buildGraph(p,2);
        for (Map.Entry<Paper, List<Paper>> entry : graph.entrySet()) {
            System.out.println("Paper " + entry.getKey().get_uid() +
                    " cites: " + entry.getValue().stream().map(Paper::get_uid).map(String::valueOf).collect(Collectors.joining(", ")));
        }
    }
    public void info(String idx)  {
        int i;
        try {
            i = Integer.parseInt(idx);
        }
        catch (NumberFormatException e){
            invailed_cmd();
            return;
        }
        Paper p = papers.get(i);
        System.out.println(p.toString());
        System.out.println(p.get_uid());
        System.out.println(p.getCited_uid());
        System.out.println(p.getRelation_uid());
    }
    public static void main(String[] args) throws IOException, SQLException, ExecutionException, InterruptedException {
        MainCMD cmd = new MainCMD();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            System.out.print("input cmd:");
            String line = reader.readLine();
            if(line.isEmpty()){
                continue;
            }
            String body,head;
            if(line.contains(" ")){
                body = line.substring(line.indexOf(" ")+1);
                head = line.split(" ")[0];
            }
            else {
                head = line;
                body="";
            }
            head = head.toLowerCase();
            switch (head){
                case "exit" -> cmd.exit();
                case "help" -> cmd.help();
                case "get" -> cmd.get(body);
                case "print" -> cmd.print();
                case "cited" -> cmd.cited(body);
                case "submit" -> cmd.submit(body);
                case "sql" -> cmd.SQL();
                case "delete" -> cmd.delete(body);
                case "sqlclean" -> cmd.SQLclean();
                case "submits" -> cmd.submits();
                case "cg" -> cmd.CiteGraph(body);
                case "bg" -> cmd.buildGraph(body);
                case "info" -> cmd.info(body);
                default -> cmd.invailed_cmd();
            }
        }
    }
}
