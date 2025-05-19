package com.autocoin.post.infrastructure.repository;

import com.autocoin.post.domain.entity.Post;
import com.autocoin.post.domain.PostRepository;
import com.autocoin.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final PostJpaRepository postJpaRepository;

    @Override
    public Post save(Post post) {
        return postJpaRepository.save(post);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return postJpaRepository.findById(id);
    }

    @Override
    public List<Post> findAllByOrderByCreatedAtDesc() {
        return postJpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<Post> findByUserOrderByCreatedAtDesc(User user) {
        return postJpaRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public void delete(Post post) {
        postJpaRepository.delete(post);
    }
}
