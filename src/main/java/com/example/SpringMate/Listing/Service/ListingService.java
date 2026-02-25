package com.example.SpringMate.Listing.Service;

import com.example.SpringMate.Listing.DTO.*;
import com.example.SpringMate.Listing.Entity.ContactMessage;
import com.example.SpringMate.Listing.Entity.ContactMessageStatus;
import com.example.SpringMate.Listing.Entity.Category;
import com.example.SpringMate.Listing.Entity.Condition;
import com.example.SpringMate.Listing.Entity.Listing;
import com.example.SpringMate.Listing.Entity.ListingImage;
import com.example.SpringMate.Listing.Repository.CategoryRepository;
import com.example.SpringMate.Listing.Repository.ContactMessageRepository;
import com.example.SpringMate.Listing.Repository.ConditionRepository;
import com.example.SpringMate.Listing.Repository.ListingImageRepository;
import com.example.SpringMate.Listing.Repository.ListingRepository;
import com.example.SpringMate.Location.Service.LocationService;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import com.example.SpringMate.Shared.Exception.*;
import com.example.SpringMate.Shared.Helper.InputSanitizer;
import com.example.SpringMate.Shared.Service.EmailService;
import com.example.SpringMate.Shared.Service.EmailTemplateService;
import com.example.SpringMate.Storage.Service.StorageService;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.User.Service.CoreUserService;
import com.example.SpringMate.Util.AuthenticatedUser;
import com.example.SpringMate.Util.PaginatedResponse;
import com.example.SpringMate.Util.ResponseMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final CategoryRepository categoryRepository;
    private final ConditionRepository conditionRepository;
    private final LocationService locationService;
    private final CoreUserService coreUserService;
    private final StorageService storageService;
    private final ResponseMapper responseMapper;
    private final EmailService emailService;
    private final ContactMessageRepository contactMessageRepository;
    private final EmailTemplateService emailTemplateService;

    public PaginatedResponse<FetchListingItemsResponseDto> getAllRecords(
            FetchListingsRequestDto queryParams,
            AuthenticatedUser authenticatedUser,
            Boolean deleted
    ) {
        Pageable pageable = PageRequest.of(
                queryParams.getPage(),
                queryParams.getSize(),
                Sort.by(Sort.Direction.DESC, "postedAt")
        );

        String searchString = queryParams.getSearchString();
        if (searchString != null) {
            searchString = searchString.trim();
            if (searchString.isBlank()) {
                searchString = "";
            }
        } else {
            searchString = "";
        }
        Page<FetchListingItemsProjection> pagedRecords = listingRepository
                .findAllByFilters(
                        authenticatedUser == null ? null : authenticatedUser.id(),
                        queryParams.getCategoryId(),
                        queryParams.getMinPrice(),
                        queryParams.getMaxPrice(),
                        queryParams.getCountryId(),
                        queryParams.getStateId(),
                        queryParams.getCityId(),
                        searchString,
                        deleted,
                        pageable);
        return new PaginatedResponse<>(pagedRecords.getContent()
                .stream()
                .map(this::injectPreSignedUrl)
                .toList(),

                pagedRecords.getNumber(),
                pagedRecords.getTotalElements(),
                pagedRecords.getTotalPages());
    }

    public List<String> suggestListingTitles(String query, int limit) {
        if (limit <= 0) return List.of();
        int capped = Math.min(limit, 20);

        String q = query == null ? null : query.trim();
        if (q == null || q.isBlank()) return List.of();

        Pageable pageable = PageRequest.of(0, capped);
        List<String> raw = listingRepository.suggestTitles(q, pageable);

        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String s : raw) {
            if (s == null) continue;
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) unique.add(trimmed);
            if (unique.size() >= capped) break;
        }
        return new ArrayList<>(unique);
    }

    public PaginatedResponse<FetchListingItemsResponseDto> getRecordsByUser(
            UUID uuid,
            int page,
            int size,
            boolean getFavorites
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "postedAt")
        );

        Page<FetchListingItemsProjection> pagedRecords = getFavorites ?
                listingRepository
                        .findFavoritesByUser(
                                coreUserService.getUserOrThrowByUUID(uuid).getId(),
                                pageable) : listingRepository
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

    /**
     * Rolls back uploaded S3 images on failure to prevent orphaned storage objects.
     */
    @Transactional
    public CreateListingResponseDto createRecord(
            CreateListingRequestDto requestDto,
            AuthenticatedUser authenticatedUser
    ) {
        if (requestDto.getImages() != null && requestDto.getImages().size() > Constants.Images.Listing.MAX_LIMIT) {
            throw new BadRequestException("Max 6 images allowed");
        }

        Category category = (requestDto.getCategoryId() != null)
                ? categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Please select a valid category"))
                : categoryRepository.findByName(Constants.DEFAULT_CATEGORY)
                .orElseThrow(() -> new InternalServerException("Default category not found"));

        Condition condition = conditionRepository.findById(requestDto.getConditionId())
                .orElseThrow(() -> new BadRequestException("Please select a valid condition"));

        User user = coreUserService.getUserOrThrowByUUID(authenticatedUser.uuid());

        Listing item = Listing.builder()
                .price(requestDto.getPrice())
                // No HTML allowed in titles
                .title(InputSanitizer.sanitizePlainText(requestDto.getTitle()))
                // Allow safe HTML (links, bold, lists)
                .description(InputSanitizer.sanitizeHtml(requestDto.getDescription()))
                .seller(user)
                .location(locationService.getOrCreateOne(
                        requestDto.getCityId(),
                        requestDto.getStateId(),
                        requestDto.getCountryId()
                ))
                .category(category)
                .condition(condition)
                .build();

        Listing savedItem = listingRepository.save(item);

        List<ListingImage> listingImages = new ArrayList<>();
        List<String> keysToCleanup = new ArrayList<>();

        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            try {
                for (ImageDto imageDto : requestDto.getImages()) {
                    String objectKey = getString(imageDto);

                    keysToCleanup.add(objectKey);

                    listingImages.add(ListingImage.builder()
                            .url(objectKey)
                            .listing(savedItem)
                            .isCover(imageDto.isCover())
                            .build());
                }

                long coverCount = listingImages.stream().filter(ListingImage::isCover).count();
                if (coverCount == 0) {
                    listingImages.get(0).setCover(true);
                } else if (coverCount > 1) {
                    boolean firstSeen = false;
                    for (ListingImage li : listingImages) {
                        if (li.isCover()) {
                            if (!firstSeen) firstSeen = true;
                            else li.setCover(false);
                        }
                    }
                }

                listingImageRepository.saveAll(listingImages);

            } catch (RuntimeException ex) {
                log.error(
                        "Listing image processing failed, rolling back uploads listing=[ID {}]",
                        savedItem.getId(),
                        ex
                );
                for (String key : keysToCleanup) {
                    try {
                        storageService.deleteImage(key);
                    } catch (Exception cleanupEx) {
                        log.error(
                                "Upload rollback failed listing=[ ID {}]",
                                savedItem.getId(),
                                cleanupEx
                        );
                    }
                }
                throw new InternalServerException();
            }
        }

        log.info(
                "Listing created listing=[ID {}] seller=[ UUID {}]",
                savedItem.getId(),
                authenticatedUser.uuid()
        );

        return new CreateListingResponseDto(savedItem.getId());
    }

    @NotNull
    private static String getString(ImageDto imageDto) {
        String objectKey = imageDto.getObjectKey();

        if (objectKey == null || objectKey.isBlank()) {
            throw new BadRequestException("Each image must contain an objectKey");
        }

        if (!objectKey.startsWith(AwsS3Directory.LISTINGS.getName() + "/")) {
            throw new BadRequestException("Invalid objectKey for listing image: " + objectKey);
        }
        return objectKey;
    }

    /**
     * Cleans up uploaded S3 images on failure to prevent orphaned storage objects.
     */
    @Transactional
    public void uploadListingImages(FallbackImageUploadDto dto, AuthenticatedUser authenticatedUser) {
        if (dto.getImages() == null || dto.getImages().isEmpty()) {
            throw new BadRequestException("Invalid request body");
        }

        Listing listing = listingRepository.findByIdAndDeletedFalse(dto.getListingId())
                .orElseThrow(() -> new BadRequestException("Listing not found"));

        if (!listing.getSeller().getUuid().equals(authenticatedUser.uuid())) {
            log.warn(
                    "Unauthorized image upload attempt listing=[ ID {}] user=[ UUID {}]",
                    dto.getListingId(),
                    authenticatedUser.uuid()
            );
            throw new UnauthorizedException();
        }

        List<ListingImage> listingImages = new ArrayList<>();
        List<String> uploadedKeys = new ArrayList<>();

        try {
            for (ImageDto imageDto : dto.getImages()) {
                MultipartFile file = imageDto.getImage();
                if (file == null || file.isEmpty()) {
                    throw new BadRequestException("Empty image provided");
                }

                String imgKey = storageService.uploadImage(AwsS3Directory.LISTINGS, file);
                if (imgKey == null) {
                    throw new InternalServerException("Upload failed for file: " + file.getOriginalFilename());
                }

                uploadedKeys.add(imgKey);

                listingImages.add(ListingImage.builder()
                        .url(imgKey)
//                        .listing(listingRepository.getReferenceById(listing.getId())) // can use if fetching the item is not needed!
                        .listing(listing)
                        .isCover(imageDto.isCover())
                        .build());
            }

            long coverCount = listingImages.stream().filter(ListingImage::isCover).count();
            if (coverCount > 1) {
                boolean firstMarked = false;
                for (ListingImage li : listingImages) {
                    if (li.isCover()) {
                        if (!firstMarked) firstMarked = true;
                        else li.setCover(false);
                    }
                }
            }

            listingImageRepository.saveAll(listingImages);
            log.info(
                    "Listing images uploaded listing=[ID {}] imageCount={}",
                    dto.getListingId(),
                    dto.getImages().size()
            );


        } catch (RuntimeException ex) {
            log.error(
                    "Listing image upload failed, performing cleanup listing=[ID {}]",
                    dto.getListingId(),
                    ex
            );
            for (String key : uploadedKeys) {
                try {
                    storageService.deleteImage(key);
                } catch (Exception deleteEx) {
                    log.error(
                            "Upload rollback failed listing=[ ID {}]",
                            dto.getListingId(),
                            deleteEx
                    );
                }
            }
            throw new InternalServerException("Images upload failed!");
        }
    }


    @Transactional
    public void deleteRecord(
            Long itemId,
            AuthenticatedUser authenticatedUser
    ) {

        log.warn(
                "Listing deleted listing=[ID {}] deletedBy=[UUID {}] admin={}",
                itemId,
                authenticatedUser.uuid(),
                authenticatedUser.isAdmin()
        );

        Listing listing = getByIdOrThrow(itemId);

        if (authenticatedUser.isAdmin()) {
            listingRepository.softDeleteById(itemId);
        } else {
            if (listing.getSeller().getUuid()
                    .equals(authenticatedUser.uuid())) {
                listingRepository.softDeleteById(itemId);
            } else {
                throw new ForbiddenException();
            }
        }
    }

    @Transactional
    public void deleteRecords(
            List<Long> ids,
            AuthenticatedUser authenticatedUser
    ) {

        log.warn(
                "Bulk listing delete count={} admin=[UUID {}]",
                ids.size(),
                authenticatedUser.id()
        );

        if (authenticatedUser.isAdmin()) {
            listingRepository.softDeleteByIds(ids);
        } else {
            throw new ForbiddenException();
        }
    }

    public ListingResponseDto getOne(Long itemId) {
        Optional<Listing> listingOptional = listingRepository
                .findWithRelationsByIdAndDeletedFalse(itemId);
        return listingOptional.map(responseMapper::mapListing)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
    }

    /**
     * Sends an email to the listing seller on behalf of an authenticated user.
     */
    public void contactSellerByEmail(
            Long listingId,
            ContactSellerEmailRequestDto dto,
            AuthenticatedUser authenticatedUser
    ) {
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        var sellerContact = listingRepository.findSellerContactByListingId(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));

        if (sellerContact.getSellerEmail() == null || sellerContact.getSellerEmail().isBlank()) {
            throw new InternalServerException("Seller email not available");
        }

        // Prevent emailing yourself via this flow
        if (sellerContact.getSellerUuid() != null && sellerContact.getSellerUuid().equals(authenticatedUser.uuid())) {
            throw new BadRequestException("You cannot contact your own listing");
        }

        String subject = InputSanitizer.stripTagsPlainText(dto.getSubject());
        String body = InputSanitizer.stripTagsPlainText(dto.getBody());

        ContactMessage audit = ContactMessage.builder()
                .listingId(sellerContact.getListingId())
                .sellerId(sellerContact.getSellerId())
                .buyerId(authenticatedUser.id())
                .status(ContactMessageStatus.QUEUED)
                .createdAt(LocalDateTime.now())
                .subjectLength(subject.length())
                .bodyLength(body.length())
                .build();

        audit = contactMessageRepository.save(audit);

        String htmlContent = emailTemplateService.generateContactSellerEmail(
                Constants.EmailHeaders.CONTACT_SELLER,
                sellerContact.getListingTitle() == null ? ("Listing #" + sellerContact.getListingId()) : sellerContact.getListingTitle(),
                authenticatedUser.name(),
                authenticatedUser.email(),
                normalizeListingUrl(dto.getListingUrl()),
                body
        );

        try {
            emailService.sendEmail(sellerContact.getSellerEmail(), subject, htmlContent);
            audit.setStatus(ContactMessageStatus.SENT);
            audit.setSentAt(LocalDateTime.now());
            contactMessageRepository.save(audit);
        } catch (Exception ex) {
            log.error("Failed to send contact email listing=[ID {}] to={}", listingId, sellerContact.getSellerEmail(), ex);
            audit.setStatus(ContactMessageStatus.FAILED);
            audit.setFailureReason(truncateFailureReason(ex.getMessage()));
            contactMessageRepository.save(audit);
            throw new InternalServerException("Failed to send email");
        }
    }

    private String truncateFailureReason(String msg) {
        if (msg == null) return null;
        String trimmed = msg.trim();
        if (trimmed.length() <= 500) return trimmed;
        return trimmed.substring(0, 500);
    }

    private String normalizeListingUrl(String url) {
        if (url == null) return null;
        String trimmed = url.trim();
        if (trimmed.isBlank()) return null;
        // Basic safety: allow only http(s)
        if (!(trimmed.startsWith("http://") || trimmed.startsWith("https://"))) return null;
        return trimmed.length() > 500 ? trimmed.substring(0, 500) : trimmed;
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

        ConditionDto conditionDTO = null;
        if (record.getCondition() != null) {
            var cond = record.getCondition();
            conditionDTO = ConditionDto.builder()
                    .id(cond.getId())
                    .code(cond.getCode())
                    .label(cond.getLabel())
                    .description(cond.getDescription())
                    .sortOrder(cond.getSortOrder())
                    .build();
        }

        return FetchListingItemsResponseDto.builder()
                .id(record.getId())
                .title(record.getTitle())
                .description(record.getDescription())
                .price(record.getPrice())
                .postedAt(record.getPostedAt())
                .isDeleted(record.getDeleted())
                .category(CategoryDto
                        .builder()
                        .id(record.getCategory().getId())
                        .name(record.getCategory().getName())
                        .build())
                .condition(conditionDTO)
                .coverImageUrl(storageService.getPreSignedUrl(record.getCoverImageUrl()))
                .isFavorite(record.getIsFavorite())
                .location(locationDTO)
                .build();
    }

    @Cacheable(value = Constants.CacheNamespace.CONDITION, key = "'active'")
    public FetchConditionsResponseDto getAllConditions() {
        return new FetchConditionsResponseDto(
                conditionRepository.findByActiveTrueOrderBySortOrderAsc()
        );
    }

}
