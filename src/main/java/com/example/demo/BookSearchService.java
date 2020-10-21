package com.example.demo;

import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class BookSearchService {
    @Autowired
    private final EntityManager entityManager;

    @Autowired
    public BookSearchService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void initializeHibernateSearch() {

        try {
            SearchSession searchSession = Search.session( entityManager );

            MassIndexer indexer = searchSession.massIndexer( Book.class );
            indexer.startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public List<Book> searchByAuthor(String keyword){
        SearchSession searchSession = Search.session( entityManager );

        SearchResult<Book> result = searchSession.search( Book.class )
                .where( f -> f.match()
                        .fields( "title", "authors.lastName", "authors.firstName" )
                        .matching( keyword )
                )
                .fetchAll();

        long totalHitCount = result.total().hitCount();
        return result.hits();
    }

    public List<Book> searchByKeyword(String keyword){
        SearchSession searchSession = Search.session( entityManager );

        SearchResult<Book> result = searchSession.search( Book.class )
                .where( f -> f.match()
                        .fields( "keywords" )
                        .matching( keyword )
                )
                .fetchAll();

        long totalHitCount = result.total().hitCount();
        return result.hits();
    }

}
