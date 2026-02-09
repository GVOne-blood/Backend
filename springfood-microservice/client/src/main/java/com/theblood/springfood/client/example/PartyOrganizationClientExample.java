//package com.viettel.dvs.client.example;
//
//import api.com.theblood.springfood.client.ClientRequest;
//import api.com.theblood.springfood.client.ClientResponse;
//import com.viettel.dvs.client.api.PartyOrganizationClient;
//import autoconf.com.theblood.springfood.client.ClientInjectionBeanPostProcessor.InjectClient;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.UUID;
//
/// **
// * Comprehensive examples demonstrating how to use PartyOrganizationClient with both REST and gRPC protocols.
// * This class shows proper client injection, request building, response handling, and error management.
// *
// * The client supports both REST (Feign) and gRPC protocols, configured via client-config.yml.
// * Protocol selection is managed automatically by the client infrastructure.
// */
//@Component
//public class PartyOrganizationClientExample implements CommandLineRunner {
//
//    private static final Logger logger = LoggerFactory.getLogger(PartyOrganizationClientExample.class);
//
//    // Inject PartyOrganizationClient using the @InjectClient annotation
//    // The client will be automatically configured based on client-config.yml settings
//    @InjectClient
//    private PartyOrganizationClient partyOrganizationClient;
//
//    // Alternative: Specify protocol explicitly (optional)
//    // @InjectClient(protocol = "rest")
//    // private PartyOrganizationClient restPartyOrganizationClient;
//
//    // @InjectClient(protocol = "grpc")
//    // private PartyOrganizationClient grpcPartyOrganizationClient;
//
//    @Override
//    public void run(String... args) throws Exception {
//        logger.info("Starting PartyOrganizationClient examples...");
//        logger.info("Client info: Service={}, Protocol={}, Healthy={}",
//            partyOrganizationClient.getServiceName(),
//            partyOrganizationClient.getProtocol(),
//            partyOrganizationClient.isHealthy());
//
//        try {
//            // Run all examples in sequence
//            demonstrateCreateOrganization();
//            demonstrateGetOrganization();
//            demonstrateUpdateOrganization();
//            demonstratePartialUpdateOrganization();
//            demonstrateListOrganizations();
//            demonstrateImportOrganizations();
//            demonstrateExportOrganizations();
//            demonstrateDeleteOrganization();
//
//        } catch (Exception e) {
//            logger.error("Error running examples", e);
//        }
//
//        logger.info("PartyOrganizationClient examples completed.");
//    }
//
//    /**
//     * Example 1: Create a new organization
//     * Demonstrates POST request with comprehensive organization data
//     */
//    private void demonstrateCreateOrganization() {
//        logger.info("=== Example 1: Create Organization ===");
//
//        try {
//            // Build a complete OrganizationDto with sample data
//            PartyOrganizationClient.OrganizationDto newOrganization = PartyOrganizationClient.OrganizationDto.builder()
//                .organizationCode("ORG" + System.currentTimeMillis())
//                .organizationName("Example Organization " + System.currentTimeMillis())
//                .organizationGroupCode("TYPE_001")
//                .organizationFieldId("FIELD_001")
/// /                .effectiveDate("20240101")
/// /                .expiredDate("20251231")
//                .organizationStatus(1) // Active
//                .organizationOrder(100L)
//                .organizationParentId("PARENT_001")
//                .organizationPath("/ROOT/PARENT_001/")
//                .organizationCountryId("VN")
//                .organizationAddress("123 Example Street, Example City")
//                .description("This is an example organization created for testing purposes")
//                .decisionId("DECISION_001")
//                .organizationType(1)
//                .formationType(1)
//                .managerId("MANAGER_001")
//                .build();
//
//            // Create the request with headers and metadata
//            ClientRequest<PartyOrganizationClient.OrganizationDto> request = ClientRequest.<PartyOrganizationClient.OrganizationDto>builder()
//                .body(newOrganization)
//                .build()
//                .addHeader("Content-Type", "application/json")
//                .addHeader("X-Request-ID", UUID.randomUUID().toString());
//
//            // Make the API call
//            ClientResponse<PartyOrganizationClient.OrganizationDto> response = partyOrganizationClient.createOrganization(request);
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info("✓ Organization created successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//
//                PartyOrganizationClient.OrganizationDto createdOrganization = response.getBody();
//                if (createdOrganization != null) {
//                    logger.info("Created organization ID: {}", createdOrganization.getId());
//                    logger.info("Created organization code: {}", createdOrganization.getOrganizationCode());
//                    logger.info("Created organization name: {}", createdOrganization.getOrganizationName());
//                }
//            } else {
//                logger.error("✗ Failed to create organization");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during create organization", e);
//        }
//    }
//
//    /**
//     * Example 2: Get an organization by ID
//     * Demonstrates GET request with path parameter
//     */
//    private void demonstrateGetOrganization() {
//        logger.info("=== Example 2: Get Organization ===");
//
//        try {
//            // Use a sample organization ID (in real scenario, use actual ID from create response)
//            String shopId = "sample-organization-id-123";
//
//            // Make the API call - note that ID is passed as a method parameter
//            ClientResponse<PartyOrganizationClient.OrganizationDto> response = partyOrganizationClient.getOrganization(shopId);
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info("✓ Organization retrieved successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//
//                PartyOrganizationClient.OrganizationDto organization = response.getBody();
//                if (organization != null) {
//                    logger.info("Organization ID: {}", organization.getId());
//                    logger.info("Organization code: {}", organization.getOrganizationCode());
//                    logger.info("Organization name: {}", organization.getOrganizationName());
//                    logger.info("Organization type: {}", organization.getOrganizationGroupCode());
//                    logger.info("Organization status: {}", organization.getOrganizationStatus());
//                    logger.info("Organization address: {}", organization.getOrganizationAddress());
//                } else {
//                    logger.warn("Response body is null");
//                }
//            } else {
//                logger.error("✗ Failed to get organization");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//
//                // Handle specific error cases
//                switch (response.getStatusCode()) {
//                    case 404:
//                        logger.error("Organization with ID {} not found", shopId);
//                        break;
//                    case 403:
//                        logger.error("Access denied - insufficient permissions");
//                        break;
//                    case 500:
//                        logger.error("Internal server error - please try again later");
//                        break;
//                    default:
//                        logger.error("Unexpected error occurred");
//                }
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during get organization", e);
//        }
//    }
//
//    /**
//     * Example 3: Update an organization (full update)
//     * Demonstrates PUT request with path parameter and request body
//     */
//    private void demonstrateUpdateOrganization() {
//        logger.info("=== Example 3: Update Organization ===");
//
//        try {
//            String shopId = "sample-organization-id-123";
//
//            // Create updated organization data
//            PartyOrganizationClient.OrganizationDto updatedOrganization = PartyOrganizationClient.OrganizationDto.builder()
//                .id(shopId)
//                .organizationCode("ORG_UPDATED")
//                .organizationName("Updated Organization Name")
//                .organizationGroupCode("TYPE_002")
//                .organizationFieldId("FIELD_002")
////                .effectiveDate("20240201")
////                .expiredDate("20261231")
//                .organizationStatus(1)
//                .organizationOrder(200L)
//                .organizationParentId("PARENT_002")
//                .organizationPath("/ROOT/PARENT_002/")
//                .organizationCountryId("VN")
//                .organizationAddress("456 Updated Street, Updated City")
//                .description("This organization has been updated")
//                .decisionId("DECISION_002")
//                .organizationType(2)
//                .formationType(2)
//                .managerId("MANAGER_002")
//                .build();
//
//            // Build the request with headers
//            ClientRequest<PartyOrganizationClient.OrganizationDto> request =
//                ClientRequest.<PartyOrganizationClient.OrganizationDto>builder()
//                    .body(updatedOrganization)
//                    .build()
//                    .addHeader("Content-Type", "application/json")
//                    .addHeader("X-Request-ID", UUID.randomUUID().toString())
//                    .addHeader("X-User-ID", "admin-user-123");
//
//            // Make the API call
//            ClientResponse<PartyOrganizationClient.OrganizationDto> response =
//                partyOrganizationClient.updateOrganization(shopId, request);
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info("✓ Organization updated successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//
//                PartyOrganizationClient.OrganizationDto updatedOrg = response.getBody();
//                if (updatedOrg != null) {
//                    logger.info("Updated organization ID: {}", updatedOrg.getId());
//                    logger.info("Updated organization name: {}", updatedOrg.getOrganizationName());
//                    logger.info("Updated organization address: {}", updatedOrg.getOrganizationAddress());
//                }
//            } else {
//                logger.error("✗ Failed to update organization");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during update organization", e);
//        }
//    }
//
//    /**
//     * Example 4: Partial update of an organization
//     * Demonstrates PATCH request with path parameter and partial data
//     */
//    private void demonstratePartialUpdateOrganization() {
//        logger.info("=== Example 4: Partial Update Organization ===");
//
//        try {
//            String shopId = "sample-organization-id-123";
//
//            // Create partial update data - only fields that need to be updated
//            PartyOrganizationClient.OrganizationDto partialUpdate = PartyOrganizationClient.OrganizationDto.builder()
//                .id(shopId)
//                .organizationName("Partially Updated Organization Name")
//                .organizationAddress("789 Partial Update Street")
//                .description("This organization has been partially updated")
//                .organizationStatus(1)
//                .build();
//
//            // Build the request
//            ClientRequest<PartyOrganizationClient.OrganizationDto> request =
//                ClientRequest.<PartyOrganizationClient.OrganizationDto>builder()
//                    .body(partialUpdate)
//                    .build()
//                    .addHeader("Content-Type", "application/merge-patch+json")
//                    .addHeader("X-Request-ID", UUID.randomUUID().toString());
//
//            // Make the API call
//            ClientResponse<PartyOrganizationClient.OrganizationDto> response =
//                partyOrganizationClient.partialUpdateOrganization(shopId, request);
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info("✓ Organization partially updated successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//
//                PartyOrganizationClient.OrganizationDto updatedOrg = response.getBody();
//                if (updatedOrg != null) {
//                    logger.info("Updated organization name: {}", updatedOrg.getOrganizationName());
//                    logger.info("Updated organization address: {}", updatedOrg.getOrganizationAddress());
//                }
//            } else {
//                logger.error("✗ Failed to partially update organization");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during partial update organization", e);
//        }
//    }
//
//    /**
//     * Example 5: List organizations with filtering and pagination
//     * Demonstrates GET request with query parameters
//     */
//    private void demonstrateListOrganizations() {
//        logger.info("=== Example 5: List Organizations ===");
//
//        try {
//            // Build query with filtering and pagination parameters
//            PartyOrganizationClient.OrganizationQuery query = PartyOrganizationClient.OrganizationQuery.builder()
//                .organizationCode("ORG") // Filter by code pattern
//                .organizationName("Example") // Filter by name pattern
//                .organizationGroupCode("TYPE_001") // Filter by type
//                .organizationStatus(1) // Filter by active status
//                .page(0) // First page (0-based)
//                .size(10) // 10 items per page
//                .sortBy("organizationName") // Sort by name
//                .sortDirection("asc") // Ascending order
//                .build();
//
//            // Create the request with query parameters
//            ClientRequest<PartyOrganizationClient.OrganizationQuery> request =
//                ClientRequest.<PartyOrganizationClient.OrganizationQuery>builder()
//                    .body(query)
//                    .build()
//                    .addHeader("Accept", "application/json")
//                    .addQueryParam("includeInactive", "false"); // Additional query param
//
//            // Make the API call
//            ClientResponse<PartyOrganizationClient.OrganizationListResponse> response =
//                partyOrganizationClient.listOrganizations(request);
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info("✓ Organizations listed successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//
//                PartyOrganizationClient.OrganizationListResponse listResponse = response.getBody();
//                if (listResponse != null) {
//                    logger.info("Total elements: {}", listResponse.totalElements());
//                    logger.info("Total pages: {}", listResponse.totalPages());
//                    logger.info("Current page size: {}", listResponse.data() != null ? listResponse.data().size() : 0);
//
//                    if (listResponse.data() != null) {
//                        logger.info("Organizations in current page:");
//                        for (int i = 0; i < listResponse.data().size(); i++) {
//                            PartyOrganizationClient.OrganizationDto org = listResponse.data().get(i);
//                            logger.info("  {}. {} (Code: {}, ID: {})",
//                                i + 1,
//                                org.getOrganizationName(),
//                                org.getOrganizationCode(),
//                                org.getId());
//                        }
//                    }
//                } else {
//                    logger.warn("Response body is null");
//                }
//            } else {
//                logger.error("✗ Failed to list organizations");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during list organizations", e);
//        }
//    }
//
//    /**
//     * Example 6: Import organizations from file
//     * Demonstrates POST request with file URL parameter
//     */
//    private void demonstrateImportOrganizations() {
//        logger.info("=== Example 6: Import Organizations ===");
//
//        try {
//            // Create import request with file URL
//            PartyOrganizationClient.ImportOrganizationRequest importRequest =
//                new PartyOrganizationClient.ImportOrganizationRequest(
//                    "https://example.com/organizations.csv" // File URL
//                );
//
//            // Build the request
//            ClientRequest<PartyOrganizationClient.ImportOrganizationRequest> request =
//                ClientRequest.<PartyOrganizationClient.ImportOrganizationRequest>builder()
//                    .body(importRequest)
//                    .build()
//                    .addHeader("Content-Type", "application/json")
//                    .addHeader("X-Request-ID", UUID.randomUUID().toString())
//                    .addHeader("X-Import-Source", "external-system");
//
//            // Make the API call
//            ClientResponse<Void> response = partyOrganizationClient.importOrganizations(request);
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info("✓ Organizations import initiated successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//            } else {
//                logger.error("✗ Failed to import organizations");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during import organizations", e);
//        }
//    }
//
//    /**
//     * Example 7: Export organizations
//     * Demonstrates POST request for export operation
//     */
//    private void demonstrateExportOrganizations() {
//        logger.info("=== Example 7: Export Organizations ===");
//
//        try {
//            // Make the API call - no request body needed for export
//            ClientResponse<PartyOrganizationClient.CarboneResponseData> response =
//                partyOrganizationClient.exportOrganizations();
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info("✓ Organizations export completed successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//
//                PartyOrganizationClient.CarboneResponseData exportData = response.getBody();
//                if (exportData != null) {
//                    logger.info("Export success: {}", exportData.getSuccess());
//                    logger.info("Render ID: {}", exportData.getRenderId());
//                    logger.info("Download URL: {}", exportData.getDownloadUrl());
//                    if (exportData.getError() != null) {
//                        logger.warn("Export error: {}", exportData.getError());
//                    }
//                } else {
//                    logger.warn("Response body is null");
//                }
//            } else {
//                logger.error("✗ Failed to export organizations");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during export organizations", e);
//        }
//    }
//
//    /**
//     * Example 8: Delete an organization
//     * Demonstrates DELETE request with path parameter
//     */
//    private void demonstrateDeleteOrganization() {
//        logger.info("=== Example 8: Delete Organization ===");
//
//        try {
//            String shopId = "sample-organization-id-to-delete";
//
//            // Make the API call - DELETE operations typically don't have request body
//            ClientResponse<Void> response = partyOrganizationClient.deleteOrganization(shopId);
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info("✓ Organization deleted successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//            } else {
//                logger.error("✗ Failed to delete organization");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//
//                // Handle specific error cases for delete
//                switch (response.getStatusCode()) {
//                    case 404:
//                        logger.error("Organization with ID {} not found", shopId);
//                        break;
//                    case 403:
//                        logger.error("Access denied - insufficient permissions to delete");
//                        break;
//                    case 409:
//                        logger.error("Conflict - organization cannot be deleted (may have dependencies)");
//                        break;
//                    default:
//                        logger.error("Unexpected error occurred during deletion");
//                }
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during delete organization", e);
//        }
//    }
//
//    /**
//     * Utility method demonstrating error handling best practices
//     */
//    private void handleClientResponse(ClientResponse<?> response, String operation) {
//        if (response.isSuccess()) {
//            logger.info("✓ {} completed successfully in {}ms",
//                operation, response.getResponseTime());
//        } else {
//            logger.error("✗ {} failed with status {} in {}ms: {}",
//                operation, response.getStatusCode(), response.getResponseTime(), response.getErrorMessage());
//
//            // Log response headers for debugging
//            if (!response.getHeaders().isEmpty()) {
//                logger.debug("Response headers: {}", response.getHeaders());
//            }
//        }
//    }
//
//    /**
//     * Example demonstrating how to create a minimal organization for testing
//     */
//    private PartyOrganizationClient.OrganizationDto createMinimalOrganization() {
//        return PartyOrganizationClient.OrganizationDto.builder()
//            .organizationCode("TEST" + System.currentTimeMillis())
//            .organizationName("Test Organization")
//            .organizationGroupCode("TYPE_TEST")
//            .organizationFieldId("FIELD_TEST")
////            .effectiveDate("20240101")
//            .organizationStatus(1)
//            .organizationOrder(1L)
//            .organizationCountryId("VN")
//            .organizationAddress("Test Address")
//            .description("Test organization for validation")
//            .organizationType(1)
//            .formationType(1)
//            .build();
//    }
//}
