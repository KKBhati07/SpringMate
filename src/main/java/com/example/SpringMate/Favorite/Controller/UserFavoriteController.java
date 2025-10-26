package com.example.SpringMate.Favorite.Controller;

import com.example.SpringMate.Favorite.Service.UserFavoriteService;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Util.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(Urls.UserFavorites.FAVORITE_BASE)
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @PostMapping(Urls.UserFavorites.SET)
    public ResponseEntity<Response<Map<String,Boolean>>> setUnsetFavorite(
            @RequestParam(name = "user_id") Long userId,
            @RequestParam(name = "listing_id") Long listingId
    ){

        return ResponseEntity.status(
                HttpStatus.CREATED)
                .body(new Response<>
                        (userFavoriteService.setUnsetFavorite(userId, listingId),
                                "Request successful")
                );
    }
}
