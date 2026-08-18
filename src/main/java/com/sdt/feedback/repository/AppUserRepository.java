package com.sdt.feedback.repository;

import com.sdt.feedback.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    @Query(
            value = """
                    SELECT *
                    FROM public.app_user
                    WHERE LOWER(BTRIM(username)) = LOWER(BTRIM(:username))
                    """,
            nativeQuery = true
    )
    Optional<AppUser> findByNormalizedUsername(@Param("username") String username);
}
