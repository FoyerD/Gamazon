package Application;

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

    public StoreDTO(String id, String name, String description, String founderId, boolean isOpen, Set<String> owners, Set<String> managers) {  
        this.id = id;
        this.name = name;
        this.description = description;
        this.founderId = founderId;
        this.isOpen = isOpen;
        this.owners = owners != null ? owners : Set.of();
        this.managers = managers != null ? managers : Set.of();
    }

    public StoreDTO(Store store) {
        this.id = store.getId();
        this.name = store.getName();
        this.description = store.getDescription();
        this.founderId = store.getFounderId();
        this.isOpen = store.isOpen();
        this.owners = new HashSet<>(store.getOwners());
        this.managers = new HashSet<>(store.getManagers());
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

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFounderId() {
        return founderId;
    }

    public void setFounderId(String founderId) {
        this.founderId = founderId;
    }

    public Set<String> getOwners() {
        return new HashSet<>(owners);
    }

    public void setOwners(Set<String> owners) {
        this.owners = owners != null ? new HashSet<>(owners) : new HashSet<>();
    }

    public Set<String> getManagers() {
        return new HashSet<>(managers);
    }

    public void setManagers(Set<String> managers) {
        this.managers = managers != null ? new HashSet<>(managers) : new HashSet<>();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
}
