package Infrastructure.JpaSpringRepositories;

import Domain.management.Permission;
import Domain.management.PermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IJpaPermissionRepository extends JpaRepository<Permission, PermissionId> {
    List<Permission> findAllByStoreId(String storeId);
}
