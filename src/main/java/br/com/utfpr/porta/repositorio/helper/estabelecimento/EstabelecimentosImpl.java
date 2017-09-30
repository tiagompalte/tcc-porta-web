package br.com.utfpr.porta.repositorio.helper.estabelecimento;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.repositorio.filtro.EstabelecimentoFiltro;
import br.com.utfpr.porta.repositorio.paginacao.PaginacaoUtil;

public class EstabelecimentosImpl implements EstabelecimentosQueries {
	
	@PersistenceContext
	private EntityManager manager;
	
	@Autowired
	private PaginacaoUtil paginacaoUtil;

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public Page<Estabelecimento> filtrar(EstabelecimentoFiltro filtro, Pageable pageable) {		
		Criteria criteria = manager.unwrap(Session.class).createCriteria(Estabelecimento.class);				
		paginacaoUtil.preparar(criteria, pageable);
		adicionarFiltro(filtro, criteria);
		List<Estabelecimento> filtrados = criteria.list();
		return new PageImpl<>(filtrados, pageable, total(filtro));
	}
	
	private Long total(EstabelecimentoFiltro filtro) {
		Criteria criteria = manager.unwrap(Session.class).createCriteria(Estabelecimento.class);
		adicionarFiltro(filtro, criteria);
		criteria.setProjection(Projections.rowCount());
		return (Long) criteria.uniqueResult();
	}

	private void adicionarFiltro(EstabelecimentoFiltro filtro, Criteria criteria) {
		if (filtro != null) {
			if (filtro.getCidade() != null && !StringUtils.isEmpty(filtro.getCidade())) {
				criteria.add(Restrictions.eq("endereco.cidade", filtro.getCidade()));
			}
			if (filtro.getEstado() != null && !StringUtils.isEmpty(filtro.getEstado())) {
				criteria.add(Restrictions.eq("endereco.estado", filtro.getEstado()));
			}
		}
	}

}
