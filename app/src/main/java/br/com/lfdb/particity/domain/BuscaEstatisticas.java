package br.com.lfdb.particity.domain;

import java.io.Serializable;

public class BuscaEstatisticas implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long categoria;
	private Periodo periodo = Periodo.ULTIMOS_6_MESES;

	public Periodo getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
	}

	public Long getCategoria() {
		return categoria;
	}

	public void setCategoria(Long categoria) {
		this.categoria = categoria;
	}

}
