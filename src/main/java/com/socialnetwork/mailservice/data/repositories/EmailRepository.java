package com.socialnetwork.mailservice.data.repositories;

import com.socialnetwork.mailservice.data.entities.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, Long> {
}
