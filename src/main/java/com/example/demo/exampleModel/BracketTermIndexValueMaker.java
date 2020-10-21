package com.example.demo.exampleModel;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;


import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by VenkataSaiRa.Chavali on 6/23/2017.
 */
public class BracketTermIndexValueMaker implements IndexValueMaker<SubstanceDemo> {
    @Override
    public void createIndexableValues(SubstanceDemo substance, Consumer<IndexableValue> consumer) {
        
        Pattern p = Pattern.compile("(?:[ \\]])\\[([ \\-A-Za-z0-9]+)\\]");
        if (substance.getNames() != null) {
            substance.getNames().stream()
                    .filter(a -> a.getName().trim().endsWith("]"))
                    .forEach(n -> {
                        //ASPIRIN1,23[asguyasgda]asgduytqwqd [INN][USAN]
                        Matcher m = p.matcher(n.getName());
                        while (m.find()) {
                            String loc = m.group(1);
                            consumer.accept(IndexableValue.simpleFacetStringValue("GInAS Tag",loc));
        
                        }
                    });
        }
        }
}
