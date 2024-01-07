package com.spring.security.services.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.spring.security.services.IJWTUtilityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;

/*CREACIÓN DE JWT Y LA VALIDACIÓN DEL JWT*/
@Service
public class JWTUtilityServiceImpl implements IJWTUtilityService {
    //AÑADIMOS LOS VALUE, EN EL QUE PASAMOS LAS RUTAS DE LA LLAVE PRIVADA Y PÚBLICA
    @Value("classpath:jwtKeys/private_key.pem")
    private Resource privateKeyResource;

    @Value("classpath:jwtKeys/public_key.pem")
    private Resource publicKeyResource;

    /*2 metodos publicos: para generar el JWT Y el otro para validar que el JWT que nos pasan es correcto*/
    //METODO PARA GENERAR EL JWT
    @Override
    public String generateJWT(Long userId) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
        PrivateKey privateKey= loadPrivateKey(privateKeyResource);

        JWSSigner signer = new RSASSASigner(privateKey); //firmar la informacion

        Date now= new Date();
        JWTClaimsSet claimsSet= new JWTClaimsSet.Builder() //definimos los claims (payload)
                .subject(userId.toString())
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + 14400000)) //+4horas
                .build();

        SignedJWT signedJWT= new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet); //HEADER
        signedJWT.sign(signer); //le pasamos la firma de la privateKey
        return signedJWT.serialize();
    }
    //METODO PARA VALIDAR EL JWT QUE NOS PASEN
    @Override
    public JWTClaimsSet parseJWT(String jwt) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException { //aqui nos pasan el token de quien se quiera autenticar con nuestra aplicación
        PublicKey publicKey= loadPublicKey(publicKeyResource);
        SignedJWT signedJWT= SignedJWT.parse(jwt);
        JWSVerifier verifier= new RSASSAVerifier((RSAPublicKey) publicKey);
        if(!signedJWT.verify(verifier)){
            throw new JOSEException(("Invalid signature"));
        }
        JWTClaimsSet claimsSet= signedJWT.getJWTClaimsSet();

        if(claimsSet.getExpirationTime().before(new Date())){
            throw new JOSEException("Expired token");
        }

        return claimsSet;
    }

    /*MÉTODOS QUE LEEN LA LLAVE PRIVADA Y PUBLICA QUE HEMOS CREADO*/

    //Método que lee la llave privada
    private PrivateKey loadPrivateKey(Resource resource) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes= Files.readAllBytes(Paths.get(resource.getURI())); //obtenemos la ruta de la llave publica.
        String privateKeyPEM = new String(keyBytes, StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "") //para que lea solo la parte codificada
                .replaceAll("\\s", ""); // "\\s" elimina los espacios en blanco

        byte[] decodedKey = Base64.getDecoder().decode(privateKeyPEM);//decodificamos lo que queda dentro del string, en base 64 porque nuestra privateKey esta en base 64
        KeyFactory keyFactory= KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
    }
    //Método que lee la llave pública
    private PublicKey loadPublicKey(Resource resource) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes= Files.readAllBytes(Paths.get(resource.getURI())); //obtenemos la ruta de la llave publica.
        String publicKeyPEM = new String(keyBytes, StandardCharsets.UTF_8)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "") //para que lea solo la parte codificada
                .replaceAll("\\s", ""); // "\\s" elimina los espacios en blanco

        byte[] decodedKey = Base64.getDecoder().decode(publicKeyPEM);//decodificamos lo que queda dentro del string, en base 64 porque nuestra publicKey esta en base 64
        KeyFactory keyFactory= KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
    }


}
