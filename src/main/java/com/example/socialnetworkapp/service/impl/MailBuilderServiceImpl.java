package com.example.socialnetworkapp.service.impl;

import com.example.socialnetworkapp.service.MailBuilderService;
import com.example.socialnetworkapp.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MailBuilderServiceImpl implements MailBuilderService {

    private static final String TEMPLATE_NAME = "mailTemplate";

    private static final String VARIABLE_NAME = "message";

    @Autowired
    private TemplateService templateService;

    @Override
    public String buildMail(String message) {
        return templateService.build(TEMPLATE_NAME, VARIABLE_NAME, message);
    }
}
