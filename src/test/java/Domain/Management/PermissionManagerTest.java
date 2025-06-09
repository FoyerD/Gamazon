package Domain.Management;

import Domain.Repos.IPermissionRepository;

import Domain.management.Permission;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PermissionManagerTest {

    private PermissionManager permissionManager;
    private IPermissionRepository permissionRepository;

    @Before
    public void setUp() {
        permissionRepository = mock(IPermissionRepository.class);
        permissionManager = new PermissionManager(permissionRepository);
    }

    @Test
    public void testAppointFirstStoreOwner_CreatesFounderPermission() {
        String userId = "founder";
        String storeId = "store1";

        when(permissionRepository.get(storeId, userId)).thenReturn(null);

        permissionManager.appointFirstStoreOwner(userId, storeId);

        verify(permissionRepository).add(eq(storeId), eq(userId), any(Permission.class));
    }

    @Test
    public void testCheckPermission_HasPermission_DoesNotThrow() {
        String userId = "user1";
        String storeId = "store1";
        Permission permission = mock(Permission.class);
        when(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS)).thenReturn(true);

        when(permissionRepository.get(storeId, userId)).thenReturn(permission);

        permissionManager.checkPermission(userId, storeId, PermissionType.SUPERVISE_MANAGERS);
        verify(permission).hasPermission(PermissionType.SUPERVISE_MANAGERS);
    }

    @Test(expected = SecurityException.class)
    public void testCheckPermission_MissingPermission_Throws() {
        String userId = "user1";
        String storeId = "store1";

        when(permissionRepository.get(storeId, userId)).thenReturn(null);

        permissionManager.checkPermission(userId, storeId, PermissionType.SUPERVISE_MANAGERS);
    }

    @Test
    public void testBanUser_CreatesBannedPermission() {
        String bannerId = "admin";
        String userId = "userToBan";

        Permission bannerPermission = mock(Permission.class);
        when(bannerPermission.hasPermission(PermissionType.BAN_USERS)).thenReturn(true);
        when(permissionRepository.get("1", bannerId)).thenReturn(bannerPermission);

        boolean result = permissionManager.banUser(bannerId, userId, new Date());

        assertTrue(result);
        verify(permissionRepository).add(eq("1"), eq(userId), any(Permission.class));
    }

    @Test
    public void testUnbanUser_RemovesPermission() {
        String unbannerId = "admin";
        String userId = "bannedUser";

        Permission bannerPermission = mock(Permission.class);
        when(bannerPermission.hasPermission(PermissionType.BAN_USERS)).thenReturn(true);
        when(permissionRepository.get("1", unbannerId)).thenReturn(bannerPermission);

        Permission bannedPermission = mock(Permission.class);
        when(bannedPermission.hasPermission(PermissionType.BANNED)).thenReturn(true);
        when(permissionRepository.get("1", userId)).thenReturn(bannedPermission);

        boolean result = permissionManager.unbanUser(unbannerId, userId);

        assertTrue(result);
        verify(permissionRepository).remove("1", userId);
    }

    @Test
    public void testGetUsersWithPermission_ReturnsCorrectUsers() {
        String storeId = "store1";
        Map<String, Permission> mockMap = new HashMap<>();
        Permission p1 = mock(Permission.class);
        Permission p2 = mock(Permission.class);

        when(p1.hasPermission(PermissionType.HANDLE_INVENTORY)).thenReturn(true);
        when(p2.hasPermission(PermissionType.HANDLE_INVENTORY)).thenReturn(false);

        mockMap.put("user1", p1);
        mockMap.put("user2", p2);

        when(permissionRepository.getAllPermissionsForStore(storeId)).thenReturn(mockMap);

        List<String> result = permissionManager.getUsersWithPermission(storeId, PermissionType.HANDLE_INVENTORY);

        assertEquals(1, result.size());
        assertTrue(result.contains("user1"));
    }

    @Test
    public void testRemoveAllPermissions_ClearsPermissionsAndRole() {
        String storeId = "store1";
        String userId = "user";

        Permission permission = mock(Permission.class);
        when(permissionRepository.get(storeId, userId)).thenReturn(permission);

        permissionManager.removeAllPermissions(storeId, userId);

        verify(permission).setPermissions(eq(Set.of()));
        verify(permission).setRole(null);
        verify(permissionRepository).update(storeId, userId, permission);
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveAllPermissions_NonExistentPermission_Throws() {
        String storeId = "store1";
        String userId = "ghostUser";

        when(permissionRepository.get(storeId, userId)).thenReturn(null);
        permissionManager.removeAllPermissions(storeId, userId);
    }

    @Test
    public void testIsBanned_ReturnsTrueIfNotExpired() {
        String userId = "bannedUser";
        Permission perm = mock(Permission.class);

        when(perm.hasPermission(PermissionType.BANNED)).thenReturn(true);
        when(perm.getExpirationDate()).thenReturn(new Date(System.currentTimeMillis() + 100000));
        when(permissionRepository.get("1", userId)).thenReturn(perm);

        assertTrue(permissionManager.isBanned(userId));
    }

    @Test
    public void testIsBanned_ReturnsFalseIfPermissionExpired() {
        String userId = "bannedUser";
        Permission perm = mock(Permission.class);

        when(perm.hasPermission(PermissionType.BANNED)).thenReturn(true);
        when(perm.getExpirationDate()).thenReturn(new Date(System.currentTimeMillis() - 1000));
        when(permissionRepository.get("1", userId)).thenReturn(perm);

        assertFalse(permissionManager.isBanned(userId));
        verify(permissionRepository).update(eq("1"), eq(userId), any(Permission.class));
    }

    @Test
    public void testGetUsersWithPermission_MultipleUsersSomeWithPermission() {
        String storeId = "store1";

        Permission p1 = mock(Permission.class);
        Permission p2 = mock(Permission.class);
        Permission p3 = mock(Permission.class);

        when(p1.hasPermission(PermissionType.HANDLE_INVENTORY)).thenReturn(true);
        when(p2.hasPermission(PermissionType.HANDLE_INVENTORY)).thenReturn(false);
        when(p3.hasPermission(PermissionType.HANDLE_INVENTORY)).thenReturn(true);

        Map<String, Permission> storePermissions = new HashMap<>();
        storePermissions.put("user1", p1);
        storePermissions.put("user2", p2);
        storePermissions.put("user3", p3);

        when(permissionRepository.getAllPermissionsForStore(storeId)).thenReturn(storePermissions);

        List<String> result = permissionManager.getUsersWithPermission(storeId, PermissionType.HANDLE_INVENTORY);

        assertEquals(2, result.size());
        assertTrue(result.contains("user1"));
        assertTrue(result.contains("user3"));
        assertFalse(result.contains("user2"));
    }

    @Test
    public void testGetUsersWithPermission_NoUserHasPermission() {
        String storeId = "store2";

        Permission p1 = mock(Permission.class);
        Permission p2 = mock(Permission.class);

        when(p1.hasPermission(PermissionType.MODIFY_OWNER_RIGHTS)).thenReturn(false);
        when(p2.hasPermission(PermissionType.MODIFY_OWNER_RIGHTS)).thenReturn(false);

        Map<String, Permission> storePermissions = new HashMap<>();
        storePermissions.put("userA", p1);
        storePermissions.put("userB", p2);

        when(permissionRepository.getAllPermissionsForStore(storeId)).thenReturn(storePermissions);

        List<String> result = permissionManager.getUsersWithPermission(storeId, PermissionType.MODIFY_OWNER_RIGHTS);

        assertTrue(result.isEmpty());
    }

}
