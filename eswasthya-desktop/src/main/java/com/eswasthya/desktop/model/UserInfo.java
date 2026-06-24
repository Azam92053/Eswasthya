package com.eswasthya.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
    private Long   id;
    private String username;
    private String email;
    private String name;
    private Integer age;
    private String gender;
    private String role;
    private String createdAt;

    public Long    getId()        { return id; }
    public String  getUsername()  { return username; }
    public String  getEmail()     { return email; }
    public String  getName()      { return name; }
    public Integer getAge()       { return age; }
    public String  getGender()    { return gender; }
    public String  getRole()      { return role; }
    public String  getCreatedAt() { return createdAt; }

    public void setId(Long id)              { this.id = id; }
    public void setUsername(String u)       { this.username = u; }
    public void setEmail(String e)          { this.email = e; }
    public void setName(String n)           { this.name = n; }
    public void setAge(Integer a)           { this.age = a; }
    public void setGender(String g)         { this.gender = g; }
    public void setRole(String r)           { this.role = r; }
    public void setCreatedAt(String c)      { this.createdAt = c; }

    public String getInitial() {
        return (name != null && !name.isEmpty())
               ? String.valueOf(name.charAt(0)).toUpperCase() : "?";
    }
}
