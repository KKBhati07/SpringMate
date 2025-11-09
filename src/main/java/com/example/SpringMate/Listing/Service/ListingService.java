package com.example.SpringMate.Listing.Service;

import com.example.SpringMate.Listing.DTO.*;
import com.example.SpringMate.Listing.Entity.Category;
import com.example.SpringMate.Listing.Entity.Listing;
import com.example.SpringMate.Listing.Entity.ListingImage;
import com.example.SpringMate.Listing.Repository.CategoryRepository;
import com.example.SpringMate.Listing.Repository.ListingImageRepository;
import com.example.SpringMate.Listing.Repository.ListingRepository;
import com.example.SpringMate.Location.Service.LocationService;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import com.example.SpringMate.Shared.Exception.BadRequestException;
import com.example.SpringMate.Shared.Exception.ForbiddenException;
import com.example.SpringMate.Shared.Exception.NotFoundException;
import com.example.SpringMate.Shared.Service.AwsS3Service;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.User.Service.CoreUserService;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final CategoryRepository categoryRepository;
    private final LocationService locationService;
    private final CoreUserService coreUserService;
    private final AwsS3Service awsS3Service;
    private final ResponseMapper responseMapper;

    public PaginatedResponse<FetchListingItemsResponseDto> getAllRecords(
            FetchListingsRequestDto queryParams,
            User authenticatedUser
    ) {
        Pageable pageable = PageRequest.of(
                queryParams.getPage(),
                queryParams.getSize(),
                Sort.by(Sort.Direction.DESC, "postedAt")
        );

        Page<FetchListingItemsProjection> pagedRecords = listingRepository
                .findAllByFilters(
                        authenticatedUser == null ? null : authenticatedUser.getId(),
                        queryParams.getCategoryId(),
                        queryParams.getMinPrice(),
                        queryParams.getMaxPrice(),
                        pageable);
        return new PaginatedResponse<>(pagedRecords.getContent()
                .stream()
                .map(this::injectPreSignedUrl)
                .toList(),

                pagedRecords.getNumber(),
                pagedRecords.getTotalElements(),
                pagedRecords.getTotalPages());
    }

    public PaginatedResponse<FetchListingItemsResponseDto> getRecordsByUser(
            UUID uuid,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "postedAt")
        );

        Page<FetchListingItemsProjection> pagedRecords = listingRepository
                .findAllByUser(
                        coreUserService.getUserOrThrowByUUID(uuid).getId(),
                        pageable);
        return new PaginatedResponse<>(pagedRecords.getContent()
                .stream()
                .map(this::injectPreSignedUrl)
                .toList(),
                pagedRecords.getNumber(),
                pagedRecords.getTotalElements(),
                pagedRecords.getTotalPages());
    }

    @Transactional
    public void createRecord(
            CreateListingRequestDto requestDto,
            User authenticatedUser
    ) {
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
                        requestDto.getCityId(),
                        requestDto.getStateId(),
                        requestDto.getCountryId()
                ))
                .category(category)
                .build();

        Listing savedItem = listingRepository.save(item);
        List<ListingImage> listingImages = new ArrayList<>();
        if (requestDto.getImages() != null) {
            for (CreateListingRequestDto.ImageDto imageDto : requestDto.getImages()) {
                String imgUrl = awsS3Service.uploadImage(Constants.AWS.BUCKET_NAME,
                        AwsS3Directory.LISTINGS, imageDto.getImage());
                listingImages.add(ListingImage.builder().url(imgUrl)
                        .listing(savedItem)
                        .isCover(imageDto.isCover())
                        .build());
            }
        }
        listingImageRepository.saveAll(listingImages);
    }

    @Transactional
    public void deleteRecord(
            Long itemId,
            User authenticatedUser
    ) {

        Listing listing = getByIdOrThrow(itemId);

        if (authenticatedUser.isAdmin()) {
            listingRepository.softDeleteById(itemId);
        } else {
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

    public void softDeleteByUserId(Long userId) {
        this.listingRepository.softDeleteByUserId(userId);
    }


    public Listing getByIdOrThrow(Long id) {
        return listingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
    }

    private FetchListingItemsResponseDto injectPreSignedUrl(FetchListingItemsProjection record) {

        LocationDto locationDTO = null;

        if (record.getLocation() != null) {
            var loc = record.getLocation();

            locationDTO = LocationDto.builder()
                    .city(LocationDto.CityDto.builder()
                            .id(loc.getCity().getId())
                            .name(loc.getCity().getName())
                            .build())
                    .state(LocationDto.StateDto.builder()
                            .id(loc.getState().getId())
                            .name(loc.getState().getName())
                            .build())
                    .country(LocationDto.CountryDto.builder()
                            .id(loc.getCountry().getId())
                            .name(loc.getCountry().getName())
                            .build())
                    .build();
        }

        return FetchListingItemsResponseDto.builder()
                .id(record.getId())
                .title(record.getTitle())
                .description(record.getDescription())
                .price(record.getPrice())
                .postedAt(record.getPostedAt())
                .category(CategoryDto
                        .builder()
                        .id(record.getCategory().getId())
                        .name(record.getCategory().getName())
                        .build())
                .coverImageUrl(awsS3Service.getPreSignedUrl(
                        Constants.AWS.BUCKET_NAME,
                        record.getCoverImageUrl(),
                        Constants.AWS.SIGNED_URI_EXPIRATION))
                .isFavorite(record.getIsFavorite())
                .location(locationDTO)
                .build();
    }

}
