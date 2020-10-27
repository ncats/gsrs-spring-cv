package gsrs.controller;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import ix.utils.pojopatch.PojoDiff;
import ix.utils.pojopatch.PojoPatch;
import lombok.Data;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public abstract class GsrsEntityController<T, I> {

    @Autowired
    private GsrsControllerConfiguration gsrsControllerConfiguration;

    protected abstract T fromNewJson(JsonNode json) throws IOException;

    protected abstract List<T> fromNewJsonList(JsonNode list) throws IOException;
    protected abstract T fromUpdatedJson(JsonNode json) throws IOException;

    protected abstract List<T> fromUpdatedJsonList(JsonNode list) throws IOException;

    protected abstract JsonNode toJson(T t) throws IOException;

    protected abstract T create(T t);

    protected abstract long count();

    protected abstract Optional<T> get(I id);

    protected abstract I parseIdFromString(String idAsString);

    protected abstract Optional<T> flexLookup(String someKindOfId);

    protected abstract Optional<I> flexLookupIdOnly(String someKindOfId);

    protected abstract Class<T> getEntityClass();

    protected abstract Page page(long offset, long numOfRecords, Sort sort);

    protected abstract void delete(I id);

    protected abstract I getIdFrom(T entity);

    protected abstract T update(T t);

    @GsrsRestApiPostMapping()
    public ResponseEntity<Object> createEntity(@RequestBody JsonNode newEntityJson) throws IOException {
        T newEntity = fromNewJson(newEntityJson);
        //TODO add validation in later sprint
        return new ResponseEntity<>(create(newEntity), HttpStatus.CREATED);

    }

    @GsrsRestApiPutMapping()
    public ResponseEntity<Object> updateEntity(@RequestBody JsonNode updatedEntityJson, @RequestParam Map<String, String> queryParameters) throws Exception {
        T updatedEntity = fromUpdatedJson(updatedEntityJson);
        //updatedEntity should have the same id
        I id = getIdFrom(updatedEntity);
        Optional<T> opt = get(id);
        if(!opt.isPresent()){
            return gsrsControllerConfiguration.handleBadRequest(queryParameters);
        }
        //TODO add validation in later sprint
        T oldEntity = opt.get();
        PojoPatch<T> patch = PojoDiff.getDiff(oldEntity, updatedEntity);
        System.out.println("changes = " + patch.getChanges());
        patch.apply(oldEntity);
        System.out.println("updated entity = " + oldEntity);
        //match 200 status of old GSRS
        return new ResponseEntity<>(update(oldEntity), HttpStatus.OK);
    }

    @GsrsRestApiGetMapping("/@count")
    public long getCount(){
        return count();
    }

    @GsrsRestApiGetMapping("")
    public ResponseEntity<Object> page(@RequestParam(value = "top", defaultValue = "16") long top,
                     @RequestParam(value = "skip", defaultValue = "0") long skip,
                     @RequestParam(value = "order", required = false) String order,
                     @RequestParam Map<String, String> queryParameters){


        Page<T> page = page(skip, top,parseSortFromOrderParam(order));

        return new ResponseEntity<>(new PagedResult(page), HttpStatus.OK);
    }

    private Sort parseSortFromOrderParam(String order){
        //match Gsrs Play API
        if(order ==null || order.trim().isEmpty()){
            return Sort.sort(getEntityClass());
        }
        char firstChar = order.charAt(0);
        if('$'==firstChar){
            return Sort.by(Sort.Direction.DESC, order.substring(1));
        }
        if('^'==firstChar){
            return Sort.by(Sort.Direction.ASC, order.substring(1));
        }
        return Sort.by(Sort.Direction.ASC, order);
    }
    @Data
    public static class PagedResult<T>{
        private long total, count,skip, top;
        private List<T> content;

        public PagedResult(Page<T> page){
            this.total = page.getTotalElements();
            this.count= page.getNumberOfElements();
            this.skip = page.getSize() * page.getNumber();
            this.top = page.getSize();
            content = page.toList();
        }
    }

    @GsrsRestApiGetMapping(value = {"/{id:$ID}", "({id:$ID})"})
    public ResponseEntity<Object> getById(@PathVariable String id, @RequestParam Map<String, String> queryParameters){
        Optional<T> obj = get(parseIdFromString(id));
        if(obj.isPresent()){
            return new ResponseEntity<>(obj.get(), HttpStatus.OK);
        }
        return gsrsControllerConfiguration.handleNotFound(queryParameters);
    }
    @GsrsRestApiGetMapping(value = {"/{id:$NOT_ID}", "({id:$NOT_ID})"} )
    public ResponseEntity<Object> getByFlexId(@PathVariable String id, @RequestParam Map<String, String> queryParameters){
        Optional<T> obj = flexLookup(id);
        if(obj.isPresent()){
            return new ResponseEntity<>(obj.get(), HttpStatus.OK);
        }
        return gsrsControllerConfiguration.handleNotFound(queryParameters);
    }
    @GsrsRestApiDeleteMapping(value = {"/{id:$ID}", "({id:$ID})"})
    public ResponseEntity<Object> deleteById(@PathVariable String id, @RequestParam Map<String, String> queryParameters){
        I parsedId = parseIdFromString(id);
        return deleteEntity(parsedId);
    }

    private ResponseEntity<Object> deleteEntity(I parsedId) {
        delete(parsedId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GsrsRestApiDeleteMapping(value = {"/{id:$NOT_ID}", "({id:$NOT_ID})"} )
    public ResponseEntity<Object> deleteByFlexId(@PathVariable String id, @RequestParam Map<String, String> queryParameters){
        Optional<I> idOptional = flexLookupIdOnly(id);
        if(idOptional.isPresent()){

            return deleteEntity(idOptional.get());
        }
        return gsrsControllerConfiguration.handleNotFound(queryParameters);
    }

    @Data
    public static class LuceneSearchRequestField{
        private String field;
        private String matches;

    }
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "type",
            defaultImpl = LuceneSearchRequestOr.class
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = LuceneSearchRequestOr.class, name = "or"),
            @JsonSubTypes.Type(value = LuceneSearchRequestAnd.class, name = "and"),
            @JsonSubTypes.Type(value = LuceneSearchRequestLeaf.class, name = "matches")
    })
    public interface LuceneSearchRequestOp {



        PredicateFinalStep doIt(SearchPredicateFactory spf);
    }
    @Data
    public class LuceneSearchRequest{
        private LuceneSearchRequestOp op;

        public PredicateFinalStep doIt(SearchPredicateFactory spf){
            return op.doIt(spf);
        }
    }
    @Data
    public static class LuceneSearchRequestOr implements LuceneSearchRequestOp {
        private List<LuceneSearchRequestOp> opList;

        @Override
        public PredicateFinalStep doIt(SearchPredicateFactory spf) {
            BooleanPredicateClausesStep<?> step =spf.bool();
            for(LuceneSearchRequestOp f : opList) {
                step = step.should( f.doIt(spf));
            }
            return step;
        }
    }
    @Data
    public static class LuceneSearchRequestLeaf implements LuceneSearchRequestOp {
        private LuceneSearchRequestField value;

        @Override
        public PredicateFinalStep doIt(SearchPredicateFactory spf) {

            return spf.simpleQueryString().field(value.getField())
                    .matching(value.getMatches());


        }
    }

    @Data
    public static class LuceneSearchRequestAnd implements LuceneSearchRequestOp {
        private List<LuceneSearchRequestOp> opList;

        @Override
        public PredicateFinalStep doIt(SearchPredicateFactory spf) {
            BooleanPredicateClausesStep<?> step =spf.bool();
            for(LuceneSearchRequestOp f : opList) {
                step = step.must(f.doIt(spf));
            }
            return step;
        }
    }
    /*
     SEARCH_OPERATION(new Operation("search",
                Argument.of(null, String.class, "query"),
                Argument.of(0, int.class, "top"),
                Argument.of(0, int.class, "skip"),
                Argument.of(0, int.class, "fdim"))),
     */
    @GsrsRestApiGetMapping(value = "/search", apiVersions = 1)
    public ResponseEntity<Object> searchV1(@RequestParam("q") String query,
                                           @RequestParam("top") Optional<Integer> top,
                                           @RequestParam("skip") Optional<Integer> skip,
                                           @RequestParam("fdim") Optional<Integer> fdim,
                                           @RequestParam Map<String, String> queryParameters){

        List<T> hits = indexSearchV1(query, top, skip, fdim);
        //even if list is empty we want to return an empty list not a 404
        return new ResponseEntity<>(hits, HttpStatus.OK);
    }

    protected abstract List<T> indexSearchV1(String query, Optional<Integer> top, Optional<Integer> skip, Optional<Integer> fdim);
    protected abstract List<T> indexSearchV2(LuceneSearchRequestOp op, Optional<Integer> top, Optional<Integer> skip, Optional<Integer> fdim);

    @GsrsRestApiPostMapping(value = "/search", apiVersions = 2)
    public ResponseEntity<Object> searchV2(@RequestBody LuceneSearchRequestOp op,
                                           @RequestParam("top") Optional<Integer> top,
                                           @RequestParam("skip") Optional<Integer> skip,
                                           @RequestParam("fdim") Optional<Integer> fdim,
                                           @RequestParam Map<String, String> queryParameters){

        List<T> hits = indexSearchV2(op, top, skip, fdim);

        //even if list is empty we want to return an empty list not a 404
        return new ResponseEntity<>(hits, HttpStatus.OK);
    }

    /*
      CREATE_OPERATION(new Operation("create")),
        VALIDATE_OPERATION(new Operation("validate")),
        //TODO: implement
        RESOLVE_OPERATION(new Operation("resolve",
                Argument.of(null, String.class, "id"))),
        UPDATE_ENTITY_OPERATION(new Operation("updateEntity")),
        PATCH_OPERATION(new Operation("patch",
                Argument.of(null, Id.class, "id"))),
        COUNT_OPERATION(new Operation("count")),
        STREAM_OPERATION(new Operation("stream",
                Argument.of(null, String.class, "field"),
                Argument.of(0, int.class , "top"),
                Argument.of(0, int.class , "skip"))),
        SEARCH_OPERATION(new Operation("search",
                Argument.of(null, String.class, "query"),
                Argument.of(0, int.class, "top"),
                Argument.of(0, int.class, "skip"),
                Argument.of(0, int.class, "fdim"))),
        GET_OPERATION(new Operation("get",
                Argument.of(null, Id.class, "id"),
                Argument.of(null, String.class, "expand"))),
        DELETE_OPERATION(new Operation("delete",
                Argument.of(null, Id.class, "id"))),
        DOC_OPERATION(new Operation("doc",
                Argument.of(null, Id.class, "id"))),
        EDITS_OPERATION(new Operation("edits",
                Argument.of(null, Id.class, "id"))),
        APPROVE_OPERATION(new Operation("approve",
                Argument.of(null, Id.class, "id"))),
        UPDATE_OPERATION(new Operation("update",
                Argument.of(null, Id.class, "id"),
                Argument.of(null, String.class, "field")

                )),
        FIELD_OPERATION(new Operation("field",
                Argument.of(null, Id.class, "id"),
                Argument.of(null, String.class, "field"))),
        PAGE_OPERATION(new Operation("page",
                Argument.of(10, int.class, "top"),
                Argument.of(0, int.class, "skip"),
                Argument.of(null, String.class, "filter"))),
        STRUCTURE_SEARCH_OPERATION(new Operation("structureSearch",
                Argument.of(null, String.class, "query"),
                Argument.of("substructure", String.class, "type"),
                Argument.of(.8, double.class, "cutoff"),
                Argument.of(0, int.class, "top"),
                Argument.of(0, int.class, "skip"),
                Argument.of(0, int.class, "fdim"),
                Argument.of("", String.class, "field"))),
        SEQUENCE_SEARCH_OPERATION(new Operation("sequenceSearch",
                Argument.of(null, String.class, "query"),
                Argument.of(CutoffType.SUB, CutoffType.class, "cutofftype"),
                Argument.of(.8, double.class, "cutoff"),
                Argument.of(0, int.class, "top"),
                Argument.of(0, int.class, "skip"),
                Argument.of(0, int.class, "fdim"),
                Argument.of("", String.class, "field"),
                Argument.of("", String.class, "seqType"))),


		HIERARCHY_OPERATION(new Operation("hierarchy",
				Argument.of(null, Id.class, "id"))),

		EXPORT_FORMATS_OPERATION(new Operation("exportFormats")),
		EXPORT_OPTIONS_OPERATION(new Operation("exportOptions",
				Argument.of(null, String.class, "etagId"),
				Argument.of(true, boolean.class, "publicOnly"))),
		EXPORT_OPERATION(new Operation("createExport",
				Argument.of(null, String.class, "etagId"),
				Argument.of(null, String.class, "format"),
				Argument.of(true, boolean.class, "publicOnly"))),
		;

     */
}
