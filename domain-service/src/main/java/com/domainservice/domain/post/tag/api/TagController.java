package com.domainservice.domain.post.tag.api;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.post.tag.model.dto.response.TagResponse;
import com.domainservice.domain.post.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public BaseResponse<List<TagResponse>> getTags() {

        List<TagResponse> tags = tagService.getTags();

        return new BaseResponse<>(tags, "태그 목록을 조회합니다.");

    }
}