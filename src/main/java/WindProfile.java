public class WindProfile {

    private final double speed;
    private final int angle;

    public WindProfile(double speed, int angle) {
        this.speed = speed;
        this.angle = angle;
    }

    public double getSpeed() {
        return speed;
    }

    public int getAngle() {
        return angle;
    }
}
