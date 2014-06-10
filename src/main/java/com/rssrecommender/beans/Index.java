/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rssrecommender.beans;

import com.rssrecommender.entity.Article;
import com.rssrecommender.entity.User;
import com.rssrecommender.facade.ArticleFacade;
import com.rssrecommender.facade.UserFacade;
import com.rssrecommender.services.MahoutService;
import com.rssrecommender.services.RssDownloaderService;
import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;

/**
 *
 * @author bzzzt
 */
@ManagedBean
@ViewScoped
public class Index implements Serializable {

    @Inject
    ArticleFacade articleFacade;
    @Inject
    RssDownloaderService service;
    @Inject
    MahoutService mahoutService;
    @Inject
    UserFacade uf;
    @Inject
    Principal p;

    private List<Article> articles;
    private List<Article> featured;

    @PostConstruct
    public void init() {
        if (p != null) {
            if (!p.getName().equalsIgnoreCase("anonymous")) {
                User u = uf.findByUsername(p.getName());
                articles = mahoutService.getArticles(u.getId());
            } else {
                articles = articleFacade.findLatest(10);
            }
        }
        featured = articleFacade.findMostViewedLastWeek();
    }

    public void download(AjaxBehaviorEvent e) {
        service.downloadAndSave();
    }

    public String increaseViews(Article a) {
        a.setViews(a.getViews() + 1);
        articleFacade.edit(a);
        return "/exit";
    }

//    public void generateLikes(AjaxBehaviorEvent e){
//        mahoutService.generateLikes();
//    }
    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public String getUsername() {
        return p.getName();
    }

    public List<Article> getFeatured() {
        return featured;
    }

    public void setFeatured(List<Article> featured) {
        this.featured = featured;
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/index?faces-redirect=true";
    }

}
