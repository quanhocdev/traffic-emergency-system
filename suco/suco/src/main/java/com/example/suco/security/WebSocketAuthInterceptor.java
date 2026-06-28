package com.example.suco.security;


import com.example.suco.model.TruSo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;


@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {


    private final JwtService jwtService;


    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }



    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {


        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );


        if(accessor == null){
            return message;
        }



        if(accessor.getCommand() == StompCommand.CONNECT){


            System.out.println("========== WS CONNECT ==========");

            System.out.println(
                    "Headers : "
                    + accessor.toNativeHeaderMap()
            );


            Map<String,Object> attrs =
                    accessor.getSessionAttributes();



            /*
             * TRỤ SỞ WEB
             */

            if(attrs != null){

                Object obj =
                        attrs.get("currentTruSo");


                if(obj instanceof TruSo truSo){


                    accessor.setUser(
                            new Principal(){

                                @Override
                                public String getName(){
                                    return String.valueOf(
                                            truSo.getId()
                                    );
                                }

                            }
                    );


                    System.out.println(
                            "WS Login SESSION "
                            + truSo.getTenTruSo()
                    );


                    return message;
                }
            }





            /*
             * ANDROID JWT
             */


            String header =
                    accessor.getFirstNativeHeader(
                            "Authorization"
                    );


            if(header != null &&
                    header.startsWith("Bearer ")) {


                String token =
                        header.substring(7);


                Claims claims =
                        jwtService.extractAllClaims(token);



                String uid =
                        claims.getSubject();



                if(uid == null){
                    throw new IllegalArgumentException(
                            "JWT missing subject"
                    );
                }


                accessor.setUser(
                        new Principal(){

                            @Override
                            public String getName(){
                                return uid;
                            }

                        }
                );


                System.out.println(
                        "WS Login JWT "
                        + uid
                );

            }

        }


        return message;

    }

}