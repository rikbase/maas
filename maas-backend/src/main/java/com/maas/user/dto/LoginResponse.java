package com.maas.user.dto;

public record LoginResponse(
    String token,
    UserVO user
) {}
