package com.autocoin.post.application.service;

import com.autocoin.file.application.service.S3Uploader;
import com.autocoin.global.exception.ResourceNotFoundException;
import com.autocoin.post.domain.entity.Post;
import com.autocoin.post.dto.request.PostRequestDto;
import com.autocoin.post.dto.response.PostResponseDto;
import com.autocoin.post.domain.PostRepository;
import com.autocoin.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final S3Uploader s3Uploader;
    private static final String S3_DIRECTORY = "posts";

    /**
     * 게시글 작성
     * @param requestDto 게시글 작성 요청 DTO
     * @return 작성된 게시글 응답 DTO
     */
    @Transactional
    public PostResponseDto createPost(PostRequestDto requestDto, User user) {
        try {
            // 파일 업로드
            String fileUrl = null;
            String fileName = null;
            String fileKey = null;
            
            // 디버깅 로그 추가
            log.info("createPost 초기 파라미터: requestDto.title={}, requestDto.file={}", 
                    requestDto.getTitle(), 
                    requestDto.getFile() != null ? requestDto.getFile().getOriginalFilename() : "null");
            
            MultipartFile file = requestDto.getFile();
            if (file != null && !file.isEmpty()) {
                log.info("upload 호출 전: file.name={}, file.size={}", 
                        file.getOriginalFilename(), file.getSize());
                
                // S3 업로더를 통한 파일 업로드
                fileUrl = s3Uploader.upload(file, S3_DIRECTORY);
                
                log.info("upload 호출 후: fileUrl={}", fileUrl);
                
                fileName = file.getOriginalFilename();
                // S3 업로드 후 반환되는 URL에서 키 추출
                fileKey = S3_DIRECTORY + "/" + fileName.substring(fileName.lastIndexOf("/") + 1);
            } else {
                log.warn("file이 null이거나 비어 있습니다.");
            }

            // 작성자 정보 처리 - writer가 없으면 현재 사용자의 username 사용
            String writerName = (requestDto.getWriter() != null && !requestDto.getWriter().isEmpty()) 
                    ? requestDto.getWriter() 
                    : (user != null ? user.getUsername() : "anonymous");
            
            // 제목과 내용이 없는 경우 기본값 설정
            String title = (requestDto.getTitle() != null && !requestDto.getTitle().isEmpty())
                    ? requestDto.getTitle()
                    : "제목 없음";
                    
            String content = (requestDto.getContent() != null && !requestDto.getContent().isEmpty())
                    ? requestDto.getContent()
                    : "내용 없음";

            // 게시글 저장
            Post.PostBuilder postBuilder = Post.builder()
                    .title(title)
                    .content(content)
                    .writer(writerName)
                    .fileUrl(fileUrl)
                    .fileName(fileName)
                    .fileKey(fileKey);
            
            // 사용자가 있는 경우에만 user 설정
            if (user != null) {
                postBuilder.user(user);
            }
            
            Post post = postBuilder.build();
            log.info("저장할 게시글: title={}, fileUrl={}, fileName={}", 
                    post.getTitle(), post.getFileUrl(), post.getFileName());

            Post savedPost = postRepository.save(post);
            log.info("저장된 게시글: id={}, title={}, fileUrl={}", 
                    savedPost.getId(), savedPost.getTitle(), savedPost.getFileUrl());
                    
            return PostResponseDto.of(savedPost);
        } catch (IOException e) {
            log.error("글 작성 중 파일 업로드 오류: {}", e.getMessage());
            throw new RuntimeException("글 작성 중 파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 게시글 상세 조회
     * @param id 게시글 ID
     * @return 게시글 응답 DTO
     */
    public PostResponseDto getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + id));
        return PostResponseDto.of(post);
    }

    /**
     * 게시글 목록 조회
     * @return 게시글 목록
     */
    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(PostResponseDto::of)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자별 게시글 목록 조회
     * @param user 사용자 객체
     * @return 해당 사용자의 게시글 목록
     */
    public List<PostResponseDto> getPostsByUser(User user) {
        return postRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(PostResponseDto::of)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 수정
     * @param id 게시글 ID
     * @param requestDto 게시글 수정 요청 DTO
     * @return 수정된 게시글 응답 DTO
     */
    @Transactional
    public PostResponseDto updatePost(Long id, PostRequestDto requestDto, User user) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + id));

        try {
            // 새 파일이 업로드된 경우
            String fileUrl = null;
            String fileName = null;
            String fileKey = null;

            MultipartFile file = requestDto.getFile();
            if (file != null && !file.isEmpty()) {
                // 기존 파일이 있으면 삭제
                if (post.getFileKey() != null) {
                    s3Uploader.delete(post.getFileKey());
                }
                
                // 새 파일 업로드
                fileUrl = s3Uploader.upload(file, S3_DIRECTORY);
                fileName = file.getOriginalFilename();
                fileKey = S3_DIRECTORY + "/" + file.getOriginalFilename(); // 실제 구현에서는 생성된 파일명을 사용해야 합니다
            }
            
            // 작성자 정보 처리 - writer가 없으면 현재 사용자의 username 사용 또는 기존 writer 유지
            String writerName = post.getWriter(); // 기본적으로 기존 작성자 정보 유지
            if (requestDto.getWriter() != null && !requestDto.getWriter().isEmpty()) {
                writerName = requestDto.getWriter(); // 새 작성자 정보가 있으면 사용
            }
            
            // 제목과 내용 처리 - null 또는 빈 문자열이면 기존 값 유지
            String title = (requestDto.getTitle() != null && !requestDto.getTitle().isEmpty())
                    ? requestDto.getTitle()
                    : post.getTitle();
                    
            String content = (requestDto.getContent() != null && !requestDto.getContent().isEmpty())
                    ? requestDto.getContent()
                    : post.getContent();

            // 게시글 업데이트
            post.update(title, content, fileUrl, fileName, fileKey, writerName);
            return PostResponseDto.of(post);
        } catch (IOException e) {
            log.error("글 수정 중 파일 업로드 오류: {}", e.getMessage());
            throw new RuntimeException("글 수정 중 파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 게시글 삭제
     * @param id 게시글 ID
     */
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + id));

        // S3에서 파일 삭제
        if (post.getFileKey() != null) {
            s3Uploader.delete(post.getFileKey());
        }

        // 게시글 삭제
        postRepository.delete(post);
    }
}