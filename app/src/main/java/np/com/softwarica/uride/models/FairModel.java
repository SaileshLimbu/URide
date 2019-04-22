package np.com.softwarica.uride.models;

public class FairModel {
    private double regular;
    private double bike;

    public FairModel(double regular, double bike) {
        this.regular = regular;
        this.bike = bike;
    }

    public double getRegular() {
        return regular;
    }

    public void setRegular(double regular) {
        this.regular = regular;
    }

    public double getBike() {
        return bike;
    }

    public void setBike(double bike) {
        this.bike = bike;
    }
}
