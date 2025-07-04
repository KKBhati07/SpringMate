package com.example.SpringMate.Listing.Controller;

import com.example.SpringMate.Listing.DTO.FetchListingItemsProjection;
import com.example.SpringMate.Listing.DTO.FetchListingRequestDto;
import com.example.SpringMate.Listing.Service.ListingService;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            ){
        return  listingService.fetchRecords(queryParams);
    }
}
