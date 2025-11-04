package com.common.asset.image.infrastructure;

import org.springframework.data.repository.Repository;

import com.common.asset.image.domain.entity.Image;
import com.common.asset.image.domain.repository.ImageRepository;

@org.springframework.stereotype.Repository
public interface ImageJpaRepository extends Repository<Image, String>, ImageRepository {

}
