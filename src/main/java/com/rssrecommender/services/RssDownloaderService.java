/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rssrecommender.services;

import com.rssrecommender.entity.Article;
import com.rssrecommender.facade.ArticleFacade;
import com.rssrecommender.feed.Feed;
import com.rssrecommender.feed.FeedMessage;
import com.rssrecommender.feed.RSSFeedParser;
import java.util.Calendar;
import java.util.List;
import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 *
 * @author bzzzt
 */
@Singleton
@Startup
public class RssDownloaderService {

    public static final String[] feeds = {
        "http://www.vogella.com/article.rss",
        "http://searchoracle.techtarget.com/rss/Oracle-news-and-trends.xml",
        "http://whatis.techtarget.com/rss/Enterprise-IT-news-roundup.xml",
        "http://whatis.techtarget.com/rss/Enterprise-IT-tips-and-expert-advice.xml",
        "http://searchcloudapplications.techtarget.com/rss/News-on-SaaS-PaaS-cloud-app-integration-and-development.xml",
//        "http://www.theserverside.com/rss/forum.tss?forum_id=3",
//        "http://www.theserverside.com/rss/forum.tss?forum_id=33",
//        "http://www.theserverside.com/rss/forum.tss?forum_id=35",
//        "http://www.theserverside.com/rss/forum.tss?forum_id=25"
    };

    @Inject
    ArticleFacade articleFacade;

    public RssDownloaderService() {
    }

    public List<FeedMessage> getMessages() {
//        RSSFeedParser parser = new RSSFeedParser();
//        Feed feed = parser.readFeed();
//        System.out.println(feed);
//        return feed.getMessages();
        return null;
    }

    @Schedule(second = "1", minute = "*/15", hour = "*", persistent = true)
    @Asynchronous
    public void downloadAndSave() {
        System.out.println("Retrieving feeds");
        for (String url : feeds) {
            System.out.println("Retrieving " + url);
            RSSFeedParser parser = new RSSFeedParser(url);
            Feed feed = parser.readFeed();
            if (feed != null && feed.getMessages() != null && !feed.getMessages().isEmpty()) {
                for (FeedMessage m : feed.getMessages()) {
                    String uid = m.getLink();
                    long count = articleFacade.countWithLink(uid);
                    if (count == 0) {
                        Article a = new Article();
                        a.setAuthor(m.getAuthor().trim());
                        a.setDescription(m.getDescription().trim().replaceAll("\\<.*?>",""));
                        a.setGuid(m.getGuid());
                        a.setLink(m.getLink());
                        a.setTitle(m.getTitle());
                        a.setDate(m.getDate());
                        articleFacade.edit(a);
                    }
                }
            }
        }
    }
}
