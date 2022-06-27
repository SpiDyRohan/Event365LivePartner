package com.ebabu.event365live.host.fragments.rsvp_invites.model;


import java.util.Comparator;

public class MyContact {
    private String id;
    private String contactName;
    private String contactNumber;
    private boolean isChecked;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((contactNumber == null) ? 0 : contactNumber.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MyContact other = (MyContact) obj;
        if (contactNumber == null) {
            if (other.contactNumber != null)
                return false;
        } else if (!contactNumber.equals(other.contactNumber))
            return false;
        return true;
    }

    public class SortByName implements Comparator<MyContact> {

        @Override
        public int compare(MyContact contact, MyContact t1) {
            return contact.getContactName().toUpperCase().compareTo(t1.contactName.toUpperCase());
        }
    }
}
