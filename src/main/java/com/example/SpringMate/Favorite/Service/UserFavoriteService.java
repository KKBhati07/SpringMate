package com.example.SpringMate.Favorite.Service;

import com.example.SpringMate.Favorite.Entity.UserFavorite;
import com.example.SpringMate.Favorite.Repository.UserFavoriteRepository;
import com.example.SpringMate.Listing.Entity.Listing;
import com.example.SpringMate.Listing.Service.ListingService;
import com.example.SpringMate.Shared.Exception.BadRequestException;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.User.Service.CoreUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFavoriteService {
    private final CoreUserService coreUserService;
    private final ListingService listingService;
    private final UserFavoriteRepository userFavoriteRepository;


    /**
     * Prevents users from favoriting their own listings to maintain data integrity.
     */
    public Map<String, Boolean> setFavorite(Long userId, Long listingId, boolean isFavorite) {
        User user = coreUserService.getUserOrThrowById(userId);
        Listing listing = listingService.getByIdOrThrow(listingId);

        if (user.getId().equals(listing.getSeller().getId())) {
            log.info(
                    "User toggling favorite user=[UUID {}] listing=[ID {}]",
                    user.getId(),
                    listingId
            );

            throw new BadRequestException("Cannot mark owned listings as favorite");
        }

        UserFavorite favorite = userFavoriteRepository
                .findByUserAndListing(user, listing)
                .orElseGet(() -> UserFavorite.builder()
                        .user(user)
                        .listing(listing)
                        .build());

        favorite.setFavorite(isFavorite);
        userFavoriteRepository.save(favorite);

        log.info(
                "Favorite set user=[{}] listing=[{}] isFavorite={}",
                userId, listingId, isFavorite
        );

        return Map.of("is_favorite", isFavorite);
    }
}
