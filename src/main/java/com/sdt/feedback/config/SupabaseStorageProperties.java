package com.sdt.feedback.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.supabase.storage")
public record SupabaseStorageProperties(
        String url,
        String serviceRoleKey,
        String bucket
) {
}
