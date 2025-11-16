package com.domainservice.domain.post.post.model.dto;

import java.util.List;

public record UserView(
        String name,
        String nickname,
        String phoneNumber,
        String zipCode,
        String state,
        String street,
        String city,
        String details,
        String email,
        List<String> roles,
        String profileImageUrl,
        String imageId
){
}
