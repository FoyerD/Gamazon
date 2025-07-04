package Application.DTOs;

import java.util.HashSet;
import java.util.Set;

import Domain.Store.Store;

public class StoreDTO {
    private String id;
    private String name;
    private String description;
    private String founderId;
    private Set<String> owners;
    private Set<String> managers;
    private boolean isOpen;
    private boolean isPermanentlyClosed;

    public StoreDTO(String id, String name, String description, String founderId, boolean isOpen, Set<String> owners, Set<String> managers) {  
        this.id = id;
        this.name = name;
        this.description = description;
        this.founderId = founderId;
        this.isOpen = isOpen;
        this.isPermanentlyClosed = false;
        this.owners = owners != null ? owners : Set.of();
        this.managers = managers != null ? managers : Set.of();
    }

    public StoreDTO(Store store) {
        this.id = store.getId();
        this.name = store.getName();
        this.description = store.getDescription();
        this.founderId = store.getFounderId();
        this.isOpen = store.isOpen();
        this.isPermanentlyClosed = store.isPermanentlyClosed();
        this.owners = new HashSet<>(store.getOwners());
        this.managers = new HashSet<>(store.getManagers());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }


    public String getFounderId() {
        return founderId;
    }


    public Set<String> getOwners() {
        return new HashSet<>(owners);
    }


    public Set<String> getManagers() {
        return new HashSet<>(managers);
    }


    public boolean isOpen() {
        return isOpen;
    }

    public boolean isPermanentlyClosed() {
        return isPermanentlyClosed;
    }

    public void setPermanentlyClosed(boolean isPermanentlyClosed) {
        this.isPermanentlyClosed = isPermanentlyClosed;
    }
}
