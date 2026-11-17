package jpabook.jpashop.domain;


import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter // 변경 불가능 , Setter X
public class Address {

    private String city;
    private String street;
    private String zipcode;
}
