package br.com.lfdb.particity.domain;

import android.graphics.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoriaRelato implements Serializable {

    private static final long serialVersionUID = 1L;
    private long id;
    private String iconeAtivo;
    private String iconeInativo;
    private String marcador;
    private String nome;
    private ArrayList<CategoriaInventario> categoriasInventario;
    private ArrayList<CategoriaRelato> subcategorias;
    private CategoriaRelato categoriaMae;
    private boolean posicaoLivre;
    private boolean privada;

    private Long tempoResposta;
    private Long tempoResolucao;

    private boolean tempoResolucaoAtivado;
    private boolean tempoResolucaoPrivado;

    private boolean confidencial;

    private ArrayList<Status> status;
    private String cor;

    public CategoriaRelato() {
    }

    public CategoriaRelato(long id) {
        this.id = id;
    }

    public ArrayList<CategoriaInventario> getCategoriasInventario() {
        return categoriasInventario;
    }

    public void setCategoriasInventario(ArrayList<CategoriaInventario> categoriasInventario) {
        this.categoriasInventario = categoriasInventario;
    }

    public ArrayList<Status> getStatus() {
        return status;
    }

    public void setStatus(ArrayList<Status> status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIconeAtivo() {
        return iconeAtivo;
    }

    public void setIconeAtivo(String iconeAtivo) {
        this.iconeAtivo = iconeAtivo;
    }

    public String getIconeInativo() {
        return iconeInativo;
    }

    public void setIconeInativo(String iconeInativo) {
        this.iconeInativo = iconeInativo;
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

    public List<CategoriaRelato> getSubcategorias() {
        if (subcategorias == null) return Collections.<CategoriaRelato>emptyList();
        return subcategorias;
    }

    public void setSubcategorias(ArrayList<CategoriaRelato> subcategorias) {
        this.subcategorias = subcategorias;
    }

    public void addSubcategoria(CategoriaRelato categoria) {
        if (subcategorias == null) subcategorias = new ArrayList<>();
        subcategorias.add(categoria);
    }

    public CategoriaRelato getCategoriaMae() {
        return categoriaMae;
    }

    public void setCategoriaMae(CategoriaRelato categoriaMae) {
        this.categoriaMae = categoriaMae;
    }

    public boolean isSubcategoria(CategoriaRelato categoria) {
        if (subcategorias == null || subcategorias.isEmpty() || categoria == null) return false;

        for (CategoriaRelato c : subcategorias) {
            if (c.getId() == categoria.getId()) return true;
        }

        return false;
    }

    public long getTempoResposta() {
        return tempoResposta;
    }

    public void setTempoResposta(long tempoResposta) {
        this.tempoResposta = tempoResposta;
    }

    public long getTempoResolucao() {
        return tempoResolucao;
    }

    public void setTempoResolucao(Long tempoResolucao) {
        this.tempoResolucao = tempoResolucao;
    }

    public boolean isConfidencial() {
        return confidencial;
    }

    public void setConfidencial(boolean confidencial) {
        this.confidencial = confidencial;
    }

    public boolean isTempoResolucaoAtivado() {
        return tempoResolucaoAtivado;
    }

    public void setTempoResolucaoAtivado(boolean tempoResolucaoAtivado) {
        this.tempoResolucaoAtivado = tempoResolucaoAtivado;
    }

    public boolean isTempoResolucaoPrivado() {
        return tempoResolucaoPrivado;
    }

    public void setTempoResolucaoPrivado(boolean tempoResolucaoPrivado) {
        this.tempoResolucaoPrivado = tempoResolucaoPrivado;
    }

    public boolean isPosicaoLivre() {
        return posicaoLivre;
    }

    public void setPosicaoLivre(boolean posicaoLivre) {
        this.posicaoLivre = posicaoLivre;
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

    public static class Status implements Serializable {
        private long id;
        private int cor;
        private String nome;

        public Status() {
        }

        public Status(long id, String nome, String corHtml) {
            this.id = id;
            this.nome = nome;
            this.cor = Color.parseColor(corHtml);
        }

        public Status(String nome, String corHtml) {
            this.nome = nome;
            this.cor = Color.parseColor(corHtml);
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getCor() {
            return cor;
        }

        public void setCor(int cor) {
            this.cor = cor;
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
            if (!(obj instanceof Status))
                return false;
            Status other = (Status) obj;
            if (id != other.id)
                return false;
            return true;
        }
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
        if (!(obj instanceof CategoriaRelato))
            return false;
        CategoriaRelato other = (CategoriaRelato) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
