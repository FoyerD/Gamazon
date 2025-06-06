package Application.DTOs;

import java.util.List;
import java.util.Map;

import Domain.management.PermissionType;


public class EmployeeInfo {
    private final List<UserDTO> owners;
    private final Map<UserDTO, List<PermissionType>> managers;

    public EmployeeInfo(List<UserDTO> owners, Map<UserDTO, List<PermissionType>> managers) {
        this.owners = owners;
        this.managers = managers;
    }

    public List<UserDTO> getOwners() {
        return owners;
    }

    public Map<UserDTO, List<PermissionType>> getManagers() {
        return managers;
    }
    
}
