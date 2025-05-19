package com.autocoin.news.infrastructure.repository;

import com.autocoin.news.domain.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsJpaRepository extends JpaRepository<News, Long> {
    Page<News> findByCategory(News.Category category, Pageable pageable);
    boolean existsByUrl(String url);
    List<News> findTop10ByOrderByPublishedAtDesc();
    Page<News> findByOrderByPublishedAtDesc(Pageable pageable);
    Page<News> findByCategoryOrderByPublishedAtDesc(News.Category category, Pageable pageable);
    List<News> findByPublishedAtBefore(LocalDateTime dateTime);
    void deleteByPublishedAtBefore(LocalDateTime dateTime);
    Long countByCategory(News.Category category);
    List<News> findTop5ByOrderByViewCountDesc();
    
    @Query("SELECT n FROM News n WHERE n.title LIKE %:keyword% OR n.description LIKE %:keyword%")
    Page<News> findByTitleContainingOrDescriptionContaining(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(n) FROM News n WHERE n.publishedAt >= :date")
    Long countNewsAfterDate(@Param("date") LocalDateTime date);
}