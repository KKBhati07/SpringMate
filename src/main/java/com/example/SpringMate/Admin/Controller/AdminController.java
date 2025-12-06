package com.example.SpringMate.Admin.Controller;

import com.example.SpringMate.Admin.DTO.DeleteListingRequestDto;
import com.example.SpringMate.Listing.DTO.FetchListingItemsResponseDto;
import com.example.SpringMate.Listing.DTO.FetchListingsRequestDto;
import com.example.SpringMate.Listing.Service.ListingService;
import com.example.SpringMate.User.DTO.UpdateUserRequestDto;
import com.example.SpringMate.User.DTO.UpdateUserResponseDto;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Util.UserDetailsDto;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.User.Service.UserService;
import com.example.SpringMate.Shared.Urls;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Admin.ADMIN_BASE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ListingService listingService;

//    ---- USER ROUTES
    @GetMapping(value = Urls.Admin.User.GET_ALL)
    public ResponseEntity<Response<PaginatedResponse<UserDetailsDto>>> fetchAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                new Response<>(userService.fetchAll(page, size),
                "Users fetched successfully")
        );
    }

    @DeleteMapping(value = Urls.Admin.User.DELETE)
    public ResponseEntity<Void>
    deleteUser(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        userService.deleteUser(uuid, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = Urls.Admin.User.RESTORE)
    public ResponseEntity<Void> restoreUser(@PathVariable UUID uuid) {
        userService.restoreUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(
            value = Urls.Admin.User.UPDATE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<UpdateUserResponseDto>>
    updateUser(@ModelAttribute UpdateUserRequestDto updatedUser) {
        return ResponseEntity.ok(new Response<>(userService.updateUser(updatedUser),
                "User updated successfully"));
    }

//    ------- LISTING ROUTES
    @GetMapping(value = Urls.Admin.Listing.GET_ALL)
    public ResponseEntity<Response<PaginatedResponse<FetchListingItemsResponseDto>>>
    getAllListings(
            @RequestParam(value = "category_id", required = false) Long categoryId,
            @RequestParam(value = "min_price", required = false) Double minPrice,
            @RequestParam(value = "max_price", required = false) Double maxPrice,
            @RequestParam(value = "country_id", required = false) Long countryId,
            @RequestParam(value = "state_id", required = false) Long stateId,
            @RequestParam(value = "city_id", required = false) Long cityId,
            @RequestParam(value = "search", required = false) String searchString,
            @RequestParam(value = "deleted", required = false) Boolean deleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User autheticatedUser
    ) {
        return ResponseEntity.ok(
                new Response<>(listingService.getAllRecords(
                        new FetchListingsRequestDto(
                                categoryId, minPrice, maxPrice,
                                countryId,stateId,cityId,
                                searchString,
                                page, size),
                        autheticatedUser,
                        deleted != null && deleted
                ),
                        "Listings fetched successfully"));
    }

    @DeleteMapping(value = Urls.Admin.Listing.DELETE)
    public ResponseEntity<Void> deleteListings(
            @Valid @RequestBody DeleteListingRequestDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        listingService.deleteRecords(dto.getIds(), authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}
