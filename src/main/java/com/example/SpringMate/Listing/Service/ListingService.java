package com.example.SpringMate.Listing.Service;

import com.example.SpringMate.Listing.DTO.CreateListingRequestDto;
import com.example.SpringMate.Listing.DTO.FetchListingsRequestDto;
import com.example.SpringMate.Listing.DTO.FetchListingItemsProjection;
import com.example.SpringMate.Listing.DTO.ListingResponseDto;
import com.example.SpringMate.Listing.Entity.Category;
import com.example.SpringMate.Listing.Entity.Listing;
import com.example.SpringMate.Listing.Entity.ListingImage;
import com.example.SpringMate.Listing.Repository.CategoryRepository;
import com.example.SpringMate.Listing.Repository.ListingRepository;
import com.example.SpringMate.Location.Service.LocationService;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import com.example.SpringMate.Shared.Exception.BadRequestException;
import com.example.SpringMate.Shared.Exception.ForbiddenException;
import com.example.SpringMate.Shared.Exception.NotFoundException;
import com.example.SpringMate.Shared.Service.AwsS3Service;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.ResponseMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final ResponseMapper responseMapper;

    public PaginatedResponse<FetchListingItemsProjection> fetchRecords(
            FetchListingsRequestDto queryParams
    ) {
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
        return new PaginatedResponse<>(pagedRecords.getContent(),
                pagedRecords.getNumber(),
                pagedRecords.getTotalElements(),
                pagedRecords.getTotalPages());
    }

    public void createRecord(
            CreateListingRequestDto requestDto,
            User authenticatedUser
    ) {
        List<ListingImage> listingImages = new ArrayList<>();

        if (requestDto.getImages() != null) {
            for (CreateListingRequestDto.ImageDto imageDto : requestDto.getImages()) {
                String imgUrl = awsS3Service.uploadImage(Constants.AWS.BUCKET_NAME,
                        AwsS3Directory.LISTINGS, imageDto.getImage());
                listingImages.add(ListingImage.builder().url(imgUrl)
                        .isCover(imageDto.isCover()).build());
            }
        }
        Category category;
        if (requestDto.getCategoryId() != null) {
            Optional<Category> categoryOptional = categoryRepository.findById(requestDto.getCategoryId());
            if (categoryOptional.isEmpty()) {
                throw new BadRequestException("Please select a valid category");
            }
            category = categoryOptional.get();
        } else {
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
    }

    @Transactional
    public void deleteRecord(
            Long itemId,
            User authenticatedUser
    ) {

        Optional<Listing> listingOptional = listingRepository.findByIdAndDeletedFalse(itemId);
        if (listingOptional.isEmpty()) {
            throw new NotFoundException("Listing not found");
        }

        if (authenticatedUser.isAdmin()) {
            listingRepository.softDeleteById(itemId);
        } else {
            Listing listing = listingOptional.get();
            if (listing.getSeller().getUuid()
                    .equals(authenticatedUser.getUuid())) {
                listingRepository.softDeleteById(itemId);
            } else {
                throw new ForbiddenException();
            }
        }
    }

    public ListingResponseDto getOne(Long itemId) {
        Optional<Listing> listingOptional = listingRepository
                .findWithRelationsByIdAndDeletedFalse(itemId);
        return listingOptional.map(responseMapper::mapListing)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
    }

    public boolean softDeleteByUserId(Long userId) {
        this.listingRepository.softDeleteByUserId(userId);
        return true;
    }


}
