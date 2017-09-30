package br.com.utfpr.porta.repositorio.helper.porta;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.repositorio.filtro.PortaFiltro;
import br.com.utfpr.porta.repositorio.paginacao.PaginacaoUtil;

public class PortasImpl implements PortasQueries {
	
	@PersistenceContext
	private EntityManager manager;

	@Autowired
	private PaginacaoUtil paginacaoUtil;
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public Page<Porta> filtrar(PortaFiltro filtro, Pageable pageable) {
		Criteria criteria = manager.unwrap(Session.class).createCriteria(Porta.class);
		
		paginacaoUtil.preparar(criteria, pageable);
		adicionarFiltro(filtro, criteria);
		
		List<Porta> filtrados = criteria.list();
		return new PageImpl<>(filtrados, pageable, total(filtro));
	}
		
	private Long total(PortaFiltro filtro) {
		Criteria criteria = manager.unwrap(Session.class).createCriteria(Porta.class);
		adicionarFiltro(filtro, criteria);
		criteria.setProjection(Projections.rowCount());
		return (Long) criteria.uniqueResult();
	}

	private void adicionarFiltro(PortaFiltro filtro, Criteria criteria) {
		if (filtro != null) {
			if (!StringUtils.isEmpty(filtro.getDescricao())) {
				criteria.add(Restrictions.ilike("descricao", filtro.getDescricao(), MatchMode.ANYWHERE));
			}
			if(filtro.getEstabelecimento() != null && !StringUtils.isEmpty(filtro.getEstabelecimento().getCodigo())) {
				criteria.add(Restrictions.eq("estabelecimento", filtro.getEstabelecimento()));
			}			
		}
	}

}
