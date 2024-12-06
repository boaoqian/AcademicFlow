package org.qba.academicflow;

import org.qba.backend.api.GoogleAPI;
import org.qba.backend.paper.Paper;

import java.util.List;

public class InfoModel {
    public String search_name = "";
    public List<Paper> papers;
    InfoModel(String search_name) {
        this.search_name = search_name;
    }
    InfoModel(){}
}
