package com.autocoin.post.api;

import com.autocoin.post.application.PostService;
import com.autocoin.post.domain.Post;
import com.autocoin.post.dto.PostRequestDto;
import com.autocoin.post.dto.PostResponseDto;
import com.autocoin.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@Valid @RequestBody PostRequestDto requestDto, 
                                                     @AuthenticationPrincipal User user) {
        Post post = postService.createPost(requestDto, user);
        return new ResponseEntity<>(PostResponseDto.of(post), HttpStatus.CREATED);
    }
    
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId) {
        Post post = postService.findPostById(postId);
        return ResponseEntity.ok(PostResponseDto.of(post));
    }
    
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        List<Post> posts = postService.findAllPosts();
        List<PostResponseDto> responseDtos = posts.stream()
                .map(PostResponseDto::of)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<PostResponseDto>> getUserPosts(@AuthenticationPrincipal User user) {
        List<Post> posts = postService.findPostsByUser(user);
        List<PostResponseDto> responseDtos = posts.stream()
                .map(PostResponseDto::of)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }
    
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(@PathVariable Long postId,
                                                     @Valid @RequestBody PostRequestDto requestDto,
                                                     @AuthenticationPrincipal User user) {
        Post post = postService.updatePost(postId, requestDto, user);
        return ResponseEntity.ok(PostResponseDto.of(post));
    }
    
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId,
                                          @AuthenticationPrincipal User user) {
        postService.deletePost(postId, user);
        return ResponseEntity.noContent().build();
    }
}
