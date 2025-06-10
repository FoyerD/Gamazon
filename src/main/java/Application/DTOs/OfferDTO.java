package Application.DTOs;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import Domain.Pair;


public class OfferDTO {
    private final String offerId;
    private final UserDTO member;
    private final Set<UserDTO> approvedBy;
    private final Set<UserDTO> approvers;
    private final ItemDTO item;
    private final boolean isAccepted;
    // offered prices, from old to new
    private final List<Pair<String, Double>> offeredPrices; 
    private final boolean counterOffer; // true if the offer is a counter offer from store employees


    public OfferDTO(String offerId, UserDTO member, Set<UserDTO> approvedBy, Set<UserDTO> approvers, ItemDTO item, List<Pair<String, Double>> prices, boolean counterOffer, boolean isAccepted) {
        this.offerId = offerId;
        this.member = member;
        this.approvedBy = approvedBy;
        this.approvers = approvers;
        this.item = item;
        this.offeredPrices = prices;
        this.counterOffer = counterOffer;
        this.isAccepted = isAccepted;
    }

    public String getId() { return offerId.toString(); }
    public UserDTO getMember() { return member; }
    public Set<UserDTO> getApprovedBy() { return approvedBy; }
    public Set<UserDTO> getEmployeesApprovedBy() { return approvedBy.stream().filter(u -> !u.getId().equals(member.getId())).collect(Collectors.toSet()); }
    public Set<UserDTO> getApprovers() { return approvers; }
    public List<UserDTO> getEmployeeApprovers() { return approvers.stream().filter(u -> !u.getId().equals(member.getId())).toList(); }
    public Set<UserDTO> getRemainingEmployeesToApprove() { return getEmployeeApprovers().stream().filter(u -> !approvedBy.contains(u)).collect(Collectors.toSet()); }
    public ItemDTO getItem() { return item; }
    public List<Pair<String, Double>> getOfferedPrices() { return offeredPrices; }
    public boolean isAccepted() { return isAccepted; }
    public double getLastPrice() { return offeredPrices.get(offeredPrices.size() - 1).getSecond(); }
    public boolean isCounterOffer() { return counterOffer; }

    public boolean hasMemberApproved() { return approvedBy.stream().anyMatch(u -> u.getId().equals(member.getId())); }
    public List<Pair<String, Double>> getUsernamesPrice() {
        return offeredPrices.stream().map(p -> {
            String user = approvers.stream().filter(u -> u.getId().equals(p.getFirst())).map(UserDTO::getUsername).findAny().orElse("Unknown");
            return new Pair<>(user, p.getSecond());
        }).toList();

    }

    public boolean hasUserApproved(String userId) {
        return this.approvedBy.stream().anyMatch(u -> u.getId().equals(userId));
    } 
}
