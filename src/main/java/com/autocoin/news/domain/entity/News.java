package com.autocoin.news.domain.entity;

import com.autocoin.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, unique = true)
    private String url;
    
    private String imageUrl;
    
    @Column(nullable = false)
    private String source;
    
    @Column(nullable = false)
    private LocalDateTime publishedAt;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    private Category category;
    
    private Integer viewCount = 0;
    
    // 뉴스 카테고리
    public enum Category {
        CRYPTOCURRENCY, BLOCKCHAIN, FINANCE, TECHNOLOGY, MARKET, BITCOIN, ETHEREUM
    }
    
    public void updateContent(String content) {
        this.content = content;
    }
    
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    public void updateCategory(Category category) {
        this.category = category;
    }
}