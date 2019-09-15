package com.socialnetwork.mailservice.controllers;

import com.socialnetwork.mailservice.services.api.EmailService;
import com.socialnetwork.mailservice.utils.vos.EmailInputVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    @ApiOperation(value = "send a new email")
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Accepted"),
            @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<?> sendEmail(@Valid @RequestBody EmailInputVo emailInputVo) {
        emailService.sendEmail(emailInputVo);
        return null;
    }

}
