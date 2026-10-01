package com.sangampradhan.nordicpaws.models;

public class Pet {
    private String id;
    private String name;
    private String breedAndGender;
    private String statusBadge;
    private int avatarResId;
    private String localImagePath;
    private String tagType;
    private String tagAge;
    private String tagOther;
    private String stat1Val;
    private String stat2Label;
    private String stat2Val;
    private int stat2IconResId;

    // Default no-arg constructor required for Firestore
    public Pet() {
    }

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
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBreedAndGender() { return breedAndGender; }
    public void setBreedAndGender(String breedAndGender) { this.breedAndGender = breedAndGender; }

    public String getStatusBadge() { return statusBadge; }
    public void setStatusBadge(String statusBadge) { this.statusBadge = statusBadge; }

    public int getAvatarResId() { return avatarResId; }
    public void setAvatarResId(int avatarResId) { this.avatarResId = avatarResId; }

    public String getLocalImagePath() { return localImagePath; }
    public void setLocalImagePath(String localImagePath) { this.localImagePath = localImagePath; }

    public String getTagType() { return tagType; }
    public void setTagType(String tagType) { this.tagType = tagType; }

    public String getTagAge() { return tagAge; }
    public void setTagAge(String tagAge) { this.tagAge = tagAge; }

    public String getTagOther() { return tagOther; }
    public void setTagOther(String tagOther) { this.tagOther = tagOther; }

    public String getStat1Val() { return stat1Val; }
    public void setStat1Val(String stat1Val) { this.stat1Val = stat1Val; }

    public String getStat2Label() { return stat2Label; }
    public void setStat2Label(String stat2Label) { this.stat2Label = stat2Label; }

    public String getStat2Val() { return stat2Val; }
    public void setStat2Val(String stat2Val) { this.stat2Val = stat2Val; }

    public int getStat2IconResId() { return stat2IconResId; }
    public void setStat2IconResId(int stat2IconResId) { this.stat2IconResId = stat2IconResId; }
}
