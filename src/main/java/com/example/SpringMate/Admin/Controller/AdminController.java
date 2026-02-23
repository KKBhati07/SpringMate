package com.example.SpringMate.Admin.Controller;

import com.example.SpringMate.Admin.DTO.DeleteListingRequestDto;
import com.example.SpringMate.Listing.DTO.FetchListingItemsResponseDto;
import com.example.SpringMate.Listing.DTO.FetchListingsRequestDto;
import com.example.SpringMate.Listing.Service.ListingService;
import com.example.SpringMate.User.DTO.UpdateUserRequestDto;
import com.example.SpringMate.User.DTO.UpdateUserResponseDto;
import com.example.SpringMate.Util.AuthenticatedUser;
import com.example.SpringMate.Util.UserDetailsDto;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.User.Service.UserService;
import com.example.SpringMate.Shared.Urls;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for administrative operations.
 * Handles user management, listing moderation, and other admin-only actions.
 * All endpoints require ADMIN role authorization.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Admin.BASE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

//          **Using Lombok annotation (@Slf4j) to do the same**
//    Logger log = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final ListingService listingService;

    //    ---- USER ROUTES
    @GetMapping(value = Urls.Admin.User.GET_ALL)
    public ResponseEntity<Response<PaginatedResponse<UserDetailsDto>>> fetchAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info(
                "ADMIN_ACTION action=FETCH_USERS user=[ UUID : {}] page={} size={}",
                authenticatedUser.uuid(),
                page,
                size
        );
        return ResponseEntity.ok(
                Response.success(userService.fetchAll(page, size),
                        "Users fetched successfully")
        );
    }

    @DeleteMapping(value = Urls.Admin.User.DELETE)
    public ResponseEntity<Void>
    deleteUser(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.warn(
                "ADMIN_ACTION action=DELETE_USER user=[ UUID : {}] targetUser=user=[ UUID : {}]",
                authenticatedUser.uuid(),
                uuid
        );

        userService.deleteUser(uuid, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = Urls.Admin.User.RESTORE)
    public ResponseEntity<Void> restoreUser(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.warn(
                "ADMIN_ACTION action=RESTORE_USER user=[ UUID : {}] targetUser=user=[ UUID : {}]",
                authenticatedUser.uuid(),
                uuid
        );
        userService.restoreUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = Urls.Admin.User.UPDATE)
    public ResponseEntity<Response<UpdateUserResponseDto>>
    updateUser(
            @RequestBody UpdateUserRequestDto updatedUser,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info(
                "ADMIN_ACTION action=UPDATE_USER user=[ UUID : {}] targetUser=user=[ UUID : {}]",
                authenticatedUser.uuid(),
                updatedUser.getUuid()
        );
        log.info("[ UPDATED DATA ] : {}", updatedUser);
        return ResponseEntity.ok(Response.success(userService.updateUser(updatedUser),
                "User updated successfully"));
    }

    //    ------- LISTING ROUTES

    /**
     * Retrieves all listings with advanced filtering options.
     * Supports category, price range, location, free-text search,
     * and optionally includes soft-deleted listings for moderation.
     */
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
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info(
                "ADMIN_ACTION action=FETCH_LISTINGS user=[ UUID : {}] page={} size={} deleted={} search={}",
                authenticatedUser.uuid(),
                page,
                size,
                deleted,
                searchString
        );

        return ResponseEntity.ok(
                Response.success(listingService.getAllRecords(
                        new FetchListingsRequestDto(
                                categoryId, minPrice, maxPrice,
                                countryId, stateId, cityId,
                                searchString,
                                page, size),
                        authenticatedUser,
                        deleted != null && deleted
                ),
                        "Listings fetched successfully"));
    }

    @DeleteMapping(value = Urls.Admin.Listing.DELETE)
    public ResponseEntity<Void> deleteListings(
            @Valid @RequestBody DeleteListingRequestDto dto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.warn(
                "ADMIN_ACTION action=DELETE_LISTING user=[ UUID : {}] listingCount=[ {} ]",
                authenticatedUser.uuid(),
                dto.getIds().size()
        );
        listingService.deleteRecords(dto.getIds(), authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}
