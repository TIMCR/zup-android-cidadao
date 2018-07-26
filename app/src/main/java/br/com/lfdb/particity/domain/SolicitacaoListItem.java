package br.com.lfdb.particity.domain;

import android.graphics.Color;

import java.io.Serializable;
import java.util.ArrayList;

public class SolicitacaoListItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public String getReferencia() {
        return (referencia == null || "null".equals(referencia)) ? "" : referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public static class Status implements Serializable {
        private static final long serialVersionUID = 1L;
        private String nome;
        private int cor;

        public Status() {
        }

        public Status(String nome, String corHtml) {
            this.nome = nome;
            this.cor = Color.parseColor(corHtml);
        }

        public Status(String nome, int cor) {
            this.nome = nome;
            this.cor = cor;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public int getCor() {
            return cor;
        }

        public void setCor(int cor) {
            this.cor = cor;
        }
    }

    private String protocolo;
    private String titulo;
    private String data;
    private Status status;
    private String comentario;
    private String endereco;
    private String referencia;
    private Double latitude;
    private Double longitude;
    private CategoriaRelato categoria;
    private Long id;
    private long creatorId;

    private ArrayList<String> fotos;

    public CategoriaRelato getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaRelato categoria) {
        this.categoria = categoria;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getComentario() {
        if (comentario == null || comentario.trim().isEmpty() ||
                comentario.equalsIgnoreCase("null")) return "";
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public ArrayList<String> getFotos() {
        return fotos;
    }

    public void setFotos(ArrayList<String> fotos) {
        this.fotos = fotos;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
