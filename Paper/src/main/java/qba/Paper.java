package qba;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Paper{
    private final String title;
    private final String authorInfo;
    private final String relation_url;
    private final String cited_url;
    private final String pdf_url;
    private final int year;
    private ArrayList<Paper> relations = new ArrayList<Paper>();
    public Paper(String title, String info,String relation_url,String cited_url,String pdf_url){
        this.title = title;
        String[] info_list = info.split(",");
        this.authorInfo = info_list[0];
        String regex = "\\d{4} -";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(info);
        if (matcher.find()) {
            this.year = Integer.parseInt(matcher.group().substring(0,4));
        }
        else {
            this.year = -1;
        }
        if (relation_url!=null){
            this.relation_url = relation_url;
        }
        else{
            this.relation_url = null;
        }
        if (cited_url!=null){
            this.cited_url = cited_url;
        }
        else{
            this.cited_url = null;
        }
        if (cited_url!=null){
            this.pdf_url = pdf_url;
        }
        else{
            this.pdf_url = null;
        }
    }
    public void addRelation(Paper relation){
        relations.add(relation);
    }
    public void setRelations(ArrayList<Paper> relations) {
        this.relations = relations;
    }
    public ArrayList<Paper> getRelations(){
        return relations;
    }
    public String getTitle(){
        return title;
    }
    public String getAuthor(){
        return authorInfo;
    }
    public String getYear(){
        if (year==-1){
            return "unknow";
        }
        return String.valueOf(year);
    }
    public boolean isComplated(){
        if (year==-1){
            return false;
        }
        if (cited_url.length()<9){
            return false;
        }
        if (relation_url.length()<9){
            return false;
        }
        if (pdf_url.length()<9){
            return false;
        }
        return true;
    }
    public String getRelation_url(){return relation_url;}
    public String toString(){
        var info = "";
        info += "Title: " + title + "\n";
        info += "Author: " + authorInfo + "\n";
        if(year>0){
            info += "Year: " + year + "\n";
        }
        if (relation_url.length()>9){
            info += "Relation: " + relation_url + "\n";
        }
        if (cited_url.length()>9){
            info += "Cited: " + cited_url + "\n";
        }
        if (pdf_url.length()>9){
            info += "PDF: " + pdf_url + "\n";
        }
        info += "*".repeat(20)+"\n";
        return info;
    }
}
