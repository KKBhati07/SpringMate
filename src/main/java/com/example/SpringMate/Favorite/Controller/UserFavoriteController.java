package com.example.SpringMate.Favorite.Controller;

import com.example.SpringMate.Favorite.DTO.FavoriteRequestDto;
import com.example.SpringMate.Favorite.Service.UserFavoriteService;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Util.AuthenticatedUser;
import com.example.SpringMate.Util.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(Urls.UserFavorites.BASE)
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @PostMapping(Urls.UserFavorites.SET_UNSET)
    public ResponseEntity<Response<Map<String, Boolean>>> setUnsetFavorite(
            @RequestBody FavoriteRequestDto dto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info(
                "User toggling favorite user=[UUID {}] listing=[ID {}]",
                authenticatedUser.uuid(),
                dto.getListingId()
        );

        return ResponseEntity.status(
                        HttpStatus.CREATED)
                .body(Response.success(
                        userFavoriteService.setUnsetFavorite(
                                authenticatedUser.id(),
                                dto.getListingId()),
                        "Request successful")
                );
    }
}
