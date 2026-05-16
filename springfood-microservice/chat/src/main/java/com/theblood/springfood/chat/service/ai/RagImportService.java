package com.theblood.springfood.chat.service.ai;

import com.theblood.springfood.chat.domain.KnowledgeDocument;
import com.theblood.springfood.chat.repository.KnowledgeDocumentRepository;
import com.theblood.springfood.chat.service.rag.DocumentIngestionService;
import com.theblood.springfood.common.dto.response.ProductDetail;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagImportService {

    private static final Logger log = LoggerFactory.getLogger(RagImportService.class);

    private final RestTemplate restTemplate;
    private final DocumentIngestionService documentIngestionService;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    public RagImportService(
        RestTemplate restTemplate,
        DocumentIngestionService documentIngestionService,
        KnowledgeDocumentRepository knowledgeDocumentRepository
    ) {
        this.restTemplate = restTemplate;
        this.documentIngestionService = documentIngestionService;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
    }

    @PostConstruct
    public void autoSyncOnStartup() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                boolean hasProducts = !knowledgeDocumentRepository
                    .findBySourceType(KnowledgeDocument.DocumentSourceType.PRODUCT).isEmpty();
                boolean hasShops = !knowledgeDocumentRepository
                    .findBySourceType(KnowledgeDocument.DocumentSourceType.SHOP).isEmpty();

                if (!hasProducts) {
                    log.info("Knowledge base has no products. Starting auto-sync...");
                    importAllProducts();
                } else {
                    log.info("Knowledge base already has products, skipping product sync");
                }

                if (!hasShops) {
                    log.info("Knowledge base has no shops. Starting shop sync...");
                    importAllShops();
                } else {
                    log.info("Knowledge base already has shops, skipping shop sync");
                }
            } catch (Exception e) {
                log.error("Auto-sync failed: {}", e.getMessage());
            }
        }, "rag-auto-sync").start();
    }

    @Transactional
    public int importAllProducts() {
        log.info("Starting product import to knowledge base...");

        List<ProductDetail> products = fetchAllProducts();
        log.info("Fetched {} products from product-service", products.size());

        int totalChunks = 0;
        for (ProductDetail product : products) {
            try {
                String content = buildProductContent(product);
                Map<String, Object> metadata = buildProductMetadata(product);

                int chunks = documentIngestionService.ingestTextDocument(content, metadata);
                totalChunks += chunks;
                log.debug("Imported product {} ({}): {} chunks", product.getName(), product.getId(), chunks);
            } catch (Exception e) {
                log.error("Failed to import product {}: {}", product.getId(), e.getMessage());
            }
        }

        log.info("Product import complete: {} products, {} total chunks", products.size(), totalChunks);
        return totalChunks;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<ProductDetail> fetchAllProducts() {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://product-service/products/?size=10000",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body == null) return List.of();

            Map<String, Object> data = (Map<String, Object>) body.get("data");
            if (data == null) return List.of();

            Object content = data.get("content");
            if (content instanceof List) {
                List<Map<String, Object>> rawList = (List<Map<String, Object>>) content;
                return rawList.stream().map(this::mapToProductDetail).toList();
            }
            return List.of();
        } catch (Exception e) {
            log.error("Failed to fetch products from product-service: {}", e.getMessage());
            return List.of();
        }
    }

    private ProductDetail mapToProductDetail(Map<String, Object> map) {
        ProductDetail p = new ProductDetail();
        if (map.get("id") != null) {
            p.setId(java.util.UUID.fromString(map.get("id").toString()));
        }
        p.setName((String) map.get("name"));
        p.setDescription((String) map.get("description"));
        if (map.get("price") != null) {
            p.setPrice(new java.math.BigDecimal(map.get("price").toString()));
        }
        if (map.get("quantity") != null) {
            p.setQuantity(((Number) map.get("quantity")).intValue());
        }
        if (map.get("averageRating") != null) {
            p.setAverageRating(((Number) map.get("averageRating")).doubleValue());
        }
        if (map.get("totalFeedbacks") != null) {
            p.setTotalFeedbacks(((Number) map.get("totalFeedbacks")).longValue());
        }
        return p;
    }

    private String buildProductContent(ProductDetail product) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tên sản phẩm: ").append(product.getName()).append("\n");
        sb.append("Mô tả: ").append(product.getDescription() != null ? product.getDescription() : "Không có mô tả").append("\n");
        sb.append("Giá: ").append(String.format("%,.0f", product.getPrice())).append(" VNĐ\n");
        sb.append("Số lượng tồn kho: ").append(product.getQuantity()).append("\n");
        if (product.getAverageRating() != null && product.getAverageRating() > 0) {
            sb.append("Đánh giá trung bình: ").append(String.format("%.1f", product.getAverageRating())).append("/5 sao\n");
        }
        if (product.getTotalFeedbacks() != null) {
            sb.append("Số lượng đánh giá: ").append(product.getTotalFeedbacks()).append("\n");
        }
        return sb.toString();
    }

    private Map<String, Object> buildProductMetadata(ProductDetail product) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source_type", "PRODUCT");
        metadata.put("source_id", product.getId().toString());
        metadata.put("title", product.getName());
        metadata.put("price", product.getPrice());
        metadata.put("quantity", product.getQuantity());
        if (product.getAverageRating() != null) {
            metadata.put("average_rating", product.getAverageRating());
        }
        return metadata;
    }

    @Transactional
    public int importAllShops() {
        log.info("Starting shop import to knowledge base...");

        List<Map<String, Object>> shops = fetchAllShops();
        log.info("Fetched {} shops from shop-service", shops.size());

        int totalChunks = 0;
        for (Map<String, Object> shop : shops) {
            try {
                String content = buildShopContent(shop);
                Map<String, Object> metadata = buildShopMetadata(shop);

                int chunks = documentIngestionService.ingestTextDocument(content, metadata);
                totalChunks += chunks;
                log.debug("Imported shop {}: {} chunks", shop.get("shopName"), chunks);
            } catch (Exception e) {
                log.error("Failed to import shop {}: {}", shop.get("shopId"), e.getMessage());
            }
        }

        log.info("Shop import complete: {} shops, {} total chunks", shops.size(), totalChunks);
        return totalChunks;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Map<String, Object>> fetchAllShops() {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://shop-service/shop/?size=10000",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body == null) return List.of();

            Map<String, Object> data = (Map<String, Object>) body.get("data");
            if (data == null) return List.of();

            Object content = data.get("content");
            if (content instanceof List) {
                return (List<Map<String, Object>>) content;
            }
            return List.of();
        } catch (Exception e) {
            log.error("Failed to fetch shops from shop-service: {}", e.getMessage());
            return List.of();
        }
    }

    private String buildShopContent(Map<String, Object> shop) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tên cửa hàng: ").append(shop.getOrDefault("shopName", "")).append("\n");
        sb.append("Giới thiệu: ").append(shop.getOrDefault("introduction", "Không có giới thiệu")).append("\n");
        if (shop.get("totalProducts") != null) {
            sb.append("Số lượng sản phẩm: ").append(shop.get("totalProducts")).append("\n");
        }
        if (shop.get("totalSold") != null) {
            sb.append("Số lượng đã bán: ").append(shop.get("totalSold")).append("\n");
        }
        return sb.toString();
    }

    private Map<String, Object> buildShopMetadata(Map<String, Object> shop) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source_type", "SHOP");
        metadata.put("source_id", shop.get("shopId") != null ? shop.get("shopId").toString() : "unknown");
        metadata.put("title", shop.getOrDefault("shopName", "Unknown Shop"));
        if (shop.get("totalProducts") != null) metadata.put("total_products", shop.get("totalProducts"));
        if (shop.get("totalSold") != null) metadata.put("total_sold", shop.get("totalSold"));
        return metadata;
    }
}
