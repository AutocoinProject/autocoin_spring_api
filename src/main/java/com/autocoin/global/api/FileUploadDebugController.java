package com.autocoin.global.api;

import com.autocoin.file.application.service.S3Uploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/debug")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "디버깅 API", description = "파일 업로드 문제 해결을 위한 디버깅 API")
public class FileUploadDebugController {

    private final S3Uploader s3Uploader;

    /**
     * 단순 파일 업로드 테스트
     */
    @Operation(summary = "파일 업로드 테스트", description = "파일 업로드가 정상 작동하는지 확인하는 간단한 테스트")
    @PostMapping(value = "/file-test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> testFileUpload(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("=== 파일 업로드 테스트 시작 ===");
            log.info("파일명: {}", file != null ? file.getOriginalFilename() : "null");
            log.info("파일 크기: {}", file != null ? file.getSize() : 0);
            log.info("파일 타입: {}", file != null ? file.getContentType() : "null");
            log.info("파일이 비어있는가: {}", file != null ? file.isEmpty() : "file is null");
            
            if (file == null) {
                result.put("success", false);
                result.put("message", "파일이 null입니다");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "파일이 비어있습니다");
                return ResponseEntity.badRequest().body(result);
            }
            
            // S3 업로드 시도
            String uploadedUrl = s3Uploader.upload(file, "debug-test");
            log.info("S3 업로드 성공: {}", uploadedUrl);
            
            result.put("success", true);
            result.put("message", "파일 업로드 성공");
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("contentType", file.getContentType());
            result.put("uploadedUrl", uploadedUrl);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생: ", e);
            result.put("success", false);
            result.put("message", "파일 업로드 실패: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * AWS S3 설정 확인
     */
    @Operation(summary = "S3 설정 확인", description = "AWS S3 설정이 올바른지 확인")
    @GetMapping("/s3-config")
    public ResponseEntity<Map<String, Object>> checkS3Config(
            @Value("${cloud.aws.s3.bucket}") String bucket,
            @Value("${cloud.aws.region.static}") String region) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("bucket", bucket);
            result.put("region", region);
            result.put("bucketExists", bucket != null && !bucket.trim().isEmpty());
            result.put("regionExists", region != null && !region.trim().isEmpty());
            
            // 실제 S3 연결 테스트는 위험할 수 있으므로 기본 정보만 확인
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("S3 설정 확인 중 오류: ", e);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 멀티파트 요청 정보 확인
     */
    @Operation(summary = "멀티파트 요청 정보 확인", description = "멀티파트 요청이 올바르게 전송되는지 확인")
    @PostMapping(value = "/multipart-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> checkMultipartInfo(
            HttpServletRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("=== 멀티파트 요청 정보 확인 ===");
            
            // Request 정보
            result.put("contentType", request.getContentType());
            result.put("method", request.getMethod());
            result.put("contentLength", request.getContentLength());
            
            // Parameters
            Map<String, String> params = new HashMap<>();
            params.put("title", title);
            params.put("content", content);
            result.put("parameters", params);
            
            // File 정보
            if (file != null) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("originalFilename", file.getOriginalFilename());
                fileInfo.put("size", file.getSize());
                fileInfo.put("contentType", file.getContentType());
                fileInfo.put("isEmpty", file.isEmpty());
                result.put("file", fileInfo);
            } else {
                result.put("file", "null");
            }
            
            log.info("멀티파트 정보: {}", result);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("멀티파트 정보 확인 중 오류: ", e);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 간단한 게시글 작성 테스트 (파일 없이)
     */
    @Operation(summary = "게시글 작성 테스트 (파일 없음)", description = "파일 없이 게시글 작성이 되는지 확인")
    @PostMapping(value = "/post-test-no-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> testPostWithoutFile(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("writer") String writer) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("=== 파일 없는 게시글 테스트 ===");
            log.info("제목: {}", title);
            log.info("내용: {}", content);
            log.info("작성자: {}", writer);
            
            result.put("success", true);
            result.put("message", "파일 없는 게시글 작성 테스트 성공");
            result.put("title", title);
            result.put("content", content);
            result.put("writer", writer);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("파일 없는 게시글 테스트 중 오류: ", e);
            result.put("success", false);
            result.put("message", "테스트 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
