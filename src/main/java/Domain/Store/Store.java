package Domain.Store;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
        return new HashSet<>(this.owners);
    }
    public boolean addOwner(String userId) {
        return this.owners.add(userId);
    }

    public Set<String> getManagers() {
        return new HashSet<>(this.managers);
    }

    public boolean isOpen() {
        return isOpen;
    }

}
