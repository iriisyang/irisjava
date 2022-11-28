package com.nighthawk.spring_portfolio.mvc.person;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.format.annotation.DateTimeFormat;

import com.vladmihalcea.hibernate.type.json.JsonType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/*
Person is a POJO, Plain Old Java Object.
First set of annotations add functionality to POJO
--- @Setter @Getter @ToString @NoArgsConstructor @RequiredArgsConstructor
The last annotation connect to database
--- @Entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@TypeDef(name="json", typeClass = JsonType.class)
public class Person {
    
    // automatic unique identifier for Person record
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // email, password, roles are key attributes to login and authentication
    @NotEmpty
    @Size(min=5)
    @Column(unique=true)
    @Email
    private String email;

    @NotEmpty
    private String password;

    // @NonNull, etc placed in params of constructor: "@NonNull @Size(min = 2, max = 30, message = "Name (2 to 30 chars)") String name"
    @NonNull
    @Size(min = 2, max = 30, message = "Name (2 to 30 chars)")
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;

    @Column(unique=false)
    private int height;

    @Column(unique=false)
    private int weight;
    

    /* HashMap is used to store JSON for daily "stats"
    "stats": {
        "2022-11-13": {
            "calories": 2200,
            "steps": 8000
        }
    }
    */
    @Type(type="json")
    @Column(columnDefinition = "jsonb")
    private Map<String,Map<String, Object>> stats = new HashMap<>(); 
    

    // Constructor used when building object from an API
    public Person(String email, String password, String name, Date dob, int height, int weight) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.dob = dob;
        this.height = height;
        this.weight = weight;
    }

    // A custom getter to return age from dob attribute
    public int getAge() {
        if (this.dob != null) {
            LocalDate birthDay = this.dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int age = Period.between(birthDay, LocalDate.now()).getYears();
            return age; }
        return -1;
    }

    public void addDailySteps(int DailySteps, int Calories, String Day){
        if(stats.containsKey(Day)){
            //if it does, take stats>Day>steps and add DailySteps to it
            Map daysteps = stats.get(Day);
            daysteps.replace("calories", (int) daysteps.get("calories") + Calories);
            daysteps.replace("steps", (int) daysteps.get("steps") + DailySteps);
            stats.replace(Day, daysteps);
        }
        else{
            //if it doesn't, create a new Object with calories 0 steps DailySteps and put in stats at key Day
            HashMap newDay = new HashMap();
            newDay.put("calories", Calories);
            newDay.put("steps", DailySteps);
            stats.put(Day, newDay);
        }
    }

    public int activeDays(){
       return stats.size();
    }

    public int averageSteps(){
        Iterator<Map.Entry<String, Map<String, Object>>> iter = stats.entrySet().iterator();
        int totalSteps = 0;
        while(iter.hasNext()){
            Map.Entry<String, Map<String, Object>> entry = iter.next();
            totalSteps+=(int)entry.getValue().get("steps");
        }
        return totalSteps/stats.size();
    }

    public double getBmi() {
        double bmi = (703 * this.weight / Math.pow(this.height,2));
        return bmi;
    }

    public String getBmiToString() {
        return("{ \"bmi\": " + this.getBmi() +"}");
    }


}