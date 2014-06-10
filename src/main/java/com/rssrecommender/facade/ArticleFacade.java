/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rssrecommender.facade;

import com.rssrecommender.entity.Article;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author bzzzt
 */
@Stateless
public class ArticleFacade extends AbstractFacade<Article> {
    @PersistenceContext(unitName = "RSSRecommender")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ArticleFacade() {
        super(Article.class);
    }
    
    public long countWithLink(String uid){
        return (long) em.createQuery("SELECT count(a) FROM Article a WHERE a.link = :link").setParameter("link", uid).getSingleResult();
    }
    
    public List<Article> findLatest(int count){
        return em.createQuery("SELECT a FROM Article a ORDER BY a.date DESC").setMaxResults(count).getResultList();
    }
    
    public List<Article> findForIdIn(List<Integer> ids){
        return em.createQuery("SELECT a FROM Article a WHERE a.id IN :ids").setParameter("ids", ids).getResultList();
    }
    
    public List<Article> findMostViewedLastWeek(){
        Date date = Calendar.getInstance().getTime();
        date.setDate(date.getDate() - 7);
        return em.createQuery("SELECT a FROM Article a WHERE a.date >= :date ORDER BY a.views DESC").setParameter("date", date).setMaxResults(5).getResultList();
    }
    
}
