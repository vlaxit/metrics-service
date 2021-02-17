package spsaggregate;

import java.util.Objects;

public class Sps implements Timeable {

    private String sev;
    private String device;
    private String title;
    private String country;
    private long time;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getSev() {
        return sev;
    }

    public void setSev(String sev) {
        this.sev = sev;
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

    @Override
    public String toString(){
        return "Device: " + device + ", sev: " + sev + ", title: " + title + ", country: " + country
            + ", time: " + time;
    }

    public boolean isSuccess() {
        return sev != null && "success".equals(sev.toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Sps sps = (Sps) o;
        return time == sps.time &&
            Objects.equals(device, sps.device) &&
            sev.equals(sps.sev) &&
            Objects.equals(title, sps.title) &&
            Objects.equals(country, sps.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, sev, title, country, time);
    }
}
