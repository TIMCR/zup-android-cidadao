package br.com.lfdb.particity.api.model;

import android.content.Context;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import br.com.lfdb.particity.domain.SolicitacaoListItem;
import br.com.lfdb.particity.service.CategoriaRelatoService;
import br.com.lfdb.particity.util.DateUtils;

public class ReportItem {

    private long id;
    private String description;
    private ReportCategory category;
    private long protocol;
    private String reference;
    private long categoryId;
    private String latitude;
    private String longitude;
    private String postalCode;
    private Long inventoryItemId;
    private String address;
    private String number;
    private String district;
    private String city;
    private String state;
    private String country;
    private List<ReportImage> images;
    private DateTime createdAt;
    private DateTime updatedAt;
    private Location position;
    private ReportCategoryStatus status;
    private User user;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(Long inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<ReportImage> getImages() {
        return images;
    }

    public void setImages(List<ReportImage> images) {
        this.images = images;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProtocol() {
        return protocol;
    }

    public void setProtocol(long protocol) {
        this.protocol = protocol;
    }

    public ReportCategory getCategory() {
        return category;
    }

    public void setCategory(ReportCategory category) {
        this.category = category;
    }

    public SolicitacaoListItem compat(Context context) {
        SolicitacaoListItem item = new SolicitacaoListItem();

        item.setTitulo(category.getTitle());
        item.setCategoria(new CategoriaRelatoService().getById(context, category.getId()));
        item.setComentario(description);
        item.setCreatorId(user.getId());
        item.setData((DateUtils.getIntervaloTempo(createdAt.toDate())));
        item.setEndereco(address +
                (number != null ? ", " + number : "") +
                (postalCode != null ? ", " + postalCode : "") +
                (district != null ? ", " + district : ""));
        ArrayList<String> images = new ArrayList<>();
        for (ReportImage image : this.images) {
            images.add(image.getOriginal());
        }
        item.setFotos(images);
        item.setId(id);
        item.setLatitude(position.getLatitude());
        item.setLongitude(position.getLongitude());
        item.setProtocolo(String.valueOf(protocol));
        item.setReferencia(reference);
        item.setStatus(status.compat2());

        return item;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
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

    public Location getPosition() {
        return position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public ReportCategoryStatus getStatus() {
        return status;
    }

    public void setStatus(ReportCategoryStatus status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
