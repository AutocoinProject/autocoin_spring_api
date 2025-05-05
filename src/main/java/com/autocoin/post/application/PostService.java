package com.autocoin.post.application;

import com.autocoin.global.exception.CustomException;
import com.autocoin.global.exception.ErrorCode;
import com.autocoin.post.domain.Post;
import com.autocoin.post.domain.PostRepository;
import com.autocoin.post.dto.PostRequestDto;
import com.autocoin.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public Post createPost(PostRequestDto requestDto, User user) {
        Post post = Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .user(user)
                .build();
                
        return postRepository.save(post);
    }
    
    @Transactional(readOnly = true)
    public Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
    
    @Transactional(readOnly = true)
    public List<Post> findAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }
    
    @Transactional(readOnly = true)
    public List<Post> findPostsByUser(User user) {
        return postRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    @Transactional
    public Post updatePost(Long postId, PostRequestDto requestDto, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
                
        // 작성자 확인
        if (!post.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.NOT_POST_OWNER);
        }
        
        // 게시글 수정
        post.update(requestDto.getTitle(), requestDto.getContent());
        
        return postRepository.save(post);
    }
    
    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
                
        // 작성자 확인
        if (!post.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.NOT_POST_OWNER);
        }
        
        // 게시글 삭제
        postRepository.delete(post);
    }
}
