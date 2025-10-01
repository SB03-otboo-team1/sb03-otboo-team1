package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDto;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeOptions;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.mapper.ClothesAttributeMapper;
import com.onepiece.otboo.domain.clothes.mapper.ClothesMapper;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeOptionsRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.storage.FileStorage;
import java.io.IOException;
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
    private final ClothesAttributeMapper clothesAttributeMapper;
    private final FileStorage fileStorage;

    @Value("${aws.storage.prefix.clothes}")
    private String CLOTHES_PREFIX;

  @Override
  public CursorPageResponseDto<ClothesDto> getClothes(UUID ownerId, String cursor, UUID idAfter, int limit, String sortBy, SortDirection sortDirection, ClothesType typeEqual) {

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
    List<ClothesDto> data = clothes.stream().map(c -> {
        // attribute 조회
        List<ClothesAttributes> attributes = attributeRepository.findByClothesId(c.getId());

        // def + options 붙여서 attributeWithDefDto로 변환
        List<ClothesAttributeWithDefDto> attributeWithDefDtos =
            attributes.stream().map(a -> {
                ClothesAttributeDefs def = a.getDefinition();
                List<ClothesAttributeOptions> options = optionsRepository.findByDefinitionId(def.getId());
                String value = a.getOptionValue();
                return clothesAttributeMapper.toAttributeWithDefDto(def, a, options, value);
            }).toList();

        // ClothesDto 생성
        return clothesMapper.toDto(c, attributeWithDefDtos, fileStorage);
    }).toList();

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
    public ClothesDto createClothes(ClothesCreateRequest request, MultipartFile imageFile)
      throws IOException {

      UUID ownerId = request.ownerId();
      User owner = userRepository.findById(ownerId).orElseThrow(UserNotFoundException::new);
      String name = request.name();
      ClothesType type = request.type();
      String imageUrl = null;
      if (imageFile != null) {
          imageUrl = fileStorage.uploadFile(CLOTHES_PREFIX, imageFile);
      }

      // Clothes 엔티티 저장
      Clothes clothes = Clothes.builder()
          .owner(owner)
          .name(name)
          .type(type)
          .imageUrl(imageUrl)
          .build();

      clothesRepository.save(clothes);

      // Attributes 생성해서 저장
      List<ClothesAttributeDto> attrDto = request.attributes();
      List<ClothesAttributes> attributes =
          attrDto.stream().map(
              a -> {
                  ClothesAttributeDefs def = defRepository.findById(a.definitionId()).orElseThrow(IllegalArgumentException::new);
                  String value = a.value();
                  ClothesAttributes result =
                      ClothesAttributes.builder()
                      .clothes(clothes)
                      .definition(def)
                      .optionValue(value)
                      .build();
                  attributeRepository.save(result);
                  return result;
              }
          ).toList();

      // ClothesDto 생성용 ClothesAttributeWithDefDto 생성
      List<ClothesAttributeWithDefDto> clothesAttributeWithDefDto =
          attributes.stream().map(a -> {
              ClothesAttributeDefs def = a.getDefinition();
              List<ClothesAttributeOptions> options = optionsRepository.findByDefinitionId(def.getId());
              String value = a.getOptionValue();
              return clothesAttributeMapper.toAttributeWithDefDto(def, a, options, value);
          }).toList();

      // ClothesDto 반환
      return clothesMapper.toDto(clothes, clothesAttributeWithDefDto, fileStorage);
  }
}
