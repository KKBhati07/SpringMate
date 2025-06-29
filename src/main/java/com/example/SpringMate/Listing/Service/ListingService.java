package com.example.SpringMate.Listing.Service;

import com.example.SpringMate.Listing.DTO.FetchListingQueryParams;
import com.example.SpringMate.Listing.DTO.ListingItemsProjection;
import com.example.SpringMate.Listing.Repository.ListingRepository;
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

    public ResponseEntity<Response> fetchRecords(
            FetchListingQueryParams queryParams
    ){

        try{
            Pageable pageable = PageRequest.of(
                    queryParams.getPage(),
                    queryParams.getSize(),
                    Sort.by(Sort.Direction.DESC,"postedAt")
            );

            Page<ListingItemsProjection> pagedRecords = listingRepository
                    .findAllByFilters(queryParams.getCategoryId(),
                            queryParams.getMinPrice(),
                            queryParams.getMaxPrice(),
                            pageable);

            Map<String , Object> map = new HashMap<>();
            map.put("listings", pagedRecords.getContent());
            map.put("currentPage", pagedRecords.getNumber());
            map.put("totalItems",pagedRecords.getTotalElements());
            map.put("totalPages",pagedRecords.getTotalPages());
            return ResponseEntity.ok(new Response(map,"Listings fetched successfully"));

        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(new HashMap<>(),"Internal server error"));

        }


    }


}
