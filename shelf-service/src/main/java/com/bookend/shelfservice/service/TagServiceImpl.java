package com.bookend.shelfservice.service;

import com.bookend.shelfservice.model.Tag;
import com.bookend.shelfservice.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {
    private TagRepository tagRepository;
    @Autowired
    public void setTagRepository(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public Tag saveAndUpdate(String tag) {
        if(tagRepository.findByTag(tag)==null){
            return null;
        }
        return tagRepository.save(new Tag(tag));
    }

    @Override
    public List<Tag> allTag() {
        return tagRepository.findAll();
    }

    @Override
    public Tag findByID(String id) {
        return tagRepository.findTagById(id);
    }

    @Override
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }
}
