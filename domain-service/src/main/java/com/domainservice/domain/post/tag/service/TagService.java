package com.domainservice.domain.post.tag.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.domainservice.domain.post.tag.model.dto.response.TagResponse;
import com.domainservice.domain.post.tag.model.entity.Tag;
import com.domainservice.domain.post.tag.repository.TagRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    /**
     * 전체 태그 조회
     */
    public List<TagResponse> getTags() {
        return tagRepository.findAll()
                .stream()
                .map(TagResponse::from)
                .toList();
    }

    /**
     * 전체 태그 id 조회
     */
    public List<String> getAllTagIds() {
        return tagRepository.findAll()
                .stream()
                .map(Tag::getId)
                .toList();
    }

}
