package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    public String id;
    public String username;
    @JsonProperty("registration_date")
    public long registrationDate;
    @JsonProperty("display_name")
    public String displayName;
    @JsonProperty("limited_account")
    public String limitedAccount;
    public JsonNode location;
    public String role;
    @JsonProperty("chosen_role")
    public String chosenRole;
    public JsonNode status;
    @JsonProperty("primary_currency")
    public JsonNode primaryCurrency;
    public String date;
    public String city;
    public String st;
    public String pCurr;

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        System.out.println(id);this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        System.out.println(username);
        this.username = username;
    }

    public long getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(long registrationDate) {
        Date date = new Date(this.registrationDate * 1000);
        DateFormat formatter = new SimpleDateFormat("MMM dd yyyy");
        this.date = formatter.format(date);
        System.out.println(date);
        this.registrationDate = registrationDate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLimitedAccount() {
        return limitedAccount;
    }

    public void setLimitedAccount(String limitedAccount) {
        this.limitedAccount = limitedAccount;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getChosenRole() {
        return chosenRole;
    }

    public void setChosenRole(String chosenRole) {
        this.chosenRole = chosenRole;
    }

    public void setLocation(JsonNode location){this.location = location;
        this.city = this.location.get("country").get("name").asText();
    }
    public void setStatus(JsonNode status){this.status = status;
        this.st = this.status.get("email_verified").asText();
    }
    public void setPrimaryCurrency(JsonNode primaryCurrency){this.primaryCurrency = primaryCurrency;
    this.pCurr = this.primaryCurrency.get("name").asText();}

    public JsonNode getLocation() {
        return location;
    }

    public JsonNode getStatus() {
        return status;
    }

    public JsonNode getPrimaryCurrency() {
        return primaryCurrency;
    }

    @Override
    public String toString() {
        return  "ID =" + id + "<br />"+
                "User Name =" + username + "<br />"+
                "Registration_Date =" + date +"<br />"+
                "Limited_Account =" + limitedAccount +"<br />"+
                "Display_Name = " + displayName +"<br />"+
                "Location = " + city  +"<br />"+
                "Role =" + role +"<br />"+
                "Chosen_Role =" + chosenRole +"<br />"+
                "Status =" + st +"<br />"+
                "Primary_Currency=" + pCurr + "<br />";

    }
}
