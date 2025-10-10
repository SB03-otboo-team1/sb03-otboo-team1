package com.onepiece.otboo.domain.comment.controller;

import com.onepiece.otboo.domain.comment.service.CommentQueryService;
import com.onepiece.otboo.domain.comment.service.CommentService;
import com.onepiece.otboo.domain.comment.controller.api.CommentApi;
import com.onepiece.otboo.domain.comment.dto.request.CommentCreateRequest;
import com.onepiece.otboo.domain.comment.dto.response.CommentDto;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentService commentService;
    private final CommentQueryService commentQueryService;

    @Override
    @PostMapping("/{feedId}/comments")
    public ResponseEntity<CommentDto> createComment(
        @PathVariable("feedId") UUID feedId,
        @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentDto dto = commentService.create(feedId, request);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{feedId}/comments")
    @Override
    public ResponseEntity<CursorPageResponseDto<CommentDto>> listComments(
        @PathVariable("feedId") UUID feedId,
        @RequestParam(required = false) String cursor,
        @RequestParam(name = "idAfter", required = false) UUID idAfter,
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        CursorPageResponseDto<CommentDto> body = commentQueryService.listByFeed(feedId, cursor, idAfter, limit);
        return ResponseEntity.ok(body);
    }
}
