package com.sangampradhan.nordicpaws.models;

public class Pet {
    private String id;
    private String name;
    private String breedAndGender;
    private String statusBadge;
    private int avatarResId;
    private String tagType;
    private String tagAge;
    private String tagOther;
    private String stat1Val;
    private String stat2Label;
    private String stat2Val;
    private int stat2IconResId;

    public Pet(String id, String name, String breedAndGender, String statusBadge, int avatarResId, 
               String tagType, String tagAge, String tagOther, 
               String stat1Val, String stat2Label, String stat2Val, int stat2IconResId) {
        this.id = id;
        this.name = name;
        this.breedAndGender = breedAndGender;
        this.statusBadge = statusBadge;
        this.avatarResId = avatarResId;
        this.tagType = tagType;
        this.tagAge = tagAge;
        this.tagOther = tagOther;
        this.stat1Val = stat1Val;
        this.stat2Label = stat2Label;
        this.stat2Val = stat2Val;
        this.stat2IconResId = stat2IconResId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getBreedAndGender() { return breedAndGender; }
    public String getStatusBadge() { return statusBadge; }
    public int getAvatarResId() { return avatarResId; }
    public String getTagType() { return tagType; }
    public String getTagAge() { return tagAge; }
    public String getTagOther() { return tagOther; }
    public String getStat1Val() { return stat1Val; }
    public String getStat2Label() { return stat2Label; }
    public String getStat2Val() { return stat2Val; }
    public int getStat2IconResId() { return stat2IconResId; }
}
