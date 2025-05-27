package UI.views.dataobjects;

import java.util.List;

import Application.DTOs.UserDTO;
import Domain.management.PermissionType;

public class UserPermission {
    public final UserDTO user;
    public final List<PermissionType> permissions;
    
    public UserPermission(UserDTO user, List<PermissionType> permissions) {
        this.user = user;
        this.permissions = permissions;
    }
}