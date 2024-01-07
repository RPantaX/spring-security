package com.spring.security.services.models.validation;

import com.spring.security.persistence.entities.UserEntity;
import com.spring.security.services.models.dtos.ResponseDTO;

public class UserValidation {

    public ResponseDTO validate(UserEntity user){
        ResponseDTO response = new ResponseDTO();

        response.setNumOfErrors(0);
        //FIRST NAME
        if(user.getFirstName()==null ||
                user.getFirstName().length()<3 ||
                user.getFirstName().length()>15
        ){
            response.setNumOfErrors(response.getNumOfErrors()+1);
            response.setMessage("The first name cannot be null, it also has to be between 3 and 15 characters long");
        }
        //LAST NAME
        if(user.getLast_name()==null ||
                user.getLast_name().length()<3 ||
                user.getLast_name().length()>30
        ){
            response.setNumOfErrors(response.getNumOfErrors()+1);
            response.setMessage("The last name cannot be null, it also has to be between 3 and 30 characters long");
        }
        //EMAIL
        if(user.getEmail()==null ||
                !user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        ){
            response.setNumOfErrors(response.getNumOfErrors()+1);
            response.setMessage("The email is invalid");
        }
        //PASSWORD
        if(user.getPassword()==null ||
                !user.getPassword().matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,16}$")
        ){
            response.setNumOfErrors(response.getNumOfErrors()+1);
            response.setMessage("The password must be between 8 and 16 characters long, minimum one number, one lowercase and one uppercase letter");
        }
        return response;
    }

}
