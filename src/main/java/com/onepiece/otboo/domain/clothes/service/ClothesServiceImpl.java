package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.mapper.ClothesMapper;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeOptionsRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.storage.FileStorage;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothesServiceImpl implements ClothesService {

    private final ClothesRepository clothesRepository;
    private final UserRepository userRepository;
    private final ClothesAttributeDefRepository defRepository;
    private final ClothesAttributeOptionsRepository optionsRepository;
    private final ClothesAttributeRepository attributeRepository;
    private final ClothesMapper clothesMapper;
    private final FileStorage fileStorage;

    @Value("${aws.storage.prefix.clothes}")
    private String CLOTHES_PREFIX;

  @Override
  public CursorPageResponseDto<ClothesDto> getClothes(UUID ownerId, String cursor, UUID idAfter, int limit, String sortBy, String sortDirection, ClothesType typeEqual) {

    List<Clothes> clothes =
        clothesRepository.getClothesWithCursor(ownerId, cursor, idAfter, limit, sortBy, sortDirection, typeEqual);

      if (clothes == null) {
          clothes = Collections.emptyList();
      }

    boolean hasNext = clothes.size() > limit;

      String nextCursor = null;
      UUID nextIdAfter = null;

      if (hasNext) {
          clothes = clothes.subList(0, limit);
          Clothes lastClothes = clothes.get(limit - 1);
          switch (sortBy) {
              case "createdAt" -> {
                  nextCursor = lastClothes.getCreatedAt().toString();
              }
              case "name" -> {
                  nextCursor = lastClothes.getName();
              }
          }
          nextIdAfter = lastClothes.getId();
      }

    Long totalCount = clothesRepository.countClothes(ownerId, typeEqual);

    List<ClothesDto> data = clothesMapper.toDto(clothes, fileStorage, defRepository, optionsRepository, attributeRepository);

    log.info("옷 목록 조회 완료 - ownerId: {}, limit: {}, 전체 데이터 개수: {}", ownerId, limit, totalCount);

    return new CursorPageResponseDto<>(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        sortBy,
        sortDirection
    );
  }

  @Override
    public ClothesDto createClothes(ClothesCreateRequest request, MultipartFile imageFile) {

      UUID ownerId = request.ownerId();
      User owner = userRepository.findById(ownerId).orElseThrow(UserNotFoundException::new);
      String name = request.name();
      ClothesType type = request.type();

      Clothes clothes = Clothes.builder()
          .owner(owner)
          .name(name)
          .type(type)
          .build();

      clothesRepository.save(clothes);

      return clothesMapper.toDto(clothes, fileStorage, defRepository, optionsRepository, attributeRepository);
  }
}
