package Domain.Store;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "stores")
public class Store {
    
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "founder_id")
    private String founderId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "store_owners", joinColumns = @JoinColumn(name = "store_id"))
    private Set<String> owners = Collections.synchronizedSet(new HashSet<>());
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "store_managers", joinColumns = @JoinColumn(name = "store_id"))
    private Set<String> managers = Collections.synchronizedSet(new HashSet<>());
    
    @Column(name = "is_open")
    private boolean isOpen;
    
    @Column(name = "is_permanently_closed")
    private boolean isPermanentlyClosed;
    
    protected Store() {
        // Required by JPA
    }


    
    public Store(String id, String name, String description, String foudnerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.founderId = foudnerId;
        this.owners = Collections.synchronizedSet(new HashSet<>());
        this.managers = Collections.synchronizedSet(new HashSet<>());
        this.isOpen = true;
        this.isPermanentlyClosed = false;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String descripsion) {
        this.description = descripsion;
    }

    public String getFounderId() {
        return founderId;
    }
    public void setFounderId(String founderId) {
        this.founderId = founderId;
    }

    public Set<String> getOwners() {
        return new HashSet<>(this.owners);
    }
    public boolean addOwner(String userId) {
        return this.owners.add(userId);
    }

    public Set<String> getManagers() {
        return new HashSet<>(this.managers);
    }
    public boolean addManager(String userId) {
        return this.managers.add(userId);
    }

    public boolean isOpen() {
        return isOpen;
    }
    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public boolean isPermanentlyClosed() {
        return isPermanentlyClosed;
    }
    public void setPermanentlyClosed(boolean isPermanentlyClosed) {
        this.isPermanentlyClosed = isPermanentlyClosed;
    }
    
    public boolean removeOwner(String userId) {
        return this.owners.remove(userId);
    }
    public boolean removeManager(String userId) {
        return this.managers.remove(userId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Store store = (Store) obj;
        return id.equals(store.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
