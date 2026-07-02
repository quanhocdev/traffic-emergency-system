package com.example.suco.security;


import com.example.suco.model.TruSo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import com.example.suco.service.xacthuc.user.token.FirebaseService;
import java.security.Principal;
import java.util.Map;


@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

        

          private static final Logger log =
            LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
            

    @Autowired
    FirebaseService firebaseService;


    public WebSocketAuthInterceptor(FirebaseService firebaseService
) {
        this.firebaseService = firebaseService;
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
           log.info("STEP 1 - Authorization header exists: {}", header != null);


            if(header != null &&
                    header.startsWith("Bearer ")) {


                log.info("STEP 2 - Firebase verify");

String uid;

try {

    uid = firebaseService.extractUid(header);

    log.info("STEP 3 - Firebase UID = {}", uid);

} catch (Exception e) {

    log.error("Firebase verify failed", e);

    throw e;
}

accessor.setUser(() -> uid);

log.info("STEP 4 - User set");

            }

        }


        return message;

    }

}