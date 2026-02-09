package com.theblood.springfood.client.api;

import com.theblood.springfood.client.api.BaseClient.ServiceClient;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@ServiceClient(value = "categories", path = "/api/categories")
public interface CategoryClient extends BaseClient {
    @ClientMethod(
            httpMethod = "GET",
            path = "",
            grpcMethod = "getAllCategories",
            idempotent = true
    )
    ClientResponse<List<CategoryClient.CategoryDTO>> getAllCategories(ClientRequest<Void> request);

    @ClientMethod(
            httpMethod = "POST",
            path = "",
            grpcMethod = "createCategory",
            idempotent = true
    )
    ClientResponse<CategoryClient.CategoryDTO> createCategory(ClientRequest<CategoryDTO> request);

    @ClientMethod(
            httpMethod = "GET",
            path = "/get-by-code-and-group",
            grpcMethod = "getCategoryByCodeAndGroup",
            idempotent = true
    )
    ClientResponse<CategoryClient.CategoryDTO> getCategoryByCodeAndGroup(ClientRequest<Void> request);

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class CategoryDTO implements Serializable {

        private String id;

        @Size(max = 255)
        private String categoryGroupCode;

        @Size(max = 2000)
        private String description;

        @Size(max = 50)
        private String parentId;

        @Size(max = 255)
        private String tctKCode;

        @Size(max = 255)
        private String categoryCode;

        @Size(max = 255)
        private String categoryName;

        @Size(max = 50)
        private String mappingCategory;

        @Size(max = 50)
        private String manageDepartment;

        @Size(max = 50)
        private String shopId;

        private Integer isLock;

        private Integer isActive;

        private Integer levels;

        private Integer orderNumber;
    }
}
