package br.com.utfpr.porta.repositorio.filtro;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import br.com.utfpr.porta.modelo.Estabelecimento;

public class LogFiltro {
	
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private LocalDate dataInicio;
	
	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	private LocalTime horaInicio;
	
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private LocalDate dataFinal;
	
	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	private LocalTime horaFinal;
	
    private Estabelecimento estabelecimento;
	
	public Estabelecimento getEstabelecimento() {
		return estabelecimento;
	}

	public void setEstabelecimento(Estabelecimento estabelecimento) {
		this.estabelecimento = estabelecimento;
	}
	public LocalDate getDataInicio() {
		return dataInicio;
	}
	public void setDataInicio(LocalDate dataInicio) {
		this.dataInicio = dataInicio;
	}
	public LocalTime getHoraInicio() {
		return horaInicio;
	}
	public void setHoraInicio(LocalTime horaInicio) {
		this.horaInicio = horaInicio;
	}
	public LocalDate getDataFinal() {
		return dataFinal;
	}
	public void setDataFinal(LocalDate dataFinal) {
		this.dataFinal = dataFinal;
	}
	public LocalTime getHoraFinal() {
		return horaFinal;
	}
	public void setHoraFinal(LocalTime horaFinal) {
		this.horaFinal = horaFinal;
	}
	
}
