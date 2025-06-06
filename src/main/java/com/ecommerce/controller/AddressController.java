package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.service.interfaces.AddressService;
import com.ecommerce.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Address>>> getCurrentUserAddresses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<List<Address>> errorResponse = ApiResponse.error(
                "User not found", 
                HttpStatus.NOT_FOUND.value(), 
                (Class<List<Address>>) (Class<?>) List.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        List<Address> addresses = addressService.getAddressesByUserId(userOpt.get().getId());
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Address>> getAddressById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<Address> errorResponse = ApiResponse.error(
                "User not found", 
                HttpStatus.NOT_FOUND.value(), 
                Address.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        Optional<Address> addressOpt = addressService.getAddressById(id);
        if (addressOpt.isEmpty()) {
            ApiResponse<Address> errorResponse = ApiResponse.error(
                "Address not found with id: " + id, 
                HttpStatus.NOT_FOUND.value(), 
                Address.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Check if the address belongs to the current user
        Address address = addressOpt.get();
        User currentUser = userOpt.get();
        if (!address.getUser().getId().equals(currentUser.getId()) && 
                !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            ApiResponse<Address> errorResponse = ApiResponse.error(
                "You don't have permission to view this address", 
                HttpStatus.FORBIDDEN.value(), 
                Address.class);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Address>> createAddress(@Valid @RequestBody Address address) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<Address> errorResponse = ApiResponse.error(
                "User not found", 
                HttpStatus.NOT_FOUND.value(), 
                Address.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        address.setUser(userOpt.get());
        Address createdAddress = addressService.createAddress(address);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdAddress, "Address created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Address>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody Address address) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<Address> errorResponse = ApiResponse.error(
                "User not found", 
                HttpStatus.NOT_FOUND.value(), 
                Address.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        Optional<Address> addressOpt = addressService.getAddressById(id);
        if (addressOpt.isEmpty()) {
            ApiResponse<Address> errorResponse = ApiResponse.error(
                "Address not found with id: " + id, 
                HttpStatus.NOT_FOUND.value(), 
                Address.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Check if the address belongs to the current user
        User currentUser = userOpt.get();
        if (!addressOpt.get().getUser().getId().equals(currentUser.getId()) && 
                !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            ApiResponse<Address> errorResponse = ApiResponse.error(
                "You don't have permission to update this address", 
                HttpStatus.FORBIDDEN.value(), 
                Address.class);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        address.setId(id);
        address.setUser(currentUser);
        Address updatedAddress = addressService.updateAddress(id, address);
        return ResponseEntity.ok(ApiResponse.success(updatedAddress, "Address updated successfully"));
    }

    @PutMapping("/{id}/set-default")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value()));
        }
        
        Optional<Address> addressOpt = addressService.getAddressById(id);
        if (addressOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Address not found with id: " + id, HttpStatus.NOT_FOUND.value()));
        }
        
        // Check if the address belongs to the current user
        User currentUser = userOpt.get();
        if (!addressOpt.get().getUser().getId().equals(currentUser.getId()) && 
                !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You don't have permission to set this address as default", HttpStatus.FORBIDDEN.value()));
        }
        
        addressService.setDefaultAddress(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Default address set successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value()));
        }
        
        Optional<Address> addressOpt = addressService.getAddressById(id);
        if (addressOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Address not found with id: " + id, HttpStatus.NOT_FOUND.value()));
        }
        
        // Check if the address belongs to the current user
        User currentUser = userOpt.get();
        if (!addressOpt.get().getUser().getId().equals(currentUser.getId()) && 
                !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You don't have permission to delete this address", HttpStatus.FORBIDDEN.value()));
        }
        
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully"));
    }
}
