package com.autocoin.integration;

import com.autocoin.file.application.service.S3Uploader;
import com.autocoin.post.domain.entity.Post;
import com.autocoin.post.dto.request.PostRequestDto;
import com.autocoin.post.dto.response.PostResponseDto;
import com.autocoin.post.infrastructure.repository.PostJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
class PostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostJpaRepository postJpaRepository;

    @MockBean
    private S3Uploader s3Uploader;

    @BeforeEach
    void setUp() throws IOException {
        // S3 업로더 모킹 - 막소마장으로 모든 메서드 재정의
        mockS3Uploader();
        
        // 테스트 데이터 초기화
        postJpaRepository.deleteAll();
    }
    
    /**
     * S3Uploader를 완전히 목킹하는 메서드
     * @throws IOException 예외 발생 시
     */
    private void mockS3Uploader() throws IOException {
        // 완전히 새로 모킹 재설정
        reset(s3Uploader);
        
        // 일반적인 테스트에서는 null을 반환하도록 설정
        // 파일 업로드가 필요한 테스트만 위에서 따로 모킹함
        when(s3Uploader.upload(any(MultipartFile.class), anyString()))
            .thenReturn(null);
        
        // delete 메서드
        doNothing().when(s3Uploader).delete(anyString());
    }

    @Test
    @DisplayName("게시글 CRUD 통합 테스트")
    void postCRUD() throws Exception {
        // 테스트용 NULL 모킹 강화 - 아예 필요없음
        when(s3Uploader.upload(any(MultipartFile.class), anyString()))
            .thenReturn(null);

        // 실제 이미지 데이터를 만들기 위한 바이트 배열
        byte[] imageBytes = "test image content".getBytes();
        
        // MockMultipartFile 생성 - 정확한 형식으로 설정
        MockMultipartFile file = new MockMultipartFile(
                "file",              // 파라미터 이름 (매우 중요!)
                "test.jpg",          // 원본 파일명
                MediaType.IMAGE_JPEG_VALUE,  // 컨텐트 타입
                imageBytes             // 파일 컨텐트
        );
        
        // 메서드 호출을 로깅할 수 있도록 테스트 설정 추가
        System.out.println("MockMultipartFile 생성됨: " + file.getOriginalFilename() + ", size=" + file.getSize());
        
        // Multipart 요청 빌더 생성
        MockMultipartHttpServletRequestBuilder multipartRequest = 
                MockMvcRequestBuilders.multipart("/api/v1/posts/noauth")
                                      .file(file);
        
        // 파라미터 추가 (파일은 이미 추가했으므로 생략)
        multipartRequest
                .param("title", "테스트 제목")
                .param("content", "테스트 내용")
                .param("writer", "testuser")
                .characterEncoding("UTF-8")
                .with(csrf());
        
        // 테스트 실행
        String createResult = mockMvc.perform(multipartRequest)
                .andDo(print()) // 실행 결과 출력
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andExpect(jsonPath("$.writer").value("testuser"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 생성된 게시글의 ID 추출
        PostResponseDto createdPost = objectMapper.readValue(createResult, PostResponseDto.class);
        Long postId = createdPost.getId();
        
        // 2. 생성된 게시글 조회
        mockMvc.perform(get("/api/v1/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andExpect(jsonPath("$.writer").value("testuser"));

        // 업데이트를 위한 MockMultipartFile 생성
        MockMultipartFile updatedFile = new MockMultipartFile(
                "file",              // 파라미터 이름
                "updated.jpg",       // 원본 파일명
                MediaType.IMAGE_JPEG_VALUE,  // 컨텐트 타입
                "updated image content".getBytes()  // 파일 컨텐트
        );

        // PUT 요청을 위한 MultipartRequest 생성
        MockMultipartHttpServletRequestBuilder updateRequest = 
                MockMvcRequestBuilders.multipart("/api/v1/posts/{id}", postId);
        
        // 파일 추가
        updateRequest.file(updatedFile);
                
        // HTTP 메소드를 PUT으로 변경
        updateRequest = (MockMultipartHttpServletRequestBuilder) updateRequest.with(request -> {
            request.setMethod("PUT");
            return request;
        });
                
        // 테스트 안정성을 위해 추가 모킹 설정 - 수정 요청에도 null 반환
        when(s3Uploader.upload(any(MultipartFile.class), anyString()))
            .thenReturn(null);
        
        // 파라미터 추가
        updateRequest
                .param("title", "수정된 제목")
                .param("content", "수정된 내용")
                .param("writer", "testuser")
                .characterEncoding("UTF-8")
                .with(csrf());
        
        // 수정 요청 전송 - 상태코드만 검증
        mockMvc.perform(updateRequest)
                .andDo(print())
                .andExpect(status().isOk());
                
        // 소거하게 수정된 게시글만 조회
        mockMvc.perform(get("/api/v1/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 4. 게시글 삭제
        mockMvc.perform(delete("/api/v1/posts/{id}", postId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 삭제 확인
        assertThat(postJpaRepository.findById(postId)).isEmpty();
    }
    
    @Test
    @DisplayName("전체 게시글 목록 조회 테스트")
    void getAllPosts() throws Exception {
        // 게시글 여러 개 생성
        Post post1 = Post.builder()
                .title("제목 1")
                .content("내용 1")
                .writer("작성자 1")
                .build();
                
        Post post2 = Post.builder()
                .title("제목 2")
                .content("내용 2")
                .writer("작성자 2")
                .build();
                
        postJpaRepository.save(post1);
        postJpaRepository.save(post2);
        
        // 전체 게시글 조회
        mockMvc.perform(get("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[1].title").exists())
                // 전체 항목 수는 최소 2개 이상 (테스트 데이터에 따라 달라질 수 있음)
                .andExpect(jsonPath("$.length()").value(2));
    }
}