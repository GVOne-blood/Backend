//package com.viettel.dvs.client.example;
//
//import api.com.theblood.springfood.client.ClientRequest;
//import api.com.theblood.springfood.client.ClientResponse;
//import com.viettel.dvs.client.api.PartyMemberClient;
//import autoconf.com.theblood.springfood.client.ClientInjectionBeanPostProcessor.InjectClient;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.util.UUID;
//
/// **
// * Comprehensive examples demonstrating how to use PartyMemberClient with both REST and gRPC protocols.
// * This class shows proper client injection, request building, response handling, and error management.
// *
// * The client supports both REST (Feign) and gRPC protocols, configured via client-config.yml.
// * Protocol selection is managed automatically by the client infrastructure.
// */
//@Component
//public class PartyMemberClientExample implements CommandLineRunner {
//
//    private static final Logger logger = LoggerFactory.getLogger(PartyMemberClientExample.class);
//
//    // Inject PartyMemberClient using the @InjectClient annotation
//    // The client will be automatically configured based on client-config.yml settings
//    @InjectClient
//    private PartyMemberClient partyMemberClient;
//
//    // Alternative: Specify protocol explicitly (optional)
////     @InjectClient(protocol = "rest")
////     private PartyMemberClient partyMemberClient;
//
//    // @InjectClient(protocol = "grpc")
//    // private PartyMemberClient grpcPartyMemberClient;
//
//    @Override
//    public void run(String... args) throws Exception {
//        logger.info("Starting PartyMemberClient examples...");
//        logger.info("Client info: Service={}, Protocol={}, Healthy={}",
//            partyMemberClient.getServiceName(),
//            partyMemberClient.getProtocol(),
//            partyMemberClient.isHealthy());
//
//        try {
//            // Run all examples in sequence
//            demonstrateCreatePartyMember();
//            demonstrateGetPartyMember();
//            demonstrateUpdatePartyMember();
//            demonstrateListPartyMembers();
//            demonstrateDeletePartyMember();
//
//        } catch (Exception e) {
//            logger.error("Error running examples", e);
//        }
//
//        logger.info("PartyMemberClient examples completed.");
//    }
//
//    /**
//     * Example 1: Create a new party member
//     * Demonstrates POST request with comprehensive party member data
//     */
//    private void demonstrateCreatePartyMember() {
//        logger.info("=== Example 1: Create Party Member ===");
//
//        try {
//            // Build a complete PartyMemberDto with sample data
//            PartyMemberClient.PartyMemberDto newMember = PartyMemberClient.PartyMemberDto.builder()
//                .isActive(1)
//                .ssoUserId("user_" + System.currentTimeMillis())
//                .partyUnitRegisterId("UNIT_001")
//                .partyUnitRegisterName("Party Unit Registration Office")
//                .manageOfficialOrgId("ORG_001")
//                .profileNumber("PM" + String.format("%06d", System.currentTimeMillis() % 1000000))
//                .cardNumber("PC" + String.format("%05d", System.currentTimeMillis() % 100000))
//                .currentFullName("Nguyen Van Example")
//                .birthName("Nguyen Van Example")
//                .gender(1) // Male
//                .identifyNo("123456789" + (System.currentTimeMillis() % 100))
//                .identifyDate(Instant.now().minusSeconds(365 * 24 * 3600 * 20)) // 20 years ago
//                .identifyPlace("Ha Noi, Viet Nam")
//                .birthDate(Instant.now().minusSeconds(365 * 24 * 3600 * 30)) // 30 years ago
//                .birthAddress("123 Example Street, Example Ward")
//                .birthWardId("WARD_001")
//                .birthDistrictId("DISTRICT_001")
//                .birthProvinceId("PROVINCE_001")
//                .hometownAddress("456 Hometown Street, Hometown Ward")
//                .hometownWardId("WARD_002")
//                .hometownDistrictId("DISTRICT_002")
//                .hometownProvinceId("PROVINCE_002")
//                .residentAddress("789 Current Address, Current Ward")
//                .residentNationId("NATION_VN")
//                .residentWardId("WARD_003")
//                .residentDistrictId("DISTRICT_003")
//                .residentProvinceId("PROVINCE_003")
//                .ethnicityId("KINH")
//                .religionId("NONE")
//                .officialJoinPartyDate(Instant.now().minusSeconds(365 * 24 * 3600 * 5)) // 5 years ago
//                .officialJoinPartyPlace("Party Committee Example District")
//                .officialRecognitionDate(Instant.now().minusSeconds(365 * 24 * 3600 * 4)) // 4 years ago
//                .officialRecognitionPlace("Party Committee Example District")
//                .officialStatus(1) // Active
//                .participationStatus(1) // Participating
//                .saveStatus(1) // Saved
//                .email("example@example.com")
//                .phoneNumber("0987654321")
//                .build();
//
//            // Create the request with headers and metadata
//            ClientRequest<PartyMemberClient.PartyMemberDto> request = ClientRequest.<PartyMemberClient.PartyMemberDto>builder()
//                .body(newMember)
//                .build()
//                .addHeader("Content-Type", "application/json")
//                .addHeader("X-Request-ID", UUID.randomUUID().toString());
//
//            // Make the API call
//            ClientResponse<PartyMemberClient.PartyMemberDto> response = partyMemberClient.createPartyMember(request);
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info(" Party member created successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//
//                PartyMemberClient.PartyMemberDto createdMember = response.getBody();
//                if (createdMember != null) {
//                    logger.info("Created member ID: {}", createdMember.getId());
//                    logger.info("Created member name: {}", createdMember.getCurrentFullName());
//                    logger.info("Created member card number: {}", createdMember.getCardNumber());
//                }
//            } else {
//                logger.error("L Failed to create party member");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during create party member", e);
//        }
//    }
//
//    /**
//     * Example 2: Get a party member by ID
//     * Demonstrates GET request with path parameter
//     */
//    private void demonstrateGetPartyMember() {
//        logger.info("=== Example 2: Get Party Member ===");
//
//        try {
//            // Use a sample member ID (in real scenario, use actual ID from create response)
//            String memberId = "sample-member-id-123";
//
//            // Make the API call - note that ID is passed as a method parameter
//            ClientResponse<PartyMemberClient.PartyMemberDto> response = partyMemberClient.getPartyMember(memberId);
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info(" Party member retrieved successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//
//                PartyMemberClient.PartyMemberDto member = response.getBody();
//                if (member != null) {
//                    logger.info("Member ID: {}", member.getId());
//                    logger.info("Member name: {}", member.getCurrentFullName());
//                    logger.info("Member email: {}", member.getEmail());
//                    logger.info("Member phone: {}", member.getPhoneNumber());
//                    logger.info("Member status: {}", member.getOfficialStatus());
//                    logger.info("Join party date: {}", member.getOfficialJoinPartyDate());
//                } else {
//                    logger.warn("Response body is null");
//                }
//            } else {
//                logger.error("L Failed to get party member");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//
//                // Handle specific error cases
//                switch (response.getStatusCode()) {
//                    case 404:
//                        logger.error("Party member with ID {} not found", memberId);
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
//            logger.error("Exception during get party member", e);
//        }
//    }
//
//    /**
//     * Example 3: Update a party member
//     * Demonstrates PUT request with path parameter and request body
//     */
//    private void demonstrateUpdatePartyMember() {
//        logger.info("=== Example 3: Update Party Member ===");
//
//        try {
//            String memberId = "sample-member-id-123";
//
//            // Create update request with new data
//            PartyMemberClient.UpdatePartyMemberRequest updateData =
//                new PartyMemberClient.UpdatePartyMemberRequest(
//                    "Nguyen Van Updated", // name
//                    "updated@example.com", // email
//                    "0123456789", // phoneNumber
//                    "Information Technology", // department
//                    "Senior Developer", // position
//                    "active" // status
//                );
//
//            // Build the request with headers
//            ClientRequest<PartyMemberClient.UpdatePartyMemberRequest> request =
//                ClientRequest.<PartyMemberClient.UpdatePartyMemberRequest>builder()
//                    .body(updateData)
//                    .build()
//                    .addHeader("Content-Type", "application/json")
//                    .addHeader("X-Request-ID", UUID.randomUUID().toString())
//                    .addHeader("X-User-ID", "admin-user-123");
//
//            // Make the API call
//            ClientResponse<PartyMemberClient.PartyMemberDto> response =
//                partyMemberClient.updatePartyMember(memberId, request);
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info(" Party member updated successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//
//                PartyMemberClient.PartyMemberDto updatedMember = response.getBody();
//                if (updatedMember != null) {
//                    logger.info("Updated member ID: {}", updatedMember.getId());
//                    logger.info("Updated member name: {}", updatedMember.getCurrentFullName());
//                    logger.info("Updated member email: {}", updatedMember.getEmail());
//                }
//            } else {
//                logger.error("L Failed to update party member");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during update party member", e);
//        }
//    }
//
//    /**
//     * Example 4: List party members with filtering and pagination
//     * Demonstrates GET request with query parameters
//     */
//    private void demonstrateListPartyMembers() {
//        logger.info("=== Example 4: List Party Members ===");
//
//        try {
//            // Build query with filtering and pagination parameters
//            PartyMemberClient.PartyMemberQuery query = PartyMemberClient.PartyMemberQuery.builder()
//                .name("Nguyen") // Filter by name pattern
//                .department("Information Technology") // Filter by department
//                .status("active") // Filter by status
//                .page(0) // First page (0-based)
//                .size(10) // 10 items per page
//                .sortBy("currentFullName") // Sort by name
//                .sortDirection("asc") // Ascending order
//                .build();
//
//            // Create the request with query parameters
//            ClientRequest<PartyMemberClient.PartyMemberQuery> request =
//                ClientRequest.<PartyMemberClient.PartyMemberQuery>builder()
//                    .body(query)
//                    .build()
//                    .addHeader("Accept", "application/json")
//                    .addQueryParam("includeInactive", "false"); // Additional query param
//
//            // Make the API call
//            ClientResponse<PartyMemberClient.PartyMemberListResponse> response =
//                partyMemberClient.listPartyMembers(request);
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info(" Party members listed successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//
//                PartyMemberClient.PartyMemberListResponse listResponse = response.getBody();
//                if (listResponse != null) {
//                    logger.info("Total elements: {}", listResponse.totalElements());
//                    logger.info("Total pages: {}", listResponse.totalPages());
//                    logger.info("Current page size: {}", listResponse.data() != null ? listResponse.data().size() : 0);
//
//                    if (listResponse.data() != null) {
//                        logger.info("Members in current page:");
//                        for (int i = 0; i < listResponse.data().size(); i++) {
//                            PartyMemberClient.PartyMemberDto member = listResponse.data().get(i);
//                            logger.info("  {}. {} (ID: {}) - {}",
//                                i + 1,
//                                member.getCurrentFullName(),
//                                member.getId(),
//                                member.getEmail());
//                        }
//                    }
//                } else {
//                    logger.warn("Response body is null");
//                }
//            } else {
//                logger.error("L Failed to list party members");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during list party members", e);
//        }
//    }
//
//    /**
//     * Example 5: Delete a party member
//     * Demonstrates DELETE request with path parameter
//     */
//    private void demonstrateDeletePartyMember() {
//        logger.info("=== Example 5: Delete Party Member ===");
//
//        try {
//            String memberId = "beb49b91-2e7b-4ee3-8c4f-b2c72c755b34";
//
//            // Make the API call - DELETE operations typically don't have request body
//            ClientResponse<Void> response = partyMemberClient.deletePartyMember(memberId);
//
//            // Handle response
//            if (response.isSuccess()) {
//                logger.info(" Party member deleted successfully!");
//                logger.info("Response status: {}", response.getStatusCode());
//                logger.info("Response time: {}ms", response.getResponseTime());
//            } else {
//                logger.error("L Failed to delete party member");
//                logger.error("Status code: {}", response.getStatusCode());
//                logger.error("Error message: {}", response.getErrorMessage());
//
//                // Handle specific error cases for delete
//                switch (response.getStatusCode()) {
//                    case 404:
//                        logger.error("Party member with ID {} not found", memberId);
//                        break;
//                    case 403:
//                        logger.error("Access denied - insufficient permissions to delete");
//                        break;
//                    case 409:
//                        logger.error("Conflict - party member cannot be deleted (may have dependencies)");
//                        break;
//                    default:
//                        logger.error("Unexpected error occurred during deletion");
//                }
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception during delete party member", e);
//        }
//    }
//
//    /**
//     * Utility method demonstrating error handling best practices
//     */
//    private void handleClientResponse(ClientResponse<?> response, String operation) {
//        if (response.isSuccess()) {
//            logger.info(" {} completed successfully in {}ms",
//                operation, response.getResponseTime());
//        } else {
//            logger.error("L {} failed with status {} in {}ms: {}",
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
//     * Example demonstrating how to create a minimal party member for testing
//     */
//    private PartyMemberClient.PartyMemberDto createMinimalPartyMember() {
//        return PartyMemberClient.PartyMemberDto.builder()
//            .isActive(1)
//            .manageOfficialOrgId("ORG_TEST")
//            .profileNumber("TEST" + System.currentTimeMillis())
//            .cardNumber("TC" + (System.currentTimeMillis() % 100000))
//            .currentFullName("Test Member")
//            .birthName("Test Member")
//            .gender(1)
//            .identifyNo("TEST" + System.currentTimeMillis())
//            .identifyDate(Instant.now())
//            .identifyPlace("Test Place")
//            .birthDate(Instant.now().minusSeconds(365 * 24 * 3600 * 25))
//            .birthAddress("Test Birth Address")
//            .birthWardId("TEST_WARD")
//            .birthDistrictId("TEST_DISTRICT")
//            .birthProvinceId("TEST_PROVINCE")
//            .hometownAddress("Test Hometown")
//            .hometownWardId("TEST_HOMETOWN_WARD")
//            .hometownDistrictId("TEST_HOMETOWN_DISTRICT")
//            .hometownProvinceId("TEST_HOMETOWN_PROVINCE")
//            .residentAddress("Test Resident Address")
//            .residentNationId("VN")
//            .residentWardId("TEST_RESIDENT_WARD")
//            .residentDistrictId("TEST_RESIDENT_DISTRICT")
//            .residentProvinceId("TEST_RESIDENT_PROVINCE")
//            .ethnicityId("KINH")
//            .religionId("NONE")
//            .officialJoinPartyDate(Instant.now())
//            .officialJoinPartyPlace("Test Party Place")
//            .officialRecognitionPlace("Test Recognition Place")
//            .saveStatus(1)
//            .build();
//    }
//}
