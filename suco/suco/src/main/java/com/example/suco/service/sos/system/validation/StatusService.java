// package com.example.suco.service.sos.system.validation;

// import org.springframework.stereotype.Service;

// @Service
// public class StatusService {

//     public boolean isValidTransition(
//             String current,
//             String next
//     ) {

//         switch (current) {

//             case "CHO_XU_LY":
//                 return "DANG_XU_LY".equals(next)
//                         || "TU_CHOI".equals(next);

//             case "DANG_XU_LY":
//                 return "HOAN_THANH".equals(next);
            
//             default:
//                 return false;
//         }
//     }
// }