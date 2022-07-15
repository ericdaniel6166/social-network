package com.example.socialnetworkapp.auth.service.impl;

import com.example.socialnetworkapp.auth.dto.UserDTO;
import com.example.socialnetworkapp.auth.enums.RoleEnum;
import com.example.socialnetworkapp.auth.model.AppRole;
import com.example.socialnetworkapp.auth.model.AppUser;
import com.example.socialnetworkapp.auth.service.AccountService;
import com.example.socialnetworkapp.auth.service.RoleService;
import com.example.socialnetworkapp.auth.service.UserService;
import com.example.socialnetworkapp.dto.SimpleResponseDTO;
import com.example.socialnetworkapp.enums.MasterMessageCode;
import com.example.socialnetworkapp.exception.ResourceNotFoundException;
import com.example.socialnetworkapp.exception.SocialNetworkAppException;
import com.example.socialnetworkapp.model.MasterMessage;
import com.example.socialnetworkapp.service.MasterMessageService;
import com.example.socialnetworkapp.utils.CommonUtils;
import com.example.socialnetworkapp.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserService userService;

    private final RoleService roleService;

    private final MasterMessageService masterMessageService;

    /**
     * user can't set role to itself
     * ROLE_ROOT_ADMIN can:
     * . set any roles to any accounts
     * ROLE_ADMIN can:
     * . set role less or equal ROLE_MODERATOR to less or equal ROLE_MODERATOR account
     * less or equal ROLE_MODERATOR:
     * . Forbidden
     *
     * @param userDTO
     * @throws ResourceNotFoundException
     */
    @Override
    public SimpleResponseDTO updateRole(UserDTO userDTO) throws SocialNetworkAppException {
        RoleEnum roleEnumNew = userDTO.getRole();
        String usernameUpdate = userDTO.getUsername();
        String currentUsername = CommonUtils.getCurrentUsername();
        if (StringUtils.equals(currentUsername, usernameUpdate)) {
            log.info(Constants.ERROR_MESSAGE_SET_ROLE_YOURSELF);
            throw new AccessDeniedException(Constants.ERROR_MESSAGE_SET_ROLE_YOURSELF);
        }
        AppUser appUserUpdate = userService.findByUsername(usernameUpdate);
        AppRole appRoleOld = appUserUpdate.getAppRole();
        if (!CommonUtils.hasAuthority(RoleEnum.ROLE_ROOT_ADMIN)) {
            if (appRoleOld.getRoleName().compareTo(RoleEnum.ROLE_ADMIN) >= 0) {
                log.info(Constants.ERROR_MESSAGE_NOT_HAVE_PERMISSION_SET_ROLE_THIS_USER);
                throw new AccessDeniedException(Constants.ERROR_MESSAGE_NOT_HAVE_PERMISSION_SET_ROLE_THIS_USER);
            }
            if (roleEnumNew.compareTo(RoleEnum.ROLE_ADMIN) >= 0) {
                log.info(Constants.ERROR_MESSAGE_NOT_HAVE_PERMISSION_SET_THIS_ROLE);
                throw new AccessDeniedException(Constants.ERROR_MESSAGE_NOT_HAVE_PERMISSION_SET_THIS_ROLE);
            }
        }
        if (roleEnumNew.compareTo(appRoleOld.getRoleName()) == 0) {
            log.info(Constants.ERROR_MESSAGE_USER_ALREADY_HAD_THIS_ROLE);
            throw new SocialNetworkAppException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.name(),
                    Constants.ERROR_MESSAGE_USER_ALREADY_HAD_THIS_ROLE, null);
        }
        AppRole appRoleNew = roleService.findByRoleName(roleEnumNew);
        appUserUpdate.setAppRole(appRoleNew);
        userService.saveAndFlush(appUserUpdate);
        MasterMessage masterMessage = masterMessageService.findByMessageCode(MasterMessageCode.UPDATE_ROLE_SUCCESS);
        SimpleResponseDTO simpleResponseDTO = new SimpleResponseDTO();
        simpleResponseDTO.setTitle(StringEscapeUtils.unescapeJava(masterMessage.getTitle()));
        simpleResponseDTO.setMessage(StringEscapeUtils.unescapeJava(CommonUtils.formatString(masterMessage.getMessage(), roleEnumNew.name(), usernameUpdate)));
        return simpleResponseDTO;


    }

}