package com.autocoin.file.api;

import com.autocoin.file.application.FileService;
import com.autocoin.file.domain.File;
import com.autocoin.file.dto.FileResponseDto;
import com.autocoin.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileResponseDto> uploadFile(@RequestParam("file") MultipartFile file,
                                                    @AuthenticationPrincipal User user) {
        File savedFile = fileService.uploadFile(file, user);
        return new ResponseEntity<>(FileResponseDto.of(savedFile), HttpStatus.CREATED);
    }
    
    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponseDto> getFile(@PathVariable Long fileId) {
        File file = fileService.findFileById(fileId);
        return ResponseEntity.ok(FileResponseDto.of(file));
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<FileResponseDto>> getUserFiles(@AuthenticationPrincipal User user) {
        List<File> files = fileService.findFilesByUser(user);
        List<FileResponseDto> responseDtos = files.stream()
                .map(FileResponseDto::of)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }
    
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId,
                                         @AuthenticationPrincipal User user) {
        fileService.deleteFile(fileId, user);
        return ResponseEntity.noContent().build();
    }
}
