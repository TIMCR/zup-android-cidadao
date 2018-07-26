package br.com.lfdb.particity.api.model;

import java.util.List;

public class ReportCategoriesResponse {

    private final List<ReportCategory> categories;

    public ReportCategoriesResponse(List<ReportCategory> categories) {
        this.categories = categories;
    }

    public List<ReportCategory> getCategories() {
        return categories;
    }
}
