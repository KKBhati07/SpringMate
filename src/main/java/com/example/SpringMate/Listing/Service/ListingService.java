package com.example.SpringMate.Listing.Service;

import com.example.SpringMate.Listing.DTO.CreateListingRequestDto;
import com.example.SpringMate.Listing.DTO.FetchListingRequestDto;
import com.example.SpringMate.Listing.DTO.FetchListingItemsProjection;
import com.example.SpringMate.Listing.Entity.Category;
import com.example.SpringMate.Listing.Entity.Listing;
import com.example.SpringMate.Listing.Entity.ListingImage;
import com.example.SpringMate.Listing.Repository.CategoryRepository;
import com.example.SpringMate.Listing.Repository.ListingRepository;
import com.example.SpringMate.Location.Service.LocationService;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import com.example.SpringMate.Shared.Service.AwsS3Service;
import com.example.SpringMate.User.Entity.User;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final LocationService locationService;
    private final AwsS3Service awsS3Service;

    public ResponseEntity<Response<PaginatedResponse<FetchListingItemsProjection>>> fetchRecords(
            FetchListingRequestDto queryParams
    ) {

        try {
            Pageable pageable = PageRequest.of(
                    queryParams.getPage(),
                    queryParams.getSize(),
                    Sort.by(Sort.Direction.DESC, "postedAt")
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
            return ResponseEntity.ok(new Response<>(paginatedResponse, "Listings fetched successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(null, "Internal server error"));
        }
    }

    public ResponseEntity<Response<Boolean>> createRecord(
            CreateListingRequestDto requestDto,
            User authenticatedUser
    ) {
        try {
            List<ListingImage> listingImages = new ArrayList<>();

            if(requestDto.getImages() != null){
                for (CreateListingRequestDto.ImageDto imageDto : requestDto.getImages()) {
                    String imgUrl = awsS3Service.uploadImage(Constants.AWS.BUCKET_NAME,
                            AwsS3Directory.LISTINGS, imageDto.getImage());
                    listingImages.add(ListingImage.builder().url(imgUrl)
                            .isCover(imageDto.isCover()).build());
                }
            }
            Category category;
            if(requestDto.getCategoryId() != null){
                Optional<Category> categoryOptional = categoryRepository.findById(requestDto.getCategoryId());
                if(categoryOptional.isEmpty()){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new Response<>(false,"Please select a valid category"));
                }
                category = categoryOptional.get();
            }else{
                category = categoryRepository.findByName(Constants.DEFAULT_CATEGORY)
                        .orElseThrow(() -> new RuntimeException("Default category not found"));

            }

            Listing item = Listing.builder()
                    .price(requestDto.getPrice())
                    .title(requestDto.getTitle())
                    .description(requestDto.getDescription())
                    .seller(authenticatedUser)
                    .location(locationService.getOrCreateOne(
                            requestDto.getCity().trim().toLowerCase(),
                            requestDto.getState().trim().toLowerCase(),
                            requestDto.getCountry().trim().toLowerCase()
                    ))
                    .category(category)
                    .listingImages(listingImages)
                    .build();

            listingRepository.save(item);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Response<>(true,"Item created successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Internal server error"));
        }
    }

}
