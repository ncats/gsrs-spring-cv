package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        System.out.println("RUNNING");

        BookSearchService bookSearchService = new BookSearchService(entityManagerFactory.createEntityManager());
        bookSearchService.initializeHibernateSearch();

        Author culpepper = new Author();
        culpepper.setLastName("Culpepper");
        culpepper.setFirstName("Nicholas");

        Book englishPhys = new Book();
        englishPhys.addAuthor(culpepper);
        englishPhys.setTitle("The English Physician");
        englishPhys.setYear(1681);

        englishPhys.getKeywords().add("medical");
        englishPhys.getKeywords().add("science");
        englishPhys.getKeywords().add("antiquarian");

        Book completePhysic = new Book();
        completePhysic.addAuthor(culpepper);
        completePhysic.setTitle("The Compleat Physick");
        completePhysic.setYear(1653);

        completePhysic.getKeywords().add("medical");
        completePhysic.getKeywords().add("science");
        completePhysic.getKeywords().add("antiquarian");

        culpepper.getBooks().add(englishPhys);
        culpepper.getBooks().add(completePhysic);

        authorRepository.saveAndFlush(culpepper);


        Author darwin = new Author();
        darwin.setFirstName("Charles");
        darwin.setLastName("Darwin");

        Book originOfSpecies = new Book();
        originOfSpecies.setYear(1869);
        originOfSpecies.setTitle("On the Origin of the Species");
        originOfSpecies.addAuthor(darwin);

        originOfSpecies.getKeywords().add("science");
        originOfSpecies.getKeywords().add("antiquarian");

        darwin.getBooks().add(originOfSpecies);
        authorRepository.saveAndFlush(darwin);


        List<Book> books = bookSearchService.searchByAuthor("Culpepper");

        System.out.println(books);

        System.out.println("search for science books = " + bookSearchService.searchByKeyword("science"));

    }
}
