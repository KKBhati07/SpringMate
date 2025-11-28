package com.example.SpringMate.Favorite.Service;

import com.example.SpringMate.Favorite.Entity.UserFavorite;
import com.example.SpringMate.Favorite.Repository.UserFavoriteRepository;
import com.example.SpringMate.Listing.Entity.Listing;
import com.example.SpringMate.Listing.Service.ListingService;
import com.example.SpringMate.Shared.Exception.BadRequestException;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.User.Service.CoreUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserFavoriteService {
    private final CoreUserService coreUserService;
    private final ListingService listingService;
    private final UserFavoriteRepository userFavoriteRepository;


    public Map<String, Boolean> setUnsetFavorite(Long userId, Long listingId) {
        User user = coreUserService.getUserOrThrowById(userId);
        Listing listing = listingService.getByIdOrThrow(listingId);
        Optional<UserFavorite> existing = userFavoriteRepository
                .findByUserAndListing(user, listing);

        if(user.getId().equals(listing.getSeller().getId())){
            throw  new BadRequestException("Cannot mark owned listings as favorite");
        }

        if (existing.isPresent()) {
            UserFavorite favorite = existing.get();
            favorite.setFavorite(!favorite.isFavorite());
            userFavoriteRepository.save(favorite);
            return Map.of("is_favorite", favorite.isFavorite());

        } else {
            userFavoriteRepository.save(
                    UserFavorite.builder()
                            .user(user)
                            .listing(listing)
                            .build()
            );
            return Map.of("is_favorite", true);

        }
    }
}
