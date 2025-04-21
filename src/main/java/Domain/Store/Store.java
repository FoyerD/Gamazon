package Domain.Store;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import Domain.User.User;

public class Store {
    
    private String id;
    private String name;
    private String descripsion;
    private String foudnerId;
    private Set<String> owners;
    private Set<String> managers;
    private boolean isOpen;
    

    public Store(){
        this.id = null;
        this.name = null;
        this.descripsion = null;
        this.foudnerId = null;
        this.owners = Collections.synchronizedSet(new HashSet<>());
        this.managers = Collections.synchronizedSet(new HashSet<>());
        this.isOpen = true;
    }
    
    public Store(String id, String name, String descripsion, String foudnerId) {
        this.id = id;
        this.name = name;
        this.descripsion = descripsion;
        this.foudnerId = foudnerId;
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

    public String getDescripsion() {
        return descripsion;
    }
    public void setDescripsion(String descripsion) {
        this.descripsion = descripsion;
    }

    public String getFoudnerId() {
        return foudnerId;
    }
    public void setFoudnerId(String foudnerId) {
        this.foudnerId = foudnerId;
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

}
