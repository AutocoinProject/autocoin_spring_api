package com.autocoin.news.application.service;

import com.autocoin.global.exception.CustomException;
import com.autocoin.global.exception.ErrorCode;
import com.autocoin.news.domain.NewsRepository;
import com.autocoin.news.domain.entity.News;
import com.autocoin.news.dto.response.NewsPageResponseDto;
import com.autocoin.news.dto.response.NewsResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NewsService {
    
    private final NewsRepository newsRepository;
    private final RestTemplate restTemplate;
    
    @Value("${serp.api.key:}")
    private String serpApiKey;
    
    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    @ConditionalOnProperty(name = "news.scheduler.enabled", havingValue = "true", matchIfMissing = true)
    @Transactional
    public void collectCryptocurrencyNews() {
        // API 키가 없는 경우 뉴스 수집 스킵
        if (serpApiKey == null || serpApiKey.trim().isEmpty()) {
            log.info("SERP API 키가 설정되지 않아 뉴스 수집을 스킵합니다.");
            return;
        }
        
        log.info("암호화폐 뉴스 수집 시작");
        
        try {
            // 여러 키워드로 뉴스 수집
            collectNewsByKeyword("cryptocurrency bitcoin", News.Category.BITCOIN);
            collectNewsByKeyword("ethereum blockchain", News.Category.ETHEREUM);
            collectNewsByKeyword("crypto market", News.Category.MARKET);
            collectNewsByKeyword("blockchain technology", News.Category.BLOCKCHAIN);
            
            log.info("암호화폐 뉴스 수집 완료");
        } catch (Exception e) {
            log.error("뉴스 수집 중 오류 발생", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void collectNewsByKeyword(String keyword, News.Category category) {
        try {
            String url = String.format(
                "https://serpapi.com/search.json?q=%s&tbm=nws&num=10&api_key=%s",
                keyword.replace(" ", "+"), serpApiKey
            );
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("news_results")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> newsResults = (List<Map<String, Object>>) response.get("news_results");
                
                int newNewsCount = 0;
                for (Map<String, Object> newsItem : newsResults) {
                    String newsUrl = (String) newsItem.get("link");
                    
                    if (newsUrl != null && !newsRepository.existsByUrl(newsUrl)) {
                        News news = News.builder()
                                .title(cleanText((String) newsItem.get("title")))
                                .description(cleanText((String) newsItem.get("snippet")))
                                .url(newsUrl)
                                .imageUrl((String) newsItem.get("thumbnail"))
                                .source(cleanText((String) newsItem.get("source")))
                                .publishedAt(parseDate((String) newsItem.get("date")))
                                .category(category)
                                .viewCount(0)
                                .build();
                                
                        newsRepository.save(news);
                        newNewsCount++;
                        log.debug("새로운 뉴스 저장: {}", news.getTitle());
                    }
                }
                log.info("키워드 '{}': 새로운 뉴스 {}개 수집", keyword, newNewsCount);
            }
        } catch (Exception e) {
            log.error("키워드 '{}' 뉴스 수집 중 오류 발생: {}", keyword, e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public NewsPageResponseDto getLatestNews(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<News> newsPage;
        
        if (category != null && !category.trim().isEmpty()) {
            try {
                News.Category newsCategory = News.Category.valueOf(category.toUpperCase());
                newsPage = newsRepository.findByCategoryOrderByPublishedAtDesc(newsCategory, pageable);
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.INVALID_CATEGORY);
            }
        } else {
            newsPage = newsRepository.findByOrderByPublishedAtDesc(pageable);
        }
        
        List<NewsResponseDto> newsResponseDtos = newsPage.getContent().stream()
                .map(NewsResponseDto::fromWithoutContent)
                .collect(Collectors.toList());
        
        return NewsPageResponseDto.builder()
                .content(newsResponseDtos)
                .pageNumber(newsPage.getNumber())
                .pageSize(newsPage.getSize())
                .totalElements(newsPage.getTotalElements())
                .totalPages(newsPage.getTotalPages())
                .first(newsPage.isFirst())
                .last(newsPage.isLast())
                .hasNext(newsPage.hasNext())
                .hasPrevious(newsPage.hasPrevious())
                .build();
    }
    
    @Transactional
    public NewsResponseDto getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NEWS_NOT_FOUND));
        
        // 조회수 증가
        news.incrementViewCount();
        newsRepository.save(news);
        
        return NewsResponseDto.from(news);
    }
    
    @Transactional(readOnly = true)
    public List<NewsResponseDto> getPopularNews() {
        List<News> popularNews = newsRepository.findTop5ByOrderByViewCountDesc();
        return popularNews.stream()
                .map(NewsResponseDto::fromWithoutContent)
                .collect(Collectors.toList());
    }
    
    @Scheduled(cron = "0 0 15 * * ?") // 매일 오후 3시
    @ConditionalOnProperty(name = "news.scheduler.enabled", havingValue = "true", matchIfMissing = true)
    @Transactional
    public void cleanupOldNews() {
        log.info("오래된 뉴스 정리 시작");
        
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<News> oldNews = newsRepository.findByPublishedAtBefore(thirtyDaysAgo);
            
            if (!oldNews.isEmpty()) {
                newsRepository.deleteByPublishedAtBefore(thirtyDaysAgo);
                log.info("{}개의 오래된 뉴스를 삭제했습니다.", oldNews.size());
            } else {
                log.info("삭제할 오래된 뉴스가 없습니다.");
            }
        } catch (Exception e) {
            log.error("오래된 뉴스 정리 중 오류 발생", e);
        }
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getNewsStatistics() {
        long totalNews = newsRepository.findAll().size();
        long bitcoinNews = newsRepository.countByCategory(News.Category.BITCOIN);
        long ethereumNews = newsRepository.countByCategory(News.Category.ETHEREUM);
        long blockchainNews = newsRepository.countByCategory(News.Category.BLOCKCHAIN);
        long marketNews = newsRepository.countByCategory(News.Category.MARKET);
        
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayNews = newsRepository.findAll().stream()
                .filter(news -> news.getPublishedAt().isAfter(today))
                .count();
        
        return Map.of(
                "totalNews", totalNews,
                "todayNews", todayNews,
                "categoryStats", Map.of(
                        "BITCOIN", bitcoinNews,
                        "ETHEREUM", ethereumNews,
                        "BLOCKCHAIN", blockchainNews,
                        "MARKET", marketNews
                )
        );
    }
    
    private LocalDateTime parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            // 다양한 날짜 형식 시도
            DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                    DateTimeFormatter.ofPattern("MMM dd, yyyy"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDateTime.parse(dateString, formatter);
                } catch (DateTimeParseException e) {
                    // 다음 포맷터 시도
                }
            }
            
            // 모든 파싱 실패시 현재 시간 반환
            log.warn("날짜 파싱 실패: {}. 현재 시간으로 대체합니다.", dateString);
            return LocalDateTime.now();
            
        } catch (Exception e) {
            log.warn("날짜 파싱 중 오류 발생: {}. 현재 시간으로 대체합니다.", dateString);
            return LocalDateTime.now();
        }
    }
    
    private String cleanText(String text) {
        if (text == null) {
            return null;
        }
        
        // HTML 태그 제거 및 텍스트 정리
        return text.replaceAll("<[^>]*>", "")
                   .replaceAll("&nbsp;", " ")
                   .replaceAll("&amp;", "&")
                   .replaceAll("&lt;", "<")
                   .replaceAll("&gt;", ">")
                   .replaceAll("&quot;", "\"")
                   .trim();
    }
}