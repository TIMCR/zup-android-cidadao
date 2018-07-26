package br.com.lfdb.particity.domain;

import java.util.ArrayList;

public class ItemRelato {

	private long id;
	private double latitude;
	private double longitude;
	private String data;
	private String endereco;
	private String protocolo;
	private String descricao;
    private String referencia;
	private CategoriaRelato categoria;
	private long idItemInventario;
	private long idStatus;
	private ArrayList<String> fotos = new ArrayList<String>();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public CategoriaRelato getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaRelato categoria) {
		this.categoria = categoria;
	}

	public long getIdItemInventario() {
		return idItemInventario;
	}

	public void setIdItemInventario(long idItemInventario) {
		this.idItemInventario = idItemInventario;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getProtocolo() {
		return protocolo;
	}

	public void setProtocolo(String protocolo) {
		this.protocolo = protocolo;
	}

	public long getIdStatus() {
		return idStatus;
	}

	public void setIdStatus(long idStatus) {
		this.idStatus = idStatus;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public ArrayList<String> getFotos() {
		return fotos;
	}

	public void setFotos(ArrayList<String> fotos) {
		this.fotos = fotos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemRelato other = (ItemRelato) obj;
		if (id != other.id)
			return false;
		return true;
	}

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
