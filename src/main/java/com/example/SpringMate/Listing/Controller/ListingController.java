package com.example.SpringMate.Listing.Controller;

import com.example.SpringMate.Listing.DTO.*;
import com.example.SpringMate.Listing.Service.ListingService;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Listing.LISTING_BASE)
public class ListingController {

    private final ListingService listingService;

    @GetMapping(Urls.Listing.FETCH)
    public ResponseEntity<Response<PaginatedResponse<FetchListingItemsResponseDto>>>
    fetchAll(
            @RequestParam(value = "category_id", required = false) Long categoryId,
            @RequestParam(value = "min_price", required = false) Double minPrice,
            @RequestParam(value = "max_price", required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User autheticatedUser
    ) {
        return ResponseEntity.ok(
                new Response<>(listingService.fetchRecords(
                        new FetchListingsRequestDto(categoryId,minPrice,maxPrice,page,size),
                        autheticatedUser
                ),
                        "Listings fetched successfully"));
    }

    @PostMapping(value = Urls.Listing.CREATE_LISTING,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<Boolean>>
    createListing(@Valid
                  @ModelAttribute CreateListingRequestDto requestDto,
                  @AuthenticationPrincipal User authenticatedUser
    ) {
        listingService.createRecord(requestDto, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response<>(null,
                        "Listing created successfully"));
    }

    @DeleteMapping(Urls.Listing.DELETE_LISTING)
    public ResponseEntity<Response<Boolean>>
    deleteListing(@PathVariable Long id,
                  @AuthenticationPrincipal User authenticatedUser
    ) {
        listingService.deleteRecord(id, authenticatedUser);
        return ResponseEntity.ok(new Response<>(null,
                "Item deleted successfully"));
    }

    @GetMapping(Urls.Listing.GET_DETAILS)
    // directly returning entities can cause infinite recursion while serialization
    // due to back ref Listing -> Images -> Listing
    public ResponseEntity<Response<ListingResponseDto>>
    fetchOne(@PathVariable Long id) {
        return ResponseEntity.ok(new Response<>(
                listingService.getOne(id),
                "Item fetched successfully"));
    }
}
