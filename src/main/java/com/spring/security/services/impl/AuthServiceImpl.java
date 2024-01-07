package com.spring.security.services.impl;

import com.spring.security.persistence.entities.UserEntity;
import com.spring.security.persistence.repositories.UserRepository;
import com.spring.security.services.IAuthService;
import com.spring.security.services.IJWTUtilityService;
import com.spring.security.services.models.dtos.LoginDTO;
import com.spring.security.services.models.dtos.ResponseDTO;
import com.spring.security.services.models.validation.UserValidation;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IJWTUtilityService jwtUtilityService;

    @Autowired
    private UserValidation userValidation;

    //es mejor usar un DTO en vez de un hashmap porque hash consume muchos recursos
    @Override
    //Login
    public HashMap<String, String> login (LoginDTO loginDTO) throws Exception {
        try {
            HashMap<String, String> jwt = new HashMap<>();
            Optional<UserEntity> user= userRepository.findByEmail(loginDTO.getEmail());
            if(user.isEmpty()){
                jwt.put("error","User not registered");
                return jwt;
            }
            //verificar la contraseña
            if(verifyPassword(loginDTO.getPassword(), user.get().getPassword())){
                jwt.put("jwt", jwtUtilityService.generateJWT(user.get().getId()));
            } else{
                jwt.put("error","Authentication failed");
            }
            return jwt;
        }catch (Exception e){
            throw new Exception(e.toString());
        }
    }
    @Override
    public ResponseDTO register(UserEntity user) throws Exception{
        try{
            ResponseDTO responseDTO= userValidation.validate(user);
            if(responseDTO.getNumOfErrors()>0){
                return responseDTO;
            }
            List<UserEntity> getAllUsers= userRepository.findAll();
            for(UserEntity repearFields : getAllUsers){
                if(repearFields!=null){
                    responseDTO.setNumOfErrors(1);
                    responseDTO.setMessage("User already exists!");
                    return responseDTO;
                }
            }
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);//encriptamos la contraseña
            user.setPassword(encoder.encode(user.getPassword())); //y se ña asignamos al usuario
            userRepository.save(user);
            responseDTO.setMessage("User created succesfully!");

            return responseDTO;
        }catch (Exception e){
            throw  new Exception(e.toString());
        }
    }
    private boolean verifyPassword(String enteredPassword, String storedPassword){
        BCryptPasswordEncoder encoder= new BCryptPasswordEncoder();
        return encoder.matches(enteredPassword, storedPassword); //si hace match true, si no false
    }
}
