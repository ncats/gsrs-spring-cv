package ix.core.search;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Implementation that doesn't do anything but is marked as a component so
 * spring can find something to inject.
 */
@Component
public class NoOpIndexValueMaker implements IndexValueMaker<Object> {

    @Override
    public Class<Object> getIndexedEntityClass() {
        return Object.class;
    }

    @Override
    public void createIndexableValues(Object t, Consumer<IndexableValue> consumer) {

    }
}
