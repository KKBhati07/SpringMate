package com.example.SpringMate.Shared.Enum;

import lombok.Getter;

@Getter
public enum AwsS3Directory {
    PROFILE("profile"),
    LISTINGS("listings");

    private final String name;

    AwsS3Directory(String name) {
        this.name = name;
    }

}
