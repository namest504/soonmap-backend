package soonmap.entity;

public enum AccountType {
    NAVER("NAVER"), KAKAO("KAKAO");

    private String value;

    AccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
