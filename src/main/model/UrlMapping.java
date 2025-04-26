package model;

import jdk.jfr.DataAmount;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UrlMapping {

    private String shortUrlId; //Partition Key in our dynamodb table

    private String longUrl; // Global Secondary Index

    private LocalDateTime createdTime;

    private LocalDateTime expiryTime;

    private String createdBy;

}
