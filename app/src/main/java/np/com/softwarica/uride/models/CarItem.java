package np.com.softwarica.uride.models;

public class CarItem {
    private int carIcon;
    private String carName;
    private String carSeats;

    public CarItem(int carIcon, String carName, String carSeats) {
        this.carIcon = carIcon;
        this.carName = carName;
        this.carSeats = carSeats;
    }

    public int getCarIcon() {
        return carIcon;
    }

    public void setCarIcon(int carIcon) {
        this.carIcon = carIcon;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getCarSeats() {
        return carSeats;
    }

    public void setCarSeats(String carSeats) {
        this.carSeats = carSeats;
    }
}
