package com.domainservice.domain.post.tag.service;

import com.domainservice.domain.post.tag.model.dto.response.TagResponse;
import com.domainservice.domain.post.tag.model.entity.Tag;
import com.domainservice.domain.post.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     * 태그 생성
     */
    @Transactional
    public Tag createTag(String name) {
        log.info("태그 생성: {}", name);

        Tag tag = Tag.builder()
                .name(name)
                .build();

        return tagRepository.save(tag);
    }

    /**
     * 브랜드 존재 여부 확인
     */
    public boolean existsByName(String name) {
        return tagRepository.existsByName(name);
    }

    /**
     * 태그가 없으면 생성 (초기화용)
     */
    @Transactional
    public Tag createIfNotExists(String name) {
        if (!existsByName(name)) {
            return createTag(name);
        }
        log.info("태그 이미 존재: {}", name);
        return tagRepository.findByName(name).orElseThrow();
    }

    /**
     * 전체 태그 수 조회
     */
    @Transactional(readOnly = true)
    public long count() {
        return tagRepository.count();
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
