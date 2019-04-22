package np.com.softwarica.uride.models;

import java.io.Serializable;


public class CardDetails implements Serializable{
    private static final long serialVersionUID = 1L;
    private String cardNumber;
    private int expMonth;
    private int expYear;
    private String cvc;
    private String key;
    private String dataKey;

    public CardDetails(){

    }

    public CardDetails(String cardNumber, int expMonth, int expYear, String cvc, String key, String dataKey) {
        this.cardNumber = cardNumber;
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.cvc = cvc;
        this.key = key;
        this.dataKey = key;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(int expMonth) {
        this.expMonth = expMonth;
    }

    public int getExpYear() {
        return expYear;
    }

    public void setExpYear(int expYear) {
        this.expYear = expYear;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }
}
