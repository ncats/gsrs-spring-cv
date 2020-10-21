package gsrs.controller;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


public abstract class GsrsEntityController<T, I> {

    abstract T fromJson(JsonNode json) throws IOException;

    abstract List<T> fromJsonList(JsonNode list) throws IOException;

    abstract JsonNode toJson(T t) throws IOException;

    abstract T create(T t);

    abstract long count();

    abstract Optional<T> get(I id);

    abstract I parseIdFromString(String idAsString);

    abstract Optional<T> flexLookup(String someKindOfId);

    @GsrsRestApiPostMapping
    public ResponseEntity<Object> createEntity(@RequestBody JsonNode newEntityJson) throws IOException {
        T newEntity = fromJson(newEntityJson);
        //TODO add validation in later sprint
        return new ResponseEntity<>(create(newEntity), HttpStatus.CREATED);

    }

    @GsrsRestApiGetMapping("/@count")
    public long getCount(){
        return count();
    }

    @GsrsRestApiGetMapping(value = {"/{id:$ID}", "({id:$ID})"})
    public ResponseEntity<Object> getById(@PathVariable String id){
        Optional<T> obj = get(parseIdFromString(id));
        System.out.println("found obj =" + obj);
        if(obj.isPresent()){
            return new ResponseEntity<>(obj.get(), HttpStatus.OK);
        }
        //TODO handle error_code param to make it 500 ?
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @GsrsRestApiGetMapping(value = {"/{id:$NOT_ID}", "({id:$NOT_ID})"} )
    public ResponseEntity<Object> getByFlexId(@PathVariable String id){
        Optional<T> obj = flexLookup(id);
        System.out.println("found obj =" + obj);
        if(obj.isPresent()){
            return new ResponseEntity<>(obj.get(), HttpStatus.OK);
        }
        //TODO handle error_code param to make it 500 ?
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
