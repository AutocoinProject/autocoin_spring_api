package com.autocoin.post.api;

import com.autocoin.post.application.PostService;
import com.autocoin.post.domain.Post;
import com.autocoin.post.dto.PostRequestDto;
import com.autocoin.post.dto.PostResponseDto;
import com.autocoin.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "게시글", description = "게시글 CRUD API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // 모든 API에 JWT 인증 필요
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "게시글 작성 성공",
                content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 - 유효성 검사 실패"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @Valid @RequestBody PostRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        Post post = postService.createPost(requestDto, user);
        return new ResponseEntity<>(PostResponseDto.of(post), HttpStatus.CREATED);
    }
    
    @Operation(summary = "게시글 조회", description = "ID로 게시글을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "게시글 조회 성공",
                content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "게시글 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId) {
        Post post = postService.findPostById(postId);
        return ResponseEntity.ok(PostResponseDto.of(post));
    }
    
    @Operation(summary = "모든 게시글 조회", description = "모든 게시글 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        List<Post> posts = postService.findAllPosts();
        List<PostResponseDto> responseDtos = posts.stream()
                .map(PostResponseDto::of)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }
    
    @Operation(summary = "내 게시글 조회", description = "현재 사용자가 작성한 게시글 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "내 게시글 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/user")
    public ResponseEntity<List<PostResponseDto>> getUserPosts(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        List<Post> posts = postService.findPostsByUser(user);
        List<PostResponseDto> responseDtos = posts.stream()
                .map(PostResponseDto::of)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }
    
    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "게시글 수정 성공",
                content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 - 유효성 검사 실패"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 - 게시글 작성자가 아님"),
        @ApiResponse(responseCode = "404", description = "게시글 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
            @Valid @RequestBody PostRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        Post post = postService.updatePost(postId, requestDto, user);
        return ResponseEntity.ok(PostResponseDto.of(post));
    }
    
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 - 게시글 작성자가 아님"),
        @ApiResponse(responseCode = "404", description = "게시글 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        postService.deletePost(postId, user);
        return ResponseEntity.noContent().build();
    }
}
