package Application.DTOs;

import java.util.List;

import Domain.Pair;


public class OfferDTO {
    private final String offerId;
    private final UserDTO member;
    private final List<UserDTO> approvedBy;
    private final List<UserDTO> approvers;
    private final ItemDTO item;

    // offered prices, from old to new
    private final List<Pair<String, Double>> offeredPrices; 
    private final boolean counterOffer; // true if the offer is a counter offer from store employees


    public OfferDTO(String offerId, UserDTO member, List<UserDTO> approvedBy, List<UserDTO> approvers, ItemDTO item, List<Pair<String, Double>> prices, boolean counterOffer) {
        this.offerId = offerId;
        this.member = member;
        this.approvedBy = approvedBy;
        this.approvers = approvers;
        this.item = item;
        this.offeredPrices = prices;
        this.counterOffer = counterOffer;
    }



    public String getId() { return offerId.toString(); }
    public UserDTO getMember() { return member; }
    public List<UserDTO> getApprovedBy() { return approvedBy; }
    public List<UserDTO> getEmployeesApprovedBy() { return approvedBy.stream().filter(u -> !u.getId().equals(member.getId())).toList(); }
    public List<UserDTO> getApprovers() { return approvers; }
    public List<UserDTO> getEmployeeApprovers() { return approvers.stream().filter(u -> !u.getId().equals(member.getId())).toList(); }
    public List<UserDTO> getRemainingEmployeesToApprove() { return getEmployeeApprovers().stream().filter(u -> !approvedBy.contains(u)).toList(); }
    public ItemDTO getItem() { return item; }
    public List<Pair<String, Double>> getOfferedPrices() { return offeredPrices; }
    public double getLastPrice() { return offeredPrices.get(offeredPrices.size() - 1).getSecond(); }
    public boolean isCounterOffer() { return counterOffer; }

    public boolean hasMemberApproved() { return approvedBy.stream().anyMatch(u -> u.getId().equals(member.getId())); }
}
