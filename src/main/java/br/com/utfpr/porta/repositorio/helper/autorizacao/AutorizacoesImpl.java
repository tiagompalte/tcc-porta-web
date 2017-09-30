package br.com.utfpr.porta.repositorio.helper.autorizacao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.utfpr.porta.modelo.Autorizacao;
import br.com.utfpr.porta.repositorio.filtro.AutorizacaoFiltro;
import br.com.utfpr.porta.repositorio.paginacao.PaginacaoUtil;

public class AutorizacoesImpl implements AutorizacoesQueries {
	
	@PersistenceContext
	private EntityManager manager;
	
	@Autowired
	private PaginacaoUtil paginacaoUtil;

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public List<Autorizacao> findByCodigoUsuarioAndCodigoPorta(Long codigoUsuario, Long codigoPorta) {		
		Criteria criteria = manager.unwrap(Session.class).createCriteria(Autorizacao.class);
		criteria.add(Restrictions.eq("id.usuario.codigo", codigoUsuario));
		criteria.add(Restrictions.eq("id.porta.codigo", codigoPorta));		
		criteria.addOrder(Order.asc("tipoAutorizacao"));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public Page<Autorizacao> filtrar(AutorizacaoFiltro filtro, Pageable pageable) {		
		Criteria criteria = manager.unwrap(Session.class).createCriteria(Autorizacao.class);		
		paginacaoUtil.preparar(criteria, pageable);
		adicionarFiltro(filtro, criteria);		
		List<Autorizacao> filtrados = criteria.list();
		return new PageImpl<>(filtrados, pageable, total(filtro));
	}
	
	private Long total(AutorizacaoFiltro filtro) {
		Criteria criteria = manager.unwrap(Session.class).createCriteria(Autorizacao.class);
		adicionarFiltro(filtro, criteria);
		criteria.setProjection(Projections.rowCount());
		return (Long) criteria.uniqueResult();
	}

	private void adicionarFiltro(AutorizacaoFiltro filtro, Criteria criteria) {
		if (filtro != null) {
			if (filtro.getUsuario() != null && !StringUtils.isEmpty(filtro.getUsuario().getCodigo())) {
				criteria.add(Restrictions.eq("id.usuario", filtro.getUsuario()));
			}
			if (filtro.getPorta() != null && !StringUtils.isEmpty(filtro.getPorta().getCodigo())) {
				criteria.add(Restrictions.eq("id.porta", filtro.getPorta()));
			}
			if (filtro.getTipoAutorizacao() != null && !StringUtils.isEmpty(filtro.getTipoAutorizacao().name())) {
				criteria.add(Restrictions.eq("tipoAutorizacao", filtro.getTipoAutorizacao()));
			}
			if (filtro.getEstabelecimento() != null && !StringUtils.isEmpty(filtro.getEstabelecimento().getCodigo())) {
				criteria.add(Restrictions.eq("id.estabelecimento", filtro.getEstabelecimento()));
			}
		}
	}
	
}
