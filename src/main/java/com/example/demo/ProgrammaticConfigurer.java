package com.example.demo;

import ix.core.search.text.IndexValueMaker;
import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmMappingConfigurationContext;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmSearchMappingConfigurer;
import org.hibernate.search.mapper.pojo.bridge.TypeBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.TypeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;
import org.hibernate.search.mapper.pojo.extractor.builtin.BuiltinContainerExtractors;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.ProgrammaticMappingConfigurationContext;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.TypeMappingStep;
import org.hibernate.search.mapper.pojo.model.dependency.PojoTypeIndexingDependencyConfigurationContext;

import java.lang.reflect.Field;

public class ProgrammaticConfigurer implements HibernateOrmSearchMappingConfigurer {
    @Override
    public void configure(HibernateOrmMappingConfigurationContext context) {
        System.out.println(" IN PROGRAMMATIC CONFIGURER");
        ProgrammaticMappingConfigurationContext mapping = context.programmaticMapping();
        TypeMappingStep bookMapping = mapping.type( Book.class );

        bookMapping.indexed();
        bookMapping.property("keywords").genericField().extractor(BuiltinContainerExtractors.COLLECTION);
    }

    private <T> void adaptIndexValueMaker(ProgrammaticMappingConfigurationContext mapping, Class<T> type, IndexValueMaker<T> indexValueMaker){
        TypeMappingStep typeMappingStep= mapping.type(type);

        typeMappingStep.indexed();

        for(Field f : type.getDeclaredFields()){
            typeMappingStep.property(f.getName());
        }
    }

    private static class IndexValueMakerTypeBridge<T> implements TypeBinder {

        private Class<T> type;
        private IndexValueMaker<T> indexValueMaker;

        IndexValueMakerTypeBridge(Class<T> type, IndexValueMaker<T> indexValueMaker){
            this.type = type;
            this.indexValueMaker = indexValueMaker;
        }
        @Override
        public void bind(TypeBindingContext context) {
            PojoTypeIndexingDependencyConfigurationContext dependencies = context.dependencies();
            for(Field f : type.getDeclaredFields()){
                dependencies = dependencies.use(f.getName());
            }
            for(Field f : type.getFields()){
                dependencies = dependencies.use(f.getName());
            }
//            context.indexSchemaElement()
//                    .field(indexValueMaker.)
        }
    }
}
