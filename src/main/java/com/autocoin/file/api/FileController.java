package com.autocoin.file.api;

import com.autocoin.file.application.FileService;
import com.autocoin.file.domain.File;
import com.autocoin.file.dto.FileResponseDto;
import com.autocoin.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 파일 업로드 API
     * 파일을 S3에 업로드합니다.
     * 
     * @param file 업로드할 파일 (필수)
     * @param user 현재 인증된 사용자
     * @return 업로드된 파일 정보
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponseDto> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        File savedFile = fileService.uploadFile(file, user);
        return new ResponseEntity<>(FileResponseDto.of(savedFile), HttpStatus.CREATED);
    }
    
    /**
     * 파일 조회 API
     * ID로 파일 정보를 조회합니다.
     * 
     * @param fileId 파일 ID (필수)
     * @return 파일 정보
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponseDto> getFile(
            @PathVariable Long fileId) {
        File file = fileService.findFileById(fileId);
        return ResponseEntity.ok(FileResponseDto.of(file));
    }
    
    /**
     * 사용자 파일 목록 조회 API
     * 현재 사용자가 업로드한 파일 목록을 조회합니다.
     * 
     * @param user 현재 인증된 사용자
     * @return 파일 정보 목록
     */
    @GetMapping("/user")
    public ResponseEntity<List<FileResponseDto>> getUserFiles(
            @AuthenticationPrincipal User user) {
        List<File> files = fileService.findFilesByUser(user);
        List<FileResponseDto> responseDtos = files.stream()
                .map(FileResponseDto::of)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }
    
    /**
     * 파일 삭제 API
     * 파일을 삭제합니다.
     * 
     * @param fileId 파일 ID (필수)
     * @param user 현재 인증된 사용자
     * @return 응답 없음 (204 No Content)
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal User user) {
        fileService.deleteFile(fileId, user);
        return ResponseEntity.noContent().build();
    }
}
