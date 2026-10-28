package com.twfhclife.autoreport.entity.secondary;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
public class UserRoleId implements Serializable {

    private String userId;

    private String roleId;
}
