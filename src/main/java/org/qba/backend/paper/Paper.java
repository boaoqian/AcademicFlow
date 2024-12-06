package org.qba.backend.paper;

import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.*;

public class Paper {
    /*a class store paper info and some func to prosses paper info*/
    private final String title;
    private String authorInfo = "";
    private String relation_url = "";
    private String cited_url = "";
    private int cited_count = 0;
    private String pdf_url = "";
    private int year = -1;
    private String abstract_text = "";
    private String cited_uid = "";
    private String relation_uid = "";

    public Paper(String title, String info, String relation_url, String cited_url, String pdf_url, String cited_count, String abstract_text) {
        this.title = title;
        this.abstract_text = abstract_text;
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
        }
        else {
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

    public Paper(String title, int year, String authorInfo, String relation_url, String cited_url,
                 String pdf_url, int cited_count, String abstract_text, String cited_uid, String relation_uid) {
        this.title = title;
        this.year = year;
        this.authorInfo = authorInfo;
        this.relation_url = relation_url;
        this.cited_url = cited_url;
        this.pdf_url = pdf_url;
        this.cited_count = cited_count;
        this.abstract_text = abstract_text;
        this.relation_uid = relation_uid;
        this.cited_uid = cited_uid;
    }

    public int hashCode() {
        return get_uid();  // 使用 Objects.hash 来计算哈希值
    }

    public String getRelation_uid() {
        if(relation_uid==null||relation_uid.isEmpty()){
            return null;
        }
        return relation_uid;
    }
    public void setRelation_uid(List<Paper> relation_uid) {
        this.relation_uid = relation_uid.stream().map(Paper::get_uid).map(Object::toString).collect(Collectors.joining(","));
    }
    public String getCited_uid(){
        if (cited_uid==null||cited_uid.isEmpty()) {
            return null;
        }
        return cited_uid;
    }
    public void setCited_uid(String cited){
        this.cited_uid = cited;
    }
    public void add_cited_uid(int cited){
        if(cited_uid.isEmpty()){
            cited_uid = ""+cited;
        }
        else this.cited_uid += ","+cited;
    }
    public void setCited_uid(List<Paper> cited){
        this.cited_uid = cited.stream().map(Paper::get_uid).map(Object::toString).collect(Collectors.joining(","));
    }
    public String getAbstract() {
        return abstract_text;
    }

    public String getTitle() {
        return title;
    }

    public int getCited_count() {
        return cited_count;
    }

    public String getAuthor() {
        return authorInfo;
    }

    public int getYear() {
        return year;
    }

    public String getPdf_url() {
        return pdf_url;
    }

    public int get_uid(){
        return Objects.hash(title)*31+Objects.hash(pdf_url);
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
    public String getCited_url() {
        return cited_url;
    }

    public String toString() {
        var info = "\n";
        info += "Title: " + title + "\n";
        info += "Author: " + authorInfo + "\n";
        if (year > 0) {
            info += "Year: " + year + "\n";
        }
        if (abstract_text!=null&&abstract_text.length()>10){
            info += "Abstract: " + abstract_text + "\n";
        }
        info += "*".repeat(20) + "\n";
        return info;
    }

    public static Stream<Paper> filter(List<Paper> papers, PaperFilter Filter) {
        return Filter.filter(papers);
    }

    public interface PaperFilter {
        Stream<Paper> filter(List<Paper> papers);
    }

    public static class CitedCountFilter implements PaperFilter {
        float threshold = -1;
        int limit = -1;

        public CitedCountFilter(float threshold) {
            this.threshold = max(min(threshold, 1), 0);
        }

        public CitedCountFilter(int limit) {
            this.limit = limit;
        }

        @Override
        public Stream<Paper> filter(List<Paper> papers) {
            if (threshold < 0) {
                limit = max(min(limit, papers.size()), 0);
            } else limit = (int) ceil(papers.size() * threshold);
            return papers.stream().filter(Paper::isComplated).sorted(Comparator.comparing(Paper::getCited_count).reversed()).limit(limit);
        }
    }
    public static class CitedFilter implements PaperFilter {
        @Override
        public Stream<Paper> filter(List<Paper> papers) {
            return papers.stream().filter(paper -> paper.getCited_count() > 0);
        }
    }

    public static class YearFilter implements PaperFilter {
        int newest = Year.now().getValue() + 1;
        int oldest = newest - 6;

        YearFilter(int span) {
            if (newest - span < 1900) {
                throw new IllegalArgumentException("Year limit must be greater than or equal to 1900");
            } else this.oldest = newest - span;
        }

        YearFilter(int newest, int oldest) {
            if (newest < oldest) {
                throw new IllegalArgumentException("newest order than ordest");
            }
            this.newest = newest;
            this.oldest = oldest;
        }

        YearFilter() {
            //default newest = now year + 1
            //default ordest = now year - 5
        }

        @Override
        public Stream<Paper> filter(List<Paper> papers) {
            return papers.stream().filter(Paper::isComplated).filter(p -> p.getYear() <= newest && p.getYear() >= oldest);
        }
    }
}
