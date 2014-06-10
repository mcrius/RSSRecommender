/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rssrecommender.facade;

import com.rssrecommender.entity.UserLikes;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author bzzzt
 */
@Stateless
public class UserLikesFacade extends AbstractFacade<UserLikes> {
    @PersistenceContext(unitName = "RSSRecommender")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UserLikesFacade() {
        super(UserLikes.class);
    }
    
}
