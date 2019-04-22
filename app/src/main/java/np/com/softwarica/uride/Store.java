package np.com.softwarica.uride;

import java.util.ArrayList;

import np.com.softwarica.uride.models.CardDetails;
import np.com.softwarica.uride.models.FairModel;

public class Store {
    private static final Store ourInstance = new Store();
    private ArrayList<CardDetails> listCardDetails;
    private FairModel fairDetails;
    private float commission;

    public static Store getInstance() {
        return ourInstance;
    }

    public FairModel getFairDetails() {
        return fairDetails;
    }

    public void setFairDetails(FairModel fairDetails) {
        this.fairDetails = fairDetails;
    }

    public float getCommission() {
        return commission;
    }

    public void setCommission(float commission) {
        this.commission = commission;
    }

    public void clear() {
        listCardDetails.clear();
    }

    public void addCardToArray(CardDetails item) {
        listCardDetails.add(item);
    }

    public CardDetails getCard(int i) {
        return listCardDetails.get(i);
    }

    public ArrayList<CardDetails> getAllCardsDetail() {
        return this.listCardDetails;
    }
}
