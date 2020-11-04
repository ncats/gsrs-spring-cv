package gsrs;

import ix.core.CombinedEntityProcessor;
import ix.core.EntityProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EntityProcessorFactory {
    private static Object MAP_VALUE_TOKEN = new Object();
    @Autowired(required = false)
    private List<EntityProcessor> entityProcessors;

    private Map<Class, List<EntityProcessor>> processorMapByClass = new ConcurrentHashMap<>();
    private Map<Class, EntityProcessor> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init(){
        //entityProcessors field may be null if there's no EntityProcessor to inject
        if(entityProcessors !=null) {
            for (EntityProcessor ep : entityProcessors) {
                Class entityClass = ep.getEntityClass();
                if (entityClass == null) {
                    continue;
                }
                processorMapByClass.computeIfAbsent(entityClass, k -> new ArrayList<>()).add(ep);
            }
        }
    }

    public EntityProcessor getCombinedEntityProcessorFor(Object o){
        Class entityClass = o.getClass();
        return cache.computeIfAbsent(entityClass, k-> {
            Map<EntityProcessor, Object> list = new IdentityHashMap<>();

            for (Map.Entry<Class, List<EntityProcessor>> entry : processorMapByClass.entrySet()) {
                if (entry.getKey().isAssignableFrom(k)) {
                    for (EntityProcessor ep : entry.getValue()) {
                        list.put(ep, MAP_VALUE_TOKEN);
                    }
                }
            }
            Set<EntityProcessor> processors = list.keySet();

            return new CombinedEntityProcessor(k, processors);
        });
    }
}
