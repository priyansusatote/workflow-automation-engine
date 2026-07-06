package com.priyansu.workflow.service.Impl;

import com.priyansu.workflow.security.SecurityUtils;
import com.priyansu.workflow.service.CurrentUserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    @Override
    public UUID getCurrentUserId() {

        return SecurityUtils
                .getCurrentUser()
                .userId();
    }
}
