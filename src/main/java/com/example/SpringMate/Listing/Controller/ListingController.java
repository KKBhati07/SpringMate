package com.example.SpringMate.Listing.Controller;

import com.example.SpringMate.Listing.DTO.CreateListingRequestDto;
import com.example.SpringMate.Listing.DTO.FetchListingItemsProjection;
import com.example.SpringMate.Listing.DTO.FetchListingRequestDto;
import com.example.SpringMate.Listing.Service.ListingService;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Listing.LISTING_BASE)
public class ListingController {

    private final ListingService listingService;

    @GetMapping(Urls.Listing.FETCH)
    public ResponseEntity<Response<PaginatedResponse<FetchListingItemsProjection>>>
    fetchAll(
            @ModelAttribute FetchListingRequestDto queryParams
    ) {
        return listingService.fetchRecords(queryParams);
    }

    @PostMapping(Urls.Listing.CREATE_LISTING)
    public ResponseEntity<Response<Boolean>>
    createListing(@Valid
                  @RequestBody CreateListingRequestDto requestDto,
                  @AuthenticationPrincipal User authenticatedUser
    ) {
        System.out.println("Method Reached here!!");
        return listingService.createRecord(requestDto, authenticatedUser);
    }
}
