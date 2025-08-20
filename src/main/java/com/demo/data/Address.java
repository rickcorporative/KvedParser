package com.demo.data;

import com.demo.utils.Generator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {
    protected String address = Generator.genString(8);
    protected String city = Generator.genString(8);
    protected String state = "Arizona";
    protected String zipCode = String.valueOf(Generator.genInt(10000, 99999));

    public Address() {

    }

    public Address(String state) {
        this.state = state;
    }
}
