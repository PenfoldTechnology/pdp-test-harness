package uk.org.ca.stub.simulator.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.org.ca.stub.simulator.entity.RegisteredResource;
import uk.org.ca.stub.simulator.repository.ResourceRepository;
import uk.org.ca.stub.simulator.rest.exception.NotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Helper Controller for CAS Stub
 * 
 * Provides additional endpoints for testing purposes that are not part of the standard CAS API.
 * These endpoints expose internal data that is useful for test scenarios but should not be
 * available in production environments.
 */
@RestController
@RequestMapping("/test-helpers")
public class TestHelperController {

    private final ResourceRepository resourceRepository;

    public TestHelperController(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    /**
     * GET /test-helpers/rpt-token/{resourceId}
     * 
     * Returns the RPT token for a given resource ID.
     * This is a test helper endpoint that exposes the internally generated RPT tokens
     * that are stored in the database when resources are registered.
     * 
     * @param resourceId The UUID of the resource to get the RPT token for
     * @return The RPT token and additional resource information
     */
    @GetMapping("/rpt-token/{resourceId}")
    public ResponseEntity<Map<String, Object>> getRptToken(@PathVariable String resourceId) {
        
        RegisteredResource resource = resourceRepository.findByResourceId(resourceId)
                .orElseThrow(() -> new NotFoundException("Resource not found with ID: " + resourceId));

        Map<String, Object> response = new HashMap<>();
        response.put("resourceId", resource.getResourceId());
        response.put("rptToken", resource.getRpt());
        response.put("name", resource.getName());
        response.put("description", resource.getDescription());
        response.put("matchStatus", resource.getMatchStatus());
        response.put("resourceScopes", resource.getResourceScopes());
        response.put("patToken", resource.getPat());
        response.put("createdAt", resource.getCreatedAt());
        response.put("updatedAt", resource.getUpdatedAt());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /test-helpers/resources
     * 
     * Returns all registered resources with their RPT tokens.
     * Useful for debugging and understanding what resources exist in the database.
     * 
     * @return List of all resources with their details including RPT tokens
     */
    @GetMapping("/resources")
    public ResponseEntity<Map<String, Object>> getAllResources() {
        
        var allResources = resourceRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", allResources.size());
        response.put("resources", allResources.stream().map(resource -> {
            Map<String, Object> resourceInfo = new HashMap<>();
            resourceInfo.put("resourceId", resource.getResourceId());
            resourceInfo.put("rptToken", resource.getRpt());
            resourceInfo.put("name", resource.getName());
            resourceInfo.put("description", resource.getDescription());
            resourceInfo.put("matchStatus", resource.getMatchStatus());
            resourceInfo.put("resourceScopes", resource.getResourceScopes());
            resourceInfo.put("friendlyName", resource.getFriendlyName());
            resourceInfo.put("createdAt", resource.getCreatedAt());
            resourceInfo.put("updatedAt", resource.getUpdatedAt());
            return resourceInfo;
        }).toList());

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /test-helpers/resources
     * 
     * Deletes all dynamically created resources (not the default pre-seeded ones).
     * Useful for cleaning up test data between test runs.
     * 
     * @return Number of resources deleted
     */
    @DeleteMapping("/resources")
    public ResponseEntity<Map<String, Object>> deleteAllDynamicResources() {
        
        // Get all resources that don't have a friendlyName (which indicates they're default seeded resources)
        var dynamicResources = resourceRepository.findAll().stream()
                .filter(resource -> resource.getFriendlyName() == null || resource.getFriendlyName().isBlank())
                .toList();
        
        int deletedCount = dynamicResources.size();
        resourceRepository.deleteAll(dynamicResources);
        
        Map<String, Object> response = new HashMap<>();
        response.put("deletedCount", deletedCount);
        response.put("message", "Deleted all dynamically created resources");
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /test-helpers/health
     * 
     * Simple health check endpoint that returns the service status.
     * Useful for monitoring and ensuring the CAS stub is running correctly.
     * 
     * @return Health status information
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Consent & Authorisation Stub");
        response.put("timestamp", java.time.Instant.now().toString());
        
        // Check database connectivity by counting resources
        try {
            long resourceCount = resourceRepository.count();
            response.put("database", "UP");
            response.put("resourceCount", resourceCount);
        } catch (Exception e) {
            response.put("database", "DOWN");
            response.put("databaseError", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
} 