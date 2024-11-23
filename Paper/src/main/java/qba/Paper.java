package qba;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Paper {
    private final String title;
    private final String authorInfo;
    private final String relation_url;
    private final String cited_url;
    private final int cited_count;
    private final String pdf_url;
    private final int year;
    private ArrayList<Paper> relations = new ArrayList<>();

    public Paper(String title, String info, String relation_url, String cited_url, String pdf_url, String cited_count) {
        this.title = title;
        String[] info_list = info.split(",");
        this.authorInfo = info_list[0];
        String regex = "\\d{4} -";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(info);
        if (matcher.find()) {
            this.year = Integer.parseInt(matcher.group().substring(0, 4));
        } else {
            this.year = -1;
        }
        regex = "\\d+";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(cited_count);
        if (matcher.find()) {
            this.cited_count = Integer.parseInt(matcher.group());
        } else {
            this.cited_count = -1;
        }
        this.relation_url = relation_url;
        this.cited_url = cited_url;
        if (cited_url != null) {
            this.pdf_url = pdf_url;
        } else {
            this.pdf_url = null;
        }
    }

    public void addRelation(Paper relation) {
        relations.add(relation);
    }

    public ArrayList<Paper> getRelations() {
        return relations;
    }

    public void setRelations(ArrayList<Paper> relations) {
        this.relations = relations;
    }

    public String getTitle() {
        return title;
    }
    public int getCited_count(){
        return cited_count;
    }

    public String getAuthor() {
        return authorInfo;
    }

    public String getYear() {
        if (year == -1) {
            return "unknow";
        }
        return String.valueOf(year);
    }

    public String getPdf_url() {
        return pdf_url;
    }

    public boolean isComplated() {
        if (year == -1) {
            return false;
        }
        if (cited_count <= 0) {
            return false;
        }
        if (cited_url.length() < 9) {
            return false;
        }
        if (relation_url.length() < 9) {
            return false;
        }
        if (pdf_url.length() < 9) {
            return false;
        }
        return true;
    }

    public String getRelation_url() {
        return relation_url;
    }

    public String toString() {
        var info = "\n";
        info += "Title: " + title + "\n";
        info += "Author: " + authorInfo + "\n";
        if (year > 0) {
            info += "Year: " + year + "\n";
        }
        if (relation_url.length() > 9) {
            info += "Relation: " + relation_url + "\n";
        }
        if (cited_url.length() > 9) {
            info += "Cited: " + cited_url + "\n";
        }
        if (cited_count > 0) {
            info += "Cited: " + cited_count + "\n";
        }
        if (pdf_url.length() > 9) {
            info += "PDF: " + pdf_url + "\n";
        }
        info += "*".repeat(20) + "\n";
        return info;
    }
    public static List<Paper> filter(Stream<Paper> stream, float threshold) {
        threshold = max(min(threshold,1),0);
        List<Paper> sorted_paper = stream.filter(Paper::isComplated).sorted(Comparator.comparing(Paper::getCited_count).reversed()).toList();
        return sorted_paper.subList(0, Math.round(((float) sorted_paper.size())*threshold));
    }
    public static List<Paper> filter(Stream<Paper> stream, int threshold) {
        List<Paper> sorted_paper = stream.filter(Paper::isComplated).sorted(Comparator.comparing(Paper::getCited_count).reversed()).toList();
        threshold = max(min(threshold,sorted_paper.size()),0);
        return sorted_paper.subList(0, threshold);
    }
    public static List<Paper> filter(Stream<Paper> stream,PaperFilter filter) {
        return stream.filter(filter::accept).toList();
    }

    public interface PaperFilter {
        boolean accept(Paper paper);
    }
}
