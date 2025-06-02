package com.autocoin.file.application.service;

import com.amazonaws.services.s3.AmazonS3;

import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3에 파일 업로드
     * @param multipartFile 업로드할 파일
     * @param dirName S3 내 디렉토리명
     * @return 업로드된 파일의 S3 URL
     * @throws IOException 파일 업로드 실패 시 발생
     */
    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        log.info("=== S3Uploader.upload 시작 ===");
        log.info("파일명: {}", multipartFile != null ? multipartFile.getOriginalFilename() : "null");
        log.info("파일 크기: {}", multipartFile != null ? multipartFile.getSize() : 0);
        log.info("디렉토리: {}", dirName);
        log.info("S3 버킷: {}", bucket);
                
        if (multipartFile == null) {
            log.error("S3Uploader - multipartFile이 null입니다!");
            return null;
        }
        
        if (multipartFile.isEmpty()) {
            log.error("S3Uploader - multipartFile이 비어 있습니다! 파일 크기: {}", multipartFile.getSize());
            return null;
        }
        
        log.info("S3 업로드 준비 완료 - 파일 검증 통과");

        String originalFileName = multipartFile.getOriginalFilename();
        String fileName = createFileName(originalFileName, dirName);
        String fileKey = dirName + "/" + fileName;
        
        log.info("생성된 파일 Key: {}", fileKey);
        log.info("생성된 파일 이름: {}", fileName);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        
        log.info("ObjectMetadata 설정: ContentLength={}, ContentType={}", 
                metadata.getContentLength(), metadata.getContentType());

        try {
            log.info("S3 putObject 시작");
            amazonS3.putObject(new PutObjectRequest(bucket, fileKey, multipartFile.getInputStream(), metadata));
            log.info("S3 putObject 완료");
                    
            String s3Url = amazonS3.getUrl(bucket, fileKey).toString();
            log.info("S3 업로드 성공: URL={}", s3Url);
            return s3Url;
        } catch (IOException e) {
            log.error("S3 파일 업로드 IOException 발생: {}", e.getMessage(), e);
            throw new IOException("파일 업로드 실패", e);
        } catch (Exception e) {
            log.error("S3 파일 업로드 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new IOException("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * S3에서 파일 삭제
     * @param fileKey 삭제할 파일의 S3 키
     */
    public void delete(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return;
        }
        
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileKey));
        } catch (Exception e) {
            log.error("S3 파일 삭제 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 업로드할 파일명 생성
     * @param originalFileName 원본 파일명
     * @param dirName 디렉토리명
     * @return 생성된 파일명 (UUID 포함)
     */
    private String createFileName(String originalFileName, String dirName) {
        return UUID.randomUUID().toString() + "_" + originalFileName;
    }
}