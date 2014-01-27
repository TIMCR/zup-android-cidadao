package br.com.ntxdev.zup.domain;

import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.Color;

public class SolicitacaoListItem implements Serializable {

	private static final long serialVersionUID = 1L;

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

	private ArrayList<String> fotos;

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
}
