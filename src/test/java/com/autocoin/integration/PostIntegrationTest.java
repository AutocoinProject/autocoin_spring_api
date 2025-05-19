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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
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
        // S3 업로더 모킹
        given(s3Uploader.upload(any(), anyString())).willReturn("https://example.com/test.jpg");
        doNothing().when(s3Uploader).delete(anyString());
        
        // 테스트 데이터 초기화
        postJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글 CRUD 통합 테스트")
    void postCRUD() throws Exception {
        // 1. 게시글 생성
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        String title = "통합 테스트 제목";
        String content = "통합 테스트 내용";
        String writer = "통합 테스트 작성자";

        String createResult = mockMvc.perform(multipart("/api/v1/posts")
                        .file(file)
                        .param("title", title)
                        .param("content", content)
                        .param("writer", writer)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.writer").value(writer))
                .andExpect(jsonPath("$.fileUrl").value("https://example.com/test.jpg"))
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
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.writer").value(writer));

        // 3. 게시글 수정
        String updatedTitle = "수정된 제목";
        String updatedContent = "수정된 내용";
        
        MockMultipartFile updatedFile = new MockMultipartFile(
                "file",
                "updated.jpg",
                "image/jpeg",
                "updated image content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/posts/{id}", postId)
                        .file(updatedFile)
                        .param("title", updatedTitle)
                        .param("content", updatedContent)
                        .param("writer", writer)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value(updatedTitle))
                .andExpect(jsonPath("$.content").value(updatedContent))
                .andExpect(jsonPath("$.writer").value(writer));

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