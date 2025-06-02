package com.autocoin.post.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.autocoin.post.application.service.PostService;
import com.autocoin.post.dto.request.PostRequestDto;
import com.autocoin.post.dto.response.PostResponseDto;
import com.autocoin.user.domain.User;
import com.autocoin.user.domain.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "게시글 API", description = "게시글 CRUD API")
public class PostController {

    private final PostService postService;

    /**
     * 게시글 작성 API
     * @param requestDto 게시글 작성 요청 DTO
     * @param user 현재 인증된 사용자
     * @return 생성된 게시글 정보
     */
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다. 파일 업로드도 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "게시글 작성 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 요청", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDto> createPost(
            @Valid @ModelAttribute PostRequestDto requestDto,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        
        log.info("=== 게시글 작성 요청 시작 ===");
        log.info("Request Method: {}", request.getMethod());
        log.info("Request Content-Type: {}", request.getContentType());
        log.info("Request Content-Length: {}", request.getContentLength());
        
        log.info("Post request received: title='{}', content='{}', writer='{}', file={}", 
                requestDto.getTitle(), 
                requestDto.getContent(), 
                requestDto.getWriter(), 
                requestDto.getFile() != null ? requestDto.getFile().getOriginalFilename() : "null");
        
        // 파일 정보 상세 로깅
        if (requestDto.getFile() != null) {
            log.info("파일 상세 정보:");
            log.info("  - 파일명: {}", requestDto.getFile().getOriginalFilename());
            log.info("  - 파일 크기: {} bytes", requestDto.getFile().getSize());
            log.info("  - 파일 타입: {}", requestDto.getFile().getContentType());
            log.info("  - 파일이 비어있음: {}", requestDto.getFile().isEmpty());
        } else {
            log.warn("파일이 null입니다");
        }
        
        // 요청 값 디버깅
        if (requestDto.getTitle() == null || requestDto.getTitle().isEmpty()) {
            log.warn("Title is null or empty in the request");
        }
        if (requestDto.getContent() == null || requestDto.getContent().isEmpty()) {
            log.warn("Content is null or empty in the request");
        }
        if (requestDto.getWriter() == null || requestDto.getWriter().isEmpty()) {
            log.warn("Writer is null or empty in the request");
        }
        
        try {
            PostResponseDto response = postService.createPost(requestDto, user);
            log.info("게시글 작성 성공: id={}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("게시글 작성 중 오류 발생: ", e);
            throw e;
        }
    }
    
    /**
     * 테스트용 게시글 작성 API - 유효성 검증 없음
     * @param requestDto 게시글 작성 요청 DTO
     * @param user 현재 인증된 사용자
     * @return 생성된 게시글 정보
     */
    @Operation(summary = "(테스트용) 게시글 작성", description = "유효성 검증 없이 게시글을 작성합니다. 테스트용입니다.")
    @PostMapping(value = "/test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDto> createTestPost(
            @ModelAttribute PostRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        log.debug("Test post request received: title={}, content={}, writer={}, file={}", 
                requestDto.getTitle(), 
                requestDto.getContent(), 
                requestDto.getWriter(), 
                requestDto.getFile() != null ? requestDto.getFile().getOriginalFilename() : "null");
        
        // 기본값 설정
        if (requestDto.getTitle() == null || requestDto.getTitle().isEmpty() || 
            requestDto.getContent() == null || requestDto.getContent().isEmpty()) {
            // DTO가 불변 객체이므로 새 객체 생성
            requestDto = PostRequestDto.builder()
                    .title(requestDto.getTitle() != null && !requestDto.getTitle().isEmpty() 
                            ? requestDto.getTitle() : "테스트 제목")
                    .content(requestDto.getContent() != null && !requestDto.getContent().isEmpty() 
                            ? requestDto.getContent() : "테스트 내용")
                    .writer(requestDto.getWriter())
                    .file(requestDto.getFile())
                    .build();
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(requestDto, user));
    }
    
    /**
     * 간단한 파일 업로드 테스트 API
     * @param title 제목
     * @param content 내용
     * @param writer 작성자
     * @param file 파일
     * @return 업로드 결과
     */
    @Operation(summary = "(테스트용) 파일 업로드 확인", description = "파일 업로드가 정상적으로 작동하는지 확인하는 테스트 API")
    @PostMapping(value = "/upload-test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> testFileUpload(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("writer") String writer,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        Map<String, Object> result = new HashMap<>();
        
        log.info("=== 파일 업로드 테스트 ===");
        log.info("title: {}", title);
        log.info("content: {}", content);
        log.info("writer: {}", writer);
        log.info("file: {}", file != null ? file.getOriginalFilename() : "null");
        
        result.put("success", true);
        result.put("title", title);
        result.put("content", content);
        result.put("writer", writer);
        
        if (file != null && !file.isEmpty()) {
            result.put("fileReceived", true);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("contentType", file.getContentType());
        } else {
            result.put("fileReceived", false);
            result.put("fileName", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 인증 필요 없는 게시글 작성 API - 설정한 테스트 사용자 사용
     * @param requestDto 게시글 작성 요청 DTO
     * @return 생성된 게시글 정보
     */
    @Operation(summary = "(테스트용) 인증 없는 게시글 작성", description = "인증 없이 계정을 하드코딩해서 게시글을 작성합니다. 테스트용입니다.")
    @PostMapping(value = "/noauth", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDto> createPostNoAuth(
            @ModelAttribute PostRequestDto requestDto) {
        log.info("No auth post request received: title='{}', content='{}', writer='{}', file={}", 
                requestDto.getTitle(), 
                requestDto.getContent(), 
                requestDto.getWriter(), 
                requestDto.getFile() != null ? requestDto.getFile().getOriginalFilename() : "null");
        
        // 기본값 설정
        String title = (requestDto.getTitle() != null && !requestDto.getTitle().isEmpty())
                ? requestDto.getTitle()
                : "테스트 제목";
                
        String content = (requestDto.getContent() != null && !requestDto.getContent().isEmpty())
                ? requestDto.getContent()
                : "테스트 내용";
                
        String writer = (requestDto.getWriter() != null && !requestDto.getWriter().isEmpty())
                ? requestDto.getWriter()
                : "testuser";
        
        // 새 DTO 생성 - 사용자 정보 없이
        PostRequestDto newDto = PostRequestDto.builder()
                .title(title)
                .content(content)
                .writer(writer)
                .file(requestDto.getFile()) // 파일 그대로 전달 - 디버깅 메시지 추가
                .build();
        
        log.info("Processed request: title='{}', content='{}', writer='{}', file={}",
                newDto.getTitle(),
                newDto.getContent(),
                newDto.getWriter(),
                newDto.getFile() != null ? "present" + newDto.getFile().getOriginalFilename() : "null");
        
        // 테스트용으로 파일 내용 확인
        if (newDto.getFile() != null) {
            try {
                log.info("File content type: {}, size: {}", 
                        newDto.getFile().getContentType(), 
                        newDto.getFile().getSize());
            } catch (Exception e) {
                log.error("Error checking file: {}", e.getMessage());
            }
        }
        
        // 사용자 정보는 null로 전달 (선택적 관계)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(newDto, null));
    }

    /**
     * 게시글 목록 조회 API
     * @return 게시글 목록
     */
    @Operation(summary = "게시글 목록 조회", description = "모든 게시글을 작성일 기준 내림차순으로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    /**
     * 게시글 상세 조회 API
     * @param id 게시글 ID
     * @return 게시글 상세 정보
     */
    @Operation(summary = "게시글 상세 조회", description = "ID를 사용하여 특정 게시글의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPost(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    /**
     * 게시글 수정 API
     * @param id 게시글 ID
     * @param requestDto 게시글 수정 요청 DTO
     * @return 수정된 게시글 정보
     */
    @Operation(summary = "게시글 수정", description = "특정 게시글의 내용을 수정합니다. 파일 업로드도 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 요청", content = @Content),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDto> updatePost(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long id,
            @Valid @ModelAttribute PostRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.updatePost(id, requestDto, user));
    }

    /**
     * 게시글 삭제 API
     * @param id 게시글 ID
     * @return 응답 없음 (204 No Content)
     */
    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다. 관련된 파일도 S3에서 삭제됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content),
        @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 사용자별 게시글 목록 조회 API
     * @param user 현재 인증된 사용자
     * @return 사용자의 게시글 목록
     */
    @Operation(summary = "사용자별 게시글 목록 조회", description = "현재 인증된 사용자의 모든 게시글을 작성일 기준 내림차순으로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/my")
    public ResponseEntity<List<PostResponseDto>> getMyPosts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.getPostsByUser(user));
    }


}