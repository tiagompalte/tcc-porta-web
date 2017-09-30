package br.com.utfpr.porta.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "autorizacao")
public class Autorizacao implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private AutorizacaoId id;
	
	@NotNull(message = "Escolha o tipo de autorização")
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_autorizacao")
	private TipoAutorizacao tipoAutorizacao;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "dia_semana")
	private DiaSemana diaSemana;
	
	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	@Column(name = "hora_inicio")
	private LocalTime horaInicio;
	
	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	@Column(name = "hora_fim")
	private LocalTime horaFim;
	
	@DateTimeFormat(pattern = "dd/MM/yyyy hh:mm")
	@Column(name = "data_hora_inicio")
	private LocalDateTime dataHoraInicio; 
	
	@DateTimeFormat(pattern = "dd/MM/yyyy hh:mm")	
	@Column(name = "data_hora_fim")
	private LocalDateTime dataHoraFim;
	
	@Transient
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private LocalDate dataTemporaria;
	
	@Transient
	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	private LocalTime horaInicioTemporaria;
	
	@Transient
	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	private LocalTime horaFimTemporaria;
	
	@PostLoad
	private void postLoad() {
		if(tipoAutorizacao != null && tipoAutorizacao.compareTo(TipoAutorizacao.TEMPORARIO) == 0
				&& dataHoraInicio != null && dataHoraFim != null) {
			dataTemporaria = dataHoraInicio.toLocalDate();
			horaInicioTemporaria = dataHoraInicio.toLocalTime();
			horaFimTemporaria = dataHoraFim.toLocalTime();
		}
	}
	
	public String getDescricao() {
		
		if(tipoAutorizacao != null && id != null && id.getPorta() != null 
				&& id.getUsuario() != null && id.getUsuario().getPessoa() != null) {
			return "autorização " + tipoAutorizacao.getDescricao().toLowerCase()
					+ " da porta '" + id.getPorta().getDescricao() + "'" 
					+ " usuário '" + id.getUsuario().getPessoa().getNome() + "'"; 
		}			
		return "autorização";
	}
		
	public boolean isNovo() {
		return id != null && id.getSequencia() == null;
	}

	public AutorizacaoId getId() {
		return id;
	}

	public void setId(AutorizacaoId id) {
		this.id = id;
	}

	public TipoAutorizacao getTipoAutorizacao() {
		return tipoAutorizacao;
	}

	public void setTipoAutorizacao(TipoAutorizacao tipoAutorizacao) {
		this.tipoAutorizacao = tipoAutorizacao;
	}

	public DiaSemana getDiaSemana() {
		return diaSemana;
	}

	public void setDiaSemana(DiaSemana diaSemana) {
		this.diaSemana = diaSemana;
	}

	public LocalTime getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(LocalTime horaInicio) {
		this.horaInicio = horaInicio;
	}

	public LocalTime getHoraFim() {
		return horaFim;
	}

	public void setHoraFim(LocalTime horaFim) {
		this.horaFim = horaFim;
	}

	public LocalDateTime getDataHoraInicio() {
		return dataHoraInicio;
	}

	public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
		this.dataHoraInicio = dataHoraInicio;
	}

	public LocalDateTime getDataHoraFim() {
		return dataHoraFim;
	}

	public void setDataHoraFim(LocalDateTime dataHoraFim) {
		this.dataHoraFim = dataHoraFim;
	}
	
	public LocalDate getDataTemporaria() {
		return dataTemporaria;
	}

	public void setDataTemporaria(LocalDate dataTemporaria) {
		this.dataTemporaria = dataTemporaria;
	}

	public LocalTime getHoraInicioTemporaria() {
		return horaInicioTemporaria;
	}

	public void setHoraInicioTemporaria(LocalTime horaInicioTemporaria) {
		this.horaInicioTemporaria = horaInicioTemporaria;
	}

	public LocalTime getHoraFimTemporaria() {
		return horaFimTemporaria;
	}

	public void setHoraFimTemporaria(LocalTime horaFimTemporaria) {
		this.horaFimTemporaria = horaFimTemporaria;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Autorizacao other = (Autorizacao) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	} 	

}
