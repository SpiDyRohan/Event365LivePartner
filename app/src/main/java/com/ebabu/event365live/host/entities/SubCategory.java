package com.ebabu.event365live.host.entities;

public class SubCategory {

    public SubCategory() {
    }

    public SubCategory(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    private  int id;
    private String subCategoryName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    @Override
    public String toString() {
        return "SubCategory{" +
                "id=" + id +
                ", subCategoryName='" + subCategoryName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o){

        if(o instanceof SubCategory){
            SubCategory subCategory=(SubCategory)o;
            return subCategory.getSubCategoryName().equalsIgnoreCase(subCategoryName);
        }
        return false;
    }
}
