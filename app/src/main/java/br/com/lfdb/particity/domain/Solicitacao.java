package br.com.lfdb.particity.domain;

import java.io.Serializable;
import java.util.ArrayList;

public class Solicitacao implements Serializable {

	private static final long serialVersionUID = 1L;

	private CategoriaRelato categoria;
	private double latitude;
	private double longitude;
	private ArrayList<String> fotos = new ArrayList<>();
	private String comentario;
    private String endereco;
    private String referencia;
	private boolean redeSocial = false;
    private Long idItemInventario;

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	public void setLatitudeLongitude(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEndereco() {
        return endereco;
    }

	public ArrayList<String> getFotos() {
		return fotos;
	}
	
	public void adicionarFoto(String foto) {
		fotos.add(foto);
	}
	
	public void removerFoto(String foto) {
		fotos.remove(foto);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public boolean isRedeSocial() {
		return redeSocial;
	}

	public void setRedeSocial(boolean redeSocial) {
		this.redeSocial = redeSocial;
	}

	public CategoriaRelato getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaRelato categoria) {
		this.categoria = categoria;
	}

    public Long getIdItemInventario() {
        return idItemInventario;
    }

    public void setIdItemInventario(Long idItemInventario) {
        this.idItemInventario = idItemInventario;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
