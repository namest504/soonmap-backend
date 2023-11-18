package soonmap.entity;

public enum ImageType {
    NOTICE("NOTICE"), INFO("INFO"), FLOOR("FLOOR");

    private String value;

    ImageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
