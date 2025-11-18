package com.domainservice.domain.asset.image.domain.service;

import com.domainservice.domain.asset.image.domain.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AssetService<T> {
    Image uploadProfileImage(MultipartFile file);
    List<Image> uploadProfileImageList(List<MultipartFile> files);

    Image updateProfileImage(MultipartFile profileImage, String imageId);

    void deleteProfileImageById(String imageId);
    void deleteProfileImageByS3Url(String s3Url);

    T getImage(String id);
}
