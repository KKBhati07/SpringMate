package com.example.SpringMate.Listing.Controller;

import com.example.SpringMate.Listing.DTO.*;
import com.example.SpringMate.Listing.Service.ListingService;
import com.example.SpringMate.Shared.Exception.BadRequestException;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Util.AuthenticatedUser;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing product listings.
 * Handles creation, retrieval, update, deletion, and image uploads.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Listing.BASE)
public class ListingController {

    private final ListingService listingService;

    @GetMapping(Urls.Listing.GET_ALL)
    public ResponseEntity<Response<PaginatedResponse<FetchListingItemsResponseDto>>>
    getAll(
            @RequestParam(value = "category_id", required = false) Long categoryId,
            @RequestParam(value = "min_price", required = false) Double minPrice,
            @RequestParam(value = "max_price", required = false) Double maxPrice,
            @RequestParam(value = "country_id", required = false) Long countryId,
            @RequestParam(value = "state_id", required = false) Long stateId,
            @RequestParam(value = "city_id", required = false) Long cityId,
            @RequestParam(value = "search", required = false) String searchString,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(
                Response.success(listingService.getAllRecords(
                        new FetchListingsRequestDto(
                                categoryId, minPrice, maxPrice,
                                countryId, stateId, cityId,
                                searchString,
                                page, size),
                        authenticatedUser,
                        false
                ),
                        "Listings fetched successfully"));
    }

    @GetMapping(Urls.Listing.GET_BY_USER)
    public ResponseEntity<Response<PaginatedResponse<FetchListingItemsResponseDto>>>
    getByUser(
            @RequestParam(value = "user") UUID uuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                Response.success(listingService.getRecordsByUser(
                        uuid,
                        page,
                        size,
                        false
                ),
                        "Listings fetched successfully"));
    }

    @GetMapping(Urls.Listing.GET_FAVORITES)
    public ResponseEntity<Response<PaginatedResponse<FetchListingItemsResponseDto>>>
    getFavoritesByUser(
            @RequestParam(value = "user") UUID uuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (uuid == null) {
            throw new BadRequestException("Invalid params!");
        }
        return ResponseEntity.ok(
                Response.success(listingService.getRecordsByUser(
                        uuid,
                        page,
                        size,
                        true
                ),
                        "Listings fetched successfully"));
    }

    @PostMapping(value = Urls.Listing.CREATE)
    public ResponseEntity<Response<CreateListingResponseDto>>
    createListing(@Valid
                  @RequestBody CreateListingRequestDto requestDto,
                  @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info(
                "Creating listing user=[ UUID {}]",
                authenticatedUser.uuid()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(listingService.createRecord(requestDto, authenticatedUser),
                        "Listing created successfully"));
    }

    /**
     * Uploads listing images using a multipart fallback flow.
     * This endpoint exists to support image upload if primary
     * image upload mechanism fails.
     */
    @PatchMapping(
            value = Urls.Listing.IMAGE_UPLOAD_FALLBACK,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> fallbackImageUpload(
            @Valid @ModelAttribute FallbackImageUploadDto requestDto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info(
                "Fallback image upload listing=[ID {}] user=[UUID {}]",
                requestDto.getListingId(),
                authenticatedUser.uuid()
        );
        listingService.uploadListingImages(requestDto, authenticatedUser);

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping(Urls.Listing.DELETE)
    public ResponseEntity<Void>
    deleteListing(@PathVariable Long id,
                  @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info(
                "Deleting listing=[ID {}] user=[UUID {}]",
                id,
                authenticatedUser.uuid()
        );
        listingService.deleteRecord(id, authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(Urls.Listing.GET_DETAILS)
    // directly returning entities can cause infinite recursion while serialization
    // due to back ref Listing -> Images -> Listing
    public ResponseEntity<Response<ListingResponseDto>>
    fetchOne(@PathVariable Long id) {
        return ResponseEntity.ok(Response.success(
                listingService.getOne(id),
                "Item fetched successfully"));
    }
}
