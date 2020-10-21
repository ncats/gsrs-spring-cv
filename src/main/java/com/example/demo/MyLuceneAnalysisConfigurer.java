package com.example.demo;

import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;
import org.springframework.stereotype.Component;

@Component
public class MyLuceneAnalysisConfigurer implements LuceneAnalysisConfigurer {

    @Override
    public void configure(LuceneAnalysisConfigurationContext context) {

        System.out.println("configure my analysis!!");
        context.analyzer(GsrsAnalyzers.DEFAULT_NAME ).custom()
                            .tokenizer(StandardTokenizerFactory.class)
                            .tokenFilter(NGramFilterFactory.class)
                                    .param("maxGramSize", "5")
                                    .param("minGramSize", "3");


    }
}