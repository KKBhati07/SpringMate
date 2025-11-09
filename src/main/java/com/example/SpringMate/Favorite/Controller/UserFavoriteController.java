package com.example.SpringMate.Favorite.Controller;

import com.example.SpringMate.Favorite.DTO.FavoriteRequestDto;
import com.example.SpringMate.Favorite.Service.UserFavoriteService;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Util.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(Urls.UserFavorites.FAVORITE_BASE)
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @PostMapping(Urls.UserFavorites.SET_UNSET)
    public ResponseEntity<Response<Map<String, Boolean>>> setUnsetFavorite(
            @RequestBody FavoriteRequestDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {

        return ResponseEntity.status(
                        HttpStatus.CREATED)
                .body(new Response<>
                        (userFavoriteService.setUnsetFavorite(
                                authenticatedUser.getId(),
                                dto.getListingId()),
                                "Request successful")
                );
    }
}
