package com.bookend.bookservice.service;

import com.bookend.bookservice.kafka.Producer;
import com.bookend.bookservice.model.Author;
import com.bookend.bookservice.model.Book;
import com.bookend.bookservice.model.KafkaMessage;
import com.bookend.bookservice.repository.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    private static final String BOOK_TOPIC = "adding-book";
    private static final String DELETE_TOPIC = "deleting-book";

    private BookRepository bookRepository;
    @Autowired
    public void setBookRepository(BookRepository bookRepository){
        this.bookRepository=bookRepository;
    }

    private Producer producer;
    @Autowired
    public void setProducer(Producer producer){this.producer=producer;}

    @Override
    public Book getById(String id) {
        return bookRepository.findBookById(id);
    }




    @Override
    public Book saveOrUpdate(Book book) {
        Map<String, String> message= new HashMap<String, String>();
        List<Book> books = bookRepository.findBookByBookName(book.getBookName());
        if(books.size()!=0){
           List<Book> filteredbyAuthor = books.stream()
                   .filter(b -> b.getAuthorid().equals(book.getAuthorid()))
                   .collect(Collectors.toList());
           if(filteredbyAuthor.size()!=0){
               List<Book> filteredbyDesc = filteredbyAuthor.stream()
                       .filter(b -> b.getDescription().equals(book.getDescription()))
                       .collect(Collectors.toList());
               if(filteredbyDesc.size()!=0){
                   return null;
               }
           }


        }

        Book savedBook = bookRepository.save(book);
        message.put("author",book.getAuthorid());
        message.put("book",savedBook.getId());
        KafkaMessage kafkaMessage = new KafkaMessage(BOOK_TOPIC,message);
        producer.publishBook(kafkaMessage);
        return savedBook;
    }

    @Override
    public Book update(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public List<Book> getBooksofShelf(Long shelfID, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+accessToken);
        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<String[]> responseEntity =restTemplate.exchange("http://localhost:8083/api/shelf/{shelfid}", HttpMethod.GET, entity, String[].class,shelfID);
        List<String> bookIDs = Arrays.asList(responseEntity.getBody());
        List<Book> books=new ArrayList<>(bookIDs.size());
        for(int i=0;i<bookIDs.size();i++){
            Book book = bookRepository.findBookById(bookIDs.get(i));
            books.add(book);
        }
        return books;
    }

    @Override
    public List<Book> getAll() {
        return bookRepository.findAll(Sort.by(Sort.Direction.ASC,"bookName"));
    }

    @Override
    public List<Book> findByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }

    @Override
    public List<Book> search(String title,String genre, boolean rateSort,String accessToken) {
        List<Book> books = new ArrayList<>();
        if(rateSort){
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer "+accessToken);
            HttpEntity<String> entity = new HttpEntity<>("body", headers);
            ResponseEntity<String[]> responseEntity =restTemplate.exchange("http://localhost:8081/api/rate/sort/", HttpMethod.GET, entity, String[].class);
            List<String> bookIDs = Arrays.asList(responseEntity.getBody());
            books = bookIDs.stream().map(b -> bookRepository.findBookById(b)).collect(Collectors.toList());
        }
        else {
            books = bookRepository.findAll();
        }

        if(title!=null){
            books = books.stream()
                    .filter(book -> book.getBookName().toLowerCase()
                            .contains(title.toLowerCase()))
                    .collect(Collectors.toList());
            if(books.size()==0){
                return null;
            }
        }
        if(genre!=null){
            books = books.stream()
                    .filter(book -> book.getGenre().getGenre().toLowerCase()
                            .contains(genre.toLowerCase()))
                    .collect(Collectors.toList());
            if(books.size()==0){
                return null;
            }
        }

        return books;
    }

    @Override
    public void delete(String bookId) {
        KafkaMessage kafkaMessage = new KafkaMessage(DELETE_TOPIC,bookId);
        producer.deleteBook(kafkaMessage);
        bookRepository.delete(getById(bookId));
    }

    @Override
    public List<Book> findBookByVerifiedIsFalse() {
        return bookRepository.findBookByVerifiedIsFalse();
    }
}
