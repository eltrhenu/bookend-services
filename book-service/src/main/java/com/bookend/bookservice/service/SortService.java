package com.bookend.bookservice.service;

import com.bookend.bookservice.model.Book;
import com.bookend.bookservice.model.SortedLists;

public interface SortService {

    void add(Book book);


    SortedLists sort(String type);
    SortedLists findOne();
    SortedLists remove(Book book);
}
