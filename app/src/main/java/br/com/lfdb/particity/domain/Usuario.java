package br.com.lfdb.particity.domain;

import java.io.Serializable;

public class Usuario implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String nome;
	private String email;
	private String telefone;
	private String cpf;
	private String endereco;
	private String complemento;
	private String cep;
	private String bairro;
	private String senha;
	private String confirmacaoSenha;
	private String senhaAntiga;
	private String cidade;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		if (nome != null && !nome.isEmpty() && !nome.equals("null")) this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (email != null && !email.isEmpty() && !email.equals("null")) this.email = email;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		if (telefone != null && !telefone.isEmpty() && !telefone.equals("null")) this.telefone = telefone;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		if (cpf != null && !cpf.isEmpty() && !cpf.equals("null")) this.cpf = cpf;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		if (endereco != null && !endereco.isEmpty() && !endereco.equals("null")) this.endereco = endereco;
	}

	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		if (complemento != null && !complemento.isEmpty() && !complemento.equals("null")) this.complemento = complemento;
	}

	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		if (cep != null && !cep.isEmpty() && !cep.equals("null")) this.cep = cep;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		if (bairro != null && !bairro.isEmpty() && !bairro.equals("null")) this.bairro = bairro;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getConfirmacaoSenha() {
		return confirmacaoSenha;
	}

	public void setConfirmacaoSenha(String confirmacaoSenha) {
		this.confirmacaoSenha = confirmacaoSenha;
	}

	public void setSenhaAntiga(String senhaAntiga) {
		this.senhaAntiga = senhaAntiga;
	}

	public String getSenhaAntiga() {
		return senhaAntiga;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public String getCidade() {
		return cidade;
	}
}
