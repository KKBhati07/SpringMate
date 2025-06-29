package com.example.SpringMate.Listing.Controller;

import com.example.SpringMate.Listing.DTO.FetchListingQueryParams;
import com.example.SpringMate.Listing.Service.ListingService;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Util.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Listing.LISTING_BASE)
public class ListingController {

    private final ListingService listingService;

    @GetMapping(Urls.Listing.FETCH)
    public ResponseEntity<Response> fetchAll(
            @ModelAttribute FetchListingQueryParams queryParams
            ){
        return  listingService.fetchRecords(queryParams);
    }
}
