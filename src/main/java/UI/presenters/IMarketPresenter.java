package UI.presenters;

/**
 * Interface for presenting high-level marketplace and store administrative actions.
 */
public interface IMarketPresenter {

    /**
     * Opens the entire marketplace, making it accessible to users.
     * This might initialize system resources, load default stores, or unlock features.
     */
    void openMarketplace();

    /**
     * Closes a specific store in the marketplace.
     * Prevents further operations such as purchases or product browsing in the given store.
     *
     * @param storeName the name of the store to close
     */
    void closeStore(String storeName);
}