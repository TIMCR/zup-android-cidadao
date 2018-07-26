package br.com.lfdb.particity.domain;

public class Estatistica {

	private int id;
	private int cor;
	private int porcentagem;
	private int quantidade;
	private String texto;
	
	public Estatistica() {
	}
	
	public Estatistica(int id, int cor, int quantidade, String texto) {
		this.id = id;
		this.cor = cor;
		this.quantidade = quantidade;
		this.texto = texto;		
	}

	public int getCor() {
		return cor;
	}

	public void setCor(int cor) {
		this.cor = cor;
	}

	public int getPorcentagem() {
		return porcentagem;
	}

	public void setPorcentagem(int porcentagem) {
		this.porcentagem = porcentagem;
	}

	public int getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(int quantidade) {
		this.quantidade = quantidade;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		Estatistica other = (Estatistica) obj;
		if (!texto.equalsIgnoreCase(other.texto))
			return false;
		return true;
	}
}
