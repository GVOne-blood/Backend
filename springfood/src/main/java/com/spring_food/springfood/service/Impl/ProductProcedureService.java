package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.dto.request.ProductRequest;
import com.spring_food.springfood.exception.custom.InvalidDataException;
import com.spring_food.springfood.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ProductProcedureService {

    private final SimpleJdbcCall createProductCall;
    private final SimpleJdbcCall updateProductCall;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ProductProcedureService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        
        // Initialize SimpleJdbcCall for create product procedure
        this.createProductCall = new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("sp_create_product")
            .declareParameters(
                new SqlParameter("p_product_id", Types.VARCHAR),
                new SqlParameter("p_shop_id", Types.VARCHAR),
                new SqlParameter("p_name", Types.VARCHAR),
                new SqlParameter("p_sku", Types.VARCHAR),
                new SqlParameter("p_description", Types.VARCHAR),
                new SqlParameter("p_msg", Types.DATE),
                new SqlParameter("p_exp", Types.DATE),
                new SqlParameter("p_product_status", Types.VARCHAR),
                new SqlParameter("p_price", Types.DECIMAL),
                new SqlParameter("p_wholesale_price", Types.DECIMAL),
                new SqlParameter("p_quantity", Types.INTEGER),
                new SqlParameter("p_images", Types.OTHER), // JSONB type
                new SqlParameter("p_category_names", Types.VARCHAR),
                new SqlParameter("p_current_username", Types.VARCHAR),
                new SqlParameter("p_created_at", Types.TIMESTAMP),
                new SqlParameter("p_updated_at", Types.TIMESTAMP),
                new SqlOutParameter("p_result_code", Types.INTEGER),
                new SqlOutParameter("p_result_message", Types.VARCHAR)
            );
        
        // Initialize SimpleJdbcCall for update product procedure
        this.updateProductCall = new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("sp_update_product")
            .declareParameters(
                new SqlParameter("p_product_id", Types.VARCHAR),
                new SqlParameter("p_shop_id", Types.VARCHAR),  // Thêm shop_id để có thể update
                new SqlParameter("p_name", Types.VARCHAR),
                new SqlParameter("p_sku", Types.VARCHAR),
                new SqlParameter("p_description", Types.VARCHAR),
                new SqlParameter("p_msg", Types.DATE),
                new SqlParameter("p_exp", Types.DATE),
                new SqlParameter("p_product_status", Types.VARCHAR),
                new SqlParameter("p_price", Types.DECIMAL),
                new SqlParameter("p_wholesale_price", Types.DECIMAL),
                new SqlParameter("p_quantity", Types.INTEGER),
                new SqlParameter("p_images", Types.OTHER), // JSONB type
                new SqlParameter("p_category_names", Types.VARCHAR),
                new SqlParameter("p_avg_rate", Types.DECIMAL),  // Thêm avg_rate
                new SqlParameter("p_current_username", Types.VARCHAR),
                new SqlParameter("p_updated_at", Types.TIMESTAMP),
                new SqlOutParameter("p_result_code", Types.INTEGER),
                new SqlOutParameter("p_result_message", Types.VARCHAR)
            );
    }

    /**
     * Create product using stored procedure
     */
    @Transactional
    public Product createProductWithProcedure(ProductRequest request) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // Generate new product ID using MongoDB ObjectId
        String productId = new ObjectId().toString();
        
        // Prepare input parameters
        Map<String, Object> params = new HashMap<>();
        params.put("p_product_id", productId);
        params.put("p_shop_id", request.getShopId());
        params.put("p_name", request.getName());
        params.put("p_sku", request.getSku());
        params.put("p_description", request.getDescription());
        params.put("p_msg", request.getMsg() != null ? java.sql.Date.valueOf(request.getMsg()) : null);
        params.put("p_exp", request.getExp() != null ? java.sql.Date.valueOf(request.getExp()) : null);
        params.put("p_product_status", request.getStatus() != null ? request.getStatus().toString() : "ACTIVE");
        // Convert String price to BigDecimal
        params.put("p_price", new BigDecimal(request.getPrice()));
        params.put("p_wholesale_price", new BigDecimal(request.getWholesalePrice()));
        params.put("p_quantity", request.getQuantity() != null ? request.getQuantity() : 0);
        params.put("p_images", request.getImages());
        params.put("p_category_names", request.getCategoryNames());
        params.put("p_current_username", currentUsername);
        params.put("p_created_at", LocalDateTime.now());
        params.put("p_updated_at", LocalDateTime.now());
        
        // Execute stored procedure
        Map<String, Object> result = createProductCall.execute(params);
        
        // Check result
        Integer resultCode = (Integer) result.get("p_result_code");
        String resultMessage = (String) result.get("p_result_message");
        
        log.info("Create product procedure result: code={}, message={}", resultCode, resultMessage);
        
        if (resultCode != 0) {
            throw new InvalidDataException("Failed to create product: " + resultMessage);
        }
        
        // Return created product (you might want to fetch it from DB)
        Product product = new Product();
        product.setId(productId);
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(new BigDecimal(request.getPrice()));
        product.setQuantity(request.getQuantity());
        
        return product;
    }

    /**
     * Update product using stored procedure - CẬP NHẬT ĐẦY ĐỦ TẤT CẢ CÁC TRƯỜNG
     */
    @Transactional
    public Product updateProductWithProcedure(String productId, ProductRequest request) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // Prepare input parameters - Truyền TẤT CẢ các trường có thể update
        Map<String, Object> params = new HashMap<>();
        params.put("p_product_id", productId);
        
        // Foreign key - có thể chuyển product sang shop khác
        params.put("p_shop_id", request.getShopId()); 
        
        // Các trường thông tin cơ bản
        params.put("p_name", request.getName());
        params.put("p_sku", request.getSku());
        params.put("p_description", request.getDescription());
        params.put("p_msg", request.getMsg() != null ? java.sql.Date.valueOf(request.getMsg()) : null);
        params.put("p_exp", request.getExp() != null ? java.sql.Date.valueOf(request.getExp()) : null);
        params.put("p_product_status", request.getStatus() != null ? request.getStatus().toString() : null);
        
        // Các trường về giá và số lượng - Convert String to BigDecimal
        params.put("p_price", request.getPrice() != null ? new BigDecimal(request.getPrice()) : null);
        params.put("p_wholesale_price", request.getWholesalePrice() != null ? new BigDecimal(request.getWholesalePrice()) : null);
        params.put("p_quantity", request.getQuantity());
        
        // JSONB field
        params.put("p_images", request.getImages());
        
        // Many-to-many relationship với categories
        params.put("p_category_names", request.getCategoryNames());
        
        // Rating - có thể null nếu không update
        params.put("p_avg_rate", request.getAvgRate());
        
        // Metadata
        params.put("p_current_username", currentUsername);
        params.put("p_updated_at", LocalDateTime.now());
        
        // Execute stored procedure
        Map<String, Object> result = updateProductCall.execute(params);
        
        // Check result
        Integer resultCode = (Integer) result.get("p_result_code");
        String resultMessage = (String) result.get("p_result_message");
        
        log.info("Update product procedure result: code={}, message={}", resultCode, resultMessage);
        
        if (resultCode != 0) {
            throw new InvalidDataException("Failed to update product: " + resultMessage);
        }
        
        // Return updated product (you might want to fetch it from DB)
        Product product = new Product();
        product.setId(productId);
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice() != null ? new BigDecimal(request.getPrice()) : null);
        product.setQuantity(request.getQuantity());
        
        return product;
    }

    /**
     * Alternative: Create product using CallableStatement
     */
    @Transactional
    public Product createProductManual(ProductRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        String productId = new ObjectId().toString();
        
        // Using CallableStatement for stored procedure with OUT parameters
        String sql = "{CALL sp_create_product(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?)}";
        
        final Integer[] resultCode = {0};
        final String[] resultMessage = {"Success"};
        
        jdbcTemplate.execute(sql, (CallableStatement cs) -> {
            // Set input parameters
            cs.setString(1, productId);
            cs.setString(2, request.getShopId());
            cs.setString(3, request.getName());
            cs.setString(4, request.getSku());
            cs.setString(5, request.getDescription());
            cs.setDate(6, request.getMsg() != null ? java.sql.Date.valueOf(request.getMsg()) : null);
            cs.setDate(7, request.getExp() != null ? java.sql.Date.valueOf(request.getExp()) : null);
            cs.setString(8, request.getStatus() != null ? request.getStatus().toString() : "ACTIVE");
            cs.setBigDecimal(9, new BigDecimal(request.getPrice()));
            cs.setBigDecimal(10, new BigDecimal(request.getWholesalePrice()));
            cs.setInt(11, request.getQuantity() != null ? request.getQuantity() : 0);
            cs.setString(12, request.getImages());
            cs.setString(13, request.getCategoryNames());
            cs.setString(14, currentUsername);
            cs.setTimestamp(15, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            cs.setTimestamp(16, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            
            // Register output parameters
            cs.registerOutParameter(17, Types.INTEGER);
            cs.registerOutParameter(18, Types.VARCHAR);
            
            // Execute
            cs.execute();
            
            // Get output parameters
            resultCode[0] = cs.getInt(17);
            resultMessage[0] = cs.getString(18);
            
            return null;
        });
        
        if (resultCode[0] != 0) {
            throw new InvalidDataException("Failed to create product: " + resultMessage[0]);
        }
        
        Product product = new Product();
        product.setId(productId);
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(new BigDecimal(request.getPrice()));
        product.setQuantity(request.getQuantity());
        
        return product;
    }
}
