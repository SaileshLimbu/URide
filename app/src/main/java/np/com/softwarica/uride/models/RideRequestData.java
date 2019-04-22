package np.com.softwarica.uride.models;

import java.io.Serializable;
import java.util.List;


public class RideRequestData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String fullName;
    private String etaTime;
    private String etaEarning;
    private String etaDistance;
    private String pickupAddress;
    private String dropoffAddress;
    private List<Double> pickupLocation;
    private List<Double> dropoffLocation;
    private String tripKey;
    private String paymentOption;
    private String profilePic;

    public RideRequestData(String fullName, String etaTime, String etaEarning, String etaDistance, String pickupAddress, String dropoffAddress, List<Double> pickupLocation, List<Double> dropoffLocation, String tripKey, String paymentOption, String profilePic) {
        this.fullName = fullName;
        this.etaTime = etaTime;
        this.etaEarning = etaEarning;
        this.pickupAddress = pickupAddress;
        this.dropoffAddress = dropoffAddress;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.tripKey = tripKey;
        this.paymentOption = paymentOption;
        this.profilePic = profilePic;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEtaTime() {
        return etaTime;
    }

    public void setEtaTime(String etaTime) {
        this.etaTime = etaTime;
    }

    public String getEtaEarning() {
        return etaEarning;
    }

    public void setEtaEarning(String etaEarning) {
        this.etaEarning = etaEarning;
    }

    public String getEtaDistance() {
        return etaDistance;
    }

    public void setEtaDistance(String etaDistance) {
        this.etaDistance = etaDistance;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDropoffAddress() {
        return dropoffAddress;
    }

    public void setDropoffAddress(String dropoffAddress) {
        this.dropoffAddress = dropoffAddress;
    }

    public List<Double> getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(List<Double> pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public List<Double> getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(List<Double> dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    public String getTripKey() {
        return tripKey;
    }

    public void setTripKey(String tripKey) {
        this.tripKey = tripKey;
    }

    public String getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(String paymentOption) {
        this.paymentOption = paymentOption;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
