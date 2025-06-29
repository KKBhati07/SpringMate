package com.example.SpringMate.Util;

import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Shared.Service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ResponseMapper {

    private final AwsS3Service awsS3Service;

    public Map<String, Object> mapUser(User user) {
        Map<String, Object> map = new HashMap<>();
        if(user == null) return map;
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("uuid", user.getUuid());
        map.put("is_admin", user.isAdmin());
        map.put("contact_no", user.getContactNo());
        map.put("profile_url", awsS3Service.getPreSignedUrl(
                Constants.AWS.BUCKET_NAME,
                user.getProfileUrl(),
                Constants.AWS.SIGNED_URI_EXPIRATION));
        return map;
    }
}
