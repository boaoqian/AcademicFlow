package qba;

import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Math.*;

public class Paper {
    /*a class store paper info and some func to prosses paper info*/
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
