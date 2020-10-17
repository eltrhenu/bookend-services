package com.bookend.shelfservice.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "book")
public class ShelfsBook {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
  //  @SequenceGenerator(name= "my_sequence", schema = "account", allocationSize = 30)
    @Column(name = "id")
    private Long id;
    @Column(name = "bookId")
    private String bookID;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Shelf shelf;

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }

    public Shelf getShelf() {
        return shelf;
    }

    public void setShelf(Shelf shelf) {
        this.shelf = shelf;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public ShelfsBook() {
    }

    public ShelfsBook(String bookID, Shelf shelf) {
        this.bookID = bookID;
        this.shelf = shelf;
    }
}
