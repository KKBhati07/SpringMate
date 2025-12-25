package com.example.SpringMate.Favorite.Repository;

import com.example.SpringMate.Favorite.Entity.UserFavorite;
import com.example.SpringMate.Listing.Entity.Listing;
import com.example.SpringMate.User.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite,Long> {

    Optional<UserFavorite> findByUserAndListing(User user, Listing listing);
}
