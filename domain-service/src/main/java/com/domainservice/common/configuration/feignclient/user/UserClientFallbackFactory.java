//package com.domainservice.common.configuration.feignclient.user;
//
//import org.springframework.cloud.openfeign.FallbackFactory;
//import org.springframework.stereotype.Component;
//import com.common.exception.CustomException;
//import com.common.exception.vo.ProductPostExceptionCode;
//import com.common.exception.vo.UserExceptionCode;
//import com.domainservice.domain.post.post.exception.ProductPostException;
//import com.domainservice.domain.post.post.model.dto.UserView;
//import feign.FeignException;
//import lombok.extern.slf4j.Slf4j;
//
//@Component
//@Slf4j
//public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
//
//    @Override
//    public UserClient create(Throwable cause) {
//        return new UserClient() {
//            @Override
//            public UserView getUserById(String userId) {
//                log.error("UserClient 호출 실패: {}", cause.getMessage());
//
//                // 통신 과정에서 user가 존재하지 않아 예외가 날라올 경우 처리 (404)
//                if (cause instanceof FeignException.NotFound) {
//                    throw new CustomException(UserExceptionCode.USER_NOT_FOUND.getMessage());
//                }
//
//                // 기타  Feign 통신상의 오류 발생 시 예외 처리
//                throw new ProductPostException(ProductPostExceptionCode.EXTERNAL_API_ERROR);
//            }
//        };
//    }
//}