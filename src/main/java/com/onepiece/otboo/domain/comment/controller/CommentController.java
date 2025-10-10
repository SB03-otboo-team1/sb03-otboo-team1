package com.onepiece.otboo.domain.comment.controller;

import com.onepiece.otboo.domain.comment.service.CommentService;
import com.onepiece.otboo.domain.comment.controller.api.CommentApi;
import com.onepiece.otboo.domain.comment.dto.request.CommentCreateRequest;
import com.onepiece.otboo.domain.comment.dto.response.CommentDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Comment API")
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentService commentService;

    @Override
    @PostMapping("/{feedId}/comments")
    public ResponseEntity<CommentDto> createComment(
        @PathVariable("feedId") UUID feedId,
        @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentDto dto = commentService.create(feedId, request);
        return ResponseEntity.ok(dto);
    }
}
