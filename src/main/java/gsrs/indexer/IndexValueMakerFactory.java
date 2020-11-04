package gsrs.indexer;

import ix.core.search.text.CombinedIndexValueMaker;
import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.ReflectingIndexValueMaker;
import ix.core.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IndexValueMakerFactory {


    @Autowired
    private List<IndexValueMaker> indexValueMakers;
    private ReflectingIndexValueMaker reflectingIndexValueMaker = new ReflectingIndexValueMaker();

    public IndexValueMaker createIndexValueMakerFor(Object obj){
        return createIndexValueMakerFor( EntityUtils.EntityWrapper.of(obj));
    }
    public  IndexValueMaker createIndexValueMakerFor(EntityUtils.EntityWrapper<?> ew){

        Class<?> clazz = ew.getEntityClass();
        List<IndexValueMaker> acceptedList = new ArrayList<>();
        //always add reflecting indexvaluemaker
        acceptedList.add(reflectingIndexValueMaker);
        for(IndexValueMaker indexValueMaker : indexValueMakers){
            Class<?> c = indexValueMaker.getIndexedEntityClass();
            if(c.isAssignableFrom(clazz)){
                acceptedList.add(indexValueMaker);
            }
        }
        return new CombinedIndexValueMaker(clazz, acceptedList);

    }
}
