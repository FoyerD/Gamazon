package Domain.Store;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.flow.component.template.Id;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;

public class Store {
    
    private String id;
    private String name;
    private String description;
    private String founderId;
    
    private Set<String> owners;
    private Set<String> managers;
    private boolean isOpen;
    

    public Store(){
        this.id = null;
        this.name = null;
        this.description = null;
        this.founderId = null;
        this.owners = Collections.synchronizedSet(new HashSet<>());
        this.managers = Collections.synchronizedSet(new HashSet<>());
        this.isOpen = true;
    }
    
    public Store(String id, String name, String description, String foudnerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.founderId = foudnerId;
        this.owners = Collections.synchronizedSet(new HashSet<>());
        this.managers = Collections.synchronizedSet(new HashSet<>());
        this.isOpen = true;
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
