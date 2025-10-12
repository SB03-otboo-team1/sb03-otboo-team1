package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDto;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesUpdateRequest;
import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeOptions;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.exception.ClothesAttributeDefNotFoundException;
import com.onepiece.otboo.domain.clothes.exception.ClothesNotFoundException;
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
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.storage.FileStorage;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
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
  @Transactional(readOnly = true)
  public CursorPageResponseDto<ClothesDto> getClothesWithCursor(UUID ownerId, String cursor, UUID idAfter, int limit, SortBy sortBy, SortDirection sortDirection, ClothesType typeEqual) {

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
              case CREATED_AT ->
                  nextCursor = lastClothes.getCreatedAt().toString();
              case NAME ->
                  nextCursor = lastClothes.getName();
          }
          nextIdAfter = lastClothes.getId();
      }

    Long totalCount = clothesRepository.countClothes(ownerId, typeEqual);

    List<UUID> clothesIds = clothes.stream().map(Clothes::getId).toList();

    // 등록된 의상의 attribute 조회
    List<ClothesAttributes> attributes = attributeRepository.findByClothesIdIn(clothesIds);
    Map<UUID, List<ClothesAttributes>> attributesByClothesId =
        attributes.stream().collect(Collectors.groupingBy(a -> a.getClothes().getId()));

    // defId 수집
    Set<UUID> defIds = attributes.stream()
        .map(a -> a.getDefinition().getId())
        .collect(Collectors.toSet());

    // options 조회
    List<ClothesAttributeOptions> allOptions = optionsRepository.findByDefinitionIdIn(defIds);
    Map<UUID, List<ClothesAttributeOptions>> optionsByDefId =
        allOptions.stream().collect(Collectors.groupingBy(o -> o.getDefinition().getId()));

      List<ClothesDto> data = clothes.stream().map(c -> {
        // attribute 조회
        List<ClothesAttributes> attr = attributesByClothesId.getOrDefault(c.getId(), List.of());

        // def + options 붙여서 attributeWithDefDto로 변환
        List<ClothesAttributeWithDefDto> attributeWithDefDtos =
            attr.stream().map(a -> {
                ClothesAttributeDefs def = a.getDefinition();
                String value = a.getOptionValue();
                return clothesAttributeMapper.toAttributeWithDefDto(def, value);
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
  @Transactional(readOnly = true)
  public ClothesDto getClothes(UUID clothesId) {
      Clothes clothes = clothesRepository.findById(clothesId)
          .orElseThrow(() -> new ClothesNotFoundException("해당 옷 정보를 찾을 수 없습니다."));
      return clothesMapper.toDto(clothes, Collections.emptyList(), fileStorage);
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
                  ClothesAttributeDefs def = defRepository.findById(a.definitionId()).orElseThrow(
                      () -> ClothesAttributeDefNotFoundException.byId(a.definitionId())
                  );
                  String value = a.value();
                  return
                      ClothesAttributes.builder()
                      .clothes(clothes)
                      .definition(def)
                      .optionValue(value)
                      .build();
              }
          ).toList();

      attributeRepository.saveAll(attributes);

      // ClothesDto 생성용 ClothesAttributeWithDefDto 생성
      List<ClothesAttributeWithDefDto> clothesAttributeWithDefDto =
          attributes.stream().map(a -> {
              ClothesAttributeDefs def = a.getDefinition();
              String value = a.getOptionValue();
              return clothesAttributeMapper.toAttributeWithDefDto(def, value);
          }).toList();

      // ClothesDto 반환
      return clothesMapper.toDto(clothes, clothesAttributeWithDefDto, fileStorage);
  }

  @Override
  public ClothesDto updateClothes(UUID clothesId, ClothesUpdateRequest request, MultipartFile imageFile)
      throws IOException {

      Clothes clothes = clothesRepository.findById(clothesId)
          .orElseThrow(() -> new ClothesNotFoundException("의상 정보를 찾을 수 없습니다. id: " + clothesId));

      String newName = request.name();
      ClothesType newType = request.type();
      String newImageUrl = null;
      if (imageFile != null) {
          newImageUrl = fileStorage.uploadFile(CLOTHES_PREFIX, imageFile);
      }

      clothes.update(newName, newType, newImageUrl);

      Clothes updatedClothes = clothesRepository.save(clothes);

      return clothesMapper.toDto(updatedClothes, Collections.emptyList(), fileStorage);
  }

    @Override
    public void deleteClothes(UUID clothesId) {

        Clothes clothes = clothesRepository.findById(clothesId)
          .orElseThrow(() -> new ClothesNotFoundException("의상 정보를 찾을 수 없습니다. id: " + clothesId));

        fileStorage.deleteFile(clothes.getImageUrl());
        attributeRepository.deleteByClothesId(clothesId);
        clothesRepository.deleteById(clothesId);
    }

}
