package com.autocoin.file.api;

import com.autocoin.file.application.FileService;
import com.autocoin.file.domain.File;
import com.autocoin.file.dto.FileResponseDto;
import com.autocoin.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "파일", description = "파일 업로드/다운로드 관리 API")
public class FileController {

    private final FileService fileService;

    @Operation(summary = "파일 업로드", description = "파일을 S3에 업로드합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "파일 업로드 성공",
                content = @Content(schema = @Schema(implementation = FileResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 파일 형식"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "413", description = "파일 크기 초과"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponseDto> uploadFile(
            @Parameter(description = "업로드할 파일", required = true)
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        File savedFile = fileService.uploadFile(file, user);
        return new ResponseEntity<>(FileResponseDto.of(savedFile), HttpStatus.CREATED);
    }
    
    @Operation(summary = "파일 정보 조회", description = "파일 ID로 파일 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "파일 조회 성공",
                content = @Content(schema = @Schema(implementation = FileResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponseDto> getFile(
            @Parameter(description = "파일 ID", required = true)
            @PathVariable Long fileId) {
        File file = fileService.findFileById(fileId);
        return ResponseEntity.ok(FileResponseDto.of(file));
    }
    
    @Operation(summary = "내 파일 목록 조회", description = "현재 사용자가 업로드한 파일 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "파일 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/user")
    public ResponseEntity<List<FileResponseDto>> getUserFiles(
            @AuthenticationPrincipal User user) {
        List<File> files = fileService.findFilesByUser(user);
        List<FileResponseDto> responseDtos = files.stream()
                .map(FileResponseDto::of)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }
    
    @Operation(summary = "파일 삭제", description = "업로드된 파일을 삭제합니다. 소유자만 삭제할 수 있습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "파일 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 - 파일 소유자가 아님"),
        @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "삭제할 파일 ID", required = true)
            @PathVariable Long fileId,
            @AuthenticationPrincipal User user) {
        fileService.deleteFile(fileId, user);
        return ResponseEntity.noContent().build();
    }
}
