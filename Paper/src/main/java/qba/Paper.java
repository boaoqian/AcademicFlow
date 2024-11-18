package qba;

import java.util.ArrayList;

public class Paper{
    private final String title;
    private final String authorInfo;
    private String relation_url;
    private ArrayList<Paper> relations = new ArrayList<Paper>();
    public Paper(String title, String author,String relation_url){
        this.title = title;
        this.authorInfo = author;
        this.relation_url = relation_url;
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
    public String getRelation_url(){return relation_url;}
    public String toString(){
        String info = "\n"+"-".repeat(20)+"\n"+getTitle()+"\n"+getAuthor()+"\n"+getRelation_url()+"\n"+"-".repeat(20);
        return info;
    }
}
