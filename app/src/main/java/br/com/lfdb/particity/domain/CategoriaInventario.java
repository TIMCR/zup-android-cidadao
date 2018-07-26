package br.com.lfdb.particity.domain;

import java.io.Serializable;

public class CategoriaInventario implements Serializable {

    private static final long serialVersionUID = 1L;
    private long id;
    private String iconeAtivo;
    private String iconeInativo;
    private String marcador;
    private String nome;
    private String pin;
    private boolean showMarker;
    private String cor;
    private boolean privada;

    public CategoriaInventario() {
    }

    public CategoriaInventario(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIconeInativo() {
        return iconeInativo;
    }

    public void setIconeInativo(String iconeInativo) {
        this.iconeInativo = iconeInativo;
    }

    public String getIconeAtivo() {
        return iconeAtivo;
    }

    public void setIconeAtivo(String iconeAtivo) {
        this.iconeAtivo = iconeAtivo;
    }

    public String getMarcador() {
        return marcador;
    }

    public void setMarcador(String marcador) {
        this.marcador = marcador;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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
        if (!(obj instanceof CategoriaInventario))
            return false;
        CategoriaInventario other = (CategoriaInventario) obj;
        if (id != other.id)
            return false;
        return true;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public boolean isShowMarker() {
        return showMarker;
    }

    public void setShowMarker(boolean showMarker) {
        this.showMarker = showMarker;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getCor() {
        return cor;
    }

    public boolean isPrivada() {
        return privada;
    }

    public void setPrivada(boolean privada) {
        this.privada = privada;
    }
}
