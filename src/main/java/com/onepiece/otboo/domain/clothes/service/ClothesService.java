package com.onepiece.otboo.domain.clothes.service;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesUpdateRequest;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;

public class ClothesService {

    public ClothesDto findAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    public ClothesDto create(ClothesCreateRequest request, MultipartFile image) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    public void deleteById(UUID clothesId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }

    public ClothesDto findById(UUID clothesId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    public ClothesDto updateById(UUID clothesId,
            ClothesUpdateRequest request, MultipartFile image) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateById'");
    }

    public Object findByType(ClothesType type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByType'");
    }
}
