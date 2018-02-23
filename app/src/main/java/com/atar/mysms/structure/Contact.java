package com.atar.mysms.structure;

/*
 * Created by Atar on 03-Feb-18.
 */

public class Contact {

    private long mId;
    public long getId() {
        return mId;
    }
    public void setId(long id) {
        mId = id;
    }

    private String mName;
    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }

    private String mDisplayedPhoneNumber;
    public String getDisplayedPhoneNumber() {
        return mDisplayedPhoneNumber;
    }
    public void setDisplayedPhoneNumber(String displayedPhoneNumber) {
        mDisplayedPhoneNumber = displayedPhoneNumber;
    }

    private String mNormalizedPhoneNumber;
    public String getNormalizedPhoneNumber() {
        return mNormalizedPhoneNumber;
    }
    public void setNormalizedPhoneNumber(String normalizedPhoneNumber) {
        mNormalizedPhoneNumber = normalizedPhoneNumber;
    }

    private String imageUri;
    public String getImageUri() {
        return imageUri;
    }
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  Contact){
            Contact contact = (Contact)obj;
            return contact.getNormalizedPhoneNumber().equals(getNormalizedPhoneNumber())
                    && contact.getId() == getId();
        } else {
            return false;
        }
    }
}
