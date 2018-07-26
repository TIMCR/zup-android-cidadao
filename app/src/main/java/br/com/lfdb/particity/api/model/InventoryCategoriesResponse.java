package br.com.lfdb.particity.api.model;

import java.util.List;

public class InventoryCategoriesResponse {

    private final List<InventoryCategory> categories;

    public InventoryCategoriesResponse(List<InventoryCategory> categories) {
        this.categories = categories;
    }

    public List<InventoryCategory> getCategories() {
        return categories;
    }
}
