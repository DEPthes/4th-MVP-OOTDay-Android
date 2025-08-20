package com.bottari.ootday.data.repository

import android.util.Log
import com.bottari.ootday.data.model.signupModel.SignUpData
import com.bottari.ootday.data.service.MemberApiService
import com.bottari.ootday.data.service.SmsApiService
import com.bottari.ootday.domain.model.DataStoreManager
import com.bottari.ootday.domain.repository.dataClass.AuthCheckResponse
import com.bottari.ootday.domain.repository.dataClass.PhoneNumberAuthResponse
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import com.bottari.ootday.data.model.loginModel.LoginRequest
import com.bottari.ootday.data.model.loginModel.ProfileResponse
import com.bottari.ootday.domain.model.WithdrawRequest
import kotlinx.coroutines.flow.first

class AuthRepository(private val context: Context) {
    private val dataStoreManager = DataStoreManager(context)

    private val retrofitClient = RetrofitClient(context)

    private val smsApiService: SmsApiService by lazy {
        retrofitClient.createService<SmsApiService>()
    }
    private val memberApiService: MemberApiService by lazy {
        retrofitClient.createService<MemberApiService>()
    }

    //로그인 모킹함수
//    suspend fun login(
//        username: String,
//        password: String,
//    ): Result<Boolean> {
//        // TODO: 실제 API 호출 로직 구현
//        // 예시: ApiService를 통한 서버 통신
//
//        // 임시 구현 (실제로는 서버 응답에 따라 처리)
//        return if (username == "test" && password == "1234") {
//            Result.success(true)
//        } else {
//            Result.failure(Exception("아이디 혹은 비밀번호가 잘못되었습니다. 다시 시도해주세요."))
//        }
//    }

