package com.example.SpringMate.Listing.Service;

import com.example.SpringMate.Listing.DTO.FetchListingRequestDto;
import com.example.SpringMate.Listing.DTO.FetchListingItemsProjection;
import com.example.SpringMate.Listing.Repository.ListingRepository;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;

    public ResponseEntity<Response<PaginatedResponse<FetchListingItemsProjection>>> fetchRecords(
            FetchListingRequestDto queryParams
    ){

        try{
            Pageable pageable = PageRequest.of(
                    queryParams.getPage(),
                    queryParams.getSize(),
                    Sort.by(Sort.Direction.DESC,"postedAt")
            );

            Page<FetchListingItemsProjection> pagedRecords = listingRepository
                    .findAllByFilters(queryParams.getCategoryId(),
                            queryParams.getMinPrice(),
                            queryParams.getMaxPrice(),
                            pageable);

            PaginatedResponse<FetchListingItemsProjection> paginatedResponse =
                    new PaginatedResponse<>(pagedRecords.getContent(),
                            pagedRecords.getNumber(),
                            pagedRecords.getTotalElements(),
                            pagedRecords.getTotalPages());
            return ResponseEntity.ok(new Response<>(paginatedResponse,"Listings fetched successfully"));

        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(null,"Internal server error"));
        }
    }


}
