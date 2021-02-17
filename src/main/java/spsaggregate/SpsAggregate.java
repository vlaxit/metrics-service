package spsaggregate;

public class SpsAggregate {

    private String device;
    private long sps;
    private String title;
    private String country;
    private long time;

    public SpsAggregate(Sps sps, Long count) {
        this.device = sps.getDevice();
        this.sps = count;
        this.title = sps.getTitle();
        this.country = sps.getCountry();
        this.time = sps.getTime();
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public long getSps() {
        return sps;
    }

    public void setSps(long sps) {
        this.sps = sps;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
