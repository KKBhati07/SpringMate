package com.example.SpringMate.Util;

import com.example.SpringMate.Listing.DTO.*;
import com.example.SpringMate.Listing.Entity.Listing;
import com.example.SpringMate.Storage.Service.StorageService;
import com.example.SpringMate.User.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class ResponseMapper {

    private final StorageService storageService;

    public UserDetailsDto mapUser(User user) {
        if (user == null) return null;
        return UserDetailsDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .uuid(user.getUuid())
                .isAdmin(user.isAdmin())
                .contactNo(user.getContactNo())
                .deleted(user.isDeleted())
                .profileUrl(storageService.getPreSignedUrl(user.getProfileUrl()))
                .build();
    }

    public ListingResponseDto mapListing(Listing listing) {
        if (listing == null) return null;
        ListingResponseDto dto = new ListingResponseDto();
        dto.setId(listing.getId());
        dto.setTitle(listing.getTitle());
        dto.setDescription(listing.getDescription());
        dto.setPrice(listing.getPrice());
        dto.setSold(listing.isSold());
        dto.setPostedAt(listing.getPostedAt());


        if (listing.getCategory() != null) {
            dto.setCategory(CategoryDto.builder()
                    .id(listing.getCategory().getId())
                    .icon(listing.getCategory().getIcon())
                    .name(listing.getCategory().getName())
                    .build());
        }
        if (listing.getSeller() != null) {
            dto.setSeller(UserDto.builder()
                    .name(listing.getSeller().getName())
                    .profileUrl(listing.getSeller().getProfileUrl())
                    .emailVerified(listing.getSeller().isEmailVerified())
                    .deleted(listing.getSeller().isDeleted())
                    .build());
        }

        dto.setCondition(ConditionDto.builder()
                .id(listing.getCondition().getId())
                .code(listing.getCondition().getCode())
                .label(listing.getCondition().getLabel())
                .description(listing.getCondition().getDescription())
                .sortOrder(listing.getCondition().getSortOrder())
                .build());

        if (listing.getLocation() != null) {
            dto.setLocation(LocationDto.builder()
                    .state(LocationDto.StateDto.builder()
                            .id(listing.getLocation().getState().getId())
                            .name(listing.getLocation().getState().getName())
                            .build())
                    .city(LocationDto.CityDto.builder()
                            .id(listing.getLocation().getCity().getId())
                            .name(listing.getLocation().getCity().getName())
                            .build())
                    .country(LocationDto.CountryDto.builder()
                            .id(listing.getLocation().getCountry().getId())
                            .name(listing.getLocation().getCountry().getName())
                            .build())
                    .id(listing.getLocation().getId())
                    .build()
            );
        }

        if (listing.getListingImages() != null
                && !listing.getListingImages().isEmpty()) {
            dto.setImages(
                    listing.getListingImages().stream()
                            .map(img -> ListingImageDto.builder()
                                    .id(img.getId())
                                    .url(img.getUrl())
                                    .isCover(img.isCover())
                                    .build()
                            ).toList()
            );

        } else {
            dto.setImages(new ArrayList<>());
        }

        return dto;
    }
}
