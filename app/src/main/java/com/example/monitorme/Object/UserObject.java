package com.example.monitorme.Object;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;

public class UserObject  implements Serializable {

    // getters and setters for everything in the user table
    private String  uid,
            name = "--",
            phone = "--",
            image = "default",
            status = "--",
            notificationKey  = "";

    private Boolean selected = false;

    public UserObject(String uid){
        this.uid = uid;
        this.name = "";
    }
    public UserObject(String uid, String name, String phone, String image, String status){
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.image = image;
        this.status = status;
    }

    public void parseObject(DataSnapshot dataSnapshot){
        if(!dataSnapshot.exists()){ return; }

        // get values based on child name and change them to a string
        if(dataSnapshot.child("name").getValue()!=null)
            name = dataSnapshot.child("name").getValue().toString();
        if(dataSnapshot.child("image").getValue()!=null)
            image = dataSnapshot.child("image").getValue().toString();
        if(dataSnapshot.child("status").getValue()!=null)
            status = dataSnapshot.child("status").getValue().toString();
        if(dataSnapshot.child("phone").getValue()!=null)
            phone = dataSnapshot.child("phone").getValue().toString();
    }
    public String getUid() {
        return uid;
    }
    public String getPhone() {
        return phone;
    }
    public String getName() {
        return name;
    }
    public String getNotificationKey() {
        return notificationKey;
    }
    public String getImage() {
        return image;
    }
    public Boolean getSelected() {
        return selected;
    }
    public String getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // boolean value to check the user id equals the right user id
    public boolean equals(Object o) {
        if(((UserObject) o).getUid().equals(uid))
            return true;
        else
            return false;
    }

}
