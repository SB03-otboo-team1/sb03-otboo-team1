package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.config.TestJpaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
public class ClothesRepositoryTest {

  @Autowired
  private ClothesRepository clothesRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  void 옷을_등록할_수_있다() {
    // given

  }

}
