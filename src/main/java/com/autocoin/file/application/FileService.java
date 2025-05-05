package com.autocoin.file.application;

import com.autocoin.file.domain.File;
import com.autocoin.file.domain.FileRepository;
import com.autocoin.global.exception.CustomException;
import com.autocoin.global.exception.ErrorCode;
import com.autocoin.global.util.S3Uploader;
import com.autocoin.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final S3Uploader s3Uploader;
    
    private static final String S3_DIRECTORY = "files";

    @Transactional
    public File uploadFile(MultipartFile multipartFile, User user) {
        // S3에 파일 업로드
        String fileUrl = s3Uploader.upload(multipartFile, S3_DIRECTORY);
        
        // 파일 정보 저장
        File file = File.builder()
                .originalFileName(multipartFile.getOriginalFilename())
                .storedFileName(extractFileNameFromUrl(fileUrl))
                .fileUrl(fileUrl)
                .contentType(multipartFile.getContentType())
                .fileSize(multipartFile.getSize())
                .user(user)
                .build();
                
        return fileRepository.save(file);
    }
    
    @Transactional(readOnly = true)
    public File findFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
    }
    
    @Transactional(readOnly = true)
    public List<File> findFilesByUser(User user) {
        return fileRepository.findByUser(user);
    }
    
    @Transactional
    public void deleteFile(Long fileId, User user) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
                
        // 파일 소유자 확인
        if (!file.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        
        // S3에서 파일 삭제
        s3Uploader.delete(file.getFileUrl());
        
        // DB에서 파일 정보 삭제
        fileRepository.delete(file);
    }
    
    private String extractFileNameFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
}
