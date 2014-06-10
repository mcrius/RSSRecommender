/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rssrecommender.services;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.rssrecommender.entity.Article;
import com.rssrecommender.facade.ArticleFacade;
import com.rssrecommender.facade.UserFacade;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.ConjugateGradientOptimizer;
import org.apache.mahout.cf.taste.impl.recommender.knn.KnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
import org.apache.mahout.cf.taste.impl.similarity.GenericItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 *
 * @author bzzzt
 */
@Singleton
@Startup
public class MahoutService {

    @Inject
    ArticleFacade af;
    @Inject
    UserFacade uf;
    MysqlConnectionPoolDataSource dataSource;
    Recommender r;
    JDBCDataModel model;
    ReloadFromJDBCDataModel m;
    ItemSimilarity is;
    ItemSimilarity cis;
    IDRescorer rsr;

    @PostConstruct
    public void init() {
        try {
            dataSource = new MysqlConnectionPoolDataSource();

            dataSource.setServerName("localhost");
            dataSource.setUser("root");
            dataSource.setPassword("1234");
            dataSource.setDatabaseName("rss_rec");
            dataSource.setCachePreparedStatements(true);
            dataSource.setCachePrepStmts(true);
            dataSource.setCacheResultSetMetadata(true);
            dataSource.setAlwaysSendSetIsolation(true);
            dataSource.setElideSetAutoCommits(true);

            model = new MySQLJDBCDataModel(dataSource, "user_likes_article", "user_id", "article_id", "rating", null);
            m = new ReloadFromJDBCDataModel(model);
            is = new PearsonCorrelationSimilarity(m);

//            ItemSimilarity is = new EuclideanDistanceSimilarity(model, Weighting.WEIGHTED);
//            cis = new CachingItemSimilarity(is, 150);
            r = new GenericItemBasedRecommender(m, is);
            rsr = new IDRescorer() {

                @Override
                public double rescore(long id, double originalScore) {
                    Article a = af.find((int) id);
                    long diff = Calendar.getInstance().getTimeInMillis() - a.getDate().getTime() + 1;
                    double time = 1.0d / ( diff /(1000 * 360 * 12));  // half day period since feeds are update regularly
                    return originalScore * time;
                }

                @Override
                public boolean isFiltered(long id) {
                    //always rescore
                    return false;
                }
            };
//            Optimizer optimizer = new ConjugateGradientOptimizer();
//            r = new KnnItemBasedRecommender(m, is, optimizer, 5);
//          
        } catch (TasteException ex) {
            Logger.getLogger(MahoutService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Article> getArticles(Integer userId) {
        try {
            List<RecommendedItem> items = r.recommend(userId, 10, rsr);
            List<Integer> ids = new ArrayList<>();
            if (!items.isEmpty()) {
                for (RecommendedItem i : items) {
                    ids.add((int) i.getItemID());
                }
                return af.findForIdIn(ids);
            }
        } catch (TasteException ex) {
            Logger.getLogger(MahoutService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /*
     public void generateLikes() {
     try {
     //            User user = uf.find(1);
     Random r = new Random();
     Connection c = dataSource.getConnection();
     Statement s = c.createStatement();
     for (int i = 3; i < 10000; i++) {
     User u = uf.find(i);
     for (int j = 0; j < 10; j++) {
     Article a = af.find(r.nextInt(160) + 1);

     if (a != null) {
     UserLikes ul = new UserLikes();
     ul.setUserId(u);
     ul.setArticleId(a);
     ul.setRating(r.nextFloat());
     u.getUserLikesList().add(ul);
     uf.edit(u);
     }
     }
     }
     } catch (SQLException ex) {
     Logger.getLogger(MahoutService.class.getName()).log(Level.SEVERE, null, ex);
     }
     }
     */
}
