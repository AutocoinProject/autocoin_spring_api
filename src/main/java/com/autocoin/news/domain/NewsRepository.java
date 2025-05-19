package com.autocoin.news.domain;

import com.autocoin.news.domain.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NewsRepository {
    List<News> findAll();
    Page<News> findAll(Pageable pageable);
    List<News> findByCategory(News.Category category);
    Page<News> findByCategory(News.Category category, Pageable pageable);
    Optional<News> findById(Long id);
    News save(News news);
    void deleteById(Long id);
    boolean existsByUrl(String url);
    List<News> findTop10ByOrderByPublishedAtDesc();
    Page<News> findByOrderByPublishedAtDesc(Pageable pageable);
    Page<News> findByCategoryOrderByPublishedAtDesc(News.Category category, Pageable pageable);
    List<News> findByPublishedAtBefore(LocalDateTime dateTime);
    void deleteByPublishedAtBefore(LocalDateTime dateTime);
    Long countByCategory(News.Category category);
    List<News> findTop5ByOrderByViewCountDesc();
}