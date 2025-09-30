package com.onepiece.otboo.domain.dm.repository;

import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID>,
    DirectMessageRepositoryCustom {

}