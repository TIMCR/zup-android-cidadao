package br.com.lfdb.particity.api.model;

import java.io.Serializable;
import java.util.List;

import br.com.lfdb.particity.domain.CategoriaRelato;

public class ReportCategory implements Serializable {

    private long id;
    private String title;
    private long parentId;
    private String color;
    private boolean allowsArbitraryPosition;
    private boolean privateResolutionTime;
    private boolean resolutionTimeEnabled;
    private long resolutionTime;
    private long userResponseTime;
    private String originalIcon;
    private Icon icon;
    private Marker marker;

    private List<ReportCategoryStatus> statuses;
    private List<ReportCategory> subcategories;

    public List<ReportCategoryStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<ReportCategoryStatus> statuses) {
        this.statuses = statuses;
    }

    public List<ReportCategory> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<ReportCategory> subcategories) {
        this.subcategories = subcategories;
    }

    public long getResolutionTime() {
        return resolutionTime;
    }

    public void setResolutionTime(long resolutionTime) {
        this.resolutionTime = resolutionTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isAllowsArbitraryPosition() {
        return allowsArbitraryPosition;
    }

    public void setAllowsArbitraryPosition(boolean allowsArbitraryPosition) {
        this.allowsArbitraryPosition = allowsArbitraryPosition;
    }

    public boolean isPrivateResolutionTime() {
        return privateResolutionTime;
    }

    public void setPrivateResolutionTime(boolean privateResolutionTime) {
        this.privateResolutionTime = privateResolutionTime;
    }

    public boolean isResolutionTimeEnabled() {
        return resolutionTimeEnabled;
    }

    public void setResolutionTimeEnabled(boolean resolutionTimeEnabled) {
        this.resolutionTimeEnabled = resolutionTimeEnabled;
    }

    public long getUserResponseTime() {
        return userResponseTime;
    }

    public void setUserResponseTime(long userResponseTime) {
        this.userResponseTime = userResponseTime;
    }

    public String getOriginalIcon() {
        return originalIcon;
    }

    public void setOriginalIcon(String originalIcon) {
        this.originalIcon = originalIcon;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public CategoriaRelato compat() {
        CategoriaRelato categoria = new CategoriaRelato();

        categoria.setNome(title);

        return categoria;
    }
}
