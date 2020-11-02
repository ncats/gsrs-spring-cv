package gsrs.legacy;

import gsrs.repository.GsrsRepository;
import ix.core.models.ETag;
import ix.core.search.SearchOptions;
import ix.core.search.SearchResult;
import ix.core.search.text.TextIndexer;
import ix.core.search.text.TextIndexerFactory;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.lucene.search.Filter;
import java.io.IOException;
import java.util.Optional;

public abstract class LegacyGsrsSearchService<T> implements GsrsSearchService<T>{

    @Autowired
    private TextIndexerFactory textIndexerFactory;

    private final GsrsRepository gsrsRepository;
    private final Class<T> entityClass;

    protected LegacyGsrsSearchService(Class<T> entityClass, GsrsRepository<T, ?> repository){
        gsrsRepository= repository;
        this.entityClass = entityClass;

    }
    @Override
    public SearchResult search(String query, SearchOptions options) throws IOException {
        return textIndexerFactory.getDefaultInstance().search(gsrsRepository, options, query);
    }

    @Override
    public TextIndexer.TermVectors getTermVectors(Optional<String> field) throws IOException {
        try {
            return textIndexerFactory.getDefaultInstance().getTermVectors(entityClass, field.orElse(null));
        } catch (Exception e) {
            throw new IOException("error generating term vectors", e);
        }
    }

    @Override
    public TextIndexer.TermVectors getTermVectorsFromQuery(String query, SearchOptions options, String field) throws IOException {
        TextIndexer indexer = textIndexerFactory.getDefaultInstance();
        try {
            Query q= indexer.extractFullFacetQuery(query, options, field);
            return indexer.getTermVectors(entityClass, field, (Filter)null, q);
        } catch (ParseException e) {
            throw new IOException("error parsing lucene query '" + query + "'", e);
        }catch(Exception e){
            throw new IOException("error getting term vectors ", e);
        }
    }
}
