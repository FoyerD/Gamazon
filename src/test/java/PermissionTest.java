import org.junit.Before;
import org.junit.Test;

import Domain.management.Permission;
import Domain.management.PermissionType;
import Domain.management.RoleType;
import Domain.management.PermissionFactory;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PermissionTest {

    private Permission permission;
    private static final String PERMISSION_GIVER = "test1";
    private static final String PERMISSION_OWNER = "test2";

    @Before
    public void setUp() {
        permission = new Permission(PERMISSION_GIVER, PERMISSION_OWNER);
    }

    @Test
    public void givenNewPermission_whenInitStoreManager_thenCorrectPermissionsAssigned() {
        PermissionFactory.initPermissionAsRole(permission, RoleType.STORE_MANAGER);
        assertTrue(permission.isStoreManager());
        assertFalse(permission.isStoreOwner());
        assertFalse(permission.isStoreFounder());
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
        assertFalse(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertFalse(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertFalse(permission.hasPermission(PermissionType.DEACTIVATE_STORE));
        assertFalse(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
        assertFalse(permission.hasPermission(PermissionType.EDIT_STORE_POLICIES));
        assertFalse(permission.hasPermission(PermissionType.MODIFY_OWNER_RIGHTS));
        assertFalse(permission.hasPermission(PermissionType.VIEW_EMPLOYEE_INFO));
        assertFalse(permission.hasPermission(PermissionType.ADMINISTER_STORE));
        assertFalse(permission.hasPermission(PermissionType.OVERSEE_OFFERS));
        assertFalse(permission.hasPermission(PermissionType.CONTROL_CONTRACTS));
    }

    @Test
    public void givenNewPermission_whenInitTradingManager_thenCorrectPermissionsAssigned() {
        PermissionFactory.initPermissionAsRole(permission, RoleType.TRADING_MANAGER);
        assertFalse(permission.isStoreManager());
        assertFalse(permission.isStoreOwner());
        assertFalse(permission.isStoreFounder());
        assertTrue(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
        assertTrue(permission.hasPermission(PermissionType.EDIT_STORE_POLICIES));
        assertTrue(permission.hasPermission(PermissionType.VIEW_EMPLOYEE_INFO));
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
    }

    @Test
    public void givenNewPermission_whenInitStoreOwner_thenCorrectPermissionsAssigned() {
        PermissionFactory.initPermissionAsRole(permission, RoleType.STORE_OWNER);
        assertFalse(permission.isStoreManager());
        assertTrue(permission.isStoreOwner());
        assertFalse(permission.isStoreFounder());
        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertTrue(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertTrue(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
        assertTrue(permission.hasPermission(PermissionType.EDIT_STORE_POLICIES));
        assertTrue(permission.hasPermission(PermissionType.VIEW_EMPLOYEE_INFO));
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
        assertTrue(permission.hasPermission(PermissionType.ADMINISTER_STORE));
        assertTrue(permission.hasPermission(PermissionType.OVERSEE_OFFERS));
        assertTrue(permission.hasPermission(PermissionType.CONTROL_CONTRACTS));
    }

    @Test
    public void givenNewPermission_whenInitStoreFounder_thenCorrectPermissionsAssigned() {
        PermissionFactory.initPermissionAsRole(permission, RoleType.STORE_FOUNDER);
        assertFalse(permission.isStoreManager());
        assertFalse(permission.isStoreOwner());
        assertTrue(permission.isStoreFounder());
        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertTrue(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertTrue(permission.hasPermission(PermissionType.DEACTIVATE_STORE));
        assertTrue(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
        assertTrue(permission.hasPermission(PermissionType.EDIT_STORE_POLICIES));
        assertTrue(permission.hasPermission(PermissionType.MODIFY_OWNER_RIGHTS));
        assertTrue(permission.hasPermission(PermissionType.VIEW_EMPLOYEE_INFO));
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
        assertTrue(permission.hasPermission(PermissionType.ADMINISTER_STORE));
        assertTrue(permission.hasPermission(PermissionType.OVERSEE_OFFERS));
        assertTrue(permission.hasPermission(PermissionType.CONTROL_CONTRACTS));
    }

    @Test
    public void givenPermission_whenSetPermissions_thenOnlySelectedPermissionsAssigned() {
        permission.setPermissions(Set.of(
                PermissionType.ASSIGN_OR_REMOVE_OWNERS,
                PermissionType.HANDLE_INVENTORY
        ));
        assertTrue(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertFalse(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertTrue(permission.hasPermission(PermissionType.HANDLE_INVENTORY));

        permission.setPermissions(Set.of(
                PermissionType.SUPERVISE_MANAGERS,
                PermissionType.DEACTIVATE_STORE,
                PermissionType.MODIFY_OWNER_RIGHTS
        ));
        assertFalse(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertTrue(permission.hasPermission(PermissionType.DEACTIVATE_STORE));
        assertTrue(permission.hasPermission(PermissionType.MODIFY_OWNER_RIGHTS));

        permission.setPermissions(Set.of(
                PermissionType.ASSIGN_OR_REMOVE_OWNERS,
                PermissionType.SUPERVISE_MANAGERS,
                PermissionType.EDIT_STORE_POLICIES,
                PermissionType.OVERSEE_OFFERS
        ));
        assertTrue(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertTrue(permission.hasPermission(PermissionType.EDIT_STORE_POLICIES));
        assertTrue(permission.hasPermission(PermissionType.OVERSEE_OFFERS));
        assertFalse(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
    }

    @Test
    public void givenStoreManagerRole_whenCheckingAssignOwnerPermission_thenShouldNotHaveIt() {
        PermissionFactory.initPermissionAsRole(permission, RoleType.STORE_MANAGER);
        assertFalse(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
    }

    @Test
    public void givenStoreManagerRole_whenCheckingPurchaseAccessPermission_thenShouldHaveIt() {
        PermissionFactory.initPermissionAsRole(permission, RoleType.STORE_MANAGER);
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
    }

    @Test
    public void givenStoreFounderRole_whenCheckingAllExpectedPermissions_thenAllPresent() {
        PermissionFactory.initPermissionAsRole(permission, RoleType.STORE_FOUNDER);
        assertTrue(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertTrue(permission.hasPermission(PermissionType.DEACTIVATE_STORE));
        assertTrue(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
        assertTrue(permission.hasPermission(PermissionType.EDIT_STORE_POLICIES));
        assertTrue(permission.hasPermission(PermissionType.MODIFY_OWNER_RIGHTS));
        assertTrue(permission.hasPermission(PermissionType.VIEW_EMPLOYEE_INFO));
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
    }

    @Test
    public void givenStoreFounderRole_whenCheckingModifyOwnerPermission_thenShouldHaveIt() {
        PermissionFactory.initPermissionAsRole(permission, RoleType.STORE_FOUNDER);
        assertTrue(permission.hasPermission(PermissionType.MODIFY_OWNER_RIGHTS));
    }

    @Test
    public void givenStoreOwnerRole_whenCheckingRole_thenIsStoreOwner() {
        permission.setRole(RoleType.STORE_OWNER);
        assertTrue(permission.isStoreOwner());
    }

    @Test
    public void givenStoreOwnerRole_whenCheckingSpecificPermissions_thenCorrectPermissionsPresent() {
        PermissionFactory.initPermissionAsRole(permission, RoleType.STORE_OWNER);
        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
    }

    @Test
    public void givenPermission_whenSwitchingBetweenRolesAndPermissions_thenStateUpdatedCorrectly() {
        PermissionFactory.initPermissionAsRole(permission, RoleType.STORE_MANAGER);
        assertTrue(permission.isStoreManager());
        assertFalse(permission.isStoreOwner());

        PermissionFactory.initPermissionAsRole(permission, RoleType.STORE_OWNER);
        assertFalse(permission.isStoreManager());
        assertTrue(permission.isStoreOwner());

        PermissionFactory.initPermissionAsRole(permission, RoleType.STORE_FOUNDER);
        assertFalse(permission.isStoreManager());
        assertFalse(permission.isStoreOwner());
        assertTrue(permission.isStoreFounder());

        PermissionFactory.initPermissionAsRole(permission, RoleType.TRADING_MANAGER);
        assertFalse(permission.isStoreManager());
        assertFalse(permission.isStoreOwner());
        assertFalse(permission.isStoreFounder());
        assertTrue(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
        assertTrue(permission.hasPermission(PermissionType.EDIT_STORE_POLICIES));
        assertTrue(permission.hasPermission(PermissionType.VIEW_EMPLOYEE_INFO));
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
        assertTrue(permission.hasPermission(PermissionType.DEACTIVATE_STORE));

        permission.setPermissions(Set.of(
            PermissionType.SUPERVISE_MANAGERS,
            PermissionType.ASSIGN_OR_REMOVE_OWNERS,
            PermissionType.MODIFY_OWNER_RIGHTS,
            PermissionType.HANDLE_INVENTORY,
            PermissionType.EDIT_STORE_POLICIES,
            PermissionType.VIEW_EMPLOYEE_INFO,
            PermissionType.ACCESS_PURCHASE_RECORDS,
            PermissionType.ADMINISTER_STORE,
            PermissionType.OVERSEE_OFFERS,
            PermissionType.CONTROL_CONTRACTS
        ));
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
        assertFalse(permission.hasPermission(PermissionType.DEACTIVATE_STORE));

        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        permission.removePermission(PermissionType.SUPERVISE_MANAGERS);
        assertFalse(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
    }
}
