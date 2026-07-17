package com.one.security;

import com.one.identity.UserRole;

public record OnePrincipal(long userId, UserRole role) {
}
