package br.com.lfdb.particity.api.model;

import android.graphics.Color;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.io.Serializable;

import br.com.lfdb.particity.domain.CategoriaRelato;
import br.com.lfdb.particity.domain.SolicitacaoListItem;

public class ReportCategoryStatus implements Serializable {

    private long id;
    private String title;
    private String color;
    private boolean initial;
    @SerializedName("final")
    private boolean finalStatus;
    private DateTime createdAt;
    private DateTime updatedAt;

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isInitial() {
        return initial;
    }

    public void setInitial(boolean initial) {
        this.initial = initial;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(DateTime createdAt) {
        this.createdAt = createdAt;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(DateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(boolean finalStatus) {
        this.finalStatus = finalStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportCategoryStatus that = (ReportCategoryStatus) o;

        if (id != that.id) return false;
        return !(title != null ? !title.equals(that.title) : that.title != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }

    public CategoriaRelato.Status compat() {
        CategoriaRelato.Status status = new CategoriaRelato.Status();
        status.setCor(Color.parseColor(color));
        status.setId(id);
        status.setNome(title);
        return status;
    }

    public SolicitacaoListItem.Status compat2() {
        SolicitacaoListItem.Status status = new SolicitacaoListItem.Status();
        status.setNome(title);
        status.setCor(Color.parseColor(color));
        return status;
    }
}
