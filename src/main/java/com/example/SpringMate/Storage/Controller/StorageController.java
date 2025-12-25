package com.example.SpringMate.Storage.Controller;

import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Storage.DTO.PresignBatchRequestDto;
import com.example.SpringMate.Storage.DTO.PresignBatchResponseDto;
import com.example.SpringMate.Storage.Service.StorageService;
import com.example.SpringMate.Util.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Storage.STORAGE_BASE)
public class StorageController {

    private final StorageService storageService;

    @GetMapping(Urls.Storage.OBJECT_EXISTS)
    public ResponseEntity<Response<Map<String,Boolean>>>
    doesObjectExists(@RequestParam(value = "object_key")String objectKey) {

            return ResponseEntity.ok(
                    new Response<>(Map.of("exists",storageService.doesObjectExists(Constants.AWS.BUCKET_NAME,objectKey)),
                            "Request successful!"));
    }

    @PostMapping(Urls.Storage.PRESIGN_URL)
    public ResponseEntity<Response<PresignBatchResponseDto>>
    presignPutUrl(@RequestBody PresignBatchRequestDto presignRequestDto){
            return ResponseEntity.ok(
                    new Response<>(storageService.getPresignPutUrls(presignRequestDto),
                            "Presigned Url created successfully"));
    }
}
