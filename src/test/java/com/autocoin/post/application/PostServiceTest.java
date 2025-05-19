package com.autocoin.post.application;

import com.autocoin.file.application.service.S3Uploader;
import com.autocoin.global.exception.ResourceNotFoundException;
import com.autocoin.post.application.service.PostService;
import com.autocoin.post.domain.PostRepository;
import com.autocoin.post.domain.entity.Post;
import com.autocoin.post.dto.request.PostRequestDto;
import com.autocoin.post.dto.response.PostResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private S3Uploader s3Uploader;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("게시글 생성 성공 테스트 (파일 없음)")
    void createPostWithoutFile() {
        // given
        PostRequestDto requestDto = PostRequestDto.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .writer("테스트 작성자")
                .build();

        Post savedPost = Post.builder()
                .id(1L)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .writer(requestDto.getWriter())
                .build();

        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        // when
        PostResponseDto responseDto = postService.createPost(requestDto, null);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getId()).isEqualTo(savedPost.getId());
        assertThat(responseDto.getTitle()).isEqualTo(savedPost.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(savedPost.getContent());
        assertThat(responseDto.getWriter()).isEqualTo(savedPost.getWriter());
        assertThat(responseDto.getFileUrl()).isNull();
        
        verify(postRepository, times(1)).save(any(Post.class));
        verify(s3Uploader, never()).upload(any(MultipartFile.class), anyString());
    }

    @Test
    @DisplayName("게시글 생성 성공 테스트 (파일 포함)")
    void createPostWithFile() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        PostRequestDto requestDto = PostRequestDto.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .writer("테스트 작성자")
                .file(file)
                .build();

        String fileUrl = "https://example.com/test.jpg";
        given(s3Uploader.upload(any(MultipartFile.class), anyString())).willReturn(fileUrl);

        Post savedPost = Post.builder()
                .id(1L)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .writer(requestDto.getWriter())
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileKey("posts/" + file.getOriginalFilename())
                .build();

        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        // when
        PostResponseDto responseDto = postService.createPost(requestDto, null);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getId()).isEqualTo(savedPost.getId());
        assertThat(responseDto.getTitle()).isEqualTo(savedPost.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(savedPost.getContent());
        assertThat(responseDto.getWriter()).isEqualTo(savedPost.getWriter());
        assertThat(responseDto.getFileUrl()).isEqualTo(fileUrl);
        assertThat(responseDto.getFileName()).isEqualTo(file.getOriginalFilename());
        
        verify(postRepository, times(1)).save(any(Post.class));
        verify(s3Uploader, times(1)).upload(any(MultipartFile.class), anyString());
    }

    @Test
    @DisplayName("ID로 게시글 조회 성공 테스트")
    void getPostSuccess() {
        // given
        Long postId = 1L;
        Post post = Post.builder()
                .id(postId)
                .title("테스트 제목")
                .content("테스트 내용")
                .writer("테스트 작성자")
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        PostResponseDto responseDto = postService.getPost(postId);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getId()).isEqualTo(post.getId());
        assertThat(responseDto.getTitle()).isEqualTo(post.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(post.getContent());
        assertThat(responseDto.getWriter()).isEqualTo(post.getWriter());
        
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    @DisplayName("ID로 게시글 조회 실패 테스트 - 존재하지 않는 게시글")
    void getPostFail() {
        // given
        Long postId = 99L;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost(postId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("해당 ID의 게시글을 찾을 수 없습니다");
        
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    @DisplayName("모든 게시글 조회 테스트")
    void getAllPosts() {
        // given
        Post post1 = Post.builder()
                .id(1L)
                .title("테스트 제목 1")
                .content("테스트 내용 1")
                .writer("테스트 작성자 1")
                .build();

        Post post2 = Post.builder()
                .id(2L)
                .title("테스트 제목 2")
                .content("테스트 내용 2")
                .writer("테스트 작성자 2")
                .build();

        List<Post> posts = Arrays.asList(post1, post2);

        given(postRepository.findAllByOrderByCreatedAtDesc()).willReturn(posts);

        // when
        List<PostResponseDto> responseDtos = postService.getAllPosts();

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos.get(0).getId()).isEqualTo(post1.getId());
        assertThat(responseDtos.get(0).getTitle()).isEqualTo(post1.getTitle());
        assertThat(responseDtos.get(1).getId()).isEqualTo(post2.getId());
        assertThat(responseDtos.get(1).getTitle()).isEqualTo(post2.getTitle());
        
        verify(postRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("게시글 수정 성공 테스트 (파일 없음)")
    void updatePostWithoutFile() {
        // given
        Long postId = 1L;
        Post existingPost = Post.builder()
                .id(postId)
                .title("원본 제목")
                .content("원본 내용")
                .writer("원본 작성자")
                .build();

        PostRequestDto requestDto = PostRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .writer("원본 작성자")
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(existingPost));

        // when
        PostResponseDto responseDto = postService.updatePost(postId, requestDto, null);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getId()).isEqualTo(postId);
        assertThat(responseDto.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(requestDto.getContent());
        assertThat(responseDto.getWriter()).isEqualTo(requestDto.getWriter());
        
        verify(postRepository, times(1)).findById(postId);
        verify(s3Uploader, never()).upload(any(MultipartFile.class), anyString());
        verify(s3Uploader, never()).delete(anyString());
    }

    @Test
    @DisplayName("게시글 수정 성공 테스트 (파일 포함, 기존 파일 교체)")
    void updatePostWithFile() throws IOException {
        // given
        Long postId = 1L;
        String originalFileKey = "posts/original.jpg";
        
        Post existingPost = Post.builder()
                .id(postId)
                .title("원본 제목")
                .content("원본 내용")
                .writer("원본 작성자")
                .fileUrl("https://example.com/original.jpg")
                .fileName("original.jpg")
                .fileKey(originalFileKey)
                .build();

        MockMultipartFile newFile = new MockMultipartFile(
                "file",
                "updated.jpg",
                "image/jpeg",
                "updated image content".getBytes()
        );

        PostRequestDto requestDto = PostRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .writer("원본 작성자")
                .file(newFile)
                .build();

        String newFileUrl = "https://example.com/updated.jpg";
        given(postRepository.findById(postId)).willReturn(Optional.of(existingPost));
        given(s3Uploader.upload(any(MultipartFile.class), anyString())).willReturn(newFileUrl);
        doNothing().when(s3Uploader).delete(anyString());

        // when
        PostResponseDto responseDto = postService.updatePost(postId, requestDto, null);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getId()).isEqualTo(postId);
        assertThat(responseDto.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(requestDto.getContent());
        assertThat(responseDto.getFileUrl()).isEqualTo(newFileUrl);
        assertThat(responseDto.getFileName()).isEqualTo(newFile.getOriginalFilename());
        
        verify(postRepository, times(1)).findById(postId);
        verify(s3Uploader, times(1)).delete(originalFileKey);
        verify(s3Uploader, times(1)).upload(any(MultipartFile.class), anyString());
    }

    @Test
    @DisplayName("게시글 삭제 성공 테스트 (파일 포함)")
    void deletePostWithFile() {
        // given
        Long postId = 1L;
        String fileKey = "posts/test.jpg";
        
        Post post = Post.builder()
                .id(postId)
                .title("테스트 제목")
                .content("테스트 내용")
                .writer("테스트 작성자")
                .fileUrl("https://example.com/test.jpg")
                .fileName("test.jpg")
                .fileKey(fileKey)
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        doNothing().when(s3Uploader).delete(anyString());
        doNothing().when(postRepository).delete(any(Post.class));

        // when
        postService.deletePost(postId);

        // then
        verify(postRepository, times(1)).findById(postId);
        verify(s3Uploader, times(1)).delete(fileKey);
        verify(postRepository, times(1)).delete(post);
    }
}