    suspend fun login(request: LoginRequest): Result<Unit> {
        return try {
            val response = memberApiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.token
                // 1. DataStore에 영구 저장
                dataStoreManager.saveToken(token)
                // 2. TokenManager 변수(캐시)에 임시 저장
                TokenManager.authToken = token
                dataStoreManager.saveRememberMe(request.rememberMe)
                Result.success(Unit)
            } else {
                Result.failure(Exception("아이디 또는 비밀번호가 일치하지 않습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    // 휴대폰 번호 인증 요청 모킹 함수
//    suspend fun requestPhoneNumberAuth(phoneNumber: String): Result<PhoneNumberAuthResponse> {
//        // 백엔드 명세가 나오기 전까지 실제 API 호출 대신 가짜 응답을 반환합니다.
//        delay(500) // 실제 네트워크 지연을 흉내내기 위해 잠시 대기합니다.
//        return Result.success(PhoneNumberAuthResponse(true, "인증번호가 모의 발송되었습니다."))
//    }
//
//    // 인증 번호 확인 모킹 함수
//    suspend fun checkPhoneNumberAuth(
//        phoneNumber: String,
//        authCode: String,
//    ): Result<AuthCheckResponse> {
//        // 현재는 어떤 인증번호를 입력해도 성공으로 처리
//        delay(300) // 실제 네트워크 지연을 흉내내기 위해 잠시 대기
//        val isCodeValid = authCode == "123456" // 예시: "123456"을 올바른 인증번호로 가정
//        return if (isCodeValid) {
//            Result.success(AuthCheckResponse(true, "인증되었습니다."))
//        } else {
//            Result.success(AuthCheckResponse(false, "인증번호가 일치하지 않습니다.")) // 인증 실패 시 메시지
//        }
//    }

    suspend fun requestPhoneNumberAuth(phoneNumber: String): Result<String> {
        return try {
            val response = smsApiService.sendSmsVerification(phoneNumber)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string() // 에러 본문 읽기
                Log.e("AuthRepository", "API Error - Code: $errorCode, Body: $errorBody")
                Result.failure(Exception("서버 응답 오류 (코드: $errorCode)"))
            }
        } catch (e: Exception) {
            // 네트워크 오류 등 예외 발생 시
            Result.failure(e)
        }
    }

    suspend fun checkPhoneNumberAuth(phoneNumber: String, code: String): Result<String> {
        return try {
            val response = smsApiService.verifySmsCode(phoneNumber, code)

            // [핵심] 통신 자체는 성공했는지 (200 OK), 응답 본문이 비어있지 않은지 먼저 확인
            if (response.isSuccessful && response.body() != null) {

                // 1. 서버가 보낸 실제 메시지를 변수에 저장합니다. (예: "인증 실패")
                val serverMessage = response.body()!!

                // 2. 메시지 내용에 '성공'을 의미하는 키워드가 있는지 확인합니다.
                if (serverMessage.contains("인증되었습니다") || serverMessage.contains("성공")) {
                    // 진짜 성공했을 때만 Result.success를 반환합니다.
                    Result.success(serverMessage)
                } else {
                    // 통신은 성공했지만(200 OK), 내용이 "인증 실패" 같은 실패 메시지일 경우
                    // Result.failure를 반환하여 ViewModel에 실패를 명확히 알립니다.
                    Result.failure(Exception("인증 코드가 일치하지 않습니다. 다시 시도해주세요."))
                }

            } else {
                // 통신 자체가 실패했을 경우 (4xx, 5xx 에러)
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "인증번호가 일치하지 않습니다."))
            }
        } catch (e: Exception) {
            // 네트워크 오류 등 예외 발생 시
            Result.failure(e)
        }
    }

    suspend fun signUp(signUpData: SignUpData): Result<String> {
        return try {
            // ✨ 호출하는 서비스 인스턴스 이름 변경
            val response = memberApiService.join(signUpData)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "SignUp Error - Code: ${response.code()}, Body: $errorBody")
                Result.failure(Exception("회원가입 실패 (코드: ${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "SignUp Exception", e)
            Result.failure(e)
        }
    }

    // SplashActivity에서 사용할 토큰 유효성 검사 함수
    suspend fun validateToken(): Boolean {
        // 1. 로그인 유지' 옵션을 체크했는지 먼저 확인
        val rememberMe = dataStoreManager.getRememberMe.first()
        if (!rememberMe) return false

        // 2. 저장된 토큰이 있는지 확인 (API 호출 전 불필요한 요청 방지)
        val token = dataStoreManager.getToken.first()
        if (token.isNullOrBlank()) return false

        // 3. 토큰이 있다면, 실제 유효한지 서버에 프로필 조회를 요청
        // AuthInterceptor가 자동으로 헤더에 토큰을 추가해주므로, 여기서는 파라미터 없이 호출
        return try {
            val response = memberApiService.getMyProfile()
            response.isSuccessful // API 호출이 성공했는지 여부(true/false)를 반환
        } catch (e: Exception) {
            false // 네트워크 오류 등 예외 발생 시 무효한 토큰으로 간주
        }
    }

    suspend fun getUserProfile(): Result<ProfileResponse> {
        return try {
            // API 호출 시 더 이상 토큰을 직접 전달하지 않음
            val response = memberApiService.getMyProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("프로필 조회 실패 (코드: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun logout(): Result<Unit> {
        return try {
            val response = memberApiService.logout()
            if (response.isSuccessful) {
                // 1. DataStore 데이터 삭제
                dataStoreManager.clearData()
                // 2. TokenManager 변수 초기화
                TokenManager.authToken = null
                Result.success(Unit)
            } else {
                Result.failure(Exception("로그아웃 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun withdrawAccount(): Result<Unit> {
        return try {
            val requestBody = WithdrawRequest(agree = true)
            // API 호출 시 더 이상 토큰을 직접 전달하지 않음
            val response = memberApiService.withdraw(requestBody)
            if (response.isSuccessful) {
                dataStoreManager.clearData()
                Result.success(Unit)
            } else {
                Result.failure(Exception("회원 탈퇴 실패 (코드: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }


    }
